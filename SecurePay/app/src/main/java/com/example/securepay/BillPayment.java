package com.example.securepay;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BillPayment extends AppCompatActivity {
    CardView cardElectricity, cardWater, cardInternet, cardGas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bill Payment");
        Broadcaster broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
// Initialize CardViews
        cardElectricity = findViewById(R.id.cardElectricity);
        cardWater = findViewById(R.id.cardWater);
        cardInternet = findViewById(R.id.cardInternet);
        cardGas = findViewById(R.id.cardGas);
        // Set onClick listeners for each CardView
        cardElectricity.setOnClickListener(v -> showInputDialog("101", "Electricity Bill"));
        cardWater.setOnClickListener(v -> showInputDialog("102", "Water Bill"));
        cardInternet.setOnClickListener(v -> showInputDialog("103", "Internet Bill"));
        cardGas.setOnClickListener(v -> showInputDialog("104", "Gas Bill"));
    }

    private void showInputDialog(String billID, String billType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_transaction_confirmation, null);

        EditText editAmount = dialogView.findViewById(R.id.editAmount);
        EditText editMonth = dialogView.findViewById(R.id.editMonth);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        // Month Picker Dialog
        editMonth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, dayOfMonth) -> {
                // Set selected month and year
                String[] months = {"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};
                editMonth.setText(months[selectedMonth] + " " + selectedYear);
            }, year, month, 1);

            // Hide the day spinner
            ((ViewGroup) datePickerDialog.getDatePicker().getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        buttonCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        buttonOk.setOnClickListener(v -> {
            String amount = editAmount.getText().toString().trim();
            String month = editMonth.getText().toString().trim();

            if (amount.isEmpty() || month.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            } else {
                // Pass data to another activity
                Intent intent = new Intent(BillPayment.this, PaymentConfirmationActivity.class);
                intent.putExtra("type","Bills");
                intent.putExtra("BILL_ID", billID);
                intent.putExtra("BILL_TYPE", billType);
                intent.putExtra("AMOUNT", amount);
                intent.putExtra("MONTH", month);
                startActivity(intent);

                dialog.dismiss();
            }
        });

        dialog.show();
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