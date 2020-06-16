package com.example.morta.where;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class ListAdapter extends BaseAdapter {
     LayoutInflater mLayoutInflater;
     ArrayList<ShopList> mData;
     Context mContext;

    static final String TAG = "layout:";
    public ListAdapter(Context context, ArrayList<ShopList> data){
        mContext = context;
        mData = data;
        mLayoutInflater = LayoutInflater.from(mContext);
        Log.d(TAG, data  + "/" + context);
    }
    @Override
    public int getCount(){return mData.size();}
    @Override
    public String getItem(int position){return mData.get(position).getShop_name();}
    @Override
    public long getItemId(int position){return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = mLayoutInflater.inflate(R.layout.custom_listview, null);

        final ShopList listviewitem=mData.get(position);
        final TextView shop_name=(TextView)view.findViewById(R.id.shop_name);
        shop_name.setText(listviewitem.getShop_name());
        final TextView shop_class=(TextView)view.findViewById(R.id.shop_class);
        shop_class.setText(listviewitem.getShop_class());
        TextView address=(TextView)view.findViewById(R.id.address);
        address.setText(listviewitem.getAddress());
        final TextView latitude=(TextView)view.findViewById(R.id.latitude);
        latitude.setText(listviewitem.getLatitude());
        TextView longitude=(TextView)view.findViewById(R.id.longitude);
        longitude.setText(listviewitem.getLongitude());

        Button btnChoosePlace = view.findViewById(R.id.choose_place);


        final String lat = listviewitem.getLatitude();
        final String lon = listviewitem.getLongitude();
        final String chat_name = listviewitem.getChat_name();
        final String shop_id = listviewitem.getShop_id();

        btnChoosePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String email= mAuth.getCurrentUser().getEmail();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                databaseReference.child("place").child(chat_name).child("latitude").setValue(lat);
                databaseReference.child("place").child(chat_name).child("longitude").setValue(lon);


               db.collection("users").document(email).collection("User_data").document("user_history").update(shop_id, true);
            }
        });

        return view;
    }
}
