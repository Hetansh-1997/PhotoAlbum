package com.example.gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText email, pass;
    private TextView forgetPass, register;
    private Button loginButton;
    private SignInButton btn_google;
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email);
        pass = findViewById(R.id.login_password);
        forgetPass = findViewById(R.id.login_forget_pass_text);
        register = findViewById(R.id.login_new_user_text);
        loginButton = findViewById(R.id.login_button);
        btn_google=findViewById(R.id.google);
        btn_google.setSize(SignInButton.SIZE_WIDE);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions);
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=googleSignInClient.getSignInIntent();
                startActivityForResult(intent,100);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user =mAuth.getCurrentUser();
        if (user != null){
            //handle already login user
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // code to do on NO tapped
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private boolean validateEmail() {
        String emailInput = email.getText().toString().trim();

        if(emailInput.isEmpty()){
            email.setError("Field can't be empty");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            email.setError("Please enter a valid email address");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        String passInput = pass.getText().toString().trim();

        if(passInput.isEmpty()){
            pass.setError("Field can't be empty");
            return false;
        } else {
            pass.setError(null);
            return true;
        }
    }

    private void loginUser() {
        if (!validateEmail()|| !validatePassword()) {
            Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailInput = email.getText().toString().trim();
        String passInput = pass.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(emailInput, passInput)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100){
            Task<GoogleSignInAccount> task =GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account=task.getResult(ApiException.class);
                firebaseAuthwithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthwithGoogle(GoogleSignInAccount account) {
        AuthCredential authCredential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();

                            HashMap postMap = new HashMap();
                            postMap.put("name", account.getDisplayName());
                            postMap.put("email", account.getEmail());
                            postMap.put("profileImage", account.getPhotoUrl());
                            Toast.makeText(LoginActivity.this, ""+mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
                            User user1 =new User(account.getDisplayName(),account.getEmail(),account.getPhotoUrl().toString());
                            mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(user1)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()) {
                                                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }else{
                                                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });


                        }else {
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}