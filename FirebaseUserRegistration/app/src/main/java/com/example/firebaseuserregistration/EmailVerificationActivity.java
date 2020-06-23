package com.example.firebaseuserregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificationActivity extends AppCompatActivity {

    EditText emailid;
    Button verify;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        emailid = findViewById(R.id.emailid);
        verify = findViewById(R.id.verify);

        firebaseAuth = FirebaseAuth.getInstance();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailid.getText().toString();

                if (email.isEmpty()){

                    Toast.makeText(EmailVerificationActivity.this, "Email is required", Toast.LENGTH_SHORT).show();

                } else {

                    firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(EmailVerificationActivity.this, "Reset link send to your mailid", Toast.LENGTH_SHORT).show();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(EmailVerificationActivity.this, "Error! Reset link is not sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                /*Intent intent = new Intent(EmailVerificationActivity.this,ChangePasswordActivity.class);
                startActivity(intent);*/
            }
        });
    }
}
