package com.tfcamerademo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tfcamerademo.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        findViewById(R.id.faceBtn).setOnClickListener(this);
//        findViewById(R.id.roadBtn).setOnClickListener(this);
//        findViewById(R.id.bodyBtn).setOnClickListener(this);
//        findViewById(R.id.carAndLineBtn).setOnClickListener(this);


    }

    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        switch (v.getId()) {
            case R.id.faceBtn:
                intent.putExtra("Flag", 1);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.roadBtn:
                intent.putExtra("Flag", 2);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.bodyBtn:
                intent.putExtra("Flag", 3);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.carAndLineBtn:
                intent.putExtra("Flag", 4);
                MainActivity.this.startActivity(intent);
                break;
        }
    }
}
