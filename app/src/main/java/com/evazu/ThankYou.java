package com.evazu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class ThankYou extends AppCompatActivity {

    private Button continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        continueBtn = findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ThankYou.this, MapsActivity.class));
        Animatoo.animateSlideLeft(this);
    }
}
