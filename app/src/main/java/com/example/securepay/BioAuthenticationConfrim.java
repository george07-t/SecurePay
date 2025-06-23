package com.example.securepay;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class BioAuthenticationConfrim extends AppCompatActivity {
    private TextView toValue, accountValue, fromValue, amountValue, transferTypeValue, dateValue;
    private Button cancelButton, okButton;
    private DatabaseReference mDatabase;
    String currbalance, currname, recipientName, recipientBalance;
    private Boolean currfingerprintData = false;
    // Firebase Authentication to get the current user ID
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String recipientUserId, currentUserId;

    private String selectedBank, reccardNumber, currnewAccountNumber, amount, selectedTransferType, paymentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bio_authentication_confrim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Biometric Conformation");
        Broadcaster broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        ProgressDialog progressDialog1 = new ProgressDialog(BioAuthenticationConfrim.this);
        progressDialog1.setTitle("Loading");
        progressDialog1.setMessage("Please Wait");
        progressDialog1.show();
        toValue = findViewById(R.id.to_value);
        accountValue = findViewById(R.id.account_value);
        fromValue = findViewById(R.id.from_value);
        amountValue = findViewById(R.id.amount_value);
        transferTypeValue = findViewById(R.id.transfer_type_value);
        dateValue = findViewById(R.id.date_value);
        cancelButton = findViewById(R.id.cancel_button);
        okButton = findViewById(R.id.ok_button);
        EncryptionUtils.generateKey();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Get data passed from the previous activity
        Intent intent = getIntent();
        selectedBank = intent.getStringExtra("selectedBank");
        reccardNumber = intent.getStringExtra("reccardNumber");
        currnewAccountNumber = intent.getStringExtra("currnewAccountNumber");
        amount = intent.getStringExtra("amount");
        selectedTransferType = intent.getStringExtra("selectedTransferType");
        paymentReference = intent.getStringExtra("paymentReference");
        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Toast.makeText(this, "App can authenticate using biometrics.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No biometric features available on this device.", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric features are currently unavailable.", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No biometric data enrolled.", Toast.LENGTH_SHORT).show();
                return;
        }
        // Set dynamic data
        toValue.setText(selectedBank);
        accountValue.setText(reccardNumber);
        fromValue.setText(currnewAccountNumber);
        amountValue.setText("$ " + amount);
        transferTypeValue.setText(selectedTransferType);
        dateValue.setText(getCurrentDate());
        currentUserId = mAuth.getCurrentUser().getUid();
        fetchUserAccountDetails(currentUserId, reccardNumber);
        progressDialog1.dismiss();
        // Handle Cancel button
        cancelButton.setOnClickListener(v -> {
            Toast.makeText(BioAuthenticationConfrim.this, "Transaction Cancelled", Toast.LENGTH_SHORT).show();
            finish();  // Close this activity
        });

        // Handle OK button
        okButton.setOnClickListener(v -> {
            performTransaction();
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void performTransaction() {
        // Check if the device has biometric capabilities and if user has enrolled biometrics
        BiometricManager biometricManager = BiometricManager.from(this);

        int biometricStatus = biometricManager.canAuthenticate();

        if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            // Biometric is set up and ready to use

            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(BioAuthenticationConfrim.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(BioAuthenticationConfrim.this, "Auth error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    String transactionDetails = "Amount: " + amount + ", To: " + reccardNumber;
                    String encryptedTransaction = EncryptionUtils.encryptData(transactionDetails);

                    if (encryptedTransaction != null) {
                        // Log the encrypted transaction (for debugging)
                        System.out.println("Encrypted Transaction: " + encryptedTransaction);

                        // Update balances and history
                        updateBalancesAndHistory();

                        // Navigate back to MainActivity
                        Intent intent = new Intent(BioAuthenticationConfrim.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        // Show success message
                        Toast.makeText(BioAuthenticationConfrim.this, "Transaction Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Encryption failed
                        Toast.makeText(BioAuthenticationConfrim.this, "Encryption Failed!", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(BioAuthenticationConfrim.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setDescription("Please authenticate to confirm the transaction")
                    .setNegativeButtonText("Cancel")
                    .build();

            biometricPrompt.authenticate(promptInfo);

        } else {
            // Biometric is not set up
            String message = "Biometric authentication is not set up. Please go to your device settings to enable biometric authentication.";

            // You can provide an option to open the settings
            Intent intent = new Intent(android.provider.Settings.ACTION_BIOMETRIC_ENROLL);
            startActivity(intent);

            Toast.makeText(BioAuthenticationConfrim.this, message, Toast.LENGTH_LONG).show();
        }
    }


    private void updateBalancesAndHistory() {
        ProgressDialog progressDialog1 = new ProgressDialog(BioAuthenticationConfrim.this);
        progressDialog1.setTitle("Loading");
        progressDialog1.setMessage("Please Wait");
        progressDialog1.show();

        if (selectedBank.equals("Credit Card") || selectedBank.equals("Bill Payment")) {
            // Card transaction logic, no recipient user info required
            double transferAmount = Double.parseDouble(amount); // Use Double instead of Integer
            double senderNewBalance = Double.parseDouble(currbalance) - transferAmount;

            if (senderNewBalance < 0) {
                Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_SHORT).show();
                progressDialog1.dismiss();
                return;
            }

            // Proceed with updating the sender balance and adding transaction history
            String transactionDate = getCurrentDate();
            String transferType = selectedTransferType;
            String bankOrCard = selectedBank;

            // Encrypt sender's new balance
            String encryptedSenderBalance = SimpleEncryptionDecryption.encrypt(String.valueOf(senderNewBalance));

            // Update sender's balance
            mDatabase.child("userprofile").child(currentUserId).child("balance").setValue(encryptedSenderBalance);

            // Create transaction history for the sender
            String senderTransactionId = mDatabase.child("transactions").push().getKey();
            Transaction senderTransaction = new Transaction(transactionDate, transferType, bankOrCard, reccardNumber, "-" + amount, paymentReference);
            mDatabase.child("transactions").child(currentUserId).child(senderTransactionId).setValue(senderTransaction);

            progressDialog1.dismiss();
            Toast.makeText(this, "Transaction successful!", Toast.LENGTH_SHORT).show();
        } else {
            double transferAmount = Double.parseDouble(amount);
            double senderNewBalance = Double.parseDouble(currbalance) - transferAmount;
            double recipientNewBalance = Double.parseDouble(recipientBalance) + transferAmount;

            // Check if sender has sufficient balance
            if (senderNewBalance < 0) {
                Toast.makeText(this, "Insufficient Balance!", Toast.LENGTH_SHORT).show();
                progressDialog1.dismiss();
                return;
            }

            // Current sender's user ID

            // Prepare the transaction record
            String transactionDate = getCurrentDate();  // Current date from method
            String transferType = selectedTransferType;
            String bankOrCard = selectedBank;
            String recipientAccount = reccardNumber;

            // Encrypt balances
            String encryptedSenderBalance = SimpleEncryptionDecryption.encrypt(String.valueOf(senderNewBalance));
            String encryptedRecipientBalance = SimpleEncryptionDecryption.encrypt(String.valueOf(recipientNewBalance));

            // 1. Update sender's balance
            mDatabase.child("userprofile").child(currentUserId).child("balance").setValue(encryptedSenderBalance);

            // 2. Update recipient's balance
            mDatabase.child("userprofile").child(recipientUserId).child("balance").setValue(encryptedRecipientBalance);

            // 3. Create transaction history for sender
            String senderTransactionId = mDatabase.child("transactions").push().getKey();  // Generate unique ID for sender's transaction
            Transaction senderTransaction = new Transaction(transactionDate, transferType, bankOrCard, recipientAccount, "-" + amount, paymentReference);
            mDatabase.child("transactions").child(currentUserId).child(senderTransactionId).setValue(senderTransaction);

            // 4. Create transaction history for recipient
            String recipientTransactionId = mDatabase.child("transactions").push().getKey();  // Generate unique ID for recipient's transaction
            Transaction recipientTransaction = new Transaction(transactionDate, transferType, bankOrCard, currnewAccountNumber, "+" + amount, paymentReference);
            mDatabase.child("transactions").child(recipientUserId).child(recipientTransactionId).setValue(recipientTransaction);

            progressDialog1.dismiss();
            // Show confirmation
            Toast.makeText(this, "Transaction successful!", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchUserAccountDetails(String userId, String accountNumber) {
        // Fetch the current user's data
        mDatabase.child("userprofile").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the current user's details
                    currbalance = dataSnapshot.child("balance").getValue(String.class);
                    currname = dataSnapshot.child("name").getValue(String.class);
                    currnewAccountNumber = dataSnapshot.child("newAccountNumber").getValue(String.class);
                    currfingerprintData = dataSnapshot.child("fingerprintData").getValue(Boolean.class);
                    currnewAccountNumber = SimpleEncryptionDecryption.decrypt(currnewAccountNumber);
                    currbalance = SimpleEncryptionDecryption.decrypt(currbalance);
                    // Now fetch recipient account details based on accountNumber
                    if (accountNumber != null && accountNumber.startsWith("202")) {
                        fetchRecipientAccountDetails(accountNumber);
                    }
                } else {
                    Toast.makeText(BioAuthenticationConfrim.this, "Current user not found.", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(BioAuthenticationConfrim.this, "Failed to load current user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipientAccountDetails(String accountNumber) {

        // Query to find the user with the given accountNumber
        mDatabase.child("userprofile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean recipientFound = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String recipientAccountNumber = userSnapshot.child("newAccountNumber").getValue(String.class);

                    if (recipientAccountNumber != null && recipientAccountNumber.equals(SimpleEncryptionDecryption.encrypt(accountNumber))) {
                        recipientFound = true;

                        // Retrieve recipient account details
                        recipientName = userSnapshot.child("name").getValue(String.class);
                        recipientBalance = userSnapshot.child("balance").getValue(String.class);
                        recipientBalance = SimpleEncryptionDecryption.decrypt(recipientBalance);
                        recipientUserId = userSnapshot.getKey();
                        break;
                    }
                }

                if (!recipientFound) {

                    Toast.makeText(BioAuthenticationConfrim.this, "Recipient account not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BioAuthenticationConfrim.this, "Failed to load recipient account data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Use the default system behavior
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Default back behavior
        finish(); // Close this activity
    }
}