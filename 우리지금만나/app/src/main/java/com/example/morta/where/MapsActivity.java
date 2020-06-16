package com.example.morta.where;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.widget.Button;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback
{
    Button btn1,btn1_c, btn2, btn3, btn4;
    private GoogleMap mMap;
    private  Marker currentMarker = null;
    Marker UserMarker = null;
    final static String dbName = "app_db.db";
    public static final String ROOT_DIR = "/data/data/com.example.morta.where/databases/";
    SQLiteDatabase db;
    String sql;
    DBHelper dbHelper;
    String chat_name;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String EMAIL= mAuth.getCurrentUser().getEmail();
    int idx = EMAIL.indexOf("@");
    String email = EMAIL.substring(0, idx);
    Map<String, String>  result = new HashMap<String, String>();
    List list = new ArrayList();
    XY_Place place;
    LatLng user_position;

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
    LatLng currentPosition;
    LatLng DEFAULT_LOCATION = new LatLng(0, 0);

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private ListView shop_list;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private View mLayout;
    // Snackbar 사용하기 위해서 View가 필요, Toast에선 Context
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.layout);

        Intent intent = getIntent();
        chat_name = intent.getStringExtra("chat_name");


        mLayout = findViewById(R.id.layout);
        shop_list = findViewById(R.id.shop_list);

        locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(UPDATA_INTERVAL_MS).setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btn4 = (Button) findViewById(R.id.button4);

        final DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.layout);
        final View drawerView = (View)findViewById(R.id.drawer);
        // 드로어 화면을 열고 닫을 버튼 객체 참조
        Button btnOpenDrawer = (Button) findViewById(R.id.btn_OpenDrawer);
        Button btnCloseDrawer = (Button) findViewById(R.id.btn_CloseDrawer);

        shop_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TextView lat = (TextView)shop_list.findViewById(R.id.latitude);
                TextView lon = (TextView)shop_list.findViewById(R.id.longitude);
                Double latitude = Double.parseDouble(lat.getText().toString());
                Double longitude = Double.parseDouble(lon.getText().toString());
                LatLng pos = new LatLng(latitude ,longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(pos));
                Log.d(TAG, "포지션: "+ position + "/" + shop_list.getItemAtPosition(position));
            }
        });

        // 드로어 여는 버튼 리스너
        btnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });


        // 드로어 닫는 버튼 리스너
        btnCloseDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(drawerView);
            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap){
        mMap = googleMap;
        Log.d(TAG, "onMapReady :");
        setDefaultLocation();
        setDB(this);
        dbHelper = new DBHelper(this, "app_db.db");
        userPosition();

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

        btn1 = (Button) findViewById(R.id.button1);
        btn1_c = (Button) findViewById(R.id.button1_cancel);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GPSBackgroundSvc.class);
                intent.putExtra("chat_name", chat_name);
                Log.d("서비스?", "버튼1 실행");
                startService(intent);
            }
        });

        btn1_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GPSBackgroundSvc.class);
                Log.d("서비스?", "버튼1 취소");
                stopService(intent);
            }
        });




        btn2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
                {
                    @Override
                    public  void onMapClick(LatLng point)
                    {
                       // MarkerOptions mOptions =new MarkerOptions();
                        //mOptions.title("마커 좌표");
                        Double latitude = point.latitude;
                        Double longitude = point.longitude;
                       // mOptions.snippet(latitude.toString() + ", "+ longitude.toString());
                       // mOptions.position(new LatLng(latitude, longitude));
                       // googleMap.addMarker(mOptions);
                        ArrayList<DB_Search> search = new ArrayList<>();


                        double dist = 0.01;
                        double x_1 = latitude + dist;
                        double x_2 = latitude - dist;
                        double y_1 = longitude + dist;
                        double y_2 = longitude - dist;

                        db = dbHelper.getReadableDatabase();
                        sql = String.format("SELECT * FROM restaurant WHERE X BETWEEN %f AND %f AND Y BETWEEN %f AND %f", x_2, x_1, y_2, y_1);
                        Cursor cursor = db.rawQuery(sql, null);
                        ArrayList<ShopList> data = new ArrayList<>();
                        if(cursor.getCount()>0) {
                            while (cursor.moveToNext()) {
                                DB_Search dbs = new DB_Search(cursor.getString(0),cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(5),cursor.getString(6),cursor.getString(8));
                                search.add(dbs);
                                Log.d(TAG, "데이터 조회 성공" + list.size());
                                ShopList sec = new ShopList(cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(6),  cursor.getString(5), chat_name, cursor.getString(0));
                                data.add(sec);
                            }
                            updateList(data);
                        }
                        else
                        {
                            Log.d(TAG, "조회 결과 없음:" + sql);
                        }
                        cursor.close();
                        dbHelper.close();
                        for(int i=0; i<search.size();i++)
                        {
                            DB_Search getrow = search.get(i);
                            MarkerOptions mOptions_ =new MarkerOptions();
                            mOptions_.title(getrow.getDb_name());
                            mOptions_.snippet(getrow.getDb_lat() + ", "+ getrow.getDb_lon());
                            mOptions_.position(new LatLng(Double.parseDouble(getrow.getDb_lon()), Double.parseDouble(getrow.getDb_lat())));
                            mMap.addMarker(mOptions_);
                            Log.d(TAG, "마커찍기: "+list.size() +"반복횟수: "+ i + "이름 : "+ search.get(i).getDb_name() +"/ "+ getrow.getDb_lat() +" / "+getrow.getDb_lon());
                        }

                    }
                });
            }
        });

            btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseReference.child("place").child(chat_name).child("latitude").setValue(null);
                    databaseReference.child("place").child(chat_name).child("longitude").setValue(null);
                }
            });

           /* btn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });*/

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

            Log.d("살려줘", "startLocationUpdates : call mFUsedLocationClient.requestLocationUpdates" + Looper.myLooper());

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

        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        //mMap.moveCamera(cameraUpdate);
    }
    public void setDefaultLocation()
    {
        databaseReference.child("place").child(chat_name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                place = dataSnapshot.getValue(XY_Place.class);
                if(place.getLongitude()==null || place.getLatitude() == null)
                {
                    Log.d(TAG, "null 읽음" + DEFAULT_LOCATION);
                    return;
                }
                LatLng DEFAULT_LOCATION = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitude()));
                Log.d(TAG, "읽어오기 성공" + DEFAULT_LOCATION);
                MarkerOptions mOptions_ =new MarkerOptions();
                mOptions_.title("약속장소");
                mOptions_.snippet("약속장소입니다.");
                mOptions_.position(DEFAULT_LOCATION);
                BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.place_marker);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap placeMarker = Bitmap.createScaledBitmap(b, 120, 120, false);
                mOptions_.icon(BitmapDescriptorFactory.fromBitmap(placeMarker));
                mMap.addMarker(mOptions_);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
                mMap.moveCamera(cameraUpdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DEFAULT_LOCATION = new LatLng(0, 0);
                Log.d(TAG, "읽어오기 실패");
            }
        });

        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";

        if (currentMarker != null) currentMarker.remove();
        Log.d(TAG, "으으으으으" + DEFAULT_LOCATION);
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

    public  static void setDB(Context ctx)
    {
        File folder = new File(ROOT_DIR);
        if(folder.exists())
        { }
        else
        {
            folder.mkdir();
        }
        AssetManager assetManager = ctx.getResources().getAssets();
        File outfile = new File(ROOT_DIR+"app_db.db");
        InputStream is = null;
        FileOutputStream fo = null;
        long filesize =0;
        try
        {
            is = assetManager.open("app_db.db", AssetManager.ACCESS_BUFFER);
            filesize = is.available();
            if(outfile.length() <=0)
            {
                byte[] tempdata = new byte[(int) filesize];
                is.read(tempdata);
                is.close();
                outfile.createNewFile();
                fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
                Log.d(TAG, "복사 성공");
            }
            else
            {
            }
        }
        catch (IOException e)
        {
        }
    }

    public void userPosition()
    {
        databaseReference.child("place").child(chat_name).child("user_position").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(UserMarker != null)
               {
                   UserMarker.remove();
               }
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String user_mail = snapshot.getKey();
                    if(user_mail == email)
                    {
                        Log.d("마커-패스한 거 맞어?", email + "/" + user_mail);
                        continue;
                    }
                    Log.d("마커-패스한 거 맞어?", email + "/" + user_mail);
                    int i = 0;
                    Double latitude =0.0;
                    Double longitude=0.0;
                    Log.d("유저마커-패스했나?", "패쓰패스패스패스ㅐ" + snapshot.getKey());
                    for(DataSnapshot deeperSnapshot : snapshot.getChildren())
                    {
                        if(i==0)
                        {
                            latitude = Double.parseDouble(deeperSnapshot.getValue().toString());
                            Log.d("유저마커-패스했나?", "경도" + deeperSnapshot.getValue());
                            i++;
                        }
                        else
                        {
                            longitude = Double.parseDouble(deeperSnapshot.getValue().toString());
                            Log.d("유저마커-패스했나?", "위도" + deeperSnapshot.getValue());
                        }
                    }
                    user_position = new LatLng(latitude, longitude);
                    Log.d("유저마커-패스했나?", "패쓰패스패스패스ㅐ" + latitude +"/"+longitude);
                    MarkerOptions mOptions_ =new MarkerOptions();
                    mOptions_.title(user_mail);
                    mOptions_.snippet(user_mail + "의 현재 위치");
                    mOptions_.position(user_position);
                    Log.d("유저마커-마커 제대로 들어갔나?", user_position.toString());
                    BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.user_marker);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap placeMarker = Bitmap.createScaledBitmap(b, 120, 120, false);
                    mOptions_.icon(BitmapDescriptorFactory.fromBitmap(placeMarker));
                    UserMarker = mMap.addMarker(mOptions_);
                }


                //user_positions = dataSnapshot.getValue(User_position.class);
                //place = dataSnapshot.getValue(XY_Place.class);
               /* LatLng DEFAULT_LOCATION = new LatLng(Double.parseDouble(place.getLatitude()), Double.parseDouble(place.getLongitude()));
                Log.d(TAG, "읽어오기 성공" + DEFAULT_LOCATION);
                MarkerOptions mOptions_ =new MarkerOptions();
                mOptions_.title("약속장소");
                mOptions_.snippet("약속장소입니다.");
                mOptions_.position(DEFAULT_LOCATION);
                BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.place_marker);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap placeMarker = Bitmap.createScaledBitmap(b, 120, 120, false);
                mOptions_.icon(BitmapDescriptorFactory.fromBitmap(placeMarker));
                mMap.addMarker(mOptions_);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
                mMap.moveCamera(cameraUpdate);*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "읽어오기 실패");
            }
        });
    }

    public void updateList(ArrayList<ShopList> data)
    {
        ListAdapter adapter = new ListAdapter(this, data);
        shop_list.setAdapter(adapter);
    }

    static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name)
        {
            super(context, name, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {

        }
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
        {

        }
    }
}

