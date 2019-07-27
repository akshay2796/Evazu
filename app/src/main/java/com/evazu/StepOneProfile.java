package com.evazu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class StepOneProfile extends AppCompatActivity{

    EditText firstName, lastName, phoneNumber;
    Button continueBtn;
    ConstraintLayout parent;

    final String TAG = "StepOneProfile";

    FirebaseUser mFirebaseUser;
    DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_one_profile);

        firstName = findViewById(R.id.firstNameTxt);
        lastName = findViewById(R.id.lastNameTxt);
        phoneNumber = findViewById(R.id.phoneTxt);
        continueBtn = findViewById(R.id.takeSelfie);
        parent = findViewById(R.id.parent);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(mFirebaseUser.getUid());

        setupUI(parent);

        firstName.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN  ) && (keyCode == KeyEvent.KEYCODE_ENTER))
            {
                hideSoftKeyboard(StepOneProfile.this);
                return true;
            }
            return false;
        });
        lastName.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                hideSoftKeyboard(StepOneProfile.this);
                return true;
            }
            return false;
        });

        if(!(TextUtils.isEmpty(mFirebaseUser.getDisplayName()))) {

            String[] name = mFirebaseUser.getDisplayName().split(" ");
            String pNum = mFirebaseUser.getPhoneNumber();

            firstName.setText(name[0]);
            if (name.length == 2) {
                lastName.setText(name[1]);
            }
        }
        if(!TextUtils.isEmpty(mFirebaseUser.getPhoneNumber())) {
                    phoneNumber.setText(mFirebaseUser.getPhoneNumber().substring(3));
                }

        continueBtn.setOnClickListener(v -> {
            continueBtn.setEnabled(false);
            if(validated()) {

                UserProfileChangeRequest request  = new UserProfileChangeRequest.Builder()
                        .setDisplayName(firstName.getText().toString() + " "+lastName.getText().toString())
                        .build();

                mFirebaseUser.updateProfile(request);

                Intent intent = new Intent(StepOneProfile.this, OtpVerify.class);
                intent.putExtra("Phone", phoneNumber.getText().toString());
                startActivity(intent);
            }
            continueBtn.setEnabled(true);
        });

    }

    private boolean validated() {
        if(!TextUtils.isEmpty(firstName.getText())) {
            if(!TextUtils.isEmpty(lastName.getText())) {
                if(!TextUtils.isEmpty(phoneNumber.getText())) {
                    return true;
                }
                else {
                    customToast("Phone Number cannot be empty!");
                }
            }
            else {
                customToast("Last Name cannot be empty!");
            }
        }
        else {
            customToast("First Name cannot be empty!");
        }
        continueBtn.setEnabled(true);
        return false;
    }



    private void customToast(String message) {
        Toast.makeText(StepOneProfile.this, message, Toast.LENGTH_LONG).show();
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
                hideSoftKeyboard(StepOneProfile.this);
                firstName.setCursorVisible(false);
                lastName.setCursorVisible(false);
                phoneNumber.setCursorVisible(false);
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

//    @Override
//    public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if(keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
//            event.c
//            return true;
//        }
//
//        return false;
//    }
}
