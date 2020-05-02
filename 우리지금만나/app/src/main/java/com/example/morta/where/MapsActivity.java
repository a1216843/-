package com.example.morta.where;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Telephony;
import android.text.AlteredCharSequence;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.widget.Button;




public class MapsActivity extends AppCompatActivity
   implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback
{
        Button btn;
        private GoogleMap mMap;
        private  Marker currentMarker = null;

        private static final String TAG = "layout";
        private static final int GPS_ENABLE_REQUEST_CODE = 2001;
        private static final int UPDATA_INTERVAL_MS = 1000;
        private static final int FASTEST_UPDATE_INTERVAL_MS = 500;

        private static final int PERMISSIONS_REQUEST_CODE = 100;
        boolean needRequest = false;
        //onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.

        String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        // 앱을 실행하기 위한 퍼미션 정의

        Location mCurrentLocation;
        LatLng currentPosition;;

        private FusedLocationProviderClient mFusedLocationClient;
        private LocationRequest locationRequest;
        private Location location;

        private View mLayout;
        // Snackbar 사용하기 위해서 View가 필요, Toast에선 Context

        @Override
        protected void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            setContentView(R.layout.layout);

            mLayout = findViewById(R.id.layout);

            locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATA_INTERVAL_MS).setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        btn = (Button) findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MapsActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });
    }

        @Override
        public void onMapReady(final GoogleMap googleMap){
            mMap = googleMap;
            Log.d(TAG, "onMapReady :");
            setDefaultLocation();

            int hasFIndLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFIndLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED)
            {
                startLocationUpdates();
            }
            else
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]))
                {
                    Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                            ActivityCompat.requestPermissions( MapsActivity.this, REQUIRED_PERMISSIONS,
                                    PERMISSIONS_REQUEST_CODE);
                        }
                    }).show();
                }
                else
                {
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                }
            }

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
            {
                @Override
                public  void onMapClick(LatLng latLng)
                {
                    Log.d( TAG, "onMapClick :");
                }
            });
        }

        LocationCallback locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);

                List<Location> locationList = locationResult.getLocations();

                if (locationList.size() > 0)
                {
                    location = locationList.get(locationList.size() -1 );

                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                    String markerTitle = getCurrentAddress(currentPosition);
                    String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + "경도:" + String.valueOf(location.getLongitude());

                    Log.d(TAG, "onLocationResult :" + markerSnippet);

                    setCurrentLocation(location, markerTitle, markerSnippet);

                    mCurrentLocation = location;

                }
            }
        };

        private void startLocationUpdates()
        {
            if(!checkLocationServicesStatus())
            {
                Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
                showDialogForLocationServiceSetting();
            }
            else
            {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                    return;
                }

                Log.d(TAG, "startLocationUpdates : call mFUsedLocationClient.requestLocationUpdates");

                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                if(checkPermission())
                    mMap.setMyLocationEnabled(true);
            }

        }

        @Override
        protected  void onStart()
        {
            super.onStart();

            Log.d(TAG, "onStart");

            if (checkPermission())
            {
                Log.d(TAG, "onStart: call mFUsedLocationClient.requestLocationUpdates");
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

                if (mMap!=null)
                    mMap.setMyLocationEnabled(true);
            }
        }

        @Override
        protected void onStop() {

            super.onStop();

            if (mFusedLocationClient != null) {

                Log.d(TAG, "onStop : call stopLocationUpdates");
                mFusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }


        public  String getCurrentAddress(LatLng lating)
        {
            //지오코더 gps를 주소로 변환
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            List<Address> addresses;

            try
            {
                addresses = geocoder.getFromLocation(
                        lating.latitude,
                        lating.longitude,
                        1);
            }
            catch (IOException idException)
            {
                Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
                return "지오코더 서비스 사용불가";
            }
            catch (IllegalArgumentException illegalArgumentException)
            {
                Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
                return "잘못된 GPS 좌표";
            }

            if(addresses == null || addresses.size() == 0)
            {
                Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
                return "주소 미발견";
            }
            else
            {
                Address address = addresses.get(0);
                return address.getAddressLine(0).toString();
            }
        }

        public boolean checkLocationServicesStatus()
        {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        public void setCurrentLocation(Location location, String markerTitle, String markerSnippet)
        {
            if(currentMarker != null) currentMarker.remove();

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLatLng);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);

            currentMarker = mMap.addMarker(markerOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mMap.moveCamera(cameraUpdate);
        }

        public void setDefaultLocation()
        {
            LatLng DEFAULT_LOCATION = new LatLng(36.63, 127.48);
            String markerTitle = "위치정보 가져올 수 없음";
            String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";

            if (currentMarker != null) currentMarker.remove();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(DEFAULT_LOCATION);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currentMarker = mMap.addMarker(markerOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
            mMap.moveCamera(cameraUpdate);

        }

        //런타임 처리를 위한 메소드들

        private boolean checkPermission()
        {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                    hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
                return true;
            }

            return false;
        }

        @Override
        public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults)
        {
            if( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length)
            {
                boolean check_result = true;
                for (int result : grandResults)
                 {
                      if (result != PackageManager.PERMISSION_GRANTED)
                      {
                          check_result = false;
                          break;
                      }
                  }

                  if (check_result)
                  {
                      startLocationUpdates();
                      //퍼미션을 허용했다면 위치 업데이트 시작
                  }
                  else
                  {
                      //거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명하고 앱을 종료

                      if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1]))
                      {
                          //사용자가 거부를 선택한 경우 앱을 다시 실행하여 허용할 경우 앱 사용 가능
                          Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                              @Override
                              public  void onClick(View view)
                              {
                                  finish();
                              }
                          }).show();
                      }
                  }
            }
         }

         private void showDialogForLocationServiceSetting()
         {
             AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
             builder.setTitle("위치 서비스 비활성화");
             builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하시겠습니까?");
             builder.setCancelable(true);
             builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                     startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                 }
             });
             builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                 }
             });
             builder.create().show();
         }

         @Override
        protected  void onActivityResult(int requestCode, int resultCode, Intent data)
         {
             super.onActivityResult(requestCode, resultCode, data);

             switch (requestCode)
             {
                 case GPS_ENABLE_REQUEST_CODE:
                     //사용자가 GPS 활성 시켰는지 검사
                     if (checkLocationServicesStatus())
                     {
                         Log.d(TAG, "onActivity : GPS 활성화 되었음");

                         needRequest = true;

                         return;
                     }
                     break;
             }
         }
    }

