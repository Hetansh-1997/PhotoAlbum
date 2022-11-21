package com.example.gallery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class HidePhotoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private EditText searchBar;

    private DatabaseReference usersRef, photoRef;
    private FirebaseAuth mAuth;

    private String current_user_id, email, pass;;

    private FirebaseRecyclerAdapter<Photos, DashboardActivity.PhotoViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_photo);

        searchBar = findViewById(R.id.hide_photo_search);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        photoRef = FirebaseDatabase.getInstance().getReference().child("Photos").child(current_user_id).child("Hide");

        //set post view
        recyclerView = findViewById(R.id.hide_photos_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String st = searchBar.getText().toString().trim();
                if (TextUtils.isEmpty(st)){
                    DisplayAllPhotos(photoRef);
                } else {
                    Query query = photoRef.orderByChild("description")
                            .startAt(st).endAt(st + "\uf8ff");
                    DisplayAllPhotos(query);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayAllPhotos(photoRef);
    }

    private void DisplayAllPhotos(Query query) {

        FirebaseRecyclerOptions<Photos> options =
                new FirebaseRecyclerOptions.Builder<Photos>()
                        .setQuery(query, Photos.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Photos, DashboardActivity.PhotoViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final DashboardActivity.PhotoViewHolder holder, int position, @NonNull final Photos model) {

                        final String postKey = getRef(position).getKey();

                        Picasso.get().load(model.getPhotoImage()).into(holder.my_image);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog(postKey, model.getDescription(), model.getPhotoName(), model.getPhotoImage());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public DashboardActivity.PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_photos_activity, parent, false);
                        DashboardActivity.PhotoViewHolder viewHolder = new DashboardActivity.PhotoViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void alertDialog(String postKey, String label, String name, String Image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = HidePhotoActivity.this.getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.alert_dialog, null);

        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.photo_detail);
        textView.setText(label);

        Button editButton = (Button) view.findViewById(R.id.edit_photo_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent clickPostIntent = new Intent(HidePhotoActivity.this, EditPhotoActivity.class);
                clickPostIntent.putExtra("PostKey", postKey);
                clickPostIntent.putExtra("Type", "Hide");
                startActivity(clickPostIntent);
            }
        });

        Button hideButton = (Button) view.findViewById(R.id.hide_photo_button);
        hideButton.setText("unlock photo");


        Button deleteButton = (Button) view.findViewById(R.id.delete_photo_button);

        Dialog dialog = builder.create();

        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap postMap = new HashMap();
                postMap.put("description", label);
                postMap.put("photoImage", Image);
                postMap.put("photoName", name);
                FirebaseDatabase.getInstance().getReference().child("Photos").child(current_user_id).child("Public")
                        .child(postKey).updateChildren(postMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    photoRef.child(postKey).removeValue();
                                    Toast.makeText(HidePhotoActivity.this,"Photo got public Successfully!!!", Toast.LENGTH_LONG).show();

                                }
                                else {
                                    String msg = task.getException().getMessage();
                                    Toast.makeText(HidePhotoActivity.this,"Error Occurred2: " + msg, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                dialog.cancel();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePhoto(postKey, name);
                dialog.cancel();
            }
        });


        dialog.show();
    }

    private void deletePhoto(String postKey,String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = HidePhotoActivity.this.getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.delete_photo_alert_dialog, null);

        builder.setView(view);

        EditText passwordField = (EditText) view.findViewById(R.id.password);

        Button deleteButton = (Button) view.findViewById(R.id.delete_button);

        Dialog dialog = builder.create();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            email = snapshot.child("email").getValue().toString();

                            pass = passwordField.getText().toString().trim();
                            if (TextUtils.isEmpty(pass)){
                                Toast.makeText(HidePhotoActivity.this,"Please Enter Password First!!", Toast.LENGTH_LONG).show();
                            }
                            else {
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(email, pass);

                                mAuth.getCurrentUser().reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    photoRef.child(postKey).removeValue();
                                                    FirebaseStorage.getInstance().getReference().child("Photo Images").child(name).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(HidePhotoActivity.this,"Photo removed successfully!!", Toast.LENGTH_LONG).show();
                                                                    dialog.cancel();
                                                                }
                                                            });

                                                }
                                                else {
                                                    Toast.makeText(HidePhotoActivity.this,"Wrong Password!!", Toast.LENGTH_LONG).show();
                                                }
                                            }


                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                    }
                });


            }
        });


        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HidePhotoActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}