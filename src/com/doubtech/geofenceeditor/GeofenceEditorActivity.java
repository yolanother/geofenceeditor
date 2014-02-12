package com.doubtech.geofenceeditor;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.doubtech.geofenceeditor.Utils.DistanceUnit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class GeofenceEditorActivity extends Activity {
    private static final int ACTION_PLAY_SERVICES_ERROR = 0;
	private GoogleMap mMap;
	private Geocoder mGeocoder;
	private EditText mAddress;
	private LatLng mCurrentLocation;
	private View mGo;
	private SharedPreferences mPrefs;
	private Circle mCircle;
	private SeekBar mSlider;
	private String mName;
	private SimpleGeofence mFence;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_editor);
        
        if(Build.VERSION.SDK_INT >= 19) {
	        getWindow().getDecorView().setSystemUiVisibility(
	                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
	              | View.SYSTEM_UI_FLAG_FULLSCREEN
	              | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        
        int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(ConnectionResult.SUCCESS != code) {
        	findViewById(R.id.error_overlay).setVisibility(View.VISIBLE);
        	if(GooglePlayServicesUtil.isUserRecoverableError(code)) {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						code,
						this,
						ACTION_PLAY_SERVICES_ERROR);
				dialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
		    			// Make the user relaunch after error is handled.
		    			setResult(RESULT_CANCELED);
						finish();
					}
				});
				dialog.show();
			} else if(Actions.CREATE_GEOFENCE.equals(getIntent().getAction())) {
	    		Intent intent = new Intent();
	    		intent.putExtra("error", code);
	    		setResult(RESULT_CANCELED, intent);
	    		finish();
    		} else {
    			String message = "";
    			switch(code) {
    			case ConnectionResult.SERVICE_MISSING:
    				message = "GooglePlay services are missing";
    				break;
    			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
    				message = "GooglePlay services version update is required.";
    				break;
    			case ConnectionResult.SERVICE_DISABLED:
    				message = "GooglePlay services are disabled.";
    				break;
    			case ConnectionResult.SERVICE_INVALID:
    				message = "GooglePlay services invalid.";
    				break;
    			case ConnectionResult.DATE_INVALID:
    				message = "Date is invalid";
    				break;
    			}
    			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    			// Make the user relaunch after error is handled.
    			setResult(RESULT_CANCELED);
        		finish();
    		}
    		return;
        }

        init();
	}
	
	private void init() {
        mGeocoder = new Geocoder(GeofenceEditorActivity.this, Locale.getDefault());
        
        // Get a handle to the Map Fragment
        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);

        mAddress = (EditText) findViewById(R.id.address);
        mAddress.setOnKeyListener(new OnKeyListener() {			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(KeyEvent.KEYCODE_ENTER == keyCode) {
					go();
					return true;
				}
				return false;
			}
		});
        mAddress.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					mAddress.selectAll();
				} else {
					mAddress.setSelection(0);
				}
			}
		});
        mAddress.setSelected(false);

        mGo = findViewById(R.id.go);
        mGo.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				go();
			}
		});
        mGo.requestFocus();
        
        mSlider = (SeekBar) findViewById(R.id.radius);
        mSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser) {
					updateCircleRadius(progress);
				}
			}
		});

        mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
			
			@Override
			public void onMyLocationChange(Location myLocation) {
				if(null == mCurrentLocation) {
					setLocation(myLocation.getLatitude(), myLocation.getLongitude(), (int) mMap.getMaxZoomLevel() - 5);
				}
			}
		});
        mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng position) {
				if(mAddress.hasFocus()) {
					clearAddressfocus();
				} else {
					setLocation(position.latitude, position.longitude);
				}
			}
		});
        
        mMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			
			@Override
			public void onMarkerDragStart(Marker arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMarkerDragEnd(Marker marker) {
				LatLng pos = marker.getPosition();
				setLocation(pos.latitude, pos.longitude);
			}

			@Override
			public void onMarkerDrag(Marker arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition position) {
				updateSlider();
			}
		});

        
        float zoom = mMap.getMinZoomLevel();

        double latitude = 0;
        double longitude = 0;

        mPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
        Intent intent = getIntent();
        if(intent.hasExtra(GeofenceEditor.EXTRA_STARTING_LATITUDE) && intent.hasExtra(GeofenceEditor.EXTRA_STARTING_LONGITUDE)) {
        	latitude = intent.getDoubleExtra(GeofenceEditor.EXTRA_STARTING_LATITUDE, 0);
        	longitude = intent.getDoubleExtra(GeofenceEditor.EXTRA_STARTING_LONGITUDE, 0);
        	zoom = intent.getFloatExtra(GeofenceEditor.EXTRA_STARTING_ZOOM,
        			mPrefs.getFloat(GeofenceEditor.EXTRA_STARTING_ZOOM, mMap.getMinZoomLevel()));
        } else if(mPrefs.contains(GeofenceEditor.EXTRA_STARTING_LATITUDE) && mPrefs.contains(GeofenceEditor.EXTRA_STARTING_LONGITUDE)) {
        	latitude = mPrefs.getFloat(GeofenceEditor.EXTRA_STARTING_LATITUDE, 0);
        	longitude = mPrefs.getFloat(GeofenceEditor.EXTRA_STARTING_LONGITUDE, 0);
        	zoom = mPrefs.getFloat(GeofenceEditor.EXTRA_STARTING_ZOOM, mMap.getMinZoomLevel());
        }

        mName = intent.getStringExtra(SimpleGeofence.EXTRA_NAME);

        LatLng location = new LatLng(latitude, longitude);
    	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
    	
    	if(Actions.CREATE_GEOFENCE.equals(intent.getAction())) {

            if(Build.VERSION.SDK_INT < 19) {
	    		startActionMode(new Callback() {
					
					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public void onDestroyActionMode(ActionMode mode) {
						if(null != mFence) {
							Intent intent = new Intent();
							mFence.fillIntent(intent);
							setResult(RESULT_OK, intent);
						} else {
							setResult(RESULT_CANCELED);
						}
						finish();
					}
					
					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						
						return true;
					}
					
					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
						// TODO Auto-generated method stub
						return false;
					}
				});
            } else {
            	findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(null != mFence) {
							Intent intent = new Intent();
							mFence.fillIntent(intent);
							setResult(RESULT_OK, intent);
						} else {
							setResult(RESULT_CANCELED);
						}
						finish();
					}
				});
            }

        	mMap.getUiSettings().setMyLocationButtonEnabled(false);
        	findViewById(R.id.my_location).setOnClickListener(new OnClickListener() {
        		@Override
				public void onClick(View v) {
					Location location = mMap.getMyLocation();
					LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
					mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				}
			});
    	}
    }

	public void setLocation(double latitude, double longitude) {
		setLocation(latitude, longitude, -1);
	}

	public void setLocation(final double latitude, final double longitude, final int zoom) {
		CameraUpdate update;
		if(-1 == zoom) {
			update = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
		} else {
			update = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom);
		}

		mMap.animateCamera(update, 1000, new CancelableCallback() {
			@Override
			public void onFinish() {
				completeSetLocation(latitude, longitude, zoom);
			}

			@Override
			public void onCancel() {
			}
		});
	}
	
	private void completeSetLocation(double latitude, double longitude, int zoom) {
        mCurrentLocation = new LatLng(latitude, longitude);
		Address address = null;
        try {
			List<Address> addresses = mGeocoder.getFromLocation(mCurrentLocation.latitude, mCurrentLocation.longitude, 1);
			if(addresses.size() > 0) {
				address = addresses.get(0);
			}
			
		} catch (IOException e) {
			// Just don't show a location.
		}
        
        if(null == address) {
			mAddress.setText(mCurrentLocation.toString());
        } else {
        	StringBuilder builder = new StringBuilder(address.getAddressLine(0));
        	for(int i = 1; i < address.getMaxAddressLineIndex(); i++) {
        		builder.append(", ");
        		builder.append(address.getAddressLine(i));
        	}
        	mAddress.setText(builder.toString());
        }
        mMap.clear();
        SimpleGeofence fence = new SimpleGeofence(
        		mAddress.getText().toString(),
        		latitude,
        		longitude,
        		100,
        		SimpleGeofence.NEVER_EXPIRE,
        		SimpleGeofence.GEOFENCE_TRANSITION_DWELL);
        fence.setName(mName);
        addMarkerForFence(fence);
        Editor editor = mPrefs.edit();
        editor.putFloat(GeofenceEditor.EXTRA_STARTING_LATITUDE, (float) latitude);
        editor.putFloat(GeofenceEditor.EXTRA_STARTING_LONGITUDE, (float) longitude);
        if(-1 != zoom) {
        	editor.putFloat(GeofenceEditor.EXTRA_STARTING_ZOOM, zoom);
        }
        editor.commit();
	}

	public void addMarkerForFence(SimpleGeofence fence) {
		if (fence == null) {
			// display en error message and return
			return;
		}
		mFence = fence;
		Marker marker = mMap.addMarker(
				new MarkerOptions()
						.position(
								new LatLng(fence.getLatitude(), fence
										.getLongitude()))
						.title(null != mName ? mName : fence.getId())
						.snippet(null != mName ? fence.getId() : ""));
		marker.setDraggable(true);
		marker.showInfoWindow();

		// Instantiates a new CircleOptions object + center/radius
		CircleOptions circleOptions = new CircleOptions()
				.center(new LatLng(fence.getLatitude(), fence.getLongitude()))
				.radius(fence.getRadius()).fillColor(0x408888ff)
				.strokeColor(0xcc6666ff).strokeWidth(2);

		// Get back the mutable Circle
		mCircle = mMap.addCircle(circleOptions);
		// more operations on the circle...
		updateSlider();
	}

	public void clearAddressfocus() {
		mAddress.clearFocus();
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mAddress.getWindowToken(), 0);
	}

	public void go() {
		clearAddressfocus();
		try {
			List<Address> addresses = mGeocoder.getFromLocationName(mAddress.getText().toString(), 1);
			if(addresses.size() > 0) {
				Address addr = addresses.get(0);
				setLocation(addr.getLatitude(), addr.getLongitude());
			}
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void updateSlider() {
		if(null != mCircle) {
			mSlider.setEnabled(true);
			VisibleRegion region = mMap.getProjection().getVisibleRegion();
			double dist = Utils.distance(region.farLeft, region.farRight, DistanceUnit.Kilometers) * 1000;
			mSlider.setMax(mSlider.getMeasuredWidth());
			mSlider.setProgress((int) (mSlider.getMeasuredWidth() * mCircle.getRadius() * 2 / dist));
		} else {
			mSlider.setEnabled(false);
		}
	}
	
	private void updateCircleRadius(int progress) {
		VisibleRegion region = mMap.getProjection().getVisibleRegion();

		double dist = Utils.distance(region.farLeft, region.farRight, DistanceUnit.Kilometers) * 1000;
		double radius = progress / (float) mSlider.getMax() * dist / 2.0f;
		mCircle.setRadius(radius);
	}
}
