package com.alameda.lightningenergy.utils;

import com.alameda.lightningenergy.entity.enums.ErrorType;
import org.tron.trident.proto.Common;

public class ResourcePriceCalculator {
    private static final long ONE_DAY_IN_MILLIS = 24L * 60 * 60 * 1000;
    private static final long FIFTEEN_DAYS_IN_MILLIS = 15L * 24 * 60 * 60 * 1000;
    private static final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;


    /**
     * Calculates the energy price per unit based on the duration.
     *
     * @param durationInMillis Duration in milliseconds.
     * @return Price per unit of energy in suns.
     * @throws IllegalArgumentException If the duration exceeds 30 days.
     */
    public static long calculateEnergyPricePerUnit(long durationInMillis) throws ErrorType.ApplicationException {
        if (durationInMillis < ONE_DAY_IN_MILLIS) {
            return 60L; // Price per unit for duration within an hour
        } else if (durationInMillis < FIFTEEN_DAYS_IN_MILLIS) {
            return 55L; // Price per unit for duration between 1 hour and 15 days
        } else if (durationInMillis <= THIRTY_DAYS_IN_MILLIS) {
            return 48L; // Price per unit for duration between 15 days and 30 days
        } else {
            throw ErrorType.DURATION_EXCEEDS_MAXIMUM.getException();
        }
    }
    public static long calculateBandwidthPricePerUnit(long durationInMillis) throws ErrorType.ApplicationException {
         if (durationInMillis < FIFTEEN_DAYS_IN_MILLIS) {
            return 700; // Price per unit for duration between 1 hour and 15 days
        } else if (durationInMillis <= THIRTY_DAYS_IN_MILLIS) {
            return 650; // Price per unit for duration between 15 days and 30 days
        } else {
            throw ErrorType.DURATION_EXCEEDS_MAXIMUM.getException();
        }
    }
    public static long calculateResourcePricePerUnit(Common.ResourceCode resourceCode ,long durationInMillis ) throws ErrorType.ApplicationException {
        return resourceCode.equals(Common.ResourceCode.ENERGY) ? calculateEnergyPricePerUnit(durationInMillis) : calculateBandwidthPricePerUnit(durationInMillis);
    }
    /**
     * Calculates the total amount of resources.
     *
     * @param initialAmount Initial amount of resources (as long).
     * @param durationInMillis Duration in milliseconds.
     * @return Total amount of resources (as long).
     */
    public static long calculateTotalResources(long initialAmount, long durationInMillis) {
        // Calculate the number of days from the duration (including fractional days)
        double days = (double) durationInMillis / ONE_DAY_IN_MILLIS;

        // Total resources = initial amount + (days * initial amount)
        // Rounding the result to the nearest whole number
        return Math.round(initialAmount + (days * initialAmount));
    }


    /**
     * 将数字向上取整到最近的指定位数。
     * @param number 需要被取整的数字。
     * @param zeros 取整的基数，表示后面有多少个零。
     * @return 取整后的数字。
     */
    public static long toNearestBase(long number, int zeros) {
        long base = (long)Math.pow(10, zeros);
        if (number % base == 0) {
            return number;
        }
        return (number / base) * base ;
    }



    public static void main(String[] args) {
    }
}
