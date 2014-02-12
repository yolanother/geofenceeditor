package com.doubtech.geofenceeditor;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent;

/**
 * A single Geofence object, defined by its center (latitude and longitude position) and radius.
 */
public class SimpleGeofence {
	public static final String EXTRA_TRANSITION_TYPE = "transitionType";
	public static final String EXTRA_EXPIRATION = "expiration";
	public static final String EXTRA_RADIUS = "radius";
	public static final String EXTRA_LONGITUDE = "longitude";
	public static final String EXTRA_LATITUDE = "latitude";
	public static final String EXTRA_ID = "id";
	public static final String EXTRA_NAME = "name";

	public static final int GEOFENCE_TRANSITION_ENTER = 1;
	public static final int GEOFENCE_TRANSITION_EXIT = 2;
	public static final int GEOFENCE_TRANSITION_DWELL = 4;
	public static final long NEVER_EXPIRE = -1L;

    // Instance variables
    private String mId;
    private String mName;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center. The value is not checked for validity.
     * @param longitude Longitude of the Geofence's center. The value is not checked for validity.
     * @param radius Radius of the geofence circle. The value is not checked for validity
     * @param expiration Geofence expiration duration in milliseconds The value is not checked for
     * validity.
     * @param transition Type of Geofence transition. The value is not checked for validity.
     */
    public SimpleGeofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius,
            long expiration,
            int transition) {
        // Set the instance fields from the constructor

        // An identifier for the geofence
        this.mId = geofenceId;

        // Center of the geofence
        this.mLatitude = latitude;
        this.mLongitude = longitude;

        // Radius of the geofence, in meters
        this.mRadius = radius;

        // Expiration time in milliseconds
        this.mExpirationDuration = expiration;

        // Transition type
        this.mTransitionType = transition;
    }

    private SimpleGeofence() {
		
	}

	// Instance field getters

    /**
     * Get the geofence ID
     * @return A SimpleGeofence ID
     */
    public String getId() {
        return mId;
    }

    /**
     * Get the geofence latitude
     * @return A latitude value
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Get the geofence longitude
     * @return A longitude value
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Get the geofence radius
     * @return A radius value
     */
    public float getRadius() {
        return mRadius;
    }
    
    /**
     * Get the name of the geofence
     * @return A name representing this geofence
     */
    public String getName() {
		return mName;
	}
    
    /**
     * Set the name of the geofence
     * @param name A name representing this geofence
     */
    public void setName(String name) {
		this.mName = name;
	}

    /**
     * Get the geofence expiration duration
     * @return Expiration duration in milliseconds
     */
    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    /**
     * Get the geofence transition type
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * Get the geofence transition type
     * @return Transition type converted to a string (see Geofence)
     */
    public String getTransitionTypeString() {
    	return transitionTypeToString(mTransitionType);
    }

	public void fillIntent(Intent intent) {
		intent.putExtra(EXTRA_ID, mId);
		intent.putExtra(EXTRA_NAME, mName);
		intent.putExtra(EXTRA_LATITUDE, mLatitude);
		intent.putExtra(EXTRA_LONGITUDE, mLongitude);
		intent.putExtra(EXTRA_RADIUS, mRadius);
		intent.putExtra(EXTRA_EXPIRATION, mExpirationDuration);
		intent.putExtra(EXTRA_TRANSITION_TYPE, mTransitionType);
	}

	public static SimpleGeofence fromIntent(Intent intent) {
		// Check for required parameters
		if(!intent.hasExtra(EXTRA_ID) ||
				!intent.hasExtra(EXTRA_LATITUDE) ||
				!intent.hasExtra(EXTRA_LONGITUDE) ||
				!intent.hasExtra(EXTRA_RADIUS) ||
				!intent.hasExtra(EXTRA_EXPIRATION) ||
				!intent.hasExtra(EXTRA_TRANSITION_TYPE)) {
			return null;
		}
		SimpleGeofence geofence = new SimpleGeofence();
		geofence.mId = intent.getStringExtra(EXTRA_ID);
		geofence.mName = intent.getStringExtra(EXTRA_NAME);
		geofence.mLatitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0);
		geofence.mLongitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0);
		geofence.mRadius = intent.getFloatExtra(EXTRA_RADIUS, 100);
	    geofence.mExpirationDuration = intent.getLongExtra(EXTRA_EXPIRATION, NEVER_EXPIRE);
	    geofence.mTransitionType = intent.getIntExtra(EXTRA_TRANSITION_TYPE, GEOFENCE_TRANSITION_DWELL);
	    return geofence;
	}
	
	public static String transitionTypeToString(int transitionType) {
		String transition = "";
		
		if((GEOFENCE_TRANSITION_EXIT & transitionType) > 0) {
			transition += "exit";
		}
		
		if((GEOFENCE_TRANSITION_DWELL & transitionType) > 0) {
			if(transition.length() > 0) {
				transition += "|";
			}
			transition += "dwell";
		}
		
		if((GEOFENCE_TRANSITION_ENTER & transitionType) > 0) {
			if(transition.length() > 0) {
				transition += "|";
			}
			transition += "enter";
		}
		return transition;
	}

	public static int transitionTypeFromString(String transitionType) {
		if(null == transitionType) return 0;
		int transition = 0;
	
		for(String t : transitionType.split("|")) {
			if("exit".equals(transitionType)) {
				transition |= GEOFENCE_TRANSITION_EXIT;
			} else if("dwell".equals(transitionType)) {
				transition |= GEOFENCE_TRANSITION_DWELL;
			} else if("enter".equals(transitionType)) {
				transition |= GEOFENCE_TRANSITION_ENTER;
			} 
		}
		return transition;
	}
}
