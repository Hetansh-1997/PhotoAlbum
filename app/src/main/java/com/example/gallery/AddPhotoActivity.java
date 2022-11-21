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
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPhotoActivity extends AppCompatActivity {

    private ImageButton ib;
    private EditText ed;
    private Button btn;
    private Switch swt;

    final static int Gallery_Pick = 1;
    private Uri ImageUri;
    private String description;
    private ProgressDialog loadingBar;

    private StorageReference postReference;
    private DatabaseReference usersRef, photoRef;
    private FirebaseAuth mAuth;

    private String saveDate, saveTime, postName, current_user_id;
    private String  downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        ib = findViewById(R.id.image_button);
        ed = findViewById(R.id.ed_about_photo);
        btn = findViewById(R.id.btn_add_photo);
        swt = findViewById(R.id.switch1);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        postReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        photoRef = FirebaseDatabase.getInstance().getReference().child("Photos").child(current_user_id);

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
        if (ImageUri == null){
            Toast.makeText(AddPhotoActivity.this,"Please Select Image First!!", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(AddPhotoActivity.this,"Please Write Description First!!", Toast.LENGTH_LONG).show();
        }
        else {
            if (swt.isChecked()){
                photoRef = photoRef.child("Hide");
            }
            else {
                photoRef = photoRef.child("Public");
            }
            loadingBar.setMessage("Photo is adding");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImage();
        }
    }

    private void StoringImage() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveDate = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveTime = currentTime.format(calTime.getTime());

        postName = current_user_id + saveDate + saveTime;
        final StorageReference filePath = postReference.child("Photo Images").child(postName + ".jpg");
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
                    Toast.makeText(AddPhotoActivity.this,"Error Occurred1: " + msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SavingPostInfo() {

        HashMap postMap = new HashMap();
        postMap.put("description", description);
        postMap.put("photoImage", downloadUrl);
        postMap.put("photoName", postName + ".jpg");

        photoRef.child(postName).updateChildren(postMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            Intent intent = new Intent(AddPhotoActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            Toast.makeText(AddPhotoActivity.this,"Photo added Successfully!!!", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                        else {
                            String msg = task.getException().getMessage();
                            Toast.makeText(AddPhotoActivity.this,"Error Occurred2: " + msg, Toast.LENGTH_LONG).show();
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

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddPhotoActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}