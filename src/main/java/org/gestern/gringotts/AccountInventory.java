package org.gestern.gringotts;

import java.util.Collections;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.gestern.gringotts.currency.Currency;
import org.gestern.gringotts.currency.Denomination;

/**
 * Account inventories define operations that can be used on all inventories belonging to an account.
 * 
 * @author jast
 *
 */
public class AccountInventory {
	
	private final Inventory inventory;

	public AccountInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	
	/**
	 * Current balance of this inventory in cents (or rather atomic currency units).
	 * @return current balance of this inventory in cents
	 */
	public long balance() {
		Currency cur = Configuration.config.currency;
		long count = 0;	
        for (ItemStack stack : inventory)
    		count += cur.value(stack);

        return count;
	}

	/**
	 * Add items to this inventory corresponding to given value.
	 * If the amount is larger than available space, the space is filled and the actually
     * added amount returned.
	 * @param value value to add to this inventory
	 * @return amount actually added
	 */
    public long add(long value) {
        long remaining = value;
        
        // try denominations from largest to smallest
        for(Denomination denom : Configuration.config.currency.denominations()) {
        	if (denom.value <= remaining) {
        		ItemStack stack = new ItemStack(denom.type);
        		int stacksize = stack.getMaxStackSize();
        		long denomItemCount = remaining / denom.value;
        		
        		// add stacks in this denomination until stuff is returned
        		while (denomItemCount > 0) {
	        		int remainderStackSize = denomItemCount > stacksize? stacksize : (int)denomItemCount;
	        		stack.setAmount(remainderStackSize);
	        		
	        		int returned = 0;
	        		for (ItemStack leftover : inventory.addItem(stack).values())
	                    returned += leftover.getAmount();
	        		
	        		// reduce remaining amount by whatever was deposited
	        		long added = remainderStackSize-returned;
	        		denomItemCount -= added;
	        		remaining -= added * denom.value;
	        		
	        		// no more space for this denomination
	        		if (returned > 0) break;
        		}
        	}
        }
        
        return value - remaining;
    }
	
	/** 
	 * Remove items from this inventory corresponding to given value.
	 * @param value amount to remove
	 * @return value actually removed
	 */
	public long remove(long value) {
		Currency cur = Configuration.config.currency;
        long remaining = value;
        
        // try denominations from smallest to largest
        List<Denomination> denoms = cur.denominations();
        Collections.reverse(denoms);
        for(Denomination denom : denoms) {
        	if (denom.value <= remaining) {
        		ItemStack stack = new ItemStack(denom.type);
        		int stacksize = stack.getMaxStackSize();
        		long denomItemCount = remaining / denom.value;
        		
        		// add stacks in this denomination until stuff is returned
        		while (denomItemCount > 0) {
	        		int remainderStackSize = denomItemCount > stacksize? stacksize : (int)denomItemCount;
	        		stack.setAmount(remainderStackSize);
	        		
	        		int returned = 0;
	        		for (ItemStack leftover : inventory.removeItem(stack).values())
	                    returned += leftover.getAmount();
	        		
	        		// reduce remaining amount by whatever was removed
	        		long removed = remainderStackSize-returned;
	        		denomItemCount -= removed;
	        		remaining -= removed * denom.value;
	        		
	        		// stuff was returned, no more items of this type to take
	        		if (returned > 0) break;
        		}
        	} else {
        		// if denom value > remaining, take 1 of denom, add the rest back to the remaining alue
        		ItemStack stack = new ItemStack(denom.type);
        		stack.setAmount(1);
        		int returned = 0;
        		for (ItemStack leftover : inventory.removeItem(stack).values())
                    returned += leftover.getAmount();
        		
        		if (returned == 0)
        			remaining = denom.value - remaining;
        	}
        }
        return value - remaining;
	}
	
	
	/**
	 * TODO: Find a good distribution of denominations to represent the value.
	 * The implementation should minimize the amount of stacks used.
	 * @param value
	 * @return
	 */
//	private Map<Integer, Integer> stacking(long value) {
//		return null;
//	}
}
