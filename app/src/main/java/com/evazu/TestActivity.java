package com.evazu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TestActivity extends AppCompatActivity {

    TextView mEmail;
    Button mlogoutBtn;

    FirebaseAuth mAuth;
    FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();


        if(mFirebaseUser == null) {

            startNewActivity(LoginActivity.class);
        }

        setContentView(R.layout.activity_test);

        mEmail = findViewById(R.id.account);
        mlogoutBtn = findViewById(R.id.logoutBtn);


        mEmail.setText("Hello! "+mFirebaseUser.getEmail());

        mlogoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(TestActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();

            startNewActivity(LoginActivity.class);
        });


    }


    @Override
    public void onBackPressed() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            finish();
        }
        else {
            super.onBackPressed();
        }
        Animatoo.animateSlideLeft(TestActivity.this);
    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(TestActivity.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(TestActivity.this);
    }
}
