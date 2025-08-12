package de.finance.analytics.transactions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Banking Category for intelligent expense categorization
 * Composite Pattern: Main categories with sub-categories
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // "groceries", "transport", "housing"

    @Column(name = "display_name")
    private String displayName; // "🍔 Lebensmittel & Restaurants" (German for UI)

    @Column(name = "color_hex")
    private String colorHex = "#6C5CE7"; // Color for charts

    @Column(name = "icon")
    private String icon; // "🍔", "🚗", "🏠" for UI

    // Composite Pattern: Parent-Child categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> subCategories = new ArrayList<>();

    // Auto-categorization keywords
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // "REWE,EDEKA,Supermarkt,Lebensmittel"

    @Column(name = "is_expense")
    private Boolean isExpense = true; // true = expense, false = income

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Category(String name, String displayName, String icon) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
    }

    public boolean isMainCategory() {
        return parentCategory == null;
    }

    public boolean hasSubCategories() {
        return !subCategories.isEmpty();
    }

    public String getFullDisplayName() {
        if (icon != null && !icon.isEmpty()) {
            return icon + " " + displayName;
        }
        return displayName;
    }

    @Override
    public String toString() {
        return String.format("Category{id=%d, name='%s', icon='%s'}", id, name, icon);
    }
}