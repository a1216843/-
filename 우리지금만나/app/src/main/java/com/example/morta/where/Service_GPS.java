package com.example.morta.where;

        import android.Manifest;
        import android.app.Notification;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Address;
        import android.location.Geocoder;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.IBinder;
        import android.os.Looper;
        import android.util.Log;
        import android.widget.RemoteViews;
        import android.widget.Toast;

        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.location.LocationCallback;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationResult;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.LocationSettingsRequest;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.firestore.FirebaseFirestore;

        import java.io.IOException;
        import java.util.List;
        import java.util.Locale;

        import androidx.core.app.ActivityCompat;
        import androidx.core.app.NotificationCompat;
        import androidx.core.content.ContextCompat;
        import androidx.core.app.NotificationCompat.Builder;

        import static com.example.morta.where.ListAdapter.TAG;

public class Service_GPS extends Service {
    private String TAG = "서비스입니다.";
    private Marker currentMarker = null;
    int iThreadInterval = 5000;
    boolean bThreadGo =true;
    private NotificationManager mNotifyMgr;
    private NotificationCompat.Builder mBuilder;

    public Service_GPS()
    {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스는 한 번 실행되면 계속 실행된 상태로 있는다.
        // 따라서 서비스 특성상 intent를 받아서 처리하기에 적합하지 않다.
        // intent에 대한 처리는 onStartCommand()에서 처리해준다.
        Log.d(TAG, "onCreate() called");
        //startForeGroundService();
    }

    //**중요**//
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called");

        if(intent == null)
        {
            Log.d(TAG, "널입니다요");
            return Service.START_STICKY; //서비스가 종료되어도 자동으로 다시 실행 x
        }
        else
        {
            //intent가 null이 아닐 때 액티비티에서 전달한 내용을 뽑아낸다.
            String command = intent.getStringExtra("command");
            String name = intent.getStringExtra("name");



           Intent mMapIntent = new Intent(this, MapsActivity.class);
            PendingIntent mPendingIntent =PendingIntent.getActivity(this, 1, mMapIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
           // NotificationCompat.Builder mBuilder = new NotificationCompat.Builder()
            if(Build.VERSION.SDK_INT >= 26) {
                NotificationChannel mChannel = new NotificationChannel("채널1", "채널1", NotificationManager.IMPORTANCE_DEFAULT);
                mNotifyMgr.createNotificationChannel(mChannel);
                mBuilder = new NotificationCompat.Builder(this, mChannel.getId());
            }
            else {
                mBuilder = new NotificationCompat.Builder(this);
            }
                 mBuilder.setAutoCancel(true)
                            .setSmallIcon(android.R.drawable.btn_star)
                            .setContentTitle("우리지금만나")
                            .setContentIntent(mPendingIntent)
                            .setContentText("GPS 정보를 수집하고 있습니다.");

           mNotifyMgr.notify(001, mBuilder.build());


            bThreadGo = true;

            new Thread(mRun).start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mNotifyMgr.cancelAll();
        bThreadGo = false;
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    Runnable mRun = new Runnable() {
        @Override
        public void run() {
            try{
                while(bThreadGo == true) {
                    Log.d(TAG, "스레드 동작중");

                    Thread.sleep(iThreadInterval);

                    SetPosition();
                    // 여기부터 위치 기록을 위한 코드 작성
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Log.d(TAG, "태스크 리무브" + rootIntent);
        stopSelf();
    }

    public void SetPosition()
    {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        int hasFIndLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int hasForeGroundPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE);
        if(hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED || hasFIndLocationPermission != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "퍼미션 획득 실패");
        }
        Log.d(TAG, hasCoarseLocationPermission +" / "+ hasFIndLocationPermission+ " / "+ hasForeGroundPermission );
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String provider = location.getProvider();
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d(TAG, "위치정보: "+provider+"  경도:"+latitude + "  위도:"+longitude +"/"+ gpsLocationListener);

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
    }

    LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.d(TAG, "위치정보2"+provider+"  경도:"+latitude + "  위도:"+longitude);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference();

            databaseReference.child("place").child("test").child("latitude").setValue(latitude);
            databaseReference.child("place").child("test").child("longitude").setValue(longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
