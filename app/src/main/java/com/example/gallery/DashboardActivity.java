package com.example.gallery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
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
import android.widget.ImageView;
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

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private FloatingActionButton floatingActionButton;

    private EditText searchBar;
    private ImageButton logoutButton, profileButton;

    private DatabaseReference usersRef, photoRef;
    private FirebaseAuth mAuth;

    private String current_user_id, email, pass;;

    private FirebaseRecyclerAdapter<Photos, PhotoViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        floatingActionButton = (FloatingActionButton) findViewById(R.id.photosButton);
        searchBar = findViewById(R.id.photo_search);
        profileButton = findViewById(R.id.gallery_profile);
        logoutButton = findViewById(R.id.gallery_logout);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        photoRef = FirebaseDatabase.getInstance().getReference().child("Photos").child(current_user_id);

        //set post view
        recyclerView = findViewById(R.id.all_users_photos_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(DashboardActivity.this, Profile.class);
                startActivity(profileIntent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });


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


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddPhotoActivity.class);
                startActivity(intent);
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
                new FirebaseRecyclerAdapter<Photos, PhotoViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull final PhotoViewHolder holder, int position, @NonNull final Photos model) {

                        final String postKey = getRef(position).getKey();

                        Picasso.get().load(model.getPhotoImage()).into(holder.my_image);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog(postKey, model.getDescription(), model.getPhotoName());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_photos_activity, parent, false);
                        PhotoViewHolder viewHolder = new PhotoViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void alertDialog(String postKey, String label, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = DashboardActivity.this.getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.alert_dialog, null);

        builder.setView(view);

        TextView textView = (TextView) view.findViewById(R.id.photo_detail);
        textView.setText(label);

        Button editButton = (Button) view.findViewById(R.id.edit_photo_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent clickPostIntent = new Intent(DashboardActivity.this, EditPhotoActivity.class);
                clickPostIntent.putExtra("PostKey", postKey);
                startActivity(clickPostIntent);
            }
        });

        dialog.show();
    }

    

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView my_image;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            my_image = itemView.findViewById(R.id.image);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("EXIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
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
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}