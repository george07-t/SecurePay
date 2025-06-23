package com.example.securepay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactionList;

    // Constructor for the adapter
    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item (card view for each transaction)
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        String amount = transaction.getAmount();

        // Set the transaction details to the views
        holder.dateTextView.setText("Date: "+transaction.getDate());
        holder.amountTextView.setText("Amount: "+transaction.getAmount().substring(1));
        holder.transferTypeTextView.setText("Transfer Type: "+transaction.getTransferType());
        holder.bankOrCardTextView.setText("Transaction Method: "+transaction.getBankOrCard());
        holder.accountNumberTextView.setText("Account Number: "+transaction.getAccountNumber());
        holder.paymentReferenceTextView.setText("References: "+transaction.getPaymentReference());

        // Check if the amount is positive or negative and set the background color
        if (amount != null && !amount.isEmpty()) {
            if (amount.startsWith("-")) {
                // Negative amount, set background color to light red
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.lightRed));
            } else if (amount.startsWith("+")) {
                // Positive amount, set background color to light green
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.lightGreen));
            }
            else{

            }
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // ViewHolder class to hold views for each transaction item
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView amountTextView;
        public TextView transferTypeTextView;
        public TextView bankOrCardTextView;
        public TextView accountNumberTextView;
        public TextView paymentReferenceTextView;
        public CardView cardView;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            transferTypeTextView = itemView.findViewById(R.id.transferTypeTextView);
            bankOrCardTextView = itemView.findViewById(R.id.bankOrCardTextView);
            accountNumberTextView = itemView.findViewById(R.id.accountNumberTextView);
            paymentReferenceTextView = itemView.findViewById(R.id.paymentReferenceTextView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}