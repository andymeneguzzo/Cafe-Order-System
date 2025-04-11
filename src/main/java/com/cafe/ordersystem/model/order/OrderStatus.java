package com.cafe.ordersystem.model.order;

/**
 * Enum representing the possible statuses of an order in the café system.
 *
 * The status flows through a lifecycle from creation to completion or cancellation:
 * CREATED → PAID → IN_PREPARATION → READY → COMPLETED
 * (with potential for CANCELLED at various stages)
 */
public enum OrderStatus {

    CREATED("Order has been created but not yet paid for"),
    PAID("Payment has been received but preparation has not started"),
    IN_PREPARATION("Order is being prepared by the kitchen staff"),
    READY("Order is ready for pickup or delivery"),
    COMPLETED("Order has been delivered to the customer"),
    CANCELLED("Order has been cancelled"),
    REFUNDED("Order has been refunded");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of this status.
     *
     * @return The description of this status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines if this status allows for cancellation.
     * Orders can only be cancelled if they're not already completed, cancelled, or refunded.
     *
     * @return true if the order can be cancelled from this status
     */
    public boolean canCancel() {
        return this != CANCELLED && this != COMPLETED && this != REFUNDED;
    }

    /**
     * Determines if this status allows for refund.
     * Orders can only be refunded if they've been paid for and are not already refunded.
     *
     * @return true if the order can be refunded from this status
     */
    public boolean canRefund() {
        return this != CREATED && this != REFUNDED;
    }

    /**
     * Checks if this status represents an active order (not completed, cancelled, or refunded).
     *
     * @return true if the order is still active
     */
    public boolean isActive() {
        return this != COMPLETED && this != CANCELLED && this != REFUNDED;
    }

    /**
     * Determines if this status can progress to the next status in the normal flow.
     * Orders can't progress if they're completed, cancelled, or refunded.
     *
     * @return true if the order can progress to the next status
     */
    public boolean canProgress() {
        return this != COMPLETED && this != CANCELLED && this != REFUNDED;
    }

    /**
     * Gets the next status in the normal order flow.
     *
     * @return the next status, or null if there is no next status
     */
    public OrderStatus getNextStatus() {
        switch (this) {
            case CREATED:
                return PAID;
            case PAID:
                return IN_PREPARATION;
            case IN_PREPARATION:
                return READY;
            case READY:
                return COMPLETED;
            default:
                return null;
        }
    }
}
