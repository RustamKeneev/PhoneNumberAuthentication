package com.onlineapteka.phonenumberauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private EditText mCountryCode;
    private EditText mPhoneNumber;
    private Button mGenerateButton;
    private ProgressBar mProgressBar;
    private TextView mLoginFeedbackText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private static final String AUTH_CREDENTIALS_PUT_EXTRA = "AuthCredentials";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mCountryCode = findViewById(R.id.editTextNumber);
        mPhoneNumber = findViewById(R.id.editTextPhone);
        mGenerateButton = findViewById(R.id.generate_otp_button);
        mProgressBar = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_feed_back);

        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country_code = mCountryCode.getText().toString();
                String phone_number = mPhoneNumber.getText().toString();

                String complate_phone_number = "+"+country_code + "" + phone_number;

                Toast.makeText(LoginActivity.this,"Phone Number : " +
                        complate_phone_number,Toast.LENGTH_LONG).show();

                if (country_code.isEmpty() || phone_number.isEmpty()){
                    mLoginFeedbackText.setText("Please fill in the form to continue.");
                    mLoginFeedbackText.setVisibility(View.VISIBLE);
                }else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mGenerateButton.setEnabled(false);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complate_phone_number,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks
                    );
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                mLoginFeedbackText.setText("Verification Failed, please try again. ");
                mLoginFeedbackText.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                mGenerateButton.setEnabled(true);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken
                    forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent otpIntent  = new Intent(LoginActivity.this,OtpActivity.class);
                                otpIntent.putExtra(AUTH_CREDENTIALS_PUT_EXTRA,s);
                                startActivity(otpIntent);
                            }
                        },
                        10000
                );
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser != null){
            sendUserToHome();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mLoginFeedbackText.setVisibility(View.VISIBLE);
                                mLoginFeedbackText.setText("There was an error verifying OTP");
                            }
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mGenerateButton.setEnabled(true);
                    }
                });
    }
    public  void sendUserToHome(){
        Intent homeIntent  = new Intent(LoginActivity.this,MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}