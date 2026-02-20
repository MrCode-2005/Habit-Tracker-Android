package com.vishnu.habittracker.ui.expenses

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.habittracker.data.local.entity.ExpenseEntity
import com.vishnu.habittracker.data.repository.AuthRepository
import com.vishnu.habittracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Category definitions — mirrors expenses.js categories object.
 */
data class ExpenseCategory(
    val key: String,
    val name: String,
    val color: Color,
    val emoji: String,
    val parent: String? = null
)

val expenseCategories = listOf(
    ExpenseCategory("food_outing", "Food While Outing", Color(0xFFf59e0b), "🍽️", "food"),
    ExpenseCategory("food_online", "Online Food Orders", Color(0xFFef4444), "🛵", "food"),
    ExpenseCategory("clothing", "Clothing & Shopping", Color(0xFF8b5cf6), "👕"),
    ExpenseCategory("transportation", "Transportation", Color(0xFF3b82f6), "🚗"),
    ExpenseCategory("essentials", "Daily Essentials", Color(0xFF10b981), "🛒")
)

val categoryMap = expenseCategories.associateBy { it.key }

data class ExpenseFormState(
    val amount: String = "",
    val category: String = "food_outing",
    val date: String = LocalDate.now().toString(),
    val description: String = "",
    val editingExpenseId: String? = null,
    val isSheetOpen: Boolean = false
)

/**
 * ExpenseViewModel — manages expense list, summary calculations, and filters.
 * Port of: expenses.js (2,160 lines) — categories, renderSummaryCards, renderExpenses, CRUD
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    val expenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getActiveExpenses(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow("all")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _timeFilter = MutableStateFlow("month")
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    fun setTab(tab: String) { _currentTab.value = tab }
    fun setTimeFilter(filter: String) { _timeFilter.value = filter }

    /** Calculate totals by category group — port of renderSummaryCards() */
    fun calculateTotals(expenses: List<ExpenseEntity>): Map<String, Double> {
        val dateRange = getDateRange()
        val totals = mutableMapOf("food" to 0.0, "clothing" to 0.0, "transportation" to 0.0, "essentials" to 0.0)

        expenses.filter { !it.isDeleted && isInDateRange(it.date, dateRange) }.forEach { exp ->
            val amount = exp.amount
            if (exp.category == "food_outing" || exp.category == "food_online") {
                totals["food"] = (totals["food"] ?: 0.0) + amount
            } else {
                totals[exp.category] = (totals[exp.category] ?: 0.0) + amount
            }
        }
        totals["total"] = totals.values.sum()
        return totals
    }

    /** Get filtered expenses by tab and time range */
    fun getFilteredExpenses(expenses: List<ExpenseEntity>): List<ExpenseEntity> {
        val dateRange = getDateRange()
        var filtered = expenses.filter { !it.isDeleted && isInDateRange(it.date, dateRange) }
        val tab = _currentTab.value
        if (tab != "all") {
            filtered = when (tab) {
                "food" -> filtered.filter { it.category == "food_outing" || it.category == "food_online" }
                else -> filtered.filter { it.category == tab }
            }
        }
        return filtered.sortedByDescending { it.date }
    }

    private fun getDateRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        return when (_timeFilter.value) {
            "today" -> today to today
            "week" -> today.minusDays(7) to today
            "month" -> today.withDayOfMonth(1) to today
            "year" -> today.withDayOfYear(1) to today
            else -> LocalDate.of(2020, 1, 1) to today
        }
    }

    private fun isInDateRange(dateStr: String, range: Pair<LocalDate, LocalDate>): Boolean {
        return try {
            val date = LocalDate.parse(dateStr.take(10))
            !date.isBefore(range.first) && !date.isAfter(range.second)
        } catch (e: Exception) { false }
    }

    // Form
    fun openAddSheet() { _formState.value = ExpenseFormState(isSheetOpen = true) }
    fun openEditSheet(expense: ExpenseEntity) {
        _formState.value = ExpenseFormState(
            amount = expense.amount.toString(),
            category = expense.category,
            date = expense.date,
            description = expense.description ?: "",
            editingExpenseId = expense.id,
            isSheetOpen = true
        )
    }
    fun closeSheet() { _formState.value = _formState.value.copy(isSheetOpen = false) }
    fun updateAmount(amount: String) { _formState.value = _formState.value.copy(amount = amount) }
    fun updateCategory(category: String) { _formState.value = _formState.value.copy(category = category) }
    fun updateDescription(desc: String) { _formState.value = _formState.value.copy(description = desc) }

    fun saveExpense() {
        val form = _formState.value
        val amount = form.amount.toDoubleOrNull() ?: return
        viewModelScope.launch {
            if (form.editingExpenseId != null) {
                val existing = expenses.value.find { it.id == form.editingExpenseId } ?: return@launch
                // Soft-delete old and add new (since no updateExpense in repo)
                expenseRepository.softDeleteExpense(existing.id)
                expenseRepository.addExpense(
                    userId = userId, amount = amount, category = form.category,
                    date = form.date, description = form.description.ifBlank { null }
                )
            } else {
                expenseRepository.addExpense(
                    userId = userId, amount = amount, category = form.category,
                    date = form.date, description = form.description.ifBlank { null }
                )
            }
            _formState.value = ExpenseFormState()
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch { expenseRepository.softDeleteExpense(expenseId) }
    }
}
