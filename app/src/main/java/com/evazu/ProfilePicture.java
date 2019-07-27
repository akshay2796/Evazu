package com.evazu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProfilePicture extends AppCompatActivity implements UCropFragmentCallback {

    CircularImageView profilePic;
    File photoFile;
    ProgressBar mProgressBar;
    ConstraintLayout parent;
    Button takeSelfieBtn;
    TextView retakeSelfie;
    Uri photoFileUri;

    final String TAG = "ProfilePicture";

    private static final int REQUEST_CAMERA = 99;
    public String photoFileName = "EvazuProfilePic.jpg";

    StorageReference mStorageRef;
    FirebaseUser mFirebaseUser;

    DatabaseReference mDatabaseRef;

    boolean changePic = false;

    private final String PROFILE_PIC = "profile_pic";

    private String destinationFile = "EvazuCroppedImage";

    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            }
        }

        profilePic = findViewById(R.id.profilePic);
        mProgressBar = findViewById(R.id.progressBar);
        parent = findViewById(R.id.parent);

        takeSelfieBtn = findViewById(R.id.takeSelfie);
        retakeSelfie = findViewById(R.id.retakeSelfie);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference("users").child(mFirebaseUser.getUid()).child(PROFILE_PIC);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(mFirebaseUser.getUid());

        EvazuDatabase db = new EvazuDatabase();
        db.getValue(PROFILE_PIC, result -> {
            if(result != null) {
                //Toast.makeText(this, "Getting Profile Pic from database!", Toast.LENGTH_SHORT).show();

                parent.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);

                photoFileUri = Uri.parse(result);
                Picasso.get().load(mFirebaseUser.getPhotoUrl()).into(profilePic);

                takeSelfieBtn.setText("Continue");
                retakeSelfie.setVisibility(View.VISIBLE);

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }

        });

        takeSelfieBtn.setOnClickListener(v -> {
            takeSelfieBtn.setEnabled(false);
            if(takeSelfieBtn.getText().toString().equalsIgnoreCase("continue")) {
                Log.d(TAG, "CHANGE PIC : "+changePic);

                if (changePic) {
                    parent.setEnabled(false);
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    if (photoFileUri != null) {
                        StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoFileUri.toString());
                        mRef.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Deleted Successful");
                            } else {
                                Log.d(TAG, "Delete Unsuccessful");
                            }
                        });
                    }
                    uploadImage();
                }
                else {
                    startNewActivity(VerificationSteps.class);
                }
            }
            else {
                onLaunchCamera();
            }
            takeSelfieBtn.setEnabled(true);
        });

        retakeSelfie.setOnClickListener(v -> {
            destinationFile = destinationFile + (count++);
            parent.setEnabled(false);
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            onLaunchCamera();
        });
    }

    private void uploadImage() {

        profilePic.setDrawingCacheEnabled(true);
        profilePic.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mStorageRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(task1 -> {

            if(!task1.isSuccessful()) {
                Log.d(TAG, "Exception Generated Here");
                throw task1.getException();
            }
            return mStorageRef.getDownloadUrl();

        }).addOnCompleteListener(task12 -> {
            if(task12.isSuccessful()) {
                Uri uri = task12.getResult();
                mDatabaseRef.child(PROFILE_PIC).setValue(uri.toString());

                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build();

                FirebaseAuth.getInstance().getCurrentUser().updateProfile(request);

                startNewActivity(VerificationSteps.class);

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
            else {
                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                //Toast.makeText(this, task12.getException().getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, task12.getException().getMessage());
            }
        });
    }


    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(ProfilePicture.this, "com.evazu.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_CAMERA) {

                Uri uri = Uri.fromFile(photoFile);

                startCrop(uri);

                //Picasso.get().load(photoFile).into(profilePic);

                takeSelfieBtn.setText("Continue");
                retakeSelfie.setVisibility(View.VISIBLE);

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                changePic = true;

            }
            else if(requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                Picasso.get().load(resultUri).centerCrop().resize(500, 500).into(profilePic);

            }
            else if(requestCode == UCrop.RESULT_ERROR) {
                Toast.makeText(this, "Crop Error", Toast.LENGTH_LONG).show();
            }
            else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();

                parent.setEnabled(true);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        }
    }

    private void startCrop(@NonNull Uri uri) {

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCircleDimmedLayer(true);
        options.setCompressionQuality(50);

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFile+".jpg")))
                .withAspectRatio(1,1)
                .withOptions(options);


        uCrop.start(ProfilePicture.this);

    }

    private void startNewActivity(Class newClass) {
        Intent intent;
        intent = new Intent(ProfilePicture.this, newClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(ProfilePicture.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(ProfilePicture.this);
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
