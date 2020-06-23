package com.example.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText username,email,mobilenumber,city,password,confirmpassword;
    ImageView imageView;
    Button signup;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        username = findViewById(R.id.username);
        email = findViewById(R.id.emailid);
        mobilenumber = findViewById(R.id.mobilenumber);
        city = findViewById(R.id.city);
        password = findViewById(R.id.password);
        confirmpassword = findViewById(R.id.confirmpw);
        imageView = findViewById(R.id.imageview);
        signup = findViewById(R.id.signupid);
        progressBar = findViewById(R.id.progressbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null){

            Intent intent = new Intent(RegistrationActivity.this,WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String str_email = email.getText().toString();
                final String str_password = password.getText().toString();
                final String str_username = username.getText().toString();
                final String str_mobilenumber = mobilenumber.getText().toString();
                final String str_city = city.getText().toString();

                if (TextUtils.isEmpty(str_email)){
                    email.setError("Email is required");
                    return;
                } else if (TextUtils.isEmpty(str_password)){
                    password.setError("Password is required");
                    return;
                } else if (str_password.length()<6){
                    password.setError("Password must be above six letter");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Register user in firebase

                firebaseAuth.createUserWithEmailAndPassword(str_email,str_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(RegistrationActivity.this, "Link send succesfully", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.d(TAG,"Error : Email not send"+e.getMessage());

                                }
                            });

                            Intent intent = new Intent(RegistrationActivity.this,WelcomeActivity.class);
                            userid = firebaseAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(userid);

                            Map<String,Object> user = new HashMap<>();
                            user.put("fullname",str_username);
                            user.put("email",str_email);
                            user.put("mobile",str_mobilenumber);
                            user.put("password",str_password);
                            user.put("city",str_city);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Log.d(TAG,"OnSuccess user profile is created for "+ userid);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.d(TAG,"OnFailure : " + e.toString());
                                }
                            });

                            Toast.makeText(RegistrationActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(RegistrationActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}
