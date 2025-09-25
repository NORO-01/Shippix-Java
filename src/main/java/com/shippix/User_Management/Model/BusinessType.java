package com.shippix.User_Management.Model;

public enum BusinessType {
    RESTAURANT("Restaurant"),
    RETAIL_STORE("Retail Store"),
    GROCERY_STORE("Grocery Store"),
    PHARMACY("Pharmacy"),
    ELECTRONICS_STORE("Electronics Store"),
    CLOTHING_STORE("Clothing Store"),
    BOOKSTORE("Bookstore"),
    HARDWARE_STORE("Hardware Store"),
    BEAUTY_SALON("Beauty Salon"),
    BARBER_SHOP("Barber Shop"),
    LAUNDRY_SERVICE("Laundry Service"),
    CONVENIENCE_STORE("Convenience Store"),
    BAKERY("Bakery"),
    CAFE("Cafe"),
    GAS_STATION("Gas Station"),
    OTHER("Other");

    private final String displayName;

    BusinessType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Method to get enum from display name (useful for frontend)
    public static BusinessType fromDisplayName(String displayName) {
        for (BusinessType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid business type: " + displayName);
    }

    // Method to get all display names (useful for frontend dropdown)
    public static String[] getAllDisplayNames() {
        return java.util.Arrays.stream(values())
                .map(BusinessType::getDisplayName)
                .toArray(String[]::new);
    }
}