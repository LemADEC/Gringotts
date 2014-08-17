package org.gestern.gringotts.currency;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Representation of a currency. This contains information about the currency's denominations and their values.
 * The value is represented internally as "cents", that is, the smallest currency unit, and only gets transformed into display value
 * for communication with the user or vault.
 * 
 * @author jast
 */
public class GringottsCurrency {

    // yes, I want to be able to get the key from the key.
    // this is because I want to find a denomination's value based on its type.
    // TODO considering there are usually only very few denominations.. simplify this using just a simple friggin list or array
    private final List<Denomination> denoms = new ArrayList<>();

    /** Name of the currency. */
    public final String name;

    /** Name of the currency, plural version. */
    public final String namePlural;

    /** 
     * Currency unit divisor. Internally, all calculation is done in "cents". 
     * This multiplier changes the external representation.
     * For instance, with unit 100, every cent will be worth 0.01 currency units
     */
    public final int unit;

    /**
     * Fractional digits supported by this currency.
     * For example, with 2 digits the minimum currency value would be 0.01
     */
    public final int digits;

    /**
     * Create currency.
     * @param name name of currency
     * @param namePlural plural of currency name
     * @param digits decimal digits used in currency
     */
    public GringottsCurrency(String name, String namePlural, int digits) {
        this.name = name;
        this.namePlural = namePlural;
        this.digits = digits;

        // calculate the "unit" from digits. It's just a power of 10!
        int d=digits, u = 1;
        while (d-->0) u*=10;
        this.unit = u;
    }

    /**
     * Add a denomination and value to this currency.
     * @param type the denomination's item type
     * @param value the denomination's value
     */
    public void addDenomination(ItemStack type, double value, String name, String namePlural) {
        Denomination d = new Denomination(type, Math.round(centValue(value)), name, namePlural);

        // infrequent insertion, so I don't mind sorting on every insert
        denoms.add(d);
        Collections.sort(denoms);
    }


    /**
     * Get the value of an item stack in cents.
     * This is calculated by value_type * stacksize.
     * If the given item stack is not a valid denomination, the value is 0;
     * @param stack a stack of items
     * @return the value of given stack of items
     */
    public long value(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return 0;
        Denomination d = denominationOf(stack);
        return d!=null? d.value * stack.getAmount() : 0;
    }

    /**
     * The display value for a given cent value.
     * @param value value to calculate display value for
     * @return user representation of value
     */
    public double displayValue(long value) {
        return (double)value / unit;
    }

    /**
     * The internal calculation value of a display value.
     * @param value display value
     * @return Gringotts-internal value of given amount
     */
    public long centValue(double value) {
        return Math.round(value * unit);
    }


    /**
     * List of denominations used in this currency, in order of descending value.
     * @return List of denominations used in this currency, in order of descending value
     */
    public List<Denomination> denominations() {
        return new ArrayList<>(denoms);
    }

    /**
     * Get the denomination of an item stack.
     * @param stack the stack to get the denomination for
     * @return denomination for the item stack, or null if there is no such denomination
     */
    private Denomination denominationOf(ItemStack stack) {
        for (Denomination d : denoms)
            if (d.isDenominationOf(stack)) return d;

        return null;
    }

    /**
     * Format a currency value based on denomination names.
     * @param formatString
     * @param value
     * @return
     */
    public String format(String formatString, double value) {
        String output = "";
        String delimiter = "";
        double remainingValue = value;
        int denomCount = denoms.size();

        for (int i = 1; i <= denomCount; i++) {
            Denomination denomination = denoms.get(i);
            double denomationValue = denomination.value / unit;

            // always process the final denomination
            if (i == denomCount || remainingValue > denomationValue) {

                double denomAmount = remainingValue / denomationValue;
                remainingValue -= Math.floor(denomAmount) * denomationValue;

                String formattedAmount = "",
                    name = denomination.name, namePlural = denomination.namePlural;
                // Special-case lowest denomination, which gets decimal places for remainder.
                if (i == denomCount) {
                    formattedAmount = String.format(formatString, denomAmount);
                    // use currency name if it doesn't have its own given name
                    if (! denomination.hasName()) {
                        name = this.name;
                        namePlural = this.namePlural;
                    }
                } else
                    formattedAmount = Integer.toString((int)denomAmount);

                output = output + delimiter + formattedAmount + " " +
                        (denomAmount==1.0? name : namePlural);
                delimiter = ", ";
            }
        }

        return output;
    }
}
