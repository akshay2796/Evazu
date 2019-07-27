package com.evazu;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText userTxt;
    private Button forgotPassBtn, loginBtn;
    ProgressBar mProgressBar;
    ConstraintLayout parent;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userTxt = findViewById(R.id.userEditText);
        forgotPassBtn = findViewById(R.id.forgotPass);
        loginBtn = findViewById(R.id.login_button);
        mProgressBar = findViewById(R.id.progressBar);
        parent = findViewById(R.id.parent);

        setupUI(parent);

        mAuth = FirebaseAuth.getInstance();


        forgotPassBtn.setOnClickListener(v -> {
            if(!TextUtils.isEmpty(userTxt.getText())) {
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mAuth.sendPasswordResetEmail(userTxt.getText().toString()).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        hideSoftKeyboard(ForgotPassword.this);
                        Snackbar.make(findViewById(R.id.parent), "Email Sent Successfully!",Snackbar.LENGTH_LONG).show();

                        new Handler().postDelayed(() -> startNewActivity(LoginActivity.class), 2000);

                    }
                    else {
                        Toast.makeText(ForgotPassword.this, "Failed to reset Password!", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                });
            }
            else {
                Toast.makeText(ForgotPassword.this, "Enter email to continue!", Toast.LENGTH_LONG).show();
            }
        });

        loginBtn.setOnClickListener(v -> {
            startNewActivity(LoginActivity.class);
        });
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
                hideSoftKeyboard(ForgotPassword.this);
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


    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(ForgotPassword.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(ForgotPassword.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideLeft(ForgotPassword.this);
    }
}
