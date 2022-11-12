package com.example.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {


    private Button button;
    private EditText editText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        button = findViewById(R.id.reset_pass_btn);
        editText = findViewById(R.id.reset_pass_email);

        mAuth = FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(ResetPasswordActivity.this,"Please Enter email!!!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ResetPasswordActivity.this,"Please check your mail for reset password", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                    }
                                    else {
                                        Toast.makeText(ResetPasswordActivity.this,"Something is wrong!!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}