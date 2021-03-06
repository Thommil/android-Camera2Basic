package com.thommil.animalsgo;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.androidexperiments.shadercam.fragments.PermissionsHelper;

public class MainActivity extends AppCompatActivity implements PermissionsHelper.PermissionsListener {

    private static final String TAG = "A_GO/MainActivity";


    private PermissionsHelper mPermissionsHelper;
    private boolean mPermissionsSatisfied = false;

    private void setupPermissions() {
        //Log.d(TAG, "setupPermissions");
        mPermissionsHelper = PermissionsHelper.attach(this);
        mPermissionsHelper.setRequestedPermissions(
                Manifest.permission.CAMERA
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        //setup permissions for M or start normally
        if(PermissionsHelper.isMorHigher())
            setupPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PermissionsHelper.isMorHigher() && !mPermissionsSatisfied) {
            if (!mPermissionsHelper.checkPermissions())
                return;
            else
                mPermissionsSatisfied = true; //extra helper as callback sometimes isnt quick enough for future results
        }
    }

    /**
     * Things are good to go and we can continue on as normal. If this is called after a user
     * sees a dialog, then onResume will be called next, allowing the app to continue as normal.
     */
    @Override
    public void onPermissionsSatisfied() {
        //Log.d(TAG, "onPermissionsSatisfied");
        mPermissionsSatisfied = true;
    }

    /**
     * User did not grant the permissions needed for out app, so we show a quick toast and kill the
     * activity before it can continue onward.
     * @param failedPermissions string array of which permissions were denied
     */
    @Override
    public void onPermissionsFailed(String[] failedPermissions) {
        //Log.d(TAG, "onPermissionsFailed");
        mPermissionsSatisfied = false;
        Toast.makeText(this, "Animal-GO needs all permissions to function, please try again.", Toast.LENGTH_LONG).show();
        this.finish();
    }


}
