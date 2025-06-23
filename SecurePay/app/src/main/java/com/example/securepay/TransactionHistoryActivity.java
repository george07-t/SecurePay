package com.example.securepay;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Transaction History");
        Broadcaster broadcastReceiver1 = new Broadcaster();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        recyclerView = findViewById(R.id.transactionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference("transactions");

        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        fetchTransactions();
    }
    private void fetchTransactions() {
        // Assuming currentUserId is the current user's ID from Firebase Authentication
        mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // Correct path based on user ID
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactionList.clear(); // Clear the existing list before loading new data
                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            // Extract transaction details from the snapshot
                            String date = transactionSnapshot.child("date").getValue(String.class);
                            String transferType = transactionSnapshot.child("transferType").getValue(String.class);
                            String bankOrCard = transactionSnapshot.child("bankOrCard").getValue(String.class);
                            String accountNumber = transactionSnapshot.child("accountNumber").getValue(String.class);
                            String amount = transactionSnapshot.child("amount").getValue(String.class);
                            String paymentReference = transactionSnapshot.child("paymentReference").getValue(String.class);

                            // Create a new Transaction object using the extracted data
                            Transaction transaction = new Transaction(date, transferType, bankOrCard, accountNumber, amount, paymentReference);

                            // Add the transaction to the list
                            transactionList.add(transaction);
                        }

                        // Notify the adapter to update the RecyclerView
                        transactionAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TransactionHistoryActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
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