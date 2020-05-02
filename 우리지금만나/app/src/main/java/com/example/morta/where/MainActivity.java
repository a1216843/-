package com.example.morta.where;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    ImageButton imgbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgbtn = (ImageButton) findViewById(R.id.start_button);

        imgbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, com.example.morta.where.LogInActivity.class);
                startActivity(intent);
            }
        });

    }


}
