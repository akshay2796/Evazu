package com.evazu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class AadhaarCard extends AppCompatActivity implements UCropFragmentCallback {

    private ImageView frontSide, backSide;
    private TextView clickFront, clickBack, clickFrontTV, clickBackTV;
    private EditText card_no;
    private Button continueBtn, skipBtn;
    private Toolbar toolbar;

    private StorageReference frontStorageRef, backStorageRef;
    private DatabaseReference mDatabaseRef;
    private String user_id;

    private File photoFileFront, photoFileBack;
    private ConstraintLayout parent;
    private ProgressBar mProgressBar;
    private Uri mPhotoUriFront, mPhotoUriBack;

    private static final int REQUEST_CAMERA = 99;

    private static final String TAG = "AadhaarCard";

    private final String photoFileNameFront = "EvazuAadhaarFront.jpg";
    private final String photoFileNameBack = "EvazuAadhaarBack.jpg";
    private final String AADHAAR_FRONT = "aadhaar_front";
    private final String AADHAAR_BACK = "aadhaar_back";
    private final String AADHAAR_CARD = "aadhaar_card";

    private boolean front, back;

    private String prevClass;

    String currentSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhaar_card);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            }
        }

        Intent intent = getIntent();
        if(intent.hasExtra("prevClass")) {
            prevClass = intent.getStringExtra("prevClass");
        }

        toolbar = findViewById(R.id.toolbar_aadhaar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        frontSide = findViewById(R.id.aadhaarFront);
        backSide = findViewById(R.id.aadhaarBack);
        card_no = findViewById(R.id.card);
        continueBtn = findViewById(R.id.continueBtn);
        skipBtn = findViewById(R.id.skipBtn);

        clickFront = findViewById(R.id.click_here_front);
        clickBack = findViewById(R.id.click_here_back);
        clickFrontTV = findViewById(R.id.retakeFront);
        clickBackTV = findViewById(R.id.retakeBack);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(user_id);
        frontStorageRef = FirebaseStorage.getInstance().getReference("users").child(user_id).child(AADHAAR_FRONT);
        backStorageRef = FirebaseStorage.getInstance().getReference("users").child(user_id).child(AADHAAR_BACK);

        parent = findViewById(R.id.parent);

        mProgressBar = findViewById(R.id.progressBar);

        front = false;
        back = false;

        EvazuDatabase db = new EvazuDatabase();

        parent.setEnabled(false);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        db.getValue(AADHAAR_FRONT, result -> {
            if(result != null) {

                mPhotoUriFront = Uri.parse(result);
                Log.d(TAG, "RESULT FRONT : "+result);
                Picasso.get().load(mPhotoUriFront).into(frontSide);
                clickFront.setVisibility(View.INVISIBLE);
            }
        });

        db.getValue(AADHAAR_BACK, result -> {
            if(result != null) {
                mPhotoUriBack = Uri.parse(result);
                Picasso.get().load(mPhotoUriBack).into(backSide);
                Log.d(TAG, "RESULT BACK : "+result);
                clickBack.setVisibility(View.INVISIBLE);
            }
        });

        db.getValue(AADHAAR_CARD, result -> {
            if(result != null) {
                card_no.setText(result);
            }
        });

        parent.setEnabled(true);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        frontSide.setOnClickListener(v -> {
            onLaunchCamera(frontSide.getId());
        });

        backSide.setOnClickListener(v -> {
            onLaunchCamera(backSide.getId());
        });

        clickFrontTV.setOnClickListener(v -> {
            front = false;
            onLaunchCamera(frontSide.getId());
        });

        clickBackTV.setOnClickListener(v -> {
            back = false;
            onLaunchCamera(backSide.getId());
        });

        card_no.setOnKeyListener((v, keyCode, event) -> {
            if(card_no.getText() != null){
                String text = card_no.getText().toString();

                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if (text.length() == 5 || text.length() == 10) {
                        text = card_no.getText().toString();
                        card_no.setText(text.substring(0, text.length() - 1));
                        card_no.setSelection(card_no.length());
                    }
                }
                else {
                    if(text.length() == 4 || text.length() == 9) {
                        card_no.setText(text+"-");
                        card_no.setSelection(card_no.length());
                    }
                }
            }

            return false;
        });

        continueBtn.setOnClickListener(v -> {
            continueBtn.setEnabled(false);

            new Handler().postDelayed(() -> continueBtn.setEnabled(true), 2000);

            mProgressBar.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(card_no.getText())) {
                if(card_no.getText().toString().length() == 14) {
                    if (mPhotoUriFront == null && mPhotoUriBack == null) {
                        if (front && back) {

                            parent.setEnabled(false);
                            mProgressBar.setVisibility(ProgressBar.VISIBLE);

                            uploadImageFront();
                        }
                        else {
                            continueBtn.setEnabled(true);
                            Toast.makeText(this, "Please take aadhaar pictures to continue", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                    else {
                        parent.setEnabled(false);
                        mProgressBar.setVisibility(ProgressBar.VISIBLE);

                        uploadImageFront();
                    }
                }
                else {
                    Toast.makeText(AadhaarCard.this, "Check Your Aadhaar Card!", Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
            else {
                Toast.makeText(AadhaarCard.this, "Enter Your Aadhaar Card!", Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        skipBtn.setOnClickListener(v -> {
            if(prevClass.equals("VerificationSteps")) {
                startNewActivity(VerificationSteps.class);
            }
            else if(prevClass.equals("Profile")) {
                startNewActivity(Profile.class);
            }
            else if(prevClass.equals("Maps")) {
                startNewActivity(MapsActivity.class);
            }

        });
    }

    private void uploadImageFront() {

        frontSide.setDrawingCacheEnabled(true);
        frontSide.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) frontSide.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTaskFront = frontStorageRef.putBytes(data);

        Task<Uri> urlTaskFront = uploadTaskFront.continueWithTask(task1 -> {

            if(!task1.isSuccessful()) {
                throw task1.getException();
            }
            return frontStorageRef.getDownloadUrl();

        }).addOnCompleteListener(task12 -> {
            if(task12.isSuccessful()) {
                Uri uri = task12.getResult();

                Log.d("TAG", "FRONT : "+task12.getResult().toString());
                mDatabaseRef.child(AADHAAR_FRONT).setValue(uri.toString());

                uploadImageBack();
            }
            else {
                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                Log.d(TAG, task12.getException().getMessage());
            }
        });
    }


    private void uploadImageBack() {

        backSide.setDrawingCacheEnabled(true);
        backSide.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) backSide.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTaskBack = backStorageRef.putBytes(data);

        Task<Uri> urlTaskBack = uploadTaskBack.continueWithTask(task1 -> {

            if(!task1.isSuccessful()) {
                Log.d(TAG, "File Upload Unsuccessful!");
                throw task1.getException();
            }
            return backStorageRef.getDownloadUrl();

        }).addOnCompleteListener(task12 -> {
            if(task12.isSuccessful()) {
                Uri uri = task12.getResult();

                Log.d("TAG", "BACK : "+task12.getResult().toString());
                mDatabaseRef.child(AADHAAR_BACK).setValue(uri.toString());

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                mDatabaseRef.child("aadhaar_uploaded").setValue(1);
                mDatabaseRef.child(AADHAAR_CARD).setValue(card_no.getText().toString());

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
            else {
                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                Log.d(TAG, task12.getException().getMessage());
                Toast.makeText(AadhaarCard.this, "Upload Error! Please Retry!", Toast.LENGTH_LONG);
            }
        });
    }

    public void onLaunchCamera(int id) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference to access to future access

        Uri fileProvider = null;

        if(id == R.id.aadhaarFront) {
            currentSide = "front";
            photoFileFront = getPhotoFileUri(photoFileNameFront);
            fileProvider = FileProvider.getUriForFile(AadhaarCard.this, "com.evazu.fileprovider", photoFileFront);
        }
        else if(id == R.id.aadhaarBack) {
            currentSide = "back";
            photoFileBack = getPhotoFileUri(photoFileNameBack);
            fileProvider = FileProvider.getUriForFile(AadhaarCard.this, "com.evazu.fileprovider", photoFileBack);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        intent.putExtra("ImageView", id);

        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying camera permission rationale to provide additional context.");
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");

            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startCrop(@NonNull Uri uri) {

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setFreeStyleCropEnabled(true);
        options.setCompressionQuality(50);

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), System.currentTimeMillis()+".jpg")))
                .withOptions(options);


        uCrop.start(AadhaarCard.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                if(!front) {
                    if(photoFileFront != null) {
                        currentSide = "front";
                        Uri fronturi = Uri.fromFile(photoFileFront);

                        startCrop(fronturi);
                        //Picasso.get().load(photoFileFront).resize(500,500).into(frontSide);
                        clickFront.setVisibility(View.INVISIBLE);
                        front = true;
                    }
                }

                if(!back) {
                    if(photoFileBack != null) {
                        currentSide = "back";
                        Uri backUri = Uri.fromFile(photoFileBack);

                        startCrop(backUri);


                        //Picasso.get().load(photoFileBack).resize(500,500).into(backSide);
                        clickBack.setVisibility(View.INVISIBLE);
                        back = true;
                    }
                }

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        }

        if(requestCode == UCrop.REQUEST_CROP) {
            if(resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                if(currentSide.equals("front")) {
                    Picasso.get().load(resultUri).into(frontSide);
                    clickFrontTV.setVisibility(View.VISIBLE);
                }
                else if(currentSide.equals("back")) {
                    Picasso.get().load(resultUri).into(backSide);
                    clickBackTV.setVisibility(View.VISIBLE);
                }
            }
            else {
                Toast.makeText(this, "Crop Error!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(AadhaarCard.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(AadhaarCard.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(AadhaarCard.this);
    }

    @Override
    public void loadingProgress(boolean showLoader) {

    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        switch (result.mResultCode) {
            case RESULT_OK:
                handleCropResult(result.mResultData);
                break;
            case UCrop.RESULT_ERROR:
                Toast.makeText(this, "Cannot get Cropped Image!", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void handleCropResult(@NonNull Intent intent) {
        Log.d("HANDLE_CROP", intent.getData().toString());
    }
}
