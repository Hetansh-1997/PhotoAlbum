package com.example.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class EditPhotoActivity extends AppCompatActivity {

    private ImageButton ib;
    private EditText ed;
    private Button btn;

    final static int Gallery_Pick = 1;
    private Uri ImageUri;
    private String description;
    private ProgressDialog loadingBar;

    private StorageReference postReference;
    private DatabaseReference usersRef, photoRef;
    private FirebaseAuth mAuth;

    private String postName, postKey, current_user_id, type;
    private String  downloadUrl;
    private boolean selectedImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);
        ib = findViewById(R.id.edit_image_button);
        ed = findViewById(R.id.edit_about_photo);
        btn = findViewById(R.id.btn_edit_photo);

        postKey = getIntent().getExtras().get("PostKey").toString();
        type = getIntent().getExtras().get("Type").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        postReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        photoRef = FirebaseDatabase.getInstance().getReference().child("Photos").child(current_user_id).child(type).child(postKey);


        photoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    downloadUrl = snapshot.child("photoImage").getValue().toString();
                    Picasso.get().load(downloadUrl).into(ib);
                    ed.setText(snapshot.child("description").getValue().toString());
                    postName = snapshot.child("photoName").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });


        loadingBar = new ProgressDialog(this);



        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        description = ed.getText().toString();

        if (TextUtils.isEmpty(description)){
            Toast.makeText(EditPhotoActivity.this,"Please Write Description First!!", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setMessage("Photo is updating");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            if (selectedImage){
                StoringImage();
            } else {
                SavingPostInfo();
            }

        }
    }

    private void StoringImage() {

        final StorageReference filePath = postReference.child("Photo Images").child(postName);
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            downloadUrl = task.getResult().toString();
                            SavingPostInfo();
                        }
                    });
                }
                else {
                    String msg = task.getException().getMessage();
                    Toast.makeText(EditPhotoActivity.this,"Error Occurred1: " + msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SavingPostInfo() {

        HashMap postMap = new HashMap();
        postMap.put("description", description);
        postMap.put("photoImage", downloadUrl);

        photoRef.updateChildren(postMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            Intent intent = new Intent(EditPhotoActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(EditPhotoActivity.this,"Photo updated Successfully!!!", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                        else {
                            String msg = task.getException().getMessage();
                            Toast.makeText(EditPhotoActivity.this,"Error Occurred2: " + msg, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            ib.setImageURI(ImageUri);
            selectedImage = true;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditPhotoActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}