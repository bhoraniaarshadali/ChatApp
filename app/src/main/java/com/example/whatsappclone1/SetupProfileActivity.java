package com.example.whatsappclone1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsappclone1.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            storage = FirebaseStorage.getInstance();

             binding.profileImage.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent = new Intent();
                     intent.setAction(Intent.ACTION_GET_CONTENT);
                     intent.setType("image/*");
                     startActivityForResult(intent, 45);
                 }
             });

             binding.setupProfileButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     String name = binding.enterProfileName.getText().toString();

                     if (name.isEmpty()) {
                         binding.enterProfileName.setError("Please enter a name");
                         return;
                     }
                     if (selectedImage != null) {
                         StorageReference reference = storage.getReference()
                                 .child("Profiles")
                                 .child(Objects.requireNonNull(auth.getUid()));

                         reference.putFile(selectedImage)
                                 .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                     @Override
                                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                         if(task.isSuccessful()) {
                                             reference.getDownloadUrl()
                                                     .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                         @Override
                                                         public void onSuccess(Uri uri) {

                                                            String uid = auth.getUid();
                                                            String phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
                                                            String name = binding.enterProfileName.getText().toString();
                                                            String imageUrl = uri.toString();

                                                            User user = new User(uid, name, phone, imageUrl);

                                                            database.getReference()
                                                                    .child("users")
                                                                    .setValue(user)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    });
                                                         }
                                                     });
                                         }
                                     }
                                 });
                     }
                 }
             });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data != null) {
                binding.profileImage.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}