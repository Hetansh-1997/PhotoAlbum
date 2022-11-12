package com.example.gallery;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private TextView txt1, txt2;
    private CircleImageView circleImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference userProfileRef;
    private String userid, downloadUrl;

    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;
    private FragmentActivity myContex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txt1 = findViewById(R.id.my_profile_name);
        txt2 = findViewById(R.id.my_email);
        circleImageView = findViewById(R.id.my_profile_pic);

        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Wait");
        loadingBar.setMessage("Profile is fetching");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userid.equals(mAuth.getUid())) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, Gallery_Pick);
                }
            }
        });

        mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.child("profileImage").exists()) {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(image).into(circleImageView);
                    }
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        txt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName();
            }
        });

        txt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();
            }
        });
    }

    private void updateName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Name Update");

        LinearLayout linearLayout = new LinearLayout(Profile.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        final EditText name = new EditText(Profile.this);
        name.setLayoutParams(params);


        linearLayout.addView(name);

        builder.setView(linearLayout);

        builder.setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                mDatabase.child("Users").child(userid).child("name").setValue(name.getText().toString());
                            }
                        })
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // code to do on NO tapped
                                dialog.cancel();
                            }
                        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void updateEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Email Update");

        LinearLayout linearLayout = new LinearLayout(Profile.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        final EditText email = new EditText(Profile.this);
        email.setLayoutParams(params);


        linearLayout.addView(email);

        builder.setView(linearLayout);

        builder.setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                mAuth.getCurrentUser().updateEmail(email.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mDatabase.child("Users").child(userid).child("email").setValue(email.getText().toString());
                                                    }
                                                });

                            }
                        })
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // code to do on NO tapped
                                dialog.cancel();
                            }
                        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            if (resultCode == RESULT_OK) {
                Uri resultUri = ImageUri;
                loadingBar = new ProgressDialog(this);
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Profile image is updating");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                final StorageReference filepath = userProfileRef.child(mAuth.getUid() + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Profile.this, "Success", Toast.LENGTH_LONG).show();
                            filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    downloadUrl = task.getResult().toString();
                                    mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            dataSnapshot.getRef().child("profileImage").setValue(downloadUrl);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(Profile.this, "Fail", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txt1.setText("Name: " + (String) dataSnapshot.child("name").getValue());
                txt2.setText("Email: " + (String) dataSnapshot.child("email").getValue());
                if (dataSnapshot.child("profileImage").exists()) {
                    Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(circleImageView);
                }
                loadingBar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
            Intent intent = new Intent(Profile.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
    }
}