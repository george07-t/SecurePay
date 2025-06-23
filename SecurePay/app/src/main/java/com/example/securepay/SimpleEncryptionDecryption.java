package com.example.securepay;

import android.util.Base64;

public class SimpleEncryptionDecryption {

    // Encrypt the data using Base64 encoding
    public static String encrypt(String data) {
        return Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
    }

    // Decrypt the data using Base64 decoding
    public static String decrypt(String encryptedData) {
        byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
        return new String(decodedBytes);
    }
}