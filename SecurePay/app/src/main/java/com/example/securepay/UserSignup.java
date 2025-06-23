package com.example.securepay;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;

import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

/**
 * This activity allows the user to sign up for a new account.
 */
public class UserSignup extends AppCompatActivity {
    private static final int REQUEST_CODE = 101011;
    EditText signupEmail, signupPassword, signupname, signupnumber;
    TextView loginRedirectText;
    Button signupButton, fingerprintButton;
    private Switch showpass1;
    private FirebaseAuth mAuth;
    private BroadcastReceiver broadcastReceiver1;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private Boolean isfingerprint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_signup);
        getSupportActionBar().setTitle("Sign Up");
        broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        showpass1 = (Switch) findViewById(R.id.showpass1);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupname = findViewById(R.id.signup_name);
        signupnumber = findViewById(R.id.signup_num);
        fingerprintButton = findViewById(R.id.fingerprintButton);

        mAuth = FirebaseAuth.getInstance();

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Toast.makeText(this, "App can authenticate using biometrics.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "BIOMETRIC_ERROR_NONE_ENROLLED.", Toast.LENGTH_SHORT).show();
                break;
        }
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new androidx.biometric.BiometricPrompt(UserSignup.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(UserSignup.this, "Auth error" + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(UserSignup.this, "Success", Toast.LENGTH_SHORT).show();
                isfingerprint = true;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(UserSignup.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("SignUp").setDescription("Inset Biometric Please").setNegativeButtonText("cancel").build();
        fingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isfingerprint) {
                    userregister();
                } else {
                    Toast.makeText(UserSignup.this, "Need the Biometric authentication for Signup", Toast.LENGTH_SHORT).show();
                }
            }
        });
        showpass1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    signupPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //showpass1.getTrackDrawable().setColorFilter(getResources().getColor(R.color.splash0), PorterDuff.Mode.SRC_IN);
                } else {
                    signupPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showpass1.getTrackDrawable().setColorFilter(getResources().getColor(R.color.splash0), PorterDuff.Mode.SRC_IN);
                }
            }
        });
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserSignup.this, UserLogin.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Registers a new user account.
     */
    private void userregister() {
        ProgressDialog progressDialog = new ProgressDialog(UserSignup.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        String email = signupEmail.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();
        String name = signupname.getText().toString().trim();
        String number = signupnumber.getText().toString().trim();
        if (name.isEmpty()) {
            signupname.setError("Enter a Name");
            signupname.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if (number.isEmpty()) {
            signupnumber.setError("Enter Phone Number");
            signupnumber.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if (email.isEmpty()) {
            signupEmail.setError("Enter Email ");
            signupEmail.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Enter a Valid Email ");
            signupEmail.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if (password.isEmpty()) {
            signupPassword.setError("Enter Password ");
            signupPassword.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if (password.length() < 6) {
            signupPassword.setError("Password must have minimum 6 digits");
            signupPassword.requestFocus();
            progressDialog.dismiss();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Retrieve last account number from Firebase
                    FirebaseDatabase.getInstance().getReference("lastAccountNumber")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String lastAccountNumber = task.getResult().getValue(String.class);

                                        // Generate new account number
                                        String newAccountNumber;
                                        String balacne = "500";
                                        if (lastAccountNumber == null) {
                                            newAccountNumber = "20240001"; // Starting value
                                        } else {
                                            int lastNumber = Integer.parseInt(lastAccountNumber);
                                            newAccountNumber = String.valueOf(lastNumber + 1); // Increment
                                        }

                                        // Save new account number back to Firebase
                                        FirebaseDatabase.getInstance().getReference("lastAccountNumber")
                                                .setValue(newAccountNumber);
                                        newAccountNumber = SimpleEncryptionDecryption.encrypt(newAccountNumber);
                                        balacne = SimpleEncryptionDecryption.encrypt(balacne);

                                        // Create user profile
                                        UserProfile userProfile = new UserProfile(name, email, number, isfingerprint, newAccountNumber, balacne);
                                        FirebaseDatabase.getInstance().getReference("userprofile")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userProfile)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task1) {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(getApplicationContext(), "SignUp Successfully done", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(UserSignup.this, UserLogin.class);
                                                            startActivity(intent);
                                                            progressDialog.dismiss();
                                                            finish();
                                                        } else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(UserSignup.this, "SignUp is Unsuccessful", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(UserSignup.this, "Failed to retrieve account number", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    progressDialog.dismiss();
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(getApplicationContext(), "User is already taken", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "SignUp is Unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(UserSignup.this, UserLogin.class));
        finish();
    }
}