package com.expensetracker.config;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * This class initializes sample data when the application starts.
 * It only adds data if the database is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Clean up orphaned expenses first
        cleanupOrphanedExpenses();

        // Only initialize if database is empty
        if (categoryRepository.count() == 0) {
            log.info("Initializing sample data...");
            initializeCategories();
            initializeSampleExpenses();
            log.info("Sample data initialized successfully!");
        } else {
            log.info("Database already has data, skipping initialization.");
        }
    }

    private void cleanupOrphanedExpenses() {
        int orphanedCount = expenseRepository.countOrphanedExpenses();
        if (orphanedCount > 0) {
            log.warn("Found {} orphaned expense records (with invalid category_id)", orphanedCount);
            int deleted = expenseRepository.deleteOrphanedExpenses();
            log.info("Deleted {} orphaned expense records", deleted);
        }
    }

    private void initializeCategories() {
        List<Category> categories = List.of(
                createCategory("Food", "🍔", "#FF6B6B"),
                createCategory("Transport", "🚗", "#4ECDC4"),
                createCategory("Shopping", "🛍️", "#45B7D1"),
                createCategory("Bills", "📄", "#96CEB4"),
                createCategory("Entertainment", "🎬", "#DDA0DD"),
                createCategory("Health", "💊", "#98D8C8"),
                createCategory("Education", "📚", "#F7DC6F"),
                createCategory("Other", "📦", "#BDC3C7")
        );
        categoryRepository.saveAll(categories);
        log.info("Created {} categories", categories.size());
    }

    private void initializeSampleExpenses() {
        List<Category> categories = categoryRepository.findAll();
        Category food = categories.stream().filter(c -> c.getName().equals("Food")).findFirst().orElseThrow();
        Category transport = categories.stream().filter(c -> c.getName().equals("Transport")).findFirst().orElseThrow();
        Category shopping = categories.stream().filter(c -> c.getName().equals("Shopping")).findFirst().orElseThrow();
        Category bills = categories.stream().filter(c -> c.getName().equals("Bills")).findFirst().orElseThrow();

        List<Expense> expenses = List.of(
                createExpense("Lunch at office", new BigDecimal("250.00"), "Biryani with friends", food, LocalDate.now()),
                createExpense("Coffee", new BigDecimal("150.00"), "Starbucks", food, LocalDate.now().minusDays(1)),
                createExpense("Uber ride", new BigDecimal("180.00"), "Office to home", transport, LocalDate.now()),
                createExpense("Metro card recharge", new BigDecimal("500.00"), "Monthly recharge", transport, LocalDate.now().minusDays(5)),
                createExpense("Amazon order", new BigDecimal("1200.00"), "Headphones", shopping, LocalDate.now().minusDays(2)),
                createExpense("Electricity bill", new BigDecimal("1800.00"), "December bill", bills, LocalDate.now().minusDays(3)),
                createExpense("Groceries", new BigDecimal("850.00"), "Weekly groceries", food, LocalDate.now().minusDays(4)),
                createExpense("Movie tickets", new BigDecimal("400.00"), "Weekend movie", categories.get(4), LocalDate.now().minusDays(6))
        );
        expenseRepository.saveAll(expenses);
        log.info("Created {} sample expenses", expenses.size());
    }

    private Category createCategory(String name, String icon, String color) {
        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);
        return category;
    }

    private Expense createExpense(String title, BigDecimal amount, String description, Category category, LocalDate date) {
        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setCategory(category);
        expense.setExpenseDate(date);
        return expense;
    }
}
