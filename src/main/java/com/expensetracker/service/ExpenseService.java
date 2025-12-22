package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.ExpenseSummary;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    // GET ALL EXPENSES
    public List<ExpenseResponse> getAllExpenses() {
        log.info("Fetching all expenses");
        return expenseRepository.findAllByOrderByExpenseDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET EXPENSE BY ID
    public ExpenseResponse getExpenseById(Long id) {
        log.info("Fetching expense with id: {}", id);
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        return mapToResponse(expense);
    }

    // CREATE EXPENSE
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        log.info("Creating expense: {}", request.getTitle());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now());

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    // UPDATE EXPENSE
    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        log.info("Updating expense with id: {}", id);

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        Expense updated = expenseRepository.save(expense);
        log.info("Expense updated: {}", updated.getId());
        return mapToResponse(updated);
    }

    // DELETE EXPENSE
    @Transactional
    public void deleteExpense(Long id) {
        log.info("Deleting expense with id: {}", id);
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense", id);
        }
        expenseRepository.deleteById(id);
        log.info("Expense deleted: {}", id);
    }

    // GET EXPENSES BY DATE RANGE
    public List<ExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching expenses from {} to {}", startDate, endDate);
        return expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET EXPENSES BY CATEGORY
    public List<ExpenseResponse> getExpensesByCategory(Long categoryId) {
        log.info("Fetching expenses for category: {}", categoryId);
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return expenseRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET EXPENSE SUMMARY (for dashboard)
    public ExpenseSummary getExpenseSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Generating expense summary from {} to {}", startDate, endDate);

        List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by category
        Map<Category, List<Expense>> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<ExpenseSummary.CategorySummary> categorySummaries = byCategory.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<Expense> categoryExpenses = entry.getValue();
                    BigDecimal categoryTotal = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? categoryTotal.divide(totalAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                            : 0.0;

                    return ExpenseSummary.CategorySummary.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .amount(categoryTotal)
                            .count(categoryExpenses.size())
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .toList();

        return ExpenseSummary.builder()
                .totalAmount(totalAmount)
                .totalCount(expenses.size())
                .byCategory(categorySummaries)
                .build();
    }

    // GET CURRENT MONTH SUMMARY
    public ExpenseSummary getCurrentMonthSummary() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        return getExpenseSummary(startOfMonth, endOfMonth);
    }

    // MAPPER: Entity -> Response
    private ExpenseResponse mapToResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .categoryIcon(expense.getCategory().getIcon())
                .categoryColor(expense.getCategory().getColor())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
