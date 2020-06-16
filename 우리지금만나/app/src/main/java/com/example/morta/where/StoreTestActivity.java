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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class StoreTestActivity extends AppCompatActivity {

    private  EditText NAME, AGE;
    private Button M_button, W_button;
    private static final String TAG = "StoreTestActivity";
    private String name;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected  void onCreate(Bundle saveInstanceState) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email= mAuth.getCurrentUser().getEmail();
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name = documentSnapshot.get("name").toString();
                if(name != "")
                {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    String email= mAuth.getCurrentUser().getEmail();
                    Intent intent = new Intent(StoreTestActivity.this, StartActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }
            }
        });


        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_test);

        M_button = (Button)findViewById(R.id.M_button);
        W_button = (Button)findViewById(R.id.W_button);
        NAME = (EditText)findViewById(R.id.user_name);
        AGE = (EditText)findViewById(R.id.user_age);



        M_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_ = getIntent();
                String EMAIL = intent_.getStringExtra("email");
                Map<String, Object> user = new HashMap<>();
                user.put("name", NAME.getText().toString());
                user.put("age", AGE.getText().toString());
                user.put("sex", true);
                db.collection("users").document(EMAIL).set(user);
                       /*.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                        });*/
                Intent intent = new Intent(StoreTestActivity.this, StartActivity.class);
                intent.putExtra("email", EMAIL);
                startActivity(intent);
                finish();
            }
        });

        W_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_ = getIntent();
                String EMAIL = intent_.getStringExtra("email");
                Map<String, Object> user = new HashMap<>();
                user.put("name", NAME.getText().toString());
                user.put("age", AGE.getText().toString());
                user.put("sex", false);
                db.collection("users").document(EMAIL).set(user);
                       /*.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                        });*/
                Intent intent = new Intent(StoreTestActivity.this, StartActivity.class);
                intent.putExtra("email", EMAIL);
                startActivity(intent);
                finish();
            }
        });
    }
}
