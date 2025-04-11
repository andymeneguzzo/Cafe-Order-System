package com.cafe.ordersystem.model.product;

import com.cafe.ordersystem.model.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a product category in the café system.
 * Categories organize products into logical groups such as "Hot Drinks",
 * "Pastries", "Sandwiches", etc.
 */

@Entity
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 255)
    private String description;

    /**
     * Display order for this category on menus.
     * Lower values are displayed first.
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 1000; // position

    /**
     * Icon or image representing this category.
     */
    @Column(name = "icon_url")
    private String iconUrl;

    /**
     * Whether this category is active and should be displayed.
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Color code for displaying this category in the UI.
     */
    @Column(name = "color_code", length = 7) // RGB format
    private String colorCode;

    /**
     * Whether this category should be displayed on the main menu.
     */
    @Column(name = "show_in_menu")
    @Builder.Default
    private boolean showInMenu = true;

    /**
     * The parent category if this is a sub-category.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * Sub-categories of this category.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();

    /**
     * Products belonging to this category.
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    /**
     * Time of day when this category is available.
     * For example, "breakfast", "lunch", "dinner", "all-day".
     */
    @Column(name = "availability_time")
    private String availabilityTime;

    /**
     * Whether this category is seasonal.
     */
    @Builder.Default
    private boolean seasonal = false;

    /**
     * Adds a sub-category to this category.
     *
     * @param subCategory The sub-category to add
     */
    public void addSubCategory(Category subCategory) {
        subCategories.add(subCategory);
        subCategory.setParent(this);
    }

    /**
     * Removes a sub-category from this category.
     *
     * @param subCategory The sub-category to remove
     */
    public void removeSubCategory(Category subCategory) {
        subCategories.remove(subCategory);
        subCategory.setParent(null);
    }

    /**
     * Gets the full path name of the category (including parent categories).
     * For example, "Drinks > Hot Drinks > Coffee".
     *
     * @return The full path name
     */
    @Transient
    public String getFullPathName() {
        if(parent == null) return name;
        
        return parent.getFullPathName() + " > " + name;
    }

    /**
     * Gets all active products in this category.
     *
     * @return A list of active products
     */
    @Transient
    public List<Product> getActiveProducts() {
        return products.stream()
                .filter(Product::isActive)
                .toList();
    }

    /**
     * Counts the number of active products in this category.
     *
     * @return The count of active products
     */
    @Transient
    public int getActiveProductCount() {
        return (int) products.stream()
                .filter(Product::isActive)
                .count();
    }

    /**
     * Gets the total count of products in this category,
     * including both active and inactive products.
     *
     * @return The total product count
     */
    @Transient
    public int getTotalProductCount() {
        return products.size();
    }

    /**
     * Checks if this is a top-level category (has no parent).
     *
     * @return true if this is a top-level category
     */
    @Transient
    public boolean isTopLevel() {return parent == null;}

    /**
     * Checks if this category has any sub-categories.
     *
     * @return true if this category has sub-categories
     */
    @Transient
    public boolean hasSubCategories() {
        return !subCategories.isEmpty();
    }

    /**
     * Checks if this category is visible to customers.
     * A category is visible if it's active, set to show in menu, and has active products.
     *
     * @return true if the category is visible
     */
    @Transient
    public boolean isVisible() {
        return active && showInMenu && !getActiveProducts().isEmpty();
    }
}