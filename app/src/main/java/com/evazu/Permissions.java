package com.evazu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.squareup.picasso.Picasso;

public class Permissions extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "Permissions";

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_SMS = 101;
    private static final int REQUEST_LOCATON = 102;
    private static final int REQUEST_SETTINGS = 121;

    private boolean camera, sms, location, denied;

    private Button submitBtn;
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(this);

        camera = false;
        sms = false;
        location = false;
        denied = false;

        if(!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            startNewActivity(VerificationSteps.class);
        }
        else {
            check(false);
        }

        Log.d(TAG, "CAMERA 1: "+camera);
        Log.d(TAG, "SMS 1: "+sms);
        Log.d(TAG, "LOCATION 1: "+location);
    }

    private void check(boolean check) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            camera = true;
        }
        else {
            if(check) {
                Toast.makeText(this, "Please Enable All Permissions to Continue!", Toast.LENGTH_LONG).show();
                openSettings();
            }
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            sms = true;
        }
        else {
            if(check) {
                Toast.makeText(this, "Please Enable All Permissions to Continue!", Toast.LENGTH_LONG).show();
                openSettings();
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = true;
        }
        else {
            if(check) {
                Toast.makeText(this, "Please Enable All Permissions to Continue!", Toast.LENGTH_LONG).show();
                openSettings();
            }
        }

        if(camera && sms && location) {
            startNewActivity(VerificationSteps.class);
        }

    }

    private void cameraPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);

            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        }
        else {
            camera = true;
        }
    }

    private void smsPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, REQUEST_SMS);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, REQUEST_SMS);
            }
        }
        else {
            sms = true;
        }
    }

    private void locationPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATON);

            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATON);
            }
        }
        else {
            location = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    camera = true;
                    check(false);
                    smsPermission();
                }
                else {
                    denied = true;
                    cameraPermission();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                break;
            case REQUEST_SMS:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sms = true;
                    check(false);
                    locationPermission();
                }
                else {
                    denied = true;
                    smsPermission();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                break;
            case REQUEST_LOCATON:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location = true;
                    check(false);
                }
                else {
                    denied = true;
                    locationPermission();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }

                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_OK) {
            switch (resultCode) {
                case REQUEST_CAMERA:
                    camera = true;
                    break;
                case REQUEST_SMS:
                    sms = true;
                    break;
                case REQUEST_LOCATON:
                    location = true;
                    break;
                case REQUEST_SETTINGS:
                    if(camera && sms && location) {
                        startNewActivity(VerificationSteps.class);
                    }
                    break;
            }
            check(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraLayout:
                cameraPermission();
                break;
            case R.id.smsLayout:
                smsPermission();
                break;
            case R.id.locationLayout:
                locationPermission();
                break;
            case R.id.submitBtn:

                cameraPermission();

                if(!firstTime) {
                    check(true);
                }

                if(firstTime) {
                    firstTime = false;
                }


                break;
        }
    }

    private void startNewActivity(Class newClass) {
        Intent intent = new Intent(this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(this);
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        check(false);
    }
}
