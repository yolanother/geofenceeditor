package com.doubtech.geofenceeditor;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

public class Utils {
    public static enum DistanceUnit {
        Kilometers,
        NauticalMiles,
        Miles
    }
    public static double distance(LatLng pos1, LatLng pos2, DistanceUnit unit) {
        double theta = pos1.longitude - pos2.longitude;
        double dist = Math.sin(deg2rad(pos1.latitude))
                * Math.sin(deg2rad(pos2.latitude))
                + Math.cos(deg2rad(pos1.latitude))
                * Math.cos(deg2rad(pos2.latitude)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == DistanceUnit.Kilometers) {
            dist = dist * 1.609344;
        } else if (unit == DistanceUnit.NauticalMiles) {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    /* :: This function converts decimal degrees to radians : */
    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    /* :: This function converts radians to decimal degrees : */
    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public static Geofence toGeofence(SimpleGeofence geofence) {
        // Build a new Geofence object
        return new Geofence.Builder()
                       .setRequestId(geofence.getId())
                       .setTransitionTypes(geofence.getTransitionType())
                       .setCircularRegion(
                               geofence.getLatitude(),
                               geofence.getLongitude(),
                               geofence.getRadius())
                       .setExpirationDuration(geofence.getExpirationDuration())
                       .build();
    }
}
