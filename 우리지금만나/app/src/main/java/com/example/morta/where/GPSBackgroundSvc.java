/*
 * 
 */
package com.example.morta.where;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.core.content.ContextCompat;

public class GPSBackgroundSvc extends Service implements LocationListener {
	private String TAG = "GPSBackgroundSvc";
	private String sPackageName = "com.angel";

	private final IBinder mBinder = new LocalBinder();
	double latitude;
	double longitude;
	String chat_name;
	int iLoopValue = 0;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String EMAIL= mAuth.getCurrentUser().getEmail();
    int idx = EMAIL.indexOf("@");
    String email = EMAIL.substring(0, idx);



	int iThreadInterval = 25000;
	boolean bThreadGo = true;

	LocationManager locationMgr;
	String sBestGpsProvider = "";

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart : ");
		super.onStart(intent, startId);

		bThreadGo = true;

		chat_name=intent.getStringExtra("chat_name");

		locationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		sBestGpsProvider = LocationManager.GPS_PROVIDER;

		setGpsPosition();
		int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
		int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

		if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
		{
			Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
			return;
		}
		locationMgr.requestLocationUpdates(sBestGpsProvider, 10000, 0, this);


		new Thread(mRun).start();
	}

	@Override
	public void onDestroy() {
		try {
			Log.i(TAG, "onDestroy : ");

			bThreadGo = false;
            Log.d(TAG, chat_name + "/ "+ email + "/ "+ latitude + "/ "+ longitude);

			if (this != null && locationMgr != null) {
				locationMgr.removeUpdates(this);
			}

			TAG = null;
			sBestGpsProvider = null;
			locationMgr = null;
			mRun = null;
		} catch (Exception e) {
			Log.i(TAG, ">onDestroy : " + e.toString());
		}

		super.onDestroy();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.child("position").child(chat_name).child("user_position").child(email).setValue(null);
	}

	public void onLocationChanged(Location location) {
		try {
			Log.i(TAG, ">onLocationChanged : ");

			positionSaveProc();
		} catch (Exception e) {
			Log.i(TAG, ">onLocationChanged : " + e.toString());
		}
	}

	public void onProviderDisabled(String provider) {
		;
	}

	public void onProviderEnabled(String provider) {
		;
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		;
	}

	public class LocalBinder extends Binder {
		GPSBackgroundSvc getService() {
			return GPSBackgroundSvc.this;
		}
	}

	public interface ICallback {
		;
	}

	private ICallback mCallback;

	public void registerCallback(ICallback cb) {
		mCallback = cb;
	}

	Runnable mRun = new Runnable() {
		public void run() {
			try {
				while (bThreadGo) {
					Log.i(TAG, ">mRun");

					iLoopValue++;
					Thread.sleep(iThreadInterval);
					if (iLoopValue > 100000)
						iLoopValue = 0;

					positionSaveProc();

                    if(bThreadGo != false)
                    {
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = firebaseDatabase.getReference();
                        Log.d(TAG, chat_name + "/ " + email + "/ " + latitude + "/ " + longitude);
                        databaseReference.child("place").child(chat_name).child("user_position").child(email).child("latitude").setValue(latitude);
                        databaseReference.child("place").child(chat_name).child("user_position").child(email).child("longitude").setValue(longitude);
                    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public synchronized void positionSaveProc() {
		try {
			double dLatitude = 0;
			double dLongitude = 0;
			if (sBestGpsProvider != null && locationMgr != null) {
				int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
				int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

				if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
				{
					Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
					return;
				}
				Location lcPosition = locationMgr.getLastKnownLocation(sBestGpsProvider);
				if (lcPosition != null) {
					dLatitude = lcPosition.getLatitude();
					dLongitude = lcPosition.getLongitude();
					latitude = dLatitude;
					longitude = dLongitude;
					Log.i(TAG, ">positionSaveProc : lat(" + dLatitude + "), lot(" + dLongitude + ")");
					if (dLatitude != 0 && dLongitude != 0) {
						setSharePreferenceFloatValue("dUserContactLatitude", (float) dLatitude);
						setSharePreferenceFloatValue("dUserContactLongitude",(float) dLongitude);
						
						setLocationProvider("GPS");
					}else{
						setLocationProvider("NETWORK");
					}
				} else {
					Log.i(TAG, ">positionSaveProc : ");
					setLocationProvider("NETWORK");
				}
			}
		} catch (Exception e) {
			Log.i(TAG, ">positionSaveProc : " + e.toString());
		}
	}


	public synchronized void setLocationProvider(String parmOption) {
		if (locationMgr == null)
			return;
		if (parmOption.equals("NETWORK")) {
			Log.i(TAG, ">setLocationProvider sBestGpsProvider : " + sBestGpsProvider);
			setGpsPosition();
			sBestGpsProvider = LocationManager.NETWORK_PROVIDER;
			int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
			int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

			if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
			{
				Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
				return;
			}
			locationMgr.requestLocationUpdates(sBestGpsProvider, 10000, 0, this);
			setGpsPosition();
		} else if (parmOption.equals("GPS")) {
			Log.i(TAG, ">setLocationProvider sBestGpsProvider : " + sBestGpsProvider);
			sBestGpsProvider = LocationManager.GPS_PROVIDER;
			locationMgr.requestLocationUpdates(sBestGpsProvider, 10000, 0, this);
		}
	}


	public synchronized void setGpsPosition() {
		try {
			Log.i(TAG, ">setGpsPosition : ");
			if (locationMgr == null)
				return;
			int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
			int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

			if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
			{
				Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
				return;
			}
			Location lcPosition = locationMgr.getLastKnownLocation(sBestGpsProvider);
			if (lcPosition != null) {
				Log.i(TAG, ">setGpsPosition : lat(" + lcPosition.getLatitude() + "), lot(" + lcPosition.getLongitude() + ")");
				setSharePreferenceFloatValue("dUserContactLatitude", (float) lcPosition.getLatitude());
				setSharePreferenceFloatValue("dUserContactLongitude",(float) lcPosition.getLongitude());
			} else {
				Log.i(TAG, ">setGpsPosition : ");
			}
		} catch (Exception e) {
			Log.i(TAG, ">setGpsPosition : error : " + e.toString());
		}
	}

	public synchronized void locationChangedProc(Location location) {
		try {
			Log.i(TAG, ">locationChangedProc : ");
			if (location == null) {
				return;
			}
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			if (lat > 1 && lon > 1) {
				setSharePreferenceFloatValue("dUserContactLatitude", (float) lat);
				setSharePreferenceFloatValue("dUserContactLongitude", (float) lon);
				Log.i(TAG, ">locationChangedProc :  : " + lat);
				Log.i(TAG, ">locationChangedProc :  : " + lon);

			} else {
				Log.i(TAG, ">locationChangedProc : gps  : " + lon);
			}
		} catch (Exception e) {
			Log.i(TAG, ">locationChangedProc : " + e.toString());
		}
	}

	public synchronized void setSharePreferenceStringValue(String parmName,
			String parmValue) {
		try {
			SharedPreferences spSvc = getApplicationContext()
					.getSharedPreferences(sPackageName, MODE_PRIVATE);
			Editor ed = spSvc.edit();
			ed.putString(parmName, parmValue);
			ed.commit();
			spSvc = null;
		} catch (Exception e) {
			Log.i(TAG, ">setSharePreferenceStringValue error : " + e.toString());
		}
	}

	public synchronized String getSharePreferenceStringValue(String parmName) {
		try {
			SharedPreferences spSvc = getApplicationContext()
					.getSharedPreferences(sPackageName, MODE_PRIVATE);
			String sReturn = spSvc.getString(parmName, "");
			spSvc = null;
			return sReturn;
		} catch (Exception e) {
			Log.i(TAG, ">getSharePreferenceStringValue error : " + e.toString());
			return "";
		}
	}
	public synchronized void setSharePreferenceFloatValue(String parmName,
			float parmValue) {
		try {
			SharedPreferences spSvc = getApplicationContext()
					.getSharedPreferences(sPackageName, MODE_PRIVATE);
			Editor ed = spSvc.edit();
			ed.putFloat(parmName, parmValue);
			ed.commit();
			spSvc = null;
		} catch (Exception e) {
			Log.i(TAG, ">setSharePreferenceStringValue error : " + e.toString());
		}
	}

	public synchronized float getSharePreferenceFloatValue(String parmName) {
		try {
			SharedPreferences spSvc = getApplicationContext()
					.getSharedPreferences(sPackageName, MODE_PRIVATE);
			float sReturn = spSvc.getFloat(parmName, 0);
			spSvc = null;
			return sReturn;
		} catch (Exception e) {
			Log.i(TAG, ">getSharePreferenceStringValue error : " + e.toString());
			return 0;
		}
	}
}
