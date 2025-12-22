package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.ExpenseSummary;
import com.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ==================== CRUD OPERATIONS ====================

    // GET /api/expenses - Get all expenses
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpenses() {
        List<ExpenseResponse> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    // GET /api/expenses/{id} - Get expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable Long id) {
        ExpenseResponse expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    // POST /api/expenses - Create new expense
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse created = expenseService.createExpense(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(created));
    }

    // PUT /api/expenses/{id} - Update expense
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse updated = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", updated));
    }

    // DELETE /api/expenses/{id} - Delete expense
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.error(200, "Expense deleted successfully"));
    }

    // ==================== FILTER OPERATIONS ====================

    // GET /api/expenses/filter?startDate=2024-01-01&endDate=2024-01-31
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    // GET /api/expenses/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByCategory(
            @PathVariable Long categoryId) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    // ==================== SUMMARY / DASHBOARD ====================

    // GET /api/expenses/summary?startDate=2024-01-01&endDate=2024-01-31
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ExpenseSummary>> getExpenseSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ExpenseSummary summary = expenseService.getExpenseSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // GET /api/expenses/summary/current-month
    @GetMapping("/summary/current-month")
    public ResponseEntity<ApiResponse<ExpenseSummary>> getCurrentMonthSummary() {
        ExpenseSummary summary = expenseService.getCurrentMonthSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
