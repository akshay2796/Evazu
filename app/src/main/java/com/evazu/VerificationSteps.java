package com.evazu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerificationSteps extends AppCompatActivity implements View.OnClickListener {

    boolean mobile_verified = false;
    boolean photo_verified = false;
    boolean aadhaar_verified = false;

    private String user_id;
    private ImageView step1, step2, step3;
    private Button submitBtn;

    private ConstraintLayout step1_RL, step2_RL, step3_RL, parent;

    private DatabaseReference mDatabaseRef;

    final String TAG = "VerificationSteps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_steps);

        submitBtn = findViewById(R.id.submitBtn);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(user_id);

        setVerified();

        step1 = findViewById(R.id.step1_imv);
        step2 = findViewById(R.id.step2_imv);
        step3 = findViewById(R.id.step3_imv);
        parent = findViewById(R.id.parent);

        step1_RL = findViewById(R.id.step1_RL);
        step2_RL = findViewById(R.id.step2_RL);
        step3_RL = findViewById(R.id.step3_RL);

        step1_RL.setOnClickListener(this);
        step2_RL.setOnClickListener(this);
        step3_RL.setOnClickListener(this);

        submitBtn.setOnClickListener(this);

    }

    private void setVerified() {
        EvazuDatabase db = new EvazuDatabase();
        db.getValue("mobile_verified", result -> {
            if(result != null) {
                mobile_verified = true;
                step1.setImageResource(R.drawable.check_bg);
            }
        });
        db.getValue("profile_pic", result -> {
            if(result != null) {
                photo_verified = true;
                step2.setImageResource(R.drawable.check_bg);
            }
        });
        db.getValue("aadhaar_uploaded", result -> {
            if(result != null) {
                aadhaar_verified = true;
                step3.setImageResource(R.drawable.check_bg);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVerified();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.step1_RL:
                startActivity(new Intent(VerificationSteps.this, StepOneProfile.class));
                break;
            case R.id.step2_RL:
                startActivity(new Intent(VerificationSteps.this, ProfilePicture.class));
                break;
            case R.id.step3_RL:
                Intent intent = new Intent(VerificationSteps.this, AadhaarCard.class);
                intent.putExtra("prevClass", "VerificationSteps");

                startActivity(intent);
                break;
            case R.id.submitBtn:
                if(mobile_verified && photo_verified) {
                    startNewActivity(TermsAndConditions.class);
                }
                else {
                    Toast.makeText(this, "Please Complete Step 1 & 2 to continue", Toast.LENGTH_LONG).show();
                }
        }

    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(VerificationSteps.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(VerificationSteps.this);
    }
}
