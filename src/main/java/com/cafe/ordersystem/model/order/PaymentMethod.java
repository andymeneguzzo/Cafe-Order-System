package com.cafe.ordersystem.model.order;

/**
 * Enum representing the different payment methods accepted at the café.
 * Each method includes a description and flags for whether it requires validation
 * and if it can be processed offline.
 */
public enum PaymentMethod {

    CASH("Cash payment", false, true),
    CREDIT_CARD("Credit card payment", true, false),
    DEBIT_CARD("Debit card payment", true, false),
    MOBILE_PAYMENT("Mobile payment (Apple Pay, Google Pay, etc.)", true, false),
    LOYALTY_POINTS("Payment using loyalty program points", true, false),
    GIFT_CARD("Gift card payment", true, false),
    BANK_TRANSFER("Direct bank transfer", true, false),
    INVOICE("Payment via invoice (for business accounts)", false, false);

    private final String description;
    private final boolean requiresValidation;
    private final boolean canProcessOffline;

    PaymentMethod(String description, boolean requiresValidation, boolean canProcessOffline) {
        this.description = description;
        this.requiresValidation = requiresValidation;
        this.canProcessOffline = canProcessOffline;
    }

    /**
     * Gets the human-readable description of this payment method.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines if this payment method requires electronic validation
     * (e.g., credit card authorization).
     *
     * @return true if validation is required
     */
    public boolean requiresValidation() {
        return requiresValidation;
    }

    /**
     * Determines if this payment method can be processed when the system
     * is offline or the payment network is unavailable.
     *
     * @return true if the payment can be processed offline
     */
    public boolean canProcessOffline() {
        return canProcessOffline;
    }

    /**
     * Determines if this payment method is electronic (not cash).
     *
     * @return true if this is an electronic payment method
     */
    public boolean isElectronic() {
        return this != CASH;
    }

    /**
     * Determines if this payment method is eligible for loyalty points.
     * Typically, payments made with loyalty points do not earn additional points.
     *
     * @return true if this payment method earns loyalty points
     */
    public boolean isEligibleForLoyaltyPoints() {
        return this != LOYALTY_POINTS;
    }

    /**
     * Returns a string representation of the payment method.
     *
     * @return The name and description of the payment method
     */
    @Override
    public String toString() {
        return this.name() + " - " + this.description;
    }
}
