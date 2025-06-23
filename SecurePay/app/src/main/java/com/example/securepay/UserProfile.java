package com.example.securepay;

/**
 * This class represents a user profile.
 *
 * @author George
 */
public class UserProfile {
    public String name, email, number, newAccountNumber,balance;
    public Boolean fingerprintData;

    /**
     * Default constructor for Firebase.
     */
    public UserProfile() {
    }

    /**
     * Creates a new user profile object with the specified details.
     *
     * @param name            The user's name.
     * @param email           The user's email address.
     * @param number          The user's phone number.
     * @param fingerprintData Whether the user has fingerprint data stored.
     * @param newAccountNumber The user's account number.
     * @param balance         The user's account balance.
     */
    public UserProfile(String name, String email, String number, Boolean fingerprintData, String newAccountNumber, String balance) {
        this.name = name;
        this.email = email;
        this.number = number;
        this.fingerprintData = fingerprintData;
        this.newAccountNumber = newAccountNumber;
        this.balance = balance;
    }


}
