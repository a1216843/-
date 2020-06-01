package com.example.morta.where;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class StoreTestActivity extends AppCompatActivity {

    private  EditText NAME, AGE;
    private Button M_button, W_button;
    private static final String TAG = "StoreTestActivity";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected  void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_test);

        M_button = (Button)findViewById(R.id.M_button);
        W_button = (Button)findViewById(R.id.W_button);
        NAME = (EditText)findViewById(R.id.user_name);
        AGE = (EditText)findViewById(R.id.user_age);

        M_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> user = new HashMap<>();
                user.put("name", NAME.getText().toString());
                user.put("age", AGE.getText().toString());
                db.collection("users")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                /*intent.putExtra("user_name", NAME.getText().toString());
                intent.putExtra("user_name", AGE.getText().toString());
                intent.putExtra("user_sex", true);*/
                Intent intent = new Intent(StoreTestActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });

        W_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreTestActivity.this , StartActivity.class);
                intent.putExtra("user_name", NAME.getText().toString());
                intent.putExtra("user_name", AGE.getText().toString());
                intent.putExtra("user_sex", false);
                startActivity(intent);
                finish();
            }
        });
    }
}
