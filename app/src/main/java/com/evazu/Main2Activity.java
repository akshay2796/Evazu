package com.evazu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;

public class Main2Activity extends AppCompatActivity {

    private Button mLoginBtn;
    private TextView mRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
            startNewActivity(LoginActivity.class);

        }
        setContentView(R.layout.activity_main2);

        mLoginBtn = findViewById(R.id.loginBtn);
        mRegister = findViewById(R.id.register);

        mLoginBtn.setOnClickListener(v -> {
            startNewActivity(LoginActivity.class);
        });

        mRegister.setOnClickListener(v -> {
            startActivity(new Intent(Main2Activity.this, SignUpActivity.class));
            Animatoo.animateSlideRight(Main2Activity.this);
        });
    }

    private void startNewActivity(Class intentClass) {
        Intent intent;
        intent = new Intent(Main2Activity.this, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(Main2Activity.this);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Do you really want to exit?")
                .setNegativeButton(Html.fromHtml("<font color='#14A79C'>YES</font>"), (dialog, which) -> Main2Activity.super.onBackPressed())
                .setPositiveButton(Html.fromHtml("<font color='#14A79C'>NO</font>"), (dialog, which) -> dialog.cancel())
                .setNeutralButton(Html.fromHtml("<font color='#14A79C'>RATE US</font>"), (dialog, which) -> {
                    //final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    final String appPackageName = "com.mxtech.videoplayer.ad";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        Animatoo.animateSlideLeft(this);
    }
}
