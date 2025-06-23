package com.example.securepay;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.braintreepayments.cardform.view.CardForm;

public class CreditCardActivity extends AppCompatActivity {
    private CardForm cardForm;
    private EditText amountEditText;
    private Button paymentButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Credit Card");
        Broadcaster broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        // Initialize views
        cardForm = findViewById(R.id.card_form);
        amountEditText = findViewById(R.id.amount_section);
        paymentButton = findViewById(R.id.payment_button);

        // Setup the CardForm
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .cardholderName(CardForm.FIELD_REQUIRED)
                .postalCodeRequired(true)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("SMS is required on this number")
                .actionLabel("Purchase")
                .setup(CreditCardActivity.this);

        // Setup payment button click listener
        paymentButton.setOnClickListener(view -> {
            // Get the entered amount
            String amount = amountEditText.getText().toString();

            // Check if amount is valid
            if (amount.isEmpty()) {
                Toast.makeText(CreditCardActivity.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the card form is valid
            if (!cardForm.isValid()) {
                Toast.makeText(CreditCardActivity.this, "Please enter valid card details", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the card details
            String cardNumber = cardForm.getCardNumber();
            String cardCVV = cardForm.getCvv();
            String cardholderName = cardForm.getCardholderName();
            String postalCode = cardForm.getPostalCode();
            String mobileNumber = cardForm.getMobileNumber();

            // Create an Intent to pass the data to the confirmation activity
            Intent intent = new Intent(CreditCardActivity.this, PaymentConfirmationActivity.class);
            intent.putExtra("type","Credit Card");
            intent.putExtra("cardNumber", cardNumber);
            intent.putExtra("cardCVV", cardCVV);
            intent.putExtra("cardholderName", cardholderName);
            intent.putExtra("postalCode", postalCode);
            intent.putExtra("mobileNumber", mobileNumber);
            intent.putExtra("amount", amount);

            // Start the PaymentConfirmationActivity
            startActivity(intent);
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