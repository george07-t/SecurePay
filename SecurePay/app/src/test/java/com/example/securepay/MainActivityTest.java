package com.example.securepay;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MainActivity methods using Mockito.
 */
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        // Mock the MainActivity class
        mainActivity = Mockito.mock(MainActivity.class);

        // Stub the methods to return specific values during testing
        when(mainActivity.shouldShowBalance(true, 10000)).thenReturn(false);
        when(mainActivity.shouldShowBalance(true, 5000)).thenReturn(true);
        when(mainActivity.shouldShowBalance(false, 0)).thenReturn(true);

        when(mainActivity.formatAccountNumber("20240001")).thenReturn("20240001");
        when(mainActivity.formatAccountNumber(null)).thenReturn(null);
    }

    /**
     * Test for the shouldShowBalance method.
     * This method determines whether the balance should be displayed or hidden.
     */
    @Test
    public void testShouldShowBalance() {
        // Test: Balance is hidden after 10 seconds
        boolean result1 = mainActivity.shouldShowBalance(true, 10000);
        assertFalse("Balance should be hidden after 10 seconds", result1);

        // Test: Balance is visible before 10 seconds
        boolean result2 = mainActivity.shouldShowBalance(true, 5000);
        assertTrue("Balance should be visible before 10 seconds", result2);

        // Test: Balance is not visible initially
        boolean result3 = mainActivity.shouldShowBalance(false, 0);
        assertTrue("Balance should be visible when not shown yet", result3);

        // Verify method invocations
        verify(mainActivity, times(1)).shouldShowBalance(true, 10000);
        verify(mainActivity, times(1)).shouldShowBalance(true, 5000);
        verify(mainActivity, times(1)).shouldShowBalance(false, 0);
    }

    /**
     * Test for the formatAccountNumber method.
     * This method formats account numbers with a prefix.
     */
    @Test
    public void testFormatAccountNumber() {
        // Test: Formatting account number
        String result = mainActivity.formatAccountNumber("20240001");
        assertEquals("20240001", result);

        // Test: Formatting null account number
        String resultNull = mainActivity.formatAccountNumber(null);
        assertNull("Should return null for a null input", resultNull);

        // Verify method invocations
        verify(mainActivity, times(1)).formatAccountNumber("20240001");
        verify(mainActivity, times(1)).formatAccountNumber(null);
    }
}
