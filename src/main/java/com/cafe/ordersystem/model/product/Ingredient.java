package com.cafe.ordersystem.model.product;

import com.cafe.ordersystem.model.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an ingredient used in café products.
 * Ingredients are tracked for allergen information, dietary restrictions,
 * inventory management, and cost analysis.
 */

@Entity
@Table(name = "ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient extends AuditableEntity {

    /**
     * Enum representing units of measurement for ingredients.
     */
    @Getter
    public enum UnitOfMeasure {
        GRAM("g"),
        KILOGRAM("kg"),
        MILLILITER("ml"),
        LITER("L"),
        TEASPOON("tsp"),
        TABLESPOON("tbsp"),
        OUNCE("oz"),
        POUND("lb"),
        PIECE("pc"),
        CUP("cup"),
        PINCH("pinch"),
        EACH("each");

        private final String abbreviation;

        UnitOfMeasure(String abbreviation) {
            this.abbreviation = abbreviation;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 255)
    private String description;

    /**
     * Whether this ingredient is an allergen.
     */
    @Builder.Default
    private boolean allergen = false;

    /**
     * The type of allergen (e.g., "nuts", "dairy", "gluten").
     */
    private String allergenType;

    /**
     * Whether this ingredient is suitable for vegetarians.
     */
    @Builder.Default
    private boolean vegetarian = true;

    /**
     * Whether this ingredient is suitable for vegans.
     */
    @Builder.Default
    private boolean vegan = false;

    /**
     * Whether this ingredient is gluten-free.
     */
    @Column(name = "gluten_free")
    @Builder.Default
    private boolean glutenFree = true;

    /**
     * Current stock level of this ingredient.
     */
    @Column(name = "stock_level", precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal stockLevel = BigDecimal.ZERO;

    /**
     * Minimum stock level that should trigger reordering.
     */
    @Column(name = "reorder_threshold", precision = 10, scale = 3)
    private BigDecimal reorderThreshold;

    /**
     * Unit of measurement for this ingredient.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", length = 20)
    private UnitOfMeasure unitOfMeasure;

    /**
     * Cost per unit of this ingredient.
     */
    @Column(name = "cost_per_unit", precision = 10, scale = 4)
    private BigDecimal costPerUnit;

    /**
     * The supplier of this ingredient.
     */
    @Size(max = 100)
    private String supplier;

    /**
     * Supplier's product code for this ingredient.
     */
    @Column(name = "supplier_product_code")
    private String supplierProductCode;

    /**
     * The location where this ingredient is stored.
     */
    @Size(max = 100)
    @Column(name = "storage_location")
    private String storageLocation;

    /**
     * The temperature requirements for storing this ingredient.
     */
    @Size(max = 100)
    @Column(name = "storage_temperature")
    private String storageTemperature;

    /**
     * The shelf life of this ingredient in days.
     */
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    /**
     * The last date this ingredient was restocked.
     */
    @Column(name = "last_restocked")
    private LocalDateTime lastRestocked;

    /**
     * Whether this ingredient is currently active and available for use.
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Notes about this ingredient.
     */
    @Size(max = 500)
    private String notes;

    /**
     * Products that use this ingredient.
     */
    @ManyToMany(mappedBy = "ingredients")
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    /**
     * Adds stock to this ingredient's inventory.
     *
     * @param amount The amount to add
     * @return The new stock level
     */
    public BigDecimal addStock(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        this.stockLevel = this.stockLevel.add(amount);
        this.lastRestocked = LocalDateTime.now();

        return this.stockLevel;
    }

    /**
     * Removes stock from this ingredient's inventory.
     *
     * @param amount The amount to remove
     * @return The new stock level
     * @throws IllegalArgumentException if there is insufficient stock
     */
    public BigDecimal removeStock(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if(this.stockLevel.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient stock available");
        }

        this.stockLevel = this.stockLevel.subtract(amount);
        return this.stockLevel;
    }

    /**
     * Checks if the ingredient needs to be reordered based on the current stock level
     * and reorder threshold.
     *
     * @return true if the ingredient needs to be reordered
     */
    @Transient
    public boolean needsReordering() {
        return this.reorderThreshold != null && this.stockLevel.compareTo(this.reorderThreshold) <= 0;
    }

    /**
     * Gets the formatted stock level with the unit of measurement.
     *
     * @return The formatted stock level (e.g., "500g", "2L")
     */
    @Transient
    public String getFormattedStockLevel() {
        if(unitOfMeasure == null) {
            return stockLevel.toString();
        }

        return stockLevel.toString() + unitOfMeasure.getAbbreviation();
    }

    /**
     * Calculates the total value of the current stock.
     *
     * @return The total value (stock level * cost per unit)
     */
    @Transient
    public BigDecimal calculateStockValue() {
        if(costPerUnit == null) {
            return BigDecimal.ZERO;
        }

        return stockLevel.multiply(costPerUnit);
    }

    /**
     * Gets a description of the dietary restrictions for this ingredient.
     *
     * @return A string describing the dietary attributes
     */
    @Transient
    public String getDietaryDescription() {

        StringBuilder description = new StringBuilder();
        if(vegetarian) {
            description.append("Vegetarian");
        }
        if(vegan) {
            if(description.length() > 0) {
                description.append(", ");
            }
            description.append("Vegan");
        }

        if (glutenFree) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append("Gluten-Free");
        }

        if(description.length() == 0) {
            return "No special dietary attributes";
        }

        return description.toString();
    }

    /**
     * Gets a description of the allergen status of this ingredient.
     *
     * @return A string describing the allergen status
     */
    @Transient
    public String getAllergenDescription() {
        if(!allergen) return "Not an allergen";

        if(allergenType != null && !allergenType.isEmpty()) return "Allergen: " + allergenType;

        return "Allergen";
    }

    /**
     * Calculates days since last restock.
     *
     * @return The number of days since the ingredient was last restocked, or null if never restocked
     */
    @Transient
    public Integer getDaysSinceLastRestock() {
        if(lastRestocked == null) return null;

        return (int) ChronoUnit.DAYS.between(lastRestocked, LocalDateTime.now());
    }
}
