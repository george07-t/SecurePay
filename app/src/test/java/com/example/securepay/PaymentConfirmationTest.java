package com.example.securepay;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentConfirmation methods using Mockito.
 */
public class PaymentConfirmationTest {

    private PaymentConfirmationActivity paymentConfirmation;

    @Before
    public void setUp() {
        // Mock the PaymentConfirmation class
        paymentConfirmation = Mockito.mock(PaymentConfirmationActivity.class);

        // Stub the methods to return specific values during testing
        when(paymentConfirmation.isPaymentSuccessful("12345", "20240001", 500.0)).thenReturn(true);
        when(paymentConfirmation.isPaymentSuccessful("54321", "20240001", 1000.0)).thenReturn(false);
        when(paymentConfirmation.isPaymentSuccessful(null, null, 0.0)).thenReturn(false);
    }

    /**
     * Test for the isPaymentSuccessful method.
     * This method checks whether the payment is successful based on transaction details.
     */
    @Test
    public void testIsPaymentSuccessful() {
        // Test: Valid transaction details resulting in success
        boolean result1 = paymentConfirmation.isPaymentSuccessful("12345", "20240001", 500.0);
        assertTrue("Payment should be successful for valid details", result1);

        // Test: Invalid transaction details resulting in failure
        boolean result2 = paymentConfirmation.isPaymentSuccessful("54321", "20240001", 1000.0);
        assertFalse("Payment should fail for invalid details", result2);

        // Test: Null transaction details resulting in failure
        boolean result3 = paymentConfirmation.isPaymentSuccessful(null, null, 0.0);
        assertFalse("Payment should fail for null details", result3);

        // Verify method invocations
        verify(paymentConfirmation, times(1)).isPaymentSuccessful("12345", "20240001", 500.0);
        verify(paymentConfirmation, times(1)).isPaymentSuccessful("54321", "20240001", 1000.0);
        verify(paymentConfirmation, times(1)).isPaymentSuccessful(null, null, 0.0);
    }
}
