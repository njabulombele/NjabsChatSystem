package com.example.njabschatsystem;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.njabschatsystem.databinding.ActivitySignInBinding;
import com.example.njabschatsystem.models.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    GoogleSignInClient mGoogleSignInClient;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

      getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Please wait,Validation in Progress.");
        binding.btnGoogle.setOnClickListener(view -> {
            progressDialog.setMessage("Logging in with Google. Please wait...");
            progressDialog.show();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQ_ONE_TAP);
        });


binding.btnSignIn.setOnClickListener(view -> {

            if(!binding.txtEmail.getText().toString().isEmpty() && !binding.txtPassword.getText().toString().isEmpty())
            {
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(binding.txtEmail.getText().toString(),binding.txtPassword.getText().toString())
                        .addOnCompleteListener(task -> {
                               progressDialog.dismiss();
                               if(task.isSuccessful()){
                                   Intent intent = new Intent(SignInActivity.this,MainActivity.class);
                                   startActivity(intent);
                               }
                               else
                               {
                                   Toast.makeText(SignInActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                               }
                        });

            }
            else
            {
                Toast.makeText(SignInActivity.this, "Enter Credentials", Toast.LENGTH_SHORT).show();
            }



        });
        if(mAuth.getCurrentUser()!=null)
        {
            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
            startActivity(intent);
        }

        binding.txtClickSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this,SignUpActivity.class);
            startActivity(intent);
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


      if (requestCode == REQ_ONE_TAP)  {
          Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
          try {
              GoogleSignInAccount account = task.getResult(ApiException.class);
              Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
              firebaseAuthWithGoogle(account.getIdToken());
          }  catch (ApiException e) {
              Log.w("TAG", "Google sign in failed", e);
          }
      }

















        if (requestCode == REQ_ONE_TAP) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(account -> {
                        // Sign-in was successful. You can access account.getEmail(), account.getDisplayName(), etc.
                        // You can also use account.getIdToken() for server-side authentication.

                        // Proceed with your app, e.g., start the main activity.
                        progressDialog.dismiss();
                        firebaseAuthWithGoogle(account.getIdToken());
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Sign-in failed. Handle the error.
                        progressDialog.dismiss();
                        Toast.makeText(SignInActivity.this, "Google Sign-In failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }



    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        Users users = new Users();
                        users.setUserId(user.getUid());
                        users.setUserName(user.getDisplayName());
                        users.setProfilePic(user.getPhotoUrl().toString());
                        firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(users);


                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);

                        Toast.makeText(this, "Sign in with Google", Toast.LENGTH_SHORT).show();


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.getException());

                    }
                });
    }



    }



