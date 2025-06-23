package com.example.securepay;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BioAuthenticationConfirmTest {
    private double currentUserBalance;
    private double recipientBalance;
    private double transactionAmount;

    @Before
    public void setUp() {
        // Initialize test data
        currentUserBalance = 1000.00;
        recipientBalance = 500.00;
        transactionAmount = 100.00;
    }

    @Test
    public void testSufficientBalanceTransaction() {
        // Perform transaction logic
        boolean transactionSuccess = performTransaction(currentUserBalance, transactionAmount);

        // Update balances if transaction was successful
        if (transactionSuccess) {
            currentUserBalance -= transactionAmount;
            recipientBalance += transactionAmount;
        }

        // Assert the results
        Assert.assertTrue(transactionSuccess);
        Assert.assertEquals(900.00, currentUserBalance, 0.001);
        Assert.assertEquals(600.00, recipientBalance, 0.001);
    }

    @Test
    public void testInsufficientBalanceTransaction() {
        // Set transaction amount greater than current balance
        transactionAmount = 2000.00;

        // Perform transaction logic
        boolean transactionSuccess = performTransaction(currentUserBalance, transactionAmount);

        // Assert the results
        Assert.assertFalse(transactionSuccess);
        Assert.assertEquals(1000.00, currentUserBalance, 0.001); // No change
        Assert.assertEquals(500.00, recipientBalance, 0.001); // No change
    }

    @Test
    public void testBiometricAuthenticationSuccess() {
        // Simulate biometric authentication success
        boolean isAuthenticated = simulateBiometricAuthentication(true);

        // Assert that authentication succeeded
        Assert.assertTrue(isAuthenticated);
    }

    @Test
    public void testBiometricAuthenticationFailure() {
        // Simulate biometric authentication failure
        boolean isAuthenticated = simulateBiometricAuthentication(false);

        // Assert that authentication failed
        Assert.assertFalse(isAuthenticated);
    }

    // Helper method to simulate a transaction
    private boolean performTransaction(double balance, double amount) {
        return balance >= amount; // Return true if the balance is sufficient
    }

    // Helper method to simulate biometric authentication
    private boolean simulateBiometricAuthentication(boolean isSuccess) {
        return isSuccess; // Return the input as the simulated result
    }
}
