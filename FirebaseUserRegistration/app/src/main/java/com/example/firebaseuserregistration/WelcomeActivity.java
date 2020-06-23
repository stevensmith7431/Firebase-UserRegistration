package com.example.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class WelcomeActivity extends AppCompatActivity {

    ImageView studentPhoto;
    TextView username, emailid, mobilenumber, city, txt_verify;
    Button logout, verify, reset, changeprofile,editprofile;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userid;
    StorageReference storageReference;
    Button btn_getimage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        studentPhoto = findViewById(R.id.imageview);
        username = findViewById(R.id.username);
        emailid = findViewById(R.id.email);
        mobilenumber = findViewById(R.id.mobilenumber);
        city = findViewById(R.id.city);
        logout = findViewById(R.id.logout);
        reset = findViewById(R.id.resetid);
        changeprofile = findViewById(R.id.profileid);
        editprofile = findViewById(R.id.editprofileid);


        btn_getimage = findViewById(R.id.getimage);

        storageReference = FirebaseStorage.getInstance().getReference();

        txt_verify = findViewById(R.id.textview);
        verify = findViewById(R.id.verifyid);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        final FirebaseUser user = firebaseAuth.getCurrentUser();


        //get image

        StorageReference fileref = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get().load(uri).into(studentPhoto);
            }
        });


        if (!user.isEmailVerified()) {

            txt_verify.setVisibility(View.VISIBLE);
            verify.setVisibility(View.VISIBLE);

            verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(WelcomeActivity.this, "Link send succesfully", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d("tag", "Error : Email not send" + e.getMessage());

                        }
                    });

                }
            });

        }


        userid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("users").document(userid);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                username.setText(documentSnapshot.getString("fullname"));
                emailid.setText(documentSnapshot.getString("email"));
                mobilenumber.setText(documentSnapshot.getString("mobile"));
                city.setText(documentSnapshot.getString("city"));
            }
        });


        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(WelcomeActivity.this,EditProfile.class);
                intent.putExtra("name",username.getText().toString());
                intent.putExtra("mobile",mobilenumber.getText().toString());
                intent.putExtra("city",city.getText().toString());
                intent.putExtra("email",emailid.getText().toString());
                startActivity(intent);

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(WelcomeActivity.this, ResetPassword.class);
                startActivity(intent);
            }
        });


        changeprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent opengallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(opengallery, 1000);
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

                        Picasso.get().load(uri).into(studentPhoto);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(WelcomeActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
