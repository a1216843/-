package com.example.morta.report_1_1;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class Report_1_1 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_1_1);
    }

    public void onClickButton_1(View v)
    {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.naver.com"));
        startActivity(myIntent);
    }

    public void onClickButton_2(View v)
    {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:010"));
        startActivity(myIntent);
    }

    private int REQ_PICK_CODE=100;

    public void onClickButton_3(View v)
    {
        Intent pickerIntent = new Intent(Intent.ACTION_PICK);
        pickerIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        pickerIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickerIntent, REQ_PICK_CODE);
    }

}
