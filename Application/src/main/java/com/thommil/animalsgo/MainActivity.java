package com.thommil.animalsgo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "animals-go";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        OpenCVUtils.init();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //final Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                //MainActivity.this.startActivity(intent);
            }
        });
    }


}
