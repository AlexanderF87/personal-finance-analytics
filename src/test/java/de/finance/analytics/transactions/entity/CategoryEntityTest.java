package de.finance.analytics.transactions.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity tests for Category business logic
 * Testing composite pattern and banking-categorization
 */
@DisplayName("Category Entity Tests")
class CategoryEntityTest {

    @Test
    @DisplayName("Should create main category correctly")
    void shouldCreateMainCategoryCorrectly() {
        // Given - Main category creation
        Category groceries = new Category("groceries", "🍔 Lebensmittel & Restaurants", "🍔");

        // When & Then - Main category properties
        assertThat(groceries.getName()).isEqualTo("groceries");
        assertThat(groceries.getDisplayName()).isEqualTo("🍔 Lebensmittel & Restaurants");
        assertThat(groceries.getIcon()).isEqualTo("🍔");
        assertThat(groceries.isMainCategory()).isTrue();
        assertThat(groceries.hasSubCategories()).isFalse();
        assertThat(groceries.getIsExpense()).isTrue(); // Default
        assertThat(groceries.getIsActive()).isTrue(); // Default
    }

    @Test
    @DisplayName("Should handle composite pattern correctly")
    void shouldHandleCompositePatternCorrectly() {
        // Given - Parent category
        Category transport = new Category("transport", "🚗 Transport & Mobilität", "🚗");

        // Sub-categories
        Category publicTransport = new Category("public_transport", "🚌 Öffentliche Verkehrsmittel", "🚌");
        Category fuel = new Category("fuel", "⛽ Kraftstoff & Tanken", "⛽");

        // When - Set parent-child relationships
        publicTransport.setParentCategory(transport);
        fuel.setParentCategory(transport);
        transport.getSubCategories().add(publicTransport);
        transport.getSubCategories().add(fuel);

        // Then - Composite pattern works correctly
        assertThat(transport.isMainCategory()).isTrue();
        assertThat(transport.hasSubCategories()).isTrue();
        assertThat(transport.getSubCategories()).hasSize(2);

        assertThat(publicTransport.isMainCategory()).isFalse();
        assertThat(publicTransport.getParentCategory()).isEqualTo(transport);

        assertThat(fuel.isMainCategory()).isFalse();
        assertThat(fuel.getParentCategory()).isEqualTo(transport);
    }

    @Test
    @DisplayName("Should format full display name with icon correctly")
    void shouldFormatFullDisplayNameWithIconCorrectly() {
        // Given - Category with icon
        Category housing = new Category("housing", "Wohnen & Nebenkosten", "🏠");

        // When - Get full display name
        String fullDisplayName = housing.getFullDisplayName();

        // Then - Icon + display name
        assertThat(fullDisplayName).isEqualTo("🏠 Wohnen & Nebenkosten");
    }

    @Test
    @DisplayName("Should handle category without icon correctly")
    void shouldHandleCategoryWithoutIconCorrectly() {
        // Given - Category without icon
        Category category = new Category("other", "Sonstige Ausgaben", null);

        // When - Get full display name
        String fullDisplayName = category.getFullDisplayName();

        // Then - Only display name (no icon)
        assertThat(fullDisplayName).isEqualTo("Sonstige Ausgaben");
    }

    @Test
    @DisplayName("Should handle empty icon correctly")
    void shouldHandleEmptyIconCorrectly() {
        // Given - Category with empty icon
        Category category = new Category("test", "Test Category", "");

        // When - Get full display name
        String fullDisplayName = category.getFullDisplayName();

        // Then - Only display name (empty icon ignored)
        assertThat(fullDisplayName).isEqualTo("Test Category");
    }

    @Test
    @DisplayName("Should set default values correctly")
    void shouldSetDefaultValuesCorrectly() {
        // Given - New category
        Category category = new Category();

        // When - Check defaults
        Boolean isExpense = category.getIsExpense();
        Boolean isActive = category.getIsActive();
        String colorHex = category.getColorHex();

        // Then - Correct defaults
        assertThat(isExpense).isTrue(); // Default: expense category
        assertThat(isActive).isTrue(); // Default: active
        assertThat(colorHex).isEqualTo("#6C5CE7"); // Default color
        assertThat(category.getSubCategories()).isEmpty(); // Empty list initialized
    }

    @Test
    @DisplayName("Should create income category correctly")
    void shouldCreateIncomeCategoryCorrectly() {
        // Given - Income category
        Category salary = new Category("salary", "💰 Gehalt & Einkommen", "💰");
        salary.setIsExpense(false); // Income category

        // When & Then - Income category properties
        assertThat(salary.getIsExpense()).isFalse();
        assertThat(salary.getName()).isEqualTo("salary");
        assertThat(salary.getIcon()).isEqualTo("💰");
    }

    @Test
    @DisplayName("Should handle keywords for auto-categorization")
    void shouldHandleKeywordsForAutoCategorization() {
        // Given - Category with keywords
        Category groceries = new Category("groceries", "🍔 Lebensmittel", "🍔");
        String keywords = "REWE,EDEKA,ALDI,LIDL,Supermarkt,Restaurant,McDonald's";
        groceries.setKeywords(keywords);

        // When & Then - Keywords set correctly
        assertThat(groceries.getKeywords()).isEqualTo(keywords);
        assertThat(groceries.getKeywords()).contains("REWE");
        assertThat(groceries.getKeywords()).contains("Supermarkt");
        assertThat(groceries.getKeywords()).contains("McDonald's");
    }

    @Test
    @DisplayName("Should handle chart colors correctly")
    void shouldHandleChartColorsCorrectly() {
        // Given - Categories with different colors
        Category groceries = new Category("groceries", "Lebensmittel", "🍔");
        groceries.setColorHex("#E74C3C"); // Red

        Category transport = new Category("transport", "Transport", "🚗");
        transport.setColorHex("#3498DB"); // Blue

        Category housing = new Category("housing", "Wohnen", "🏠");
        housing.setColorHex("#2ECC71"); // Green

        // When & Then - Colors set correctly
        assertThat(groceries.getColorHex()).isEqualTo("#E74C3C");
        assertThat(transport.getColorHex()).isEqualTo("#3498DB");
        assertThat(housing.getColorHex()).isEqualTo("#2ECC71");

        // Valid hex color format (6 characters + #)
        assertThat(groceries.getColorHex()).matches("#[A-F0-9]{6}");
    }

    @Test
    @DisplayName("Should handle toString without errors")
    void shouldHandleToStringWithoutErrors() {
        // Given - Category with all fields
        Category category = new Category("test", "Test Category", "🧪");
        category.setId(123L);

        // When - toString called
        String result = category.toString();

        // Then - Contains important information
        assertThat(result).contains("Category");
        assertThat(result).contains("id=123");
        assertThat(result).contains("name='test'");
        assertThat(result).contains("icon='🧪'");
    }

    @Test
    @DisplayName("Should handle category hierarchy navigation")
    void shouldHandleCategoryHierarchyNavigation() {
        // Given - 3-level hierarchy
        Category mainCategory = new Category("transport", "Transport", "🚗");
        Category subCategory = new Category("public", "Öffentlich", "🚌");
        Category subSubCategory = new Category("bus", "Bus", "🚌");

        // Set up hierarchy
        subCategory.setParentCategory(mainCategory);
        subSubCategory.setParentCategory(subCategory);
        mainCategory.getSubCategories().add(subCategory);
        subCategory.getSubCategories().add(subSubCategory);

        // When & Then - Hierarchy navigation works
        assertThat(mainCategory.isMainCategory()).isTrue();
        assertThat(mainCategory.hasSubCategories()).isTrue();

        assertThat(subCategory.isMainCategory()).isFalse();
        assertThat(subCategory.hasSubCategories()).isTrue();
        assertThat(subCategory.getParentCategory()).isEqualTo(mainCategory);

        assertThat(subSubCategory.isMainCategory()).isFalse();
        assertThat(subSubCategory.hasSubCategories()).isFalse();
        assertThat(subSubCategory.getParentCategory()).isEqualTo(subCategory);
    }
}