package com.evazu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class OtpVerify extends AppCompatActivity {

    private String phoneNum;
    private String TAG = "OtpVerify";
    private final String countryCode = "+91";
    private String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private PhoneAuthProvider mPhoneAuthProvider;
    private PhoneAuthCredential mPhoneCredential;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private Button continueBtn;
    private ConstraintLayout parent;
    private ProgressBar mProgressBar;
    private ImageView backBtn;
    private OtpView otpView;
    private Toolbar toolbar;

    private String mVerificationId;
    private String code;
    private TextView resendOtp;

    PhoneAuthProvider.ForceResendingToken mForceResendingToken;

    boolean mVerificationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        toolbar = findViewById(R.id.toolbar_otp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        Intent intent = getIntent();

        phoneNum = intent.getStringExtra("Phone");
        phoneNum = countryCode + phoneNum;

        mPhoneAuthProvider = PhoneAuthProvider.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());

        backBtn = findViewById(R.id.backBtn);

        otpView = findViewById(R.id.otpView);
        otpView.setOtpCompletionListener(s -> verifyPhone(s));

        continueBtn = findViewById(R.id.takeSelfie);
        resendOtp = findViewById(R.id.resendCode);
        parent = findViewById(R.id.parent);
        mProgressBar = findViewById(R.id.progressBar);

        phoneAuth();

        continueBtn.setOnClickListener(v -> {
            continueBtn.setEnabled(false);
            if(!TextUtils.isEmpty(otpView.getText()) && otpView.getText().length() == 6) {
                verifyPhone(otpView.getText().toString());
            }
            continueBtn.setEnabled(true);
        });

        resendOtp.setOnClickListener(v -> {
            resendOtp.setEnabled(false);
            resendCode(phoneNum, mForceResendingToken);
            resendOtp.setEnabled(true);
        });

        backBtn.setOnClickListener(v -> onBackPressed());

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("message");
                Log.d(TAG, message);

                if(message.contains("verification")) {
                    String otpCode = "";
                    for(int i = 0;i<message.length();i++) {
                        char ch = message.charAt(i);
                        if(Character.isDigit(ch)) {
                            otpCode = message.substring(i,(i+6));
                            break;
                        }
                    }
                    final String otp = otpCode;
                    otpView.setText(otp);
                    new Handler().postDelayed(() -> verifyPhone(otp), 1000);
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            startNewActivity(LoginActivity.class);
        }
        else {
            if (mVerificationInProgress) {
                phoneAuth();
            }
        }
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void phoneAuth() {

        parent.setEnabled(false);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        if(!mVerificationInProgress) {

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                }

                @Override
                public void onVerificationFailed(FirebaseException e) {

                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(verificationId, forceResendingToken);

                    mVerificationId = verificationId;
                    mForceResendingToken = forceResendingToken;
                }
            };

            mPhoneAuthProvider.verifyPhoneNumber(
                    phoneNum, 60, TimeUnit.SECONDS, this, mCallbacks
            );
        }

        parent.setEnabled(true);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mVerificationInProgress = true;
    }

    private void verifyPhone(String otp) {

        code = otp;

        try {
            mPhoneCredential = PhoneAuthProvider.getCredential(mVerificationId, code);
        }
        catch (Exception e) {
            Toast.makeText(OtpVerify.this, "Cannot verify Phone!", Toast.LENGTH_LONG).show();
        }

        mAuth.getCurrentUser().linkWithCredential(mPhoneCredential).addOnCompleteListener(task ->  {
            if(task.isSuccessful()) {
                Toast.makeText(OtpVerify.this, "Phone Verification Successful", Toast.LENGTH_LONG).show();
                mVerificationInProgress = false;

                mDatabaseRef.child("mobile_verified").setValue(1);

                startNewActivity(VerificationSteps.class);
            }
            else {
                try {
                    throw task.getException();
                }
                catch(FirebaseAuthInvalidCredentialsException e) {
                    Toast.makeText(OtpVerify.this, "OTP Incorrect", Toast.LENGTH_LONG).show();
                } catch(FirebaseAuthUserCollisionException e) {
                    Toast.makeText(OtpVerify.this, "Phone Number Already in Use", Toast.LENGTH_LONG).show();
                }
                catch(Exception e) {
                    Log.e(TAG, e.getMessage());
                    if(e.getMessage().contains("User has already been linked to the given provider.")) {
                        Toast.makeText(OtpVerify.this, "Phone number already linked!", Toast.LENGTH_LONG).show();
                        startNewActivity(VerificationSteps.class);
                    }
                }
            }
        });
    }

    private void resendCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks, token
        );

        Toast.makeText(this, "OTP Sent Successfully", Toast.LENGTH_LONG).show();

        mVerificationInProgress = true;
    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(OtpVerify.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(OtpVerify.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideLeft(OtpVerify.this);
    }
}
