package com.evazu;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

public class SignUpActivity extends AppCompatActivity {

    private EditText userTxt, passTxt, confPassTxt;
    private TextView loginTF;
    private Button signUpBtn;
    private ProgressBar mProgressBar;
    private ConstraintLayout parent;

    private FirebaseAuth mAuth;

    private final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        userTxt = findViewById(R.id.userEditText);
        passTxt = findViewById(R.id.passEditText);
        confPassTxt = findViewById(R.id.passConfirmEditText);
        signUpBtn = findViewById(R.id.signUpBtn);
        mProgressBar = findViewById(R.id.progressBar);
        loginTF = findViewById(R.id.log_in);
        parent = findViewById(R.id.parent);

        setupUI(parent);

        signUpBtn.setOnClickListener(v -> {
            parent.setEnabled(false);
            parent.setAlpha(0.5f);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            new Handler().postDelayed(() -> signUp(),2000);

        });

        loginTF.setOnClickListener(v -> {
            startNewActivity(LoginActivity.class);
        });

    }


    private void signUp() {

        String user = userTxt.getText().toString();
        String pass = passTxt.getText().toString();
        String confPass = confPassTxt.getText().toString();
        if(TextUtils.isEmpty(user) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confPass)) {
            Toast.makeText(SignUpActivity.this, "Fill all the details!", Toast.LENGTH_LONG).show();
            parent.setEnabled(true);
            parent.setAlpha(1f);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        }
        else {
            if(pass.equals(confPass)) {
                mAuth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(task -> {

                    if(!task.isSuccessful()) {
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthUserCollisionException e) {
                            createToast("User with this email already exists! Login to Continue");
                            new Handler().postDelayed(() -> startNewActivity(LoginActivity.class), 2000);
                        } catch(FirebaseAuthWeakPasswordException e) {
                            createToast("WEAK PASSWORD: The Password should contain atleast 6 characters");
                        }   catch(Exception e) {
                            Log.e("SignUpActivity", e.getMessage());
                            createToast("Some error has occurred!");
                        }

                        parent.setEnabled(true);
                        parent.setAlpha(1f);
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                    else {
                        parent.setEnabled(true);
                        parent.setAlpha(1f);
                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        Toast.makeText(SignUpActivity.this, "SignUp Successful!", Toast.LENGTH_LONG).show();
                        startNewActivity(Permissions.class);
                        Log.d(TAG, "SignUp Successfull");
                        Log.d(TAG, "Starting Permission Activity");
                    }

                });
            }
            else {
                Toast.makeText(SignUpActivity.this, "Password and Confirm Password do not match!", Toast.LENGTH_LONG).show();
                parent.setEnabled(true);
                parent.setAlpha(1f);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        }

    }

    public static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(SignUpActivity.this);
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
        intent = new Intent(SignUpActivity.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(SignUpActivity.this);

    }

    private void createToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideLeft(SignUpActivity.this);
    }

}
