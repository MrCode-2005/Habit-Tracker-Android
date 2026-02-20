package com.vishnu.habittracker.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.habittracker.data.local.entity.ExpenseEntity
import com.vishnu.habittracker.ui.theme.HabitTrackerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Expenses Screen — category summary cards, time filter, expense list.
 * Port of: expenses.js renderSummaryCards + renderExpenses + renderExpenseCard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: ExpenseViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()
    val formState by viewModel.formState.collectAsState()

    val totals = viewModel.calculateTotals(expenses)
    val filtered = viewModel.getFilteredExpenses(expenses)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSheet() },
                containerColor = HabitTrackerColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Expense") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time filter
            item { TimeFilterRow(timeFilter) { viewModel.setTimeFilter(it) } }

            // Summary cards row
            item { SummaryCardsRow(totals) }

            // Category tabs
            item { CategoryTabs(currentTab) { viewModel.setTab(it) } }

            // Expense list
            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Receipt, null, modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No expenses recorded", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { expense ->
                    ExpenseCard(expense, viewModel)
                }
            }
        }
    }

    if (formState.isSheetOpen) { AddExpenseSheet(viewModel) }
}

@Composable
private fun TimeFilterRow(current: String, onChange: (String) -> Unit) {
    val filters = listOf("today" to "Today", "week" to "Week", "month" to "Month", "year" to "Year")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (key, label) ->
            FilterChip(
                selected = current == key, onClick = { onChange(key) },
                label = { Text(label, fontSize = 13.sp) }, shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = HabitTrackerColors.Primary
                )
            )
        }
    }
}

@Composable
private fun SummaryCardsRow(totals: Map<String, Double>) {
    val summaryItems = listOf(
        Triple("🍽️", "Food", totals["food"] ?: 0.0) to Color(0xFFf59e0b),
        Triple("👕", "Shopping", totals["clothing"] ?: 0.0) to Color(0xFF8b5cf6),
        Triple("🚗", "Transport", totals["transportation"] ?: 0.0) to Color(0xFF3b82f6),
        Triple("🛒", "Essentials", totals["essentials"] ?: 0.0) to Color(0xFF10b981),
        Triple("💰", "Total", totals["total"] ?: 0.0) to HabitTrackerColors.Primary
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(summaryItems) { (info, color) ->
            SummaryCard(emoji = info.first, label = info.second, amount = info.third, color = color)
        }
    }
}

@Composable
private fun SummaryCard(emoji: String, label: String, amount: Double, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(140.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 18.sp) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("₹${formatAmount(amount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}

@Composable
private fun CategoryTabs(current: String, onChange: (String) -> Unit) {
    val tabs = listOf("all" to "All", "food" to "🍽️ Food", "clothing" to "👕 Shopping", "transportation" to "🚗 Transport", "essentials" to "🛒 Essentials")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs) { (key, label) ->
            FilterChip(
                selected = current == key, onClick = { onChange(key) },
                label = { Text(label, fontSize = 13.sp) }, shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HabitTrackerColors.Primary.copy(alpha = 0.2f),
                    selectedLabelColor = HabitTrackerColors.Primary
                )
            )
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseEntity, viewModel: ExpenseViewModel) {
    val cat = categoryMap[expense.category]
    val color = cat?.color ?: HabitTrackerColors.Primary
    val formatted = try { LocalDate.parse(expense.expenseDate.take(10)).format(DateTimeFormatter.ofPattern("d MMM yyyy")) } catch (e: Exception) { expense.expenseDate }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Category icon
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(cat?.emoji ?: "💰", fontSize = 20.sp) }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cat?.name ?: expense.category, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("₹${formatAmount(expense.amount)}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = color)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formatted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!expense.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(expense.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Actions
            IconButton(onClick = { viewModel.openEditSheet(expense) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Edit, "Edit", modifier = Modifier.size(16.dp), tint = HabitTrackerColors.Info)
            }
            IconButton(onClick = { viewModel.deleteExpense(expense.id) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, "Delete", modifier = Modifier.size(16.dp), tint = HabitTrackerColors.Danger)
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) amount.toLong().toString()
    else String.format("%.2f", amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseSheet(viewModel: ExpenseViewModel) {
    val formState by viewModel.formState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeSheet() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                if (formState.editingExpenseId != null) "Edit Expense" else "Add Expense",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Amount
            OutlinedTextField(
                value = formState.amount, onValueChange = { viewModel.updateAmount(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Amount (₹)") },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) },
                shape = RoundedCornerShape(12.dp), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Category selector
            Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                expenseCategories.forEach { cat ->
                    FilterChip(
                        selected = formState.category == cat.key,
                        onClick = { viewModel.updateCategory(cat.key) },
                        label = { Text("${cat.emoji} ${cat.name}", fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color.copy(alpha = 0.2f),
                            selectedLabelColor = cat.color
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Note
            OutlinedTextField(
                value = formState.note, onValueChange = { viewModel.updateNote(it) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Note (optional)") },
                shape = RoundedCornerShape(12.dp), singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveExpense() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HabitTrackerColors.Primary),
                enabled = formState.amount.toDoubleOrNull() != null && formState.amount.toDoubleOrNull()!! > 0
            ) {
                Text(if (formState.editingExpenseId != null) "Update" else "Add Expense", color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
