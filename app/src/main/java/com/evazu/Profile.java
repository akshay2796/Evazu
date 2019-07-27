package com.evazu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private TextView ridesCount, emailTV, contactTV, aadhaarTV;
    private Button changePassBtn, logoutBtn;
    private ImageView backBtn;

    private CircularImageView profilePic;
    private TextView profileName;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    private EvazuDatabase db;

    private final String AADHAAR_CARD = "aadhaar_card";
    private final String TAG = "Profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(mFirebaseUser.getUid());

        db  = new EvazuDatabase();

        profilePic = findViewById(R.id.profilePic);
        profileName = findViewById(R.id.profileName);

        ridesCount = findViewById(R.id.ridesCount);

        emailTV = findViewById(R.id.emailTV);
        contactTV = findViewById(R.id.contactTV);
        aadhaarTV = findViewById(R.id.aadhaarTV);

        changePassBtn = findViewById(R.id.changePassBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        backBtn = findViewById(R.id.backBtn);

        changePassBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        setProfile();
    }

    private void setProfile() {
        Picasso.get().load(mFirebaseUser.getPhotoUrl()).into(profilePic);
        profileName.setText(mFirebaseUser.getDisplayName());

        emailTV.setText(mFirebaseUser.getEmail());
        contactTV.setText("+91"+" "+mFirebaseUser.getPhoneNumber().substring(3));

        db.getValue(AADHAAR_CARD, result -> {
            if(result != null) {
                aadhaarTV.setText(result);
            }
            else {
                aadhaarTV.setText(Html.fromHtml("<u>Attach Aadhaar</u>"));
                aadhaarTV.setTextColor(Color.RED);
                aadhaarTV.setOnClickListener(v -> {
                    Intent intent = new Intent(Profile.this, AadhaarCard.class);
                    intent.putExtra("prevClass", "Profile");
                    startActivity(intent);
                    Animatoo.animateSlideRight(this);
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProfile();
    }

    private void changePassword() {

        ViewGroup viewGroup = findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.change_password, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button cancenBtn = dialogView.findViewById(R.id.cancelBtn);
        Button submitBtn = dialogView.findViewById(R.id.submitBtn);

        EditText curPass = dialogView.findViewById(R.id.currentPass);
        EditText newPass = dialogView.findViewById(R.id.newPass);
        EditText confNewPass = dialogView.findViewById(R.id.confNewPass);

        cancenBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        submitBtn.setOnClickListener(v -> {
            String currentPwd, newPwd, confNewPwd;
            if(TextUtils.isEmpty(curPass.getText()) || TextUtils.isEmpty(newPass.getText()) || TextUtils.isEmpty(confNewPass.getText())) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show();
            }
            else {
                currentPwd = curPass.getText().toString();
                newPwd = newPass.getText().toString();
                confNewPwd = confNewPass.getText().toString();

                if(newPwd.equals(confNewPwd)) {
                    if (!newPwd.equals(currentPwd)) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        AuthCredential credential = EmailAuthProvider
                                .getCredential(mAuth.getCurrentUser().getEmail(), currentPwd);

                        user.reauthenticate(credential)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        user.updatePassword(newPwd).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Log.d(TAG, "Password updated");
                                                Toast.makeText(this, "Password Changed", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            } else {
                                                Log.d(TAG, "Error password not updated");
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "Error auth failed");
                                        Toast.makeText(this, "Incorrect Password", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "New Password cannot be same as old password", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(this, "New Password and confirm password do not match", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.show();

    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(Html.fromHtml("<font color='#000000'>Logout</font>"))
            .setMessage(Html.fromHtml("<font color='#000000'>Do you really want to log out?</font>"))
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                mAuth.signOut();
                startNewActivity(LoginActivity.class);
            })
            .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());

        AlertDialog  dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.show();
    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(Profile.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePassBtn:
                changePassword();
                break;
            case R.id.logoutBtn:
                signOut();
                break;
            case R.id.backBtn:
                onBackPressed();
                break;
        }
    }
}
