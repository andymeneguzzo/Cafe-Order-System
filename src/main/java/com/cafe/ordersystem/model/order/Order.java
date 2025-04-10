package com.cafe.ordersystem.model.order;

import com.cafe.ordersystem.model.common.AuditableEntity;
import com.cafe.ordersystem.model.customer.Customer;
import com.cafe.ordersystem.model.customer.LoyaltyProgram;
import com.cafe.ordersystem.model.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a customer order in the café system.
 * An order contains multiple order items, tracks status, payment information,
 * and provides business methods for order operations.
 */

@Entity
@Data
@Builder
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique order number for customer reference.
     * Format: Year + Month + Day + Random alphanumeric sequence
     */
    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    /**
     * Items in this order.
     * CascadeType.ALL ensures that when an order is saved/deleted, its items are also saved/deleted.
     * orphanRemoval ensures that if an item is removed from the items collection, it's deleted from the database.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_reason", length = 100)
    private String discountReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "is_takeaway")
    @Builder.Default
    private boolean takeaway = false;

    @Column(name = "table_number")
    private Integer tableNumber;

    @Column(name = "loyalty_points_earned")
    @Builder.Default
    private Integer loyaltyPointsEarned = 0;

    @Column(name = "loyalty_points_used")
    @Builder.Default
    private Integer loyaltyPointsUsed = 0;

    /**
     * Initializes a new order by generating a unique order number.
     */
    @PrePersist
    public void prePersist() {
        if(orderNumber == null) {
            this.orderNumber = generateOrderNumber();
        }
    }

    /**
     * Generates a unique order number based on date and a random UUID segment.
     * Format: YYYYMMDD-XXXX (XXXX is from UUID)
     *
     * @return A unique order number
     */
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();

        String datePart = String.format("%d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        // take first 4 chars of a UUID
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0,4).toUpperCase();

        return datePart + "-" + randomPart;
    }

    /**
     * Adds an item to the order.
     *
     * @param product The product to add
     * @param quantity The quantity to add
     * @param specialInstructions Special instructions for this item
     * @return The created OrderItem
     */
    public OrderItem addItem(Product product, int quantity, String specialInstructions) {

        if(product == null) throw new IllegalArgumentException("Product cannot be null");

        if(quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        OrderItem item = OrderItem.builder()
                .order(this)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .specialInstructions(specialInstructions)
                .build();

        this.items.add(item);
        recalculateAmounts();

        return item;
    }

    /**
     * Removes an item from the order.
     *
     * @param item The item to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(OrderItem item) {
        boolean removed = this.items.remove(item);

        if(removed) {
            recalculateAmounts();
        }

        return removed;
    }

    /**
     * Updates the quantity of an existing item.
     *
     * @param item The item to update
     * @param newQuantity The new quantity
     * @return true if the item was updated, false if the item is not in this order
     */

    public boolean updateItemQuantity(OrderItem item, int newQuantity) {
        if(newQuantity <= 0) return removeItem(item);

        if(!this.items.contains(item)) return false;

        item.setQuantity(newQuantity);
        recalculateAmounts();

        return true;
    }

    /**
     * Recalculates subtotal, tax, and total amounts based on the current items.
     */
    public void recalculateAmounts() {

        // calculate subtotal from items
        BigDecimal newSubtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotal = newSubtotal;

        // calculate tax (assuming 10%)
        BigDecimal taxRate = new BigDecimal("0.10");
        this.taxAmount = this.subtotal.multiply(taxRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        // calculate tax (subtot + tax - order-level discount)
        this.totalAmount = this.subtotal.add(this.taxAmount).subtract(this.discountAmount);
    }

    /**
     * Applies a percentage discount to the entire order.
     *
     * @param percentage The discount percentage (e.g., 10 for 10%)
     * @param reason The reason for the discount
     * @return The amount of the discount applied
     */
    public BigDecimal applyPercentageDiscount(double percentage, String reason) {
        if(percentage <= 0 || percentage > 100) throw new IllegalArgumentException("Percentage must be between 0 and 100");

        BigDecimal discountMultiplier = BigDecimal.valueOf(percentage/100.0);

        this.discountAmount = this.subtotal.multiply(discountMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
        this.discountReason = reason;

        recalculateAmounts();
        return this.discountAmount;
    }

    /**
     * Applies a fixed amount discount to the entire order.
     *
     * @param amount The fixed discount amount
     * @param reason The reason for the discount
     * @return The amount of the discount applied
     */
    public BigDecimal applyFixedDiscount(BigDecimal amount, String reason) {
        if(amount.compareTo(this.subtotal) > 0) throw new IllegalArgumentException("Discount cannot be greater than the order subtotal");

        this.discountAmount = amount;
        this.discountReason = reason;

        recalculateAmounts();
        return this.discountAmount;
    }

    /**
     * Removes any order-level discount.
     */
    public void removeDiscount() {
        this.discountAmount = BigDecimal.ZERO;
        this.discountReason = null;

        recalculateAmounts();
    }

    /**
     * Processes payment for this order.
     *
     * @param method The payment method
     * @param reference Payment reference (e.g., transaction ID, receipt number)
     */
    public void processPayment(PaymentMethod method, String reference) {
        if(this.status != OrderStatus.CREATED) throw new IllegalArgumentException("Cannot process payment for an order not created");

        this.paymentMethod = method;
        this.paymentReference = reference;
        this.paymentDate = LocalDateTime.now();
        this.status = OrderStatus.PAID;

        calculateLoyaltyPoints();
    }

    /**
     * Calculates loyalty points for this order.
     * Basic calculation: 1 point per whole currency unit spent (rounded down).
     * Only applicable if customer has a loyalty program and payment method is eligible.
     */
    private void calculateLoyaltyPoints() {
        if(customer == null || paymentMethod == null || !paymentMethod.isEligibleForLoyaltyPoints()) return;

        LoyaltyProgram loyaltyProgram = customer.getLoyaltyProgram();
        if(loyaltyProgram == null) return;

        this.loyaltyPointsEarned = totalAmount.intValue();

        // award points
        loyaltyProgram.addPoints(this.loyaltyPointsEarned);
    }

    /**
     * Updates the order status.
     *
     * @param newStatus The new status
     * @return true if the status was updated, false if the update is not allowed
     */
    public boolean updateStatus(OrderStatus newStatus) {

        if(!this.status.canProgress() && newStatus != OrderStatus.REFUNDED) {
            return false;
        }

        if(newStatus == OrderStatus.CANCELLED && this.status.canCancel()) {
            this.status = OrderStatus.CANCELLED;
            return true;
        }

        if(newStatus == OrderStatus.REFUNDED && this.status.canRefund()) {
            this.status = OrderStatus.REFUNDED;
            return true;
        }

        // progress normally
        if(this.status.getNextStatus() == newStatus) {
            this.status = newStatus;
            return true;
        }

        return false;
    }

    /**
     * Gets the total number of items in this order.
     *
     * @return The total item count
     */
    @Transient
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Checks if all items in the order have been prepared.
     *
     * @return true if all items are prepared, false otherwise
     */
    @Transient
    public boolean areAllItemsPrepared() {
        return !items.isEmpty() && items.stream().allMatch(OrderItem::isPrepared);
    }

    /**
     * Gets the preparation progress as a percentage.
     *
     * @return The percentage of items that have been prepared (0-100)
     */
    @Transient
    public int getPreparationProgress() {
        if(items.isEmpty()) return 0;

        long preparedItems = items.stream()
                .filter(OrderItem::isPrepared)
                .count();

        return (int) (preparedItems * 100 / items.size());
    }
}