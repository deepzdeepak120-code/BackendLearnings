package com.expensetracker.repository;

import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Find by category
    List<Expense> findByCategoryId(Long categoryId);

    // Find by date range
    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by date range ordered by date descending
    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(LocalDate startDate, LocalDate endDate);

    // Find all ordered by date descending
    List<Expense> findAllByOrderByExpenseDateDesc();

    // Get total amount for a date range
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Get total amount by category
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category.id = :categoryId")
    BigDecimal getTotalAmountByCategory(@Param("categoryId") Long categoryId);

    // Delete orphaned expenses (expenses with category_id that doesn't exist in categories table)
    @Modifying
    @Query(value = "DELETE FROM expenses WHERE category_id NOT IN (SELECT id FROM categories)", nativeQuery = true)
    int deleteOrphanedExpenses();

    // Count orphaned expenses
    @Query(value = "SELECT COUNT(*) FROM expenses WHERE category_id NOT IN (SELECT id FROM categories)", nativeQuery = true)
    int countOrphanedExpenses();
}
