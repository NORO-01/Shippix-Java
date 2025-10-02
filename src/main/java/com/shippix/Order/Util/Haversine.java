package com.shippix.Order.Util;

public class Haversine {

    private static final int EARTH_RADIUS_KM = 6371; // Radius of the Earth in km
    private static final int EARTH_RADIUS_MI = 3959; // Radius of the Earth in miles


//  Calculate distance between two points in kilometers
    public static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        return haversine(lat1, lon1, lat2, lon2, EARTH_RADIUS_KM);
    }


//  Calculate distance between two points in miles
    public static double distanceInMiles(double lat1, double lon1, double lat2, double lon2) {
        return haversine(lat1, lon1, lat2, lon2, EARTH_RADIUS_MI);
    }

//  Core haversine calculation.
    private static double haversine(double lat1, double lon1, double lat2, double lon2, double radius) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radius * c;
    }
}
