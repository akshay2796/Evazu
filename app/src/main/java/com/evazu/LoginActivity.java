package com.evazu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private Button loginBtn;

    private EditText userTxt, passTxt;
    TextView signUp,forgotPass;
    ProgressBar mProgressBar;
    ConstraintLayout parent;

    FirebaseAuth mAuth;

    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

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
        setContentView(R.layout.activity_login);


        loginBtn = findViewById(R.id.login_button);
        userTxt = findViewById(R.id.userEditText);
        passTxt = findViewById(R.id.passEditText);
        signUp = findViewById(R.id.sign_up);
        forgotPass = findViewById(R.id.forgotPass);
        mProgressBar = findViewById(R.id.progressBar);
        parent = findViewById(R.id.parent);

        setupUI(parent);

        loginBtn.setOnClickListener(v -> {
            loginBtn.setEnabled(false);
            parent.setAlpha(0.5f);
            parent.setEnabled(false);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);

            new Handler().postDelayed(() -> {
                signIn();
                loginBtn.setEnabled(true);
            }, 2000);

        });

        signUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            Animatoo.animateSlideRight(LoginActivity.this);
        });

        forgotPass.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            Animatoo.animateSlideRight(LoginActivity.this);
        });
    }

    private void signIn() {

        String username = userTxt.getText().toString();
        String password = passTxt.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

            Toast.makeText(LoginActivity.this, "Fill all the details!", Toast.LENGTH_LONG).show();
        }
        else {

            mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {

                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Please enter correct username or password", Toast.LENGTH_LONG).show();
                }
                else {
                    new GetRentalStatus().getResult(result -> {
                        if(result) {
                            startNewActivity(MapsActivity.class);
                        }
                        else {
                            startNewActivity(Permissions.class);
                        }
                    });
                }

            });
        }
        parent.setAlpha(1f);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        parent.setEnabled(true);

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
                hideSoftKeyboard(LoginActivity.this);
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
        intent = new Intent(LoginActivity.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(this);
    }

    @Override
    public void onBackPressed() {

        //Rate Us AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Do you really want to exit?")
                .setNegativeButton(Html.fromHtml("<font color='#14A79C'>YES</font>"), (dialog, which) -> LoginActivity.super.onBackPressed())
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v instanceof EditText) {
            if(!hasFocus) {
                ((EditText) v).setCursorVisible(false);
            }
        }
    }
}