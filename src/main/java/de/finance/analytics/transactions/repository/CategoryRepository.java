package de.finance.analytics.transactions.repository;

import de.finance.analytics.transactions.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for banking categories
 * Composite Pattern Support for main- and subcategories
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Active categories for auto-categorization
    List<Category> findAllByIsActiveTrue();

    // Main Categories (without Parent)
    List<Category> findByParentCategoryIsNullAndIsActiveTrue();

    // Sub-Categories
    List<Category> findByParentCategoryAndIsActiveTrue(Category parentCategory);

    // Find by Name
    Optional<Category> findByNameAndIsActiveTrue(String name);

    // Expense vs Income Categories
    List<Category> findByIsExpenseAndIsActiveTrue(Boolean isExpense);

    // Keyword-based search for auto-categorization
    @Query("SELECT c FROM Category c WHERE c.isActive = true " +
            "AND LOWER(c.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> findByKeywordContaining(@Param("keyword") String keyword);

    // Color-pallet for charts
    @Query("SELECT c.colorHex FROM Category c WHERE c.isActive = true AND c.colorHex IS NOT NULL")
    List<String> findAllActiveColors();
}