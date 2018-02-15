package com.example.dmorr.nomaste;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // Member Variables
    private SignInButton google_signIn_button;      // Sign in button
    private FirebaseAuth mAuth;                     // Firebase Auth object
    private final static int RC_SIGN_IN = 2;        // Sign in Code
    private GoogleApiClient mGoogleApiClient;       // Google API Client

    // This object lets you check if a user is signed in our out
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dish_page);
        setContentView(R.layout.login_page);

        // Link the google sign in button to the layout sign in button
        google_signIn_button = findViewById(R.id.google_signIn_button);

        // Get an instance of the FireBase Authentication
        mAuth = FirebaseAuth.getInstance();

        google_signIn_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Create a new AuthStateListener to add to the mAuth object
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    // Redirect user to another activity
                    startActivity(new Intent(LoginActivity.this, SelectImage.class));
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
                        Toast.makeText(LoginActivity.this, "Sign In Failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add the AuthStateListener to the FireBase mAuth object
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    // Function that actually signs the user in using the Google API Client
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            // Google Sign In was unsuccessful, display toast to user
            else {
                Toast.makeText(LoginActivity.this,"Google Sign In Failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GoogleAuth", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GoogleAuth", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Authentication Failed.", Toast.LENGTH_SHORT).show();

                            // updateUI(null);
                        }

                        // ...
                    }
                });
    }
}
