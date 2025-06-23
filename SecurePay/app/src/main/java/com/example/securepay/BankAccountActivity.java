package com.example.securepay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BankAccountActivity extends AppCompatActivity {
    private Spinner bankNameSpinner, transferTypeSpinner;
    private EditText accountNumberEditText, amountEditText, paymentReferenceEditText;
    private Button transferButton;
    private DatabaseReference userProfileRef; // Reference to the user profile database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bank Account");
        Broadcaster broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        // Initialize Firebase reference
        userProfileRef = FirebaseDatabase.getInstance().getReference("userprofile"); // assuming the path to user profile data

        // Initialize UI components
        bankNameSpinner = findViewById(R.id.bank_name_spinner);
        transferTypeSpinner = findViewById(R.id.transfer_type_spinner);
        accountNumberEditText = findViewById(R.id.account_number);
        amountEditText = findViewById(R.id.amount);
        paymentReferenceEditText = findViewById(R.id.payment_reference);
        transferButton = findViewById(R.id.transfer_button);

        // Set adapter for the Bank Name Spinner
        ArrayAdapter<CharSequence> bankAdapter = ArrayAdapter.createFromResource(this,
                R.array.bank_names, android.R.layout.simple_spinner_item);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankNameSpinner.setAdapter(bankAdapter);

        // Set adapter for the Transfer Type Spinner
        ArrayAdapter<CharSequence> transferTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.transfer_types, android.R.layout.simple_spinner_item);
        transferTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transferTypeSpinner.setAdapter(transferTypeAdapter);

        // Set onClickListener for the Transfer button
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Collect the entered data
                String selectedBank = bankNameSpinner.getSelectedItem().toString();
                String selectedTransferType = transferTypeSpinner.getSelectedItem().toString();
                String accountNumber = accountNumberEditText.getText().toString();
                String amount = amountEditText.getText().toString().trim();
                String paymentReference = paymentReferenceEditText.getText().toString();

                // Basic validation for the fields
                if (accountNumber.isEmpty() || amount.isEmpty() || paymentReference.isEmpty()) {
                    Toast.makeText(BankAccountActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    ProgressDialog progressDialog1 = new ProgressDialog(BankAccountActivity.this);
                    progressDialog1.setTitle("Loading");
                    progressDialog1.setMessage("Please Wait");
                    progressDialog1.show();
                    // Check if account number exists in the database
                    checkAccountNumberExistence(accountNumber, selectedBank, selectedTransferType, amount, paymentReference);
                    progressDialog1.dismiss();
                }
            }
        });
    }

    private void checkAccountNumberExistence(final String accountNumber, final String selectedBank, final String selectedTransferType,
                                             final String amount, final String paymentReference) {
        // Retrieve current user's account number
        getCurrentUserAccountNumber(new OnAccountNumberRetrievedListener() {
            @Override
            public void onAccountNumberRetrieved(String currentUserAccountNumber) {
                if (currentUserAccountNumber != null && accountNumber.equals(currentUserAccountNumber)) {
                    // If account numbers match, don't proceed
                    Toast.makeText(BankAccountActivity.this, "You cannot transfer to your own account", Toast.LENGTH_SHORT).show();
                    return;  // Stop the process
                }
                String newaccountNumber = SimpleEncryptionDecryption.encrypt(accountNumber);
                // Query Firebase to check if the account number exists in the user profile
                userProfileRef.orderByChild("newAccountNumber").equalTo(newaccountNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            boolean accountExists = false;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String existingAccount = snapshot.child("newAccountNumber").getValue(String.class);
                                existingAccount = SimpleEncryptionDecryption.decrypt(existingAccount);
                                if (existingAccount != null && !existingAccount.equals(currentUserAccountNumber)) {
                                    accountExists = true;
                                    break;
                                }
                            }

                            if (accountExists) {
                                // Account number exists, proceed to Payment Confirmation
                                Toast.makeText(BankAccountActivity.this, "Account number found", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(BankAccountActivity.this, PaymentConfirmationActivity.class);
                                intent.putExtra("type", "Bank Transfer");
                                intent.putExtra("selectedBank", selectedBank);
                                intent.putExtra("selectedTransferType", selectedTransferType);
                                intent.putExtra("accountNumber", accountNumber);
                                intent.putExtra("amount", amount);
                                intent.putExtra("paymentReference", paymentReference);
                                startActivity(intent);
                            } else {
                                // Account number doesn't exist, show error message
                                Toast.makeText(BankAccountActivity.this, "Account number does not exist", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Account number doesn't exist, show error message
                            Toast.makeText(BankAccountActivity.this, "Account number does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(BankAccountActivity.this, "Error checking account number", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    // Example method to get the current user's account number from Firebase (or your own source)
    private void getCurrentUserAccountNumber(final OnAccountNumberRetrievedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accountNumber = dataSnapshot.child("newAccountNumber").getValue(String.class);
                    accountNumber = SimpleEncryptionDecryption.decrypt(accountNumber);
                    listener.onAccountNumberRetrieved(accountNumber);  // Passing the account number to the listener
                } else {
                    listener.onAccountNumberRetrieved(null);  // If no data exists
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BankAccountActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                listener.onAccountNumberRetrieved(null);  // Handle error case by passing null
            }
        });
    }

    // Listener interface for returning account number asynchronously
    public interface OnAccountNumberRetrievedListener {
        void onAccountNumberRetrieved(String accountNumber);
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