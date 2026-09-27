package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TransactionEntity
import com.example.ui.screens.ai.AiReceiptScannerDialog
import com.example.ui.screens.ai.AiVoiceEntryDialog
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.PrimaryPurple
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.DateUtils
import com.example.util.FormatUtils
import com.example.util.currentStrings

val TRANSACTION_CATEGORIES = listOf(
    "Business", "Salary", "Food", "Shopping", "Transport", "Bills", "Rent", "Marketing", "Other"
)

val PAYMENT_METHODS = listOf(
    "Cash", "Bank Transfer", "UPI / Card", "Cheque", "Online"
)

@Composable
fun TransactionsScreen(
    viewModel: LedgerViewModel,
    initialAddIncome: Boolean? = null,
    onClearInitialAdd: () -> Unit = {}
) {
    val strings = currentStrings()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.transactionSearch.collectAsStateWithLifecycle()
    val typeFilter by viewModel.transactionTypeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.transactionCategoryFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.transactionSortOrder.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var defaultIsIncome by remember { mutableStateOf(true) }

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showAiScanDialog by remember { mutableStateOf(false) }
    var showAiVoiceDialog by remember { mutableStateOf(false) }

    // Handle initial add triggered from dashboard quick actions
    if (initialAddIncome != null) {
        defaultIsIncome = initialAddIncome
        editingTransaction = null
        showAddEditDialog = true
        onClearInitialAdd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.transactionsTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${transactions.size} records",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sort Dropdown Button
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .testTag("txn_sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest First") },
                            onClick = {
                                viewModel.setTransactionSortOrder("NEWEST")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest First") },
                            onClick = {
                                viewModel.setTransactionSortOrder("OLDEST")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Highest Amount") },
                            onClick = {
                                viewModel.setTransactionSortOrder("HIGH_LOW")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lowest Amount") },
                            onClick = {
                                viewModel.setTransactionSortOrder("LOW_HIGH")
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTransactionSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("txn_search_input"),
                placeholder = { Text(strings.searchTransactionsHint) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setTransactionSearch("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Income / Expense / All Segment Tabs
            val selectedTabIndex = when (typeFilter) {
                "INCOME" -> 1
                "EXPENSE" -> 2
                else -> 0
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { viewModel.setTransactionTypeFilter("ALL") },
                    text = { Text(strings.all, fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { viewModel.setTransactionTypeFilter("INCOME") },
                    text = { Text(strings.income, color = if (selectedTabIndex == 1) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { viewModel.setTransactionTypeFilter("EXPENSE") },
                    text = { Text(strings.expense, color = if (selectedTabIndex == 2) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    title = strings.all,
                    isSelected = categoryFilter == "ALL",
                    onClick = { viewModel.setTransactionCategoryFilter("ALL") }
                )
                TRANSACTION_CATEGORIES.forEach { cat ->
                    CategoryChip(
                        title = strings.category(cat),
                        isSelected = categoryFilter.equals(cat, ignoreCase = true),
                        onClick = { viewModel.setTransactionCategoryFilter(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching transactions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to add a new transaction",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { txn ->
                        TransactionCard(
                            transaction = txn,
                            currency = currency,
                            onEdit = {
                                editingTransaction = txn
                                defaultIsIncome = txn.type == "INCOME"
                                showAddEditDialog = true
                            },
                            onDelete = {
                                transactionToDelete = txn
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FABs to add transaction and AI tools
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // AI Scan Bill Mini-FAB
            SmallFloatingActionButton(
                onClick = { showAiScanDialog = true },
                containerColor = Color(0xFF67E8F9),
                contentColor = DarkNavy,
                modifier = Modifier.testTag("fab_ai_scan_bill")
            ) {
                Icon(Icons.Filled.DocumentScanner, contentDescription = strings.aiScanBill, modifier = Modifier.size(20.dp))
            }

            // AI Voice Entry Mini-FAB
            SmallFloatingActionButton(
                onClick = { showAiVoiceDialog = true },
                containerColor = Color(0xFFF472B6),
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_ai_voice_entry")
            ) {
                Icon(Icons.Filled.Mic, contentDescription = strings.aiVoiceEntry, modifier = Modifier.size(20.dp))
            }

            // Regular Add FAB
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    defaultIsIncome = typeFilter != "EXPENSE"
                    showAddEditDialog = true
                },
                modifier = Modifier.testTag("fab_add_transaction"),
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
            }
        }
    }

    if (showAiScanDialog) {
        AiReceiptScannerDialog(
            viewModel = viewModel,
            onDismiss = { showAiScanDialog = false }
        )
    }

    if (showAiVoiceDialog) {
        AiVoiceEntryDialog(
            viewModel = viewModel,
            onDismiss = { showAiVoiceDialog = false }
        )
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditTransactionDialog(
            initialTransaction = editingTransaction,
            defaultIsIncome = defaultIsIncome,
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { type, amount, category, notes, method, party ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(
                        editingTransaction!!.copy(
                            type = type,
                            amount = amount,
                            category = category,
                            notes = notes,
                            paymentMethod = method,
                            partyName = party
                        )
                    )
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        category = category,
                        notes = notes,
                        paymentMethod = method,
                        partyName = party
                    )
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(strings.deleteTransactionTitle) },
            text = { Text("${strings.deleteTransactionConfirm}\n${FormatUtils.formatCurrency(transactionToDelete!!.amount, currency)} (${strings.category(transactionToDelete!!.category)})") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(strings.deleteButton, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}

@Composable
private fun CategoryChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: TransactionEntity,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (isIncome) IncomeGreenBg else ExpenseRedBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = transaction.type,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val strings = currentStrings()
                    Text(
                        text = transaction.partyName ?: transaction.notes.ifEmpty { strings.category(transaction.category) },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${strings.category(transaction.category)} • ${strings.paymentMethod(transaction.paymentMethod)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.notes.isNotEmpty() && transaction.partyName != null) {
                        Text(
                            text = transaction.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = DateUtils.formatDate(transaction.dateMillis),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+ " else "- ") + FormatUtils.formatCurrency(transaction.amount, currency),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    defaultIsIncome: Boolean = true,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, category: String, notes: String, method: String, party: String?) -> Unit
) {
    var isIncome by remember { mutableStateOf(initialTransaction?.type?.equals("INCOME") ?: defaultIsIncome) }
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTransaction?.category ?: TRANSACTION_CATEGORIES.first()) }
    var selectedMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: PAYMENT_METHODS.first()) }
    var partyName by remember { mutableStateOf(initialTransaction?.partyName ?: "") }
    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTransaction != null) "Edit Transaction" else "Add Transaction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Income / Expense Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isIncome) IncomeGreen else Color.Transparent)
                            .clickable { isIncome = true }
                            .padding(vertical = 8.dp)
                            .testTag("dialog_type_income"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Income",
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isIncome) ExpenseRed else Color.Transparent)
                            .clickable { isIncome = false }
                            .padding(vertical = 8.dp)
                            .testTag("dialog_type_expense"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "- Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                Text("Amount ($currency)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_amount_input"),
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Party Name
                Text("Client / Payee / Source", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_party_input"),
                    placeholder = { Text("e.g. Acme Corp or Grocery") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TRANSACTION_CATEGORIES.forEach { cat ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = cat },
                            color = if (selectedCategory == cat) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method
                Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PAYMENT_METHODS.forEach { method ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedMethod = method },
                            color = if (selectedMethod == method) DarkNavy else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = method,
                                fontSize = 11.sp,
                                color = if (selectedMethod == method) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                Text("Notes / Memo", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_notes_input"),
                    placeholder = { Text("Optional memo or reference") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = ExpenseRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter a valid amount greater than 0"
                        } else {
                            onSave(
                                if (isIncome) "INCOME" else "EXPENSE",
                                amount,
                                selectedCategory,
                                notes,
                                selectedMethod,
                                partyName.ifBlank { null }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Transaction", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
