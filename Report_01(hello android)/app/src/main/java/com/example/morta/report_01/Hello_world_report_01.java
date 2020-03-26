package com.example.morta.report_01;



import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Hello_world_report_01 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world_report_01);

        final TextView textView= findViewById(R.id.textView); //TextView타입 textView의 값을 findViewById를 통해 찾아온다. (xml에서 작성한 것)
        Button btn = findViewById(R.id.btn); //마찬가지로 Button타입의 btn의 값을 찾아옴

        btn.setOnClickListener(new View.OnClickListener() //btn의 클릭을 감지하고 이벤트 발생
        {
            @Override
            public void onClick(View v)
            {
                textView.setText("Hello Android!"); //버튼 클릭 후 바뀔 textView의 Text 내용
            }
        });



    }
}
