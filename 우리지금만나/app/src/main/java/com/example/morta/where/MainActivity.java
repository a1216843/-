package com.example.morta.where;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;
import java.io.IOException;

import androidx.drawerlayout.widget.DrawerLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import org.w3c.dom.Text;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ImageButton imgbtn;
    private static  final String TAG = "Main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgbtn = (ImageButton) findViewById(R.id.start_button);

        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.morta.where.LogInActivity.class);
                //Intent란 앱 컴포넌트가 무엇을 할 것인지를 담는 메시지 객체로 다른 액티비티, 서비스 등을 실행하거나 데이터를 주고 받기 위한 용도로 사용
                startActivity(intent);
            }
        });
        //MainActivity는 AppCompatActivity를 상속하는 클래스이며, onCreate에 대한 Override는 매개변수로 Bundle savedInstanceState를 받아 이루어진다.
        //super는 자신이 아닌 부모클래스의 메소드를 사용하기 위한 예약어로 super.onCreate(savedInstanceState)는 부모클래스인 AppCompatActivity의 onCreate메소드에 savedInstanceState를 매개변수로 넣어 사용하겠다는 뜻
    }

}
