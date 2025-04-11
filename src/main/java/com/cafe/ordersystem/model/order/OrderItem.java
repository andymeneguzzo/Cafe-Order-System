package com.cafe.ordersystem.model.order;

import com.cafe.ordersystem.model.common.AuditableEntity;
import com.cafe.ordersystem.model.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a line item in an order.
 * An order item represents a specific product, its quantity, and price at the time of order.
 * It can also include special instructions or customizations.
 */

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Unit price at the time of order.
     * We store this separately from the product price because:
     * 1. Product prices can change over time
     * 2. Special discounts might apply to this specific order
     */
    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Special instructions for this item (e.g., "Extra hot", "No sugar").
     */
    @Column(name = "special_instructions", length = 255)
    private String specialInstructions;

    /**
     * Whether this item has been prepared.
     */
    @Column(name = "is_prepared")
    @Builder.Default
    private boolean prepared = false;

    /**
     * Any discount applied specifically to this item.
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Reason or code for the discount (e.g., "HAPPY_HOUR", "LOYALTY_DISCOUNT").
     */
    @Column(name = "discount_reason", length = 50)
    private String discountReason;

    /**
     * Calculates the subtotal for this item (quantity * unit price - discount).
     *
     * @return The subtotal amount
     */
    @Transient
    public BigDecimal getSubtotal() {
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return baseAmount.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    /**
     * Applies a percentage discount to this item.
     *
     * @param percentage The discount percentage (e.g., 10 for 10%)
     * @param reason The reason for the discount
     * @return The amount of the discount applied
     */
    public BigDecimal applyPercentageDiscount(double percentage, String reason) {
        if(percentage <= 0 || percentage > 100) throw new IllegalArgumentException("Percentage must be between 0 and 100");

        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountMultiplier = BigDecimal.valueOf(percentage/100);

        this.discountAmount = baseAmount.multiply(discountMultiplier).setScale(2, BigDecimal.ROUND_HALF_UP);
        this.discountReason = reason;

        return this.discountAmount;
    }

    /**
     * Applies a fixed amount discount to this item.
     *
     * @param amount The fixed discount amount
     * @param reason The reason for the discount
     * @return The amount of the discount applied
     */
    public BigDecimal applyFixedDiscount(BigDecimal amount, String reason) {
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if(amount.compareTo(baseAmount) > 0) throw new IllegalArgumentException("Discount cannot be greater than item subtotal");

        this.discountAmount = amount;
        this.discountReason = reason;

        return this.discountAmount;
    }

    /**
     * Removes any discount applied to this item.
     */
    public void removeDiscount() {
        this.discountAmount = BigDecimal.ZERO;
        this.discountReason = null;
    }

    /**
     * Marks this item as prepared.
     */
    public void markAsPrepared() {
        this.prepared = true;
    }

    /**
     * Returns the product name for convenience.
     *
     * @return The name of the product
     */
    @Transient
    public String getProductName() {
        return product != null ? product.getName() : "Unknown product";
    }
}
