package com.example.securepay;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.concurrent.Executor;

public class PaymentConfirmationActivity extends AppCompatActivity {
    private TextView account_number, transfer_amount, recipient_bank_details, recipient_bank_number, date, transfer_type, recipient_reference, card_info;
    DatabaseReference mDatabase;
    private Button confirm_button;
    private String currentUserId;  // This would be dynamically fetched from Firebase Auth or passed from the login activity
    String currnewAccountNumber, reccardNumber, selectedBank, amount, selectedTransferType, paymentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Payment Conformation");
        Broadcaster broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        ProgressDialog progressDialog1 = new ProgressDialog(PaymentConfirmationActivity.this);
        progressDialog1.setTitle("Loading");
        progressDialog1.setMessage("Please Wait");
        progressDialog1.show();
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        confirm_button = findViewById(R.id.confirm_button);
        // Initialize UI elements
        account_number = findViewById(R.id.accounts_number);
        transfer_amount = findViewById(R.id.transfer_amount);
        recipient_bank_details = findViewById(R.id.recipient_bank_details);
        recipient_bank_number = findViewById(R.id.recipient_bank_number);
        date = findViewById(R.id.date);
        transfer_type = findViewById(R.id.transfer_type);
        recipient_reference = findViewById(R.id.recipient_reference);
        card_info = findViewById(R.id.card_info);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Get current user's UID from Firebase Authentication
        currentUserId = mAuth.getCurrentUser().getUid();  // Replace with actual dynamic UID (FirebaseAuth.getInstance().getCurrentUser().getUid())
        fetchUserAccountDetails(currentUserId);
        progressDialog1.dismiss();
        String transactionType = getIntent().getStringExtra("type");

        if ("Bank Transfer".equals(transactionType)) {
            // Handle Bank Transfer Data
            card_info.setVisibility(View.GONE);
            selectedBank = getIntent().getStringExtra("selectedBank");
            selectedTransferType = getIntent().getStringExtra("selectedTransferType");
            reccardNumber = getIntent().getStringExtra("accountNumber");
            amount = getIntent().getStringExtra("amount");
            paymentReference = getIntent().getStringExtra("paymentReference");

            recipient_bank_details.setText(selectedBank);
            account_number.setText(currnewAccountNumber);
            transfer_type.setText("Transfer Type: " + selectedTransferType);
            recipient_bank_number.setText(reccardNumber);
            transfer_amount.setText("$ "+amount);
            recipient_reference.setText("Recipient Reference: " + paymentReference);
            date.setText("Date: " + getCurrentDate());


        } else if ("Credit Card".equals(transactionType)) {
            // Handle Credit Card Data
            reccardNumber = getIntent().getStringExtra("cardNumber");
            String cardCVV = getIntent().getStringExtra("cardCVV");
            paymentReference = getIntent().getStringExtra("cardholderName");
            String postalCode = getIntent().getStringExtra("postalCode");
            String mobileNumber = getIntent().getStringExtra("mobileNumber");
            amount = getIntent().getStringExtra("amount");
            selectedBank = "Credit Card";
            recipient_bank_details.setText(selectedBank);
            account_number.setText(currnewAccountNumber);
            transfer_type.setText("Transfer Type: Credit Card Transfer");
            recipient_bank_number.setText(reccardNumber);
            transfer_amount.setText("$ "+amount);
            selectedTransferType = "Visa";
            recipient_reference.setText("Card Holder: " + paymentReference);
            date.setText("Date: " + getCurrentDate());
            card_info.setText("CardCVV: " + cardCVV + "\nPostal Code: " + postalCode + "\nmobile Number: " + mobileNumber);


        } else if ("Bills".equals(transactionType)) {
            // Get data from Intent
            selectedBank = "Bill Payment";
            reccardNumber = getIntent().getStringExtra("BILL_ID");
            selectedTransferType = getIntent().getStringExtra("BILL_TYPE");
            amount = getIntent().getStringExtra("AMOUNT");
            paymentReference = getIntent().getStringExtra("MONTH");
            recipient_bank_details.setText(selectedBank);
            account_number.setText(currnewAccountNumber);
            recipient_bank_number.setText(reccardNumber);
            transfer_type.setText("Payment Type: " + selectedTransferType);
            transfer_amount.setText("$ " + amount);
            recipient_reference.setText("Due Month: " + paymentReference);
            date.setText("Date: " + getCurrentDate());
            card_info.setVisibility(View.GONE);

        }
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });
    }

    public void showConfirmationDialog() {
        Intent intent = new Intent(PaymentConfirmationActivity.this, BioAuthenticationConfrim.class);
        intent.putExtra("selectedBank", selectedBank);
        intent.putExtra("reccardNumber", reccardNumber);
        intent.putExtra("currnewAccountNumber", currnewAccountNumber);
        intent.putExtra("amount", amount);
        intent.putExtra("selectedTransferType", selectedTransferType);
        intent.putExtra("paymentReference", paymentReference);
        startActivity(intent);

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

    public String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // You can modify the format
        Date date = new Date();
        return dateFormat.format(date); // Return the current date as a string
    }

    public void fetchUserAccountDetails(String userId) {
        // Fetch the current user's data
        mDatabase.child("userprofile").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currnewAccountNumber = dataSnapshot.child("newAccountNumber").getValue(String.class);
                    currnewAccountNumber=SimpleEncryptionDecryption.decrypt(currnewAccountNumber);
                    account_number = findViewById(R.id.accounts_number);

                    // Update the UI for the current user
                    account_number.setText(currnewAccountNumber);  // Set account number

                } else {
                    Toast.makeText(PaymentConfirmationActivity.this, "Current user not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PaymentConfirmationActivity.this, "Failed to load current user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Checks if the payment is successful based on transaction details.
     *
     * @param transactionId The transaction ID.
     * @param accountId     The account ID.
     * @param amount        The transaction amount.
     * @return True if the payment is successful, false otherwise.
     */
    public boolean isPaymentSuccessful(String transactionId, String accountId, double amount) {
        // Simulate logic for checking payment success.
        if (transactionId == null || accountId == null || amount <= 0) {
            return false;
        }
        // Example logic: Assume a transaction is successful if the amount is <= 1000.
        return amount <= 1000;
    }

}


