package com.cafe.ordersystem.model.customer;

import com.cafe.ordersystem.model.common.AuditableEntity;
import com.cafe.ordersystem.model.order.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a café customer.
 * Contains customer personal information, contact details, and their
 * relationship to orders and the loyalty program.
 */

@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be between 10 ad 15 digits")
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Size(max = 200)
    private String address;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String state;

    @Size(max = 10)
    @Column(name = "postal_code")
    private String postalCode;

    @Size(max = 50)
    private String country;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "registration_date", nullable = false)
    @Builder.Default
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Size(max = 500)
    @Column(name = "dietary_preferences")
    private String dietaryPreferences;

    @Size(max = 500)
    @Column(name = "favorite_products")
    private String favoriteProducts;

    @Column(name = "marketing_consent")
    @Builder.Default
    private boolean marketingConsent = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    // Bidirectional relationship with LoyaltyProgram
    // CascadeType.ALL ensures operations are cascaded to the loyalty program
    // orphanRemoval ensures that if a customer is deleted, their loyalty program is also deleted
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LoyaltyProgram loyaltyProgram;

    // Bidirectional relationship with orders
    // We don't cascade delete because we want to keep orders even if a customer is deleted
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    /**
     * Returns the customer's full name.
     *
     * @return The customer's full name (first name + last name)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Enrolls the customer in the loyalty program if they're not already enrolled.
     *
     * @return The created or existing loyalty program
     */
    public LoyaltyProgram enrollInLoyaltyProgram() {
        if(this.loyaltyProgram == null) {
            LoyaltyProgram program = new LoyaltyProgram();
            program.setCustomer(this);
            program.setEnrollmentDate(LocalDateTime.now());
            program.setTier(LoyaltyProgram.Tier.BRONZE);
            program.setPoints(0);
            this.loyaltyProgram = program;
        }

        return this.loyaltyProgram;
    }

    /**
     * Determines if the customer has a birthday today.
     *
     * @return true if today is the customer's birthday, false otherwise
     */
    @Transient
    public boolean isBirthdayToday() {
        if(dateOfBirth == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return dateOfBirth.getMonthValue() == today.getMonthValue()
                && dateOfBirth.getDayOfMonth() == today.getDayOfMonth();
    }

    /**
     * Add an order to the customer's order history.
     * Note: This does not persist the order - that must be done separately.
     *
     * @param order The order to add
     */
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    /**
     * Remove an order from the customer's order history.
     *
     * @param order The order to remove
     */

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }

    /**
     * Get the count of orders placed by this customer.
     *
     * @return The number of orders
     */
    @Transient
    public int getOrderCount() {
        return orders.size();
    }
}
