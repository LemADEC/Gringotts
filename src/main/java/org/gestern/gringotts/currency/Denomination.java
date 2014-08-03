package org.gestern.gringotts.currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Representation of a denomination within a currency.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals.
 * Specifically, the ordering is based purely on the value of the denomination, but not the type.
 * Conversely, the equality of denominations is based purely on their respective types, and their value is not regarded.
 * 
 * @author jast
 *
 */
public class Denomination implements Comparable<Denomination> {

    /** Item type of this denomination. */
    public final ItemStack type;
    public final Material material;
    public final short damage;
    public final long value;
    public final String name;
    public final String namePlural;

    public Denomination(ItemStack type, long value, String name, String namePlural) {
        this.type = type;
        this.material = type.getType();
        this.damage = type.getDurability();
        this.value = value;
        this.name = name;
        this.namePlural = namePlural;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + damage;
        result = prime * result + material.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Denomination other = (Denomination) obj;
        return damage == other.damage && material == other.material;
    }

    @Override
    public int compareTo(Denomination other) {
        // sort in descending value order
        return Long.valueOf(other.value).compareTo(this.value);
    }

    @Override
    public String toString() {
        return String.format("Denomination: (%s) %s;%d : %d", (name == null ? "" : name), material, damage, value);
    }

    public boolean isDenominationOf(ItemStack stack) {
        return (stack.getType() == this.material) && (stack.getDurability() == this.damage);
    }

    public boolean hasName() {
        return this.namePlural != null && this.namePlural.isEmpty()
                && this.name != null && this.name.isEmpty();
    }
}
