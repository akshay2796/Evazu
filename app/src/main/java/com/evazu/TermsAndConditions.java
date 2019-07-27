package com.evazu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TermsAndConditions extends AppCompatActivity implements View.OnClickListener {

    private Button continueBtn;
    private CheckBox checkBox1, checkBox2;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private boolean b = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent.hasExtra("from_class")) {
            if(intent.getExtras().get("from_class").toString().equals("maps_terms")) {
                setContentView(R.layout.terms_and_conditions);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                b = false;
            }
            else {
                setContentView(R.layout.activity_terms_and_conditions);
            }
        }

        getSupportActionBar().setTitle("Rental Agreement");

        if(b) {

            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());

            continueBtn = findViewById(R.id.continueButton);
            checkBox1 = findViewById(R.id.checkbox1);
            checkBox2 = findViewById(R.id.checkbox2);

            checkBox1.setOnClickListener(this);
            checkBox2.setOnClickListener(this);


            continueBtn.setOnClickListener(v -> {

                if (checkBox1.isChecked() && checkBox2.isChecked()) {
                    startNewActivity(MapsActivity.class);

                    setStatus();
                } else {
                    Toast.makeText(TermsAndConditions.this, "Please agree to the rental agreement!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setStatus() {

        mDatabase.child("email").setValue(mAuth.getCurrentUser().getEmail());
        mDatabase.child("verification").setValue(1);
    }

    private void startNewActivity(Class intentClass) {
        Intent intent;
        intent = new Intent(TermsAndConditions.this, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(TermsAndConditions.this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.checkbox1 || v.getId() == R.id.checkbox2) {
            if (checkBox1.isChecked() && checkBox2.isChecked()) {
                continueBtn.setAlpha(1f);
                continueBtn.setEnabled(true);
            }
            else {
                continueBtn.setAlpha(0.2f);
                continueBtn.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!b) {
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
