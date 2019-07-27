package com.evazu;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;

import me.dm7.barcodescanner.core.CameraPreview;
import me.dm7.barcodescanner.core.CameraUtils;
import me.dm7.barcodescanner.core.CameraWrapper;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class QRCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ToggleButton torchBtn, scan_qr;
    private CardView codeView;
    private ConstraintLayout parent;
    private ImageView overlay, scooter_qr;
    private EditText codeBox;

    private ZXingScannerView scannerView;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Scan Evazu");

        parent = findViewById(R.id.parent);
        torchBtn = findViewById(R.id.torchBtn);
        scan_qr = findViewById(R.id.scan_qr);
        codeView = findViewById(R.id.codeView);
        overlay = findViewById(R.id.overlay);
        scooter_qr = findViewById(R.id.scooter_qr);
        codeBox = findViewById(R.id.codeBox);

        scannerView = new ZXingScannerView(this);
        scannerView.setFormats(Arrays.asList(BarcodeFormat.QR_CODE));

        int camera_id = CameraUtils.getDefaultCameraId();
        CameraWrapper wrapper = CameraWrapper.getWrapper(CameraUtils.getCameraInstance(camera_id), camera_id);
        mPreview = new CameraPreview(this, wrapper, null);

        parent.addView(scannerView);
        mPreview.setVisibility(View.INVISIBLE);
        parent.addView(mPreview);
        scooter_qr.bringToFront();

        setupUI(parent);

        int currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >=  Build.VERSION_CODES.M)
        {
            if(!checkPermission())
            {
                requestPermission();
            }
            else {
                torchBtn.setOnClickListener(v -> scannerView.toggleFlash());
                scan_qr.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isChecked) {
                        scannerView.setVisibility(View.INVISIBLE);
                        mPreview.setVisibility(View.VISIBLE);
                        codeView.setVisibility(View.VISIBLE);
                        codeView.bringToFront();
                        overlay.setVisibility(View.VISIBLE);
                        overlay.bringToFront();
                        scooter_qr.setImageResource(R.drawable.scooter_code);
                        scooter_qr.bringToFront();
                    }
                    else {
                        mPreview.setVisibility(View.INVISIBLE);
                        scannerView.setVisibility(View.VISIBLE);
                        codeView.setVisibility(View.INVISIBLE);
                        overlay.setVisibility(View.INVISIBLE);
                        scooter_qr.setImageResource(R.drawable.scooter_qr);
                    }
                });
            }
        }



    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    private static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(QRCodeActivity.this);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                scannerView.setResultHandler(this);
                scannerView.startCamera();

            } else {
                requestPermission();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0) {

                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    (dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{CAMERA},
                                                    REQUEST_CAMERA);
                                        }
                                    });
                        }
                    }
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(QRCodeActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText();
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='#000000'>Scan Result</font>"));
        builder.setPositiveButton("OK", (dialog, which) -> scannerView.resumeCameraPreview(QRCodeActivity.this));
        builder.setNeutralButton("Visit", (dialog, which) -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myResult));
                startActivity(browserIntent);
            }
            catch (Exception e) {
                Toast.makeText(QRCodeActivity.this, "Not a Valid URL", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                scannerView.resumeCameraPreview(this::handleResult);
                e.printStackTrace();
            }
        });
        builder.setMessage(Html.fromHtml("<font color='#000000'>"+result.getText()+"</front"));
        AlertDialog alert1 = builder.create();
        alert1.getWindow().setBackgroundDrawable(getDrawable(R.color.white));
        alert1.show();
    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        startNewActivity(MapsActivity.class);
        Animatoo.animateSlideRight(this);
        scannerView.stopCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}