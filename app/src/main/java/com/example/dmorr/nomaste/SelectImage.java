package com.example.dmorr.nomaste;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

public class SelectImage extends AppCompatActivity {

    Button logout_button;           // Logout button
    FirebaseAuth mAuth;             // Firebase Auth object
    FirebaseAuth.AuthStateListener mAuthStateListener;  // AuthStateListener checks if signed in/out
    GoogleApiClient mGoogleApiClient;       // Google API client

    private RecyclerView dashboard_recycler_view;     // RecyclerView for the select_image images
    private RecyclerViewAdapter mAdapter;  // Adapter for the RecyclerView
    private RecyclerView.LayoutManager mLayoutManager;  // Layout Manager positions items in
                                                        // the RecyclerView
    private Button browse_photos_button, upload_photo_button;
    private TextView username_display;
    private ImageView chosen_photo;
    EditText image_name;
    private Uri file_path;
    private final int PICK_IMAGE_REQUEST = 71;
    private String user_email;
    ArrayList<DashboardData> image_list = new ArrayList<>();

    // Folder path for Firebase Storage.
    private String Storage_Path = "All_Image_Uploads/";

    // Root Database Name for Firebase Database.
    private String Database_Path = "All_Image_Uploads_Database";

    // Firebase
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_image);

        // Link layout elements
        logout_button = (Button) findViewById(R.id.logout_button);
        dashboard_recycler_view = (RecyclerView) findViewById(R.id.dashboard_recycler_view);

        browse_photos_button = (Button) findViewById(R.id.browse_photos_button);
        upload_photo_button = (Button) findViewById(R.id.upload_photo_button);
        //username_display = (TextView) findViewById(R.id.username_display);
        image_name = (EditText) findViewById(R.id.image_name);
        mProgressDialog = new ProgressDialog(SelectImage.this);
        chosen_photo = (ImageView) findViewById(R.id.chosen_photo);

        chosen_photo.setVisibility(View.GONE);

        // Get an instance of the Firebase Auth object
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();

            //username_display.setText(personName);
        }

        // Set the recycler view to a fixed size
        dashboard_recycler_view.setHasFixedSize(true);

        // Firebase storage instance and it's reference (for use in the uploading and browsing of
        // images)
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // For each data snapshot, create a select_image data image and add it to the image list
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    DashboardData data = postSnapshot.getValue(DashboardData.class);
                    image_list.add(data);
                }

                // Instantiate an adapter (custom) for the recycler view
                mAdapter = new RecyclerViewAdapter(getApplicationContext(), image_list);

                // Set the adapter on the recycler view
                dashboard_recycler_view.setAdapter(mAdapter);

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });

        // Create a layout manager for the recycler view and set it
        mLayoutManager = new LinearLayoutManager(this);
        dashboard_recycler_view.setLayoutManager(mLayoutManager);

        // On Click Handler for the browse photos button
        browse_photos_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        // On Click Handler for the upload photo button
        upload_photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    // Redirect user to another activity
                    startActivity(new Intent(SelectImage.this, LoginActivity.class));
                }
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Configure Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SelectImage.this, "Sign In Failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add the AuthStateListener to the Firebase Auth object
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    // Helper function to upload the image to the FireBase DB
    private void uploadImage(){

        if(file_path != null){

            // Create a progress dialog to show the user the progress during the upload
            mProgressDialog.setTitle("Uploading...");
            mProgressDialog.show();

            StorageReference ref = mStorageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(file_path));

            ref.putFile(file_path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Getting image name from EditText and storing it
                            String temp_image_name = image_name.getText().toString().trim();

                            // Hide progress dialog
                            mProgressDialog.dismiss();

                            Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();

                            // Getting download URL for image
                            @SuppressWarnings("VisibleForTests")
                            DashboardData dashboardData = new DashboardData(temp_image_name, taskSnapshot.getDownloadUrl().toString());

                            // Getting image upload ID
                            String ImageUploadID = mDatabaseReference.push().getKey();

                            // Adding image uplaod ID's child element into DB reference
                            mDatabaseReference.child(ImageUploadID).setValue(dashboardData);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(SelectImage.this,
                                    "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            mProgressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
        else{
            Toast.makeText(getApplicationContext(), "Please select image or add an image name", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to select an image from the phone
    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // On the result of the selectImage method, if we're successful choosing an image, we'll display
    // it in the ImageView container
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the request is a pick image request, the result is an OK result, and the data is non
        // null, display the image in the ImageView
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            file_path = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), file_path);
                chosen_photo.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }
}


