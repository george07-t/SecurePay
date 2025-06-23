package com.example.securepay;

public class Transaction {
    public String date;
    public String transferType;
    public String bankOrCard;
    public String accountNumber;
    public String amount;
    public String paymentReference;

    // Default constructor required for Firebase
    public Transaction() {}

    public Transaction(String date, String transferType, String bankOrCard, String accountNumber, String amount,String paymentReference) {
        this.date = date;
        this.transferType = transferType;
        this.bankOrCard = bankOrCard;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.paymentReference=paymentReference;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getBankOrCard() {
        return bankOrCard;
    }

    public void setBankOrCard(String bankOrCard) {
        this.bankOrCard = bankOrCard;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
}

