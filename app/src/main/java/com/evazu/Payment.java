package com.evazu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class Payment extends AppCompatActivity {

    private Toolbar toolbar;
    private Button linkPaytm, updateNum;
    private EditText paytm_num;
    private TextView paytmNumber, paytmBalance, paytmNotLinked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        toolbar = findViewById(R.id.toolbar_payments);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        linkPaytm = findViewById(R.id.paytm_link);
        paytm_num = findViewById(R.id.paytm_num);
        updateNum = findViewById(R.id.updateNumber);
        paytmNumber = findViewById(R.id.paytmNumber);
        paytmBalance = findViewById(R.id.paytm_balance);
        paytmNotLinked = findViewById(R.id.paytm_not_linked);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}
