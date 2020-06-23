package com.example.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    ImageView imageView;
    EditText name,phonenumber,city,email;
    Button save;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imageView = findViewById(R.id.imageview);
        name = findViewById(R.id.name);
        phonenumber = findViewById(R.id.phonenuber);
        city = findViewById(R.id.city);
        save = findViewById(R.id.button);
        email = findViewById(R.id.emailid);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent data = getIntent();

        String str_name = data.getStringExtra("name");
        String str_mobile = data.getStringExtra("mobile");
        String str_city = data.getStringExtra("city");
        String str_email = data.getStringExtra("email");

        name.setText(str_name);
        phonenumber.setText(str_mobile);
        city.setText(str_city);
        email.setText(str_email);

        StorageReference fileref = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get().load(uri).into(imageView);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent opengallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(opengallery, 1000);
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (name.getText().toString().isEmpty() || phonenumber.getText().toString().isEmpty() ||
                        city.getText().toString().isEmpty() || email.getText().toString().isEmpty()){

                    Toast.makeText(EditProfile.this, "Please fill all details", Toast.LENGTH_SHORT).show();

                }

                final String str_email = email.getText().toString();

                user.updateEmail(str_email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        DocumentReference reference = firebaseFirestore.collection("users").document(user.getUid());
                        Map<String,Object> edit = new HashMap<>();
                        edit.put("email",str_email);
                        edit.put("fullname",name.getText().toString());
                        edit.put("city",city.getText().toString());
                        edit.put("mobile",phonenumber.getText().toString());

                        reference.update(edit).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Intent intent = new Intent(EditProfile.this,WelcomeActivity.class);
                                Toast.makeText(EditProfile.this, "profile updated & email changed", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(EditProfile.this, "Update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {

            if (resultCode == Activity.RESULT_OK) {

                Uri imageuri = data.getData();

                //Already set the image means use this.....
                //studentPhoto.setImageURI(imageuri);

                UploadImagetoFirebase(imageuri);
            }


        }
    }



    private void UploadImagetoFirebase(Uri imageuri) {


        // upload image to firebase storage
        final StorageReference fileref = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");

        fileref.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //Toast.makeText(WelcomeActivity.this, "Image Upload Successfully", Toast.LENGTH_SHORT).show();

                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).into(imageView);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(EditProfile.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
