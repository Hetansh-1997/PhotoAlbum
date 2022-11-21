package com.example.gallery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, name, pass, repass, hidepass;
    private TextView login;
    private Button register;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog loadingBar;

    private String email_id, fullName, downloadUrl, hide_pass;

    private int passsame = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.register_email);
        name = findViewById(R.id.register_name);
        pass = findViewById(R.id.register_password);
        repass = findViewById(R.id.register_repassword);
        register = findViewById(R.id.register_button);
        login = findViewById(R.id.register_already_user_text);
        hidepass =findViewById(R.id.register_hide_photo_password);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadingBar = new ProgressDialog(this);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        repass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPass1 = pass.getText().toString().trim();
                String strPass2 = repass.getText().toString().trim();

                if (!strPass1.equals(strPass2)) {
                    repass.setError("Password not matched");
                    passsame = 0;
                } else {
                    repass.setError(null);
                    passsame = 1;
                }
            }
        });
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
        } else if(passInput.length() < 6){
            pass.setError("Please enter a valid password");
            return false;
        } else {
            pass.setError(null);
            return true;
        }
    }

    private boolean validateName(){
        String nameInput = name.getText().toString().trim();

        if(nameInput.isEmpty()){
            name.setError("Field can't be empty");
            return false;
        } else if(nameInput.length() > 50){
            name.setError("Please enter a valid Name");
            return false;
        } else {
            name.setError(null);
            return true;
        }
    }

    private boolean validateHidePass(){
        String passInput = hidepass.getText().toString().trim();

        if(passInput.isEmpty()){
            hidepass.setError("Field can't be empty");
            return false;
        } else if(passInput.length() < 6){
            hidepass.setError("Please enter a valid password");
            return false;
        } else {
            hidepass.setError(null);
            return true;
        }
    }


    private void registerUser() {

        if (!validateEmail()  || !validateName() || !validatePassword() || !validateHidePass() || passsame == 0) {
            return;
        }

        email_id = email.getText().toString().trim();
        String password = pass.getText().toString().trim();
        fullName = name.getText().toString().trim();
        hide_pass = hidepass.getText().toString().trim();

        loadingBar.setMessage("Account is creating");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        mAuth.createUserWithEmailAndPassword(email_id, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            User user = new User(
                                    fullName,
                                    email_id,
                                    hide_pass
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                                startActivity(intent);
                                                loadingBar.dismiss();
                                                Toast.makeText(RegisterActivity.this, "Success", Toast.LENGTH_LONG).show();
                                            } else {
                                                //display a failure message
                                                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                                startActivity(intent);
                                                loadingBar.dismiss();
                                                Toast.makeText(RegisterActivity.this, "Fail", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}