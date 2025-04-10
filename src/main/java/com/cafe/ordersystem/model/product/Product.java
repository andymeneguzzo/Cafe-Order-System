package com.cafe.ordersystem.model.product;

import com.cafe.ordersystem.model.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a product (menu item) that can be ordered in the café.
 * Products belong to categories and can have multiple ingredients.
 */

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Many-to-many relationship with Ingredient.
     * Uses a join table named "product_ingredients" with product_id and ingredient_id columns.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_ingredients",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    @Builder.Default
    private Set<Ingredient> ingredients = new HashSet<>();

    /**
     * Preparation time in minutes.
     */
    private Integer preparationTime;

    /**
     * Calories per serving.
     */
    private Integer calories;

    /**
     * Whether this product contains allergens.
     */
    @Column(name = "contains_allergens")
    @Builder.Default
    private boolean containsAllergens = false;

    /**
     * Whether this product is suitable for vegetarians.
     */
    @Column(name = "vegetarian")
    @Builder.Default
    private boolean vegetarian = false;

    /**
     * Whether this product is suitable for vegans.
     */
    @Column(name = "vegan")
    @Builder.Default
    private boolean vegan = false;

    /**
     * Whether this product is gluten-free.
     */
    @Column(name = "gluten_free")
    @Builder.Default
    private boolean glutenFree = false;

    /**
     * Minimum stock level that should trigger reordering.
     */
    @Column(name = "reorder_threshold")
    private Integer reorderThreshold;

    /**
     * Current stock level.
     */
    @Column(name = "stock_level")
    @Builder.Default
    private Integer stockLevel = 0;

    /**
     * Date and time when this product was last restocked.
     */
    private LocalDateTime lastRestocked;

    /**
     * Whether this product is featured/highlighted on menus or promotions.
     */
    @Builder.Default
    private boolean featured = false;

    /**
     * Special notes about this product (e.g., seasonal availability).
     */
    @Size(max = 255)
    private String notes;

    /**
     * Barcode or SKU (Stock Keeping Unit) for inventory management.
     */
    @Column(name = "barcode", unique = true)
    private String barcode;

    /**
     * Adds an ingredient to this product.
     *
     * @param ingredient The ingredient to add
     */
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        updateAllergenFlag();
    }

    /**
     * Removes an ingredient from this product.
     *
     * @param ingredient The ingredient to remove
     */
    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        updateAllergenFlag();
    }

    /**
     * Updates the allergen flag based on the current ingredients.
     */
    private void updateAllergenFlag() {
        this.containsAllergens = ingredients.stream()
                .anyMatch(Ingredient::isAllergen);
    }

    /**
     * Increases the stock level by the specified amount.
     *
     * @param quantity The amount to add to stock
     * @return The new stock level
     */
    public int restockProduct(int quantity) {
        if(quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }

        this.stockLevel += quantity;
        this.lastRestocked = LocalDateTime.now();

        return this.stockLevel;
    }

    /**
     * Decreases the stock level by the specified amount.
     *
     * @param quantity The amount to remove from stock
     * @return The new stock level
     * @throws IllegalArgumentException if there is insufficient stock
     */
    public int reduceStock(int quantity) {
        if(quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if(this.stockLevel < quantity) {
            throw new IllegalArgumentException("Insufficient stock available");
        }

        this.stockLevel -= quantity;
        return this.stockLevel;
    }

    /**
     * Checks if the product needs to be reordered based on the current stock level
     * and reorder threshold.
     *
     * @return true if the product needs to be reordered
     */
    @Transient
    public boolean needsReordering() {
        return this.reorderThreshold != null && this.stockLevel <= this.reorderThreshold;
    }

    /**
     * Updates the price of the product.
     *
     * @param newPrice The new price
     * @throws IllegalArgumentException if the price is negative
     */
    public void updatePrice(BigDecimal newPrice) {
        if(newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be less than zero");
        }

        this.price = newPrice;
    }

    /**
     * Calculates the dietary attributes based on ingredients.
     * Updates vegetarian, vegan, and gluten-free flags.
     */
    public void calculateDietaryAttributes() {

        // vegetarian iff all ingredients are vegetarian
        this.vegetarian = ingredients.stream().allMatch(Ingredient::isVegetarian);

        this.vegan = ingredients.stream().allMatch(Ingredient::isVegan);

        this.glutenFree = ingredients.stream().allMatch(Ingredient::isGlutenFree);
    }

    /**
     * Returns a formatted display price (e.g., "$4.99").
     *
     * @return The formatted price string
     */
    @Transient
    public String getFormattedPrice() {
        return "$" + price.toString();
    }

    /**
     * Checks if the product is available for ordering.
     * A product is available if it's active and has stock (if stock tracking is enabled).
     *
     * @return true if the product is available
     */
    @Transient
    public boolean isAvailable() {
        return active && (reorderThreshold == null || stockLevel > 0);
    }
}
