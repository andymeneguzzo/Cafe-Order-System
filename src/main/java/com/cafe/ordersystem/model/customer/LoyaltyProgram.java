package com.cafe.ordersystem.model.customer;

import com.cafe.ordersystem.model.common.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a customer's loyalty program membership.
 * Tracks points, tier level, and related loyalty program information.
 */

@Entity
@Table(name = "loyalty_programs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyProgram extends AuditableEntity {

    /**
     * Enum representing the different tiers in the loyalty program.
     * Each tier has different benefits and point thresholds.
     */
    public enum Tier {
        BRONZE(0), SILVER(100), GOLD(300), PLATINUM(500);

        private final int pointsRequired;

        Tier(int pointsRequired) {
            this.pointsRequired = pointsRequired;
        }

        public int getPointsRequired() {
            return pointsRequired;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one relationship with Customer
    // Each loyalty program belongs to exactly one customer
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Tier tier = Tier.BRONZE;

    @Column(name = "enrollment_date", nullable = false)
    @Builder.Default
    private LocalDateTime enrollmentDate = LocalDateTime.now();

    @Column(name = "last_points_earned_date")
    private LocalDateTime lastPointsEarnedDate;

    @Column(name = "last_points_redeemed_date")
    private LocalDateTime lastPointsRedeemedDate;

    @Column(name = "points_expiration_date")
    private LocalDateTime pointsExpirationDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "member_number", unique = true, length = 20)
    private String memberNumber;

    @Column(name = "special_offers")
    @Builder.Default
    private boolean eligibleForSpecialOffers = true;

    /**
     * Adds points to the loyalty program and updates the tier if necessary.
     *
     * @param pointsToAdd The number of points to add
     * @return The updated total points
     */
    public int addPoints(int pointsToAdd) {
        if(pointsToAdd <= 0) throw new IllegalArgumentException("Points to add cannot be negative");

        this.points += pointsToAdd;
        this.lastPointsEarnedDate = LocalDateTime.now();

        // calculate point expiration, ex 1 year from now
        this.pointsExpirationDate = LocalDateTime.now().plusYears(1);

        // update tier
        updateTier();

        return this.points;
    }

    /**
     * Redeems points from the loyalty program.
     *
     * @param pointsToRedeem The number of points to redeem
     * @return The remaining points after redemption
     * @throws IllegalArgumentException if trying to redeem more points than available
     */
    public int redeemPoints(int pointsToRedeem) {
        if(pointsToRedeem <= 0) throw new IllegalArgumentException("Points to redeem cannot be negative");

        if(pointsToRedeem > this.points) throw new IllegalArgumentException("Cannot redeem more points than available");

        this.points -= pointsToRedeem;
        this.lastPointsRedeemedDate = LocalDateTime.now();

        updateTier();

        return  this.points;
    }

    /**
     * Updates the tier based on the current points balance.
     * This is called automatically when points are added or redeemed.
     */
    private void updateTier() {
        Tier newTier = Tier.BRONZE;

        // determine the appropriate tier based on points
        // base case in which looping is not necessary since above 500 points there's only PLATINUM tier
        if(this.points >= 500) {
            newTier = Tier.PLATINUM;
        }

        for(Tier tier : Tier.values()) {
            if(this.points >= tier.getPointsRequired()) {
                newTier = tier; // jumps to the next tier until it finds that this tier has more points than required
            } else {
                break;
            }
        }

        this.tier = newTier;
    }

    /**
     * Calculates points needed to reach the next tier.
     *
     * @return Number of points needed to reach the next tier, or 0 if already at highest tier
     */
    @Transient
    public int getPointsToNextTier() {

        // If already at highest tier
        if(this.tier == Tier.PLATINUM) {
            return 0;
        }

        // Get the next tier
        Tier[] tiers = Tier.values();
        Tier nextTier = tiers[this.tier.ordinal() + 1];

        // Calculate points needed
        return Math.max(0, nextTier.getPointsRequired() - this.points);
    }

    /**
     * Checks if points are about to expire (within 30 days).
     *
     * @return true if points are going to expire soon, false otherwise
     */
    @Transient
    public boolean isPointsExpiringSoon() {
        if(pointsExpirationDate == null) {
            return false;
        }

        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);

        return pointsExpirationDate.isBefore(thirtyDaysFromNow)
                && pointsExpirationDate.isAfter(LocalDateTime.now());
    }

    /**
     * Generates a loyalty program member number if one doesn't exist.
     * Format: "LP-" followed by the customer ID padded to 8 digits.
     *
     * @return The member number
     */
    public String generateMemberNumber() {
        if(this.customer != null && this.customer.getId() != null) {
            this.memberNumber = String.format("LP-%08d", this.customer.getId());
        }

        return this.memberNumber;
    }
}
