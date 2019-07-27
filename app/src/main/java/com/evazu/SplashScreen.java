package com.evazu;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    boolean result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            if(mAuth.getCurrentUser() != null) {
                new GetRentalStatus().getResult(result -> {
                    if(result) {
                        startNewActivity(MapsActivity.class);
                    }
                    else {
                        startNewActivity(Permissions.class);
                    }
                });
            }
            else {
                startNewActivity(Main2Activity.class);
            }
        }, 3000);
    }

    private void startNewActivity(Class intentClass) {
        Intent intent;
        intent = new Intent(SplashScreen.this, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(SplashScreen.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideLeft(SplashScreen.this);
    }
}
