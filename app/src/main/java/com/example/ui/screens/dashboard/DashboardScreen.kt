package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.screens.ai.AiAssistantSection
import com.example.ui.screens.ai.AiBusinessAdvisorDialog
import com.example.ui.screens.ai.AiReceiptScannerDialog
import com.example.ui.screens.ai.AiVoiceEntryDialog
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.PrimaryBrandLight
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.DateUtils
import com.example.util.FormatUtils
import com.example.util.LocalAppLanguage
import com.example.util.currentStrings

@Composable
fun DashboardScreen(
    viewModel: LedgerViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToCreateInvoice: () -> Unit,
    onNavigateToInvoiceDetail: (Long) -> Unit,
    onOpenAddTransaction: (isIncome: Boolean) -> Unit,
    onOpenAddCustomer: () -> Unit
) {
    val strings = currentStrings()
    val currentLanguage = LocalAppLanguage.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val pendingAmount by viewModel.pendingPaymentsAmount.collectAsStateWithLifecycle()
    val monthlyRevenue by viewModel.monthlyRevenue.collectAsStateWithLifecycle()
    val monthlyExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()

    val currency = user?.currency ?: "$"
    val recentTransactions = allTransactions.take(5)
    val recentInvoices = allInvoices.take(4)

    var showAiScanDialog by remember { mutableStateOf(false) }
    var showAiVoiceDialog by remember { mutableStateOf(false) }
    var showAiAdvisorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // 1. Dashboard Top Header Bar with Language Switcher
        DashboardHeader(
            businessName = user?.businessName ?: "My Business",
            userName = user?.fullName ?: "Alex Morgan",
            currentLanguage = currentLanguage,
            onToggleLanguage = {
                val next = if (currentLanguage == AppLanguage.GUJARATI) "en" else "gu"
                viewModel.setLanguage(next)
            },
            onNotificationClick = onNavigateToReminders
        )

        // 2. Main Financial Hero Card
        HeroBalanceCard(
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            currency = currency,
            strings = strings
        )

        // ✨ 3. AI Assistant Hub (Scan Bill, Voice Entry, Business Advice)
        AiAssistantSection(
            strings = strings,
            onScanBill = { showAiScanDialog = true },
            onVoiceEntry = { showAiVoiceDialog = true },
            onBusinessAdvice = { showAiAdvisorDialog = true }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Quick Actions Grid
        QuickActionsSection(
            strings = strings,
            onAddIncome = { onOpenAddTransaction(true) },
            onAddExpense = { onOpenAddTransaction(false) },
            onCreateInvoice = onNavigateToCreateInvoice,
            onAddCustomer = onOpenAddCustomer
        )

        // 4. Quick Modules Shortcuts (Customers, Suppliers, Products, Reminders)
        ManagementShortcuts(
            strings = strings,
            onCustomers = onNavigateToCustomers,
            onSuppliers = onNavigateToSuppliers,
            onProducts = onNavigateToProducts,
            onReminders = onNavigateToReminders
        )

        // 5. Monthly Performance & Pending Stats
        PerformanceSummarySection(
            monthlyRevenue = monthlyRevenue,
            monthlyExpense = monthlyExpense,
            pendingAmount = pendingAmount,
            currency = currency,
            strings = strings
        )

        // 6. Recent Transactions
        SectionHeader(
            title = strings.recentTransactions,
            actionLabel = strings.viewAll,
            onAction = onNavigateToTransactions
        )
        if (recentTransactions.isEmpty()) {
            EmptyListPlaceholder(strings.noTransactionsYet)
        } else {
            recentTransactions.forEach { txn ->
                TransactionItemRow(
                    transaction = txn,
                    currency = currency,
                    strings = strings
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Recent Invoices
        SectionHeader(
            title = strings.recentInvoices,
            actionLabel = strings.viewAll,
            onAction = onNavigateToInvoices
        )
        if (recentInvoices.isEmpty()) {
            EmptyListPlaceholder(strings.noInvoicesYet)
        } else {
            recentInvoices.forEach { invoice ->
                InvoiceItemRow(
                    invoice = invoice,
                    currency = currency,
                    strings = strings,
                    onClick = { onNavigateToInvoiceDetail(invoice.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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

    if (showAiAdvisorDialog) {
        AiBusinessAdvisorDialog(
            viewModel = viewModel,
            onDismiss = { showAiAdvisorDialog = false },
            onNavigateToReminders = onNavigateToReminders,
            onNavigateToTransactions = onNavigateToTransactions
        )
    }
}

@Composable
private fun DashboardHeader(
    businessName: String,
    userName: String,
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val strings = currentStrings()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedLivePulseIndicator(color = IncomeGreen, size = 6.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = businessName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryPurple,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            AnimatedTimeGreeting(
                userName = userName,
                isGujarati = currentLanguage == AppLanguage.GUJARATI
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Quick Language Switch Pill Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleLanguage() }
                    .testTag("btn_quick_language_switch"),
                color = PrimaryPurple.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.GUJARATI) "🇮🇳 ગુ" else "🇬🇧 EN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .testTag("dashboard_notifications_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Reminders & Alerts",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HeroBalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currency: String,
    strings: AppStrings
) {
    var isBalanceHidden by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "HeroAuroraTransition")
    val auroraOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AuroraOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("dashboard_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF042F2E),
                            Color(0xFF0F172A),
                            Color(0xFF115E59)
                        ),
                        start = androidx.compose.ui.geometry.Offset(auroraOffset * 0.5f, 0f),
                        end = androidx.compose.ui.geometry.Offset(auroraOffset + 400f, 400f)
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.totalBalance.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { isBalanceHidden = !isBalanceHidden },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle Balance Visibility",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF1E293B).copy(alpha = 0.8f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedLivePulseIndicator(color = Color(0xFF2DD4BF), size = 6.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (strings.language == AppLanguage.GUJARATI) "લાઈવ ખાતાવહી" else "Live Ledger",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2DD4BF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedCurrencyText(
                    targetAmount = totalBalance,
                    currency = currency,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    isBalanceHidden = isBalanceHidden
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Income / Expense Row inside Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A).copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total Income
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(IncomeGreen.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Income",
                                tint = IncomeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(strings.totalIncome, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            AnimatedCurrencyText(
                                targetAmount = totalIncome,
                                currency = currency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                isBalanceHidden = isBalanceHidden
                            )
                        }
                    }

                    // Total Expense
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ExpenseRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = "Expense",
                                tint = ExpenseRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(strings.totalExpenses, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            AnimatedCurrencyText(
                                targetAmount = totalExpense,
                                currency = currency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                isBalanceHidden = isBalanceHidden
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cashflow Sparkline Wave Canvas
                AnimatedCashflowWave(
                    income = totalIncome,
                    expense = totalExpense,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Financial Health Score Bar
                AnimatedFinancialHealthScore(
                    income = totalIncome,
                    expense = totalExpense,
                    isGujarati = strings.language == AppLanguage.GUJARATI
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    strings: AppStrings,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onCreateInvoice: () -> Unit,
    onAddCustomer: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = strings.quickActions,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                title = strings.income,
                subtitle = "+ ${strings.saveButton}",
                color = IncomeGreen,
                bgColor = IncomeGreenBg,
                modifier = Modifier.weight(1f),
                testTag = "btn_quick_income",
                onClick = onAddIncome
            )
            QuickActionButton(
                title = strings.expense,
                subtitle = "- ${strings.saveButton}",
                color = ExpenseRed,
                bgColor = ExpenseRedBg,
                modifier = Modifier.weight(1f),
                testTag = "btn_quick_expense",
                onClick = onAddExpense
            )
            QuickActionButton(
                title = strings.navInvoices,
                subtitle = "+ ${strings.createNewInvoice.take(6)}",
                color = PrimaryPurple,
                bgColor = PrimaryBrandLight,
                modifier = Modifier.weight(1f),
                testTag = "btn_quick_invoice",
                onClick = onCreateInvoice
            )
            QuickActionButton(
                title = strings.customers,
                subtitle = "+ ${strings.addCustomer.take(6)}",
                color = DarkNavy,
                bgColor = Color(0xFFE2E8F0),
                modifier = Modifier.weight(1f),
                testTag = "btn_quick_customer",
                onClick = onAddCustomer
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    subtitle: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(color, color.copy(alpha = 0.82f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ManagementShortcuts(
    strings: AppStrings,
    onCustomers: () -> Unit,
    onSuppliers: () -> Unit,
    onProducts: () -> Unit,
    onReminders: () -> Unit
) {
    Column {
        Text(
            text = strings.quickHub,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShortcutChip(strings.customers, Icons.Filled.Group, onCustomers)
            ShortcutChip(strings.suppliers, Icons.Filled.LocalShipping, onSuppliers)
            ShortcutChip(strings.products, Icons.Filled.Inventory2, onProducts)
            ShortcutChip(strings.reminders, Icons.Filled.Alarm, onReminders)
        }
    }
}

@Composable
private fun ShortcutChip(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PerformanceSummarySection(
    monthlyRevenue: Double,
    monthlyExpense: Double,
    pendingAmount: Double,
    currency: String,
    strings: AppStrings
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Monthly Revenue Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(strings.monthlyRevenue, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedCurrencyText(
                    targetAmount = monthlyRevenue,
                    currency = currency,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${strings.monthlyExpenses}: ${FormatUtils.formatCurrency(monthlyExpense, currency)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Pending Payments Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(strings.pendingPayments, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedCurrencyText(
                    targetAmount = pendingAmount,
                    currency = currency,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pendingAmount > 0) WarningAmber else IncomeGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (pendingAmount > 0) strings.statusPending else strings.statusPaid,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAction() }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryPurple
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionEntity,
    currency: String,
    strings: AppStrings
) {
    val isIncome = transaction.type == "INCOME"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isIncome) IncomeGreenBg else ExpenseRedBg,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = transaction.type,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.partyName ?: transaction.notes.ifEmpty { strings.category(transaction.category) },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${strings.category(transaction.category)} • ${DateUtils.formatDate(transaction.dateMillis)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+ " else "- ") + FormatUtils.formatCurrency(transaction.amount, currency),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.paymentMethod(transaction.paymentMethod),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InvoiceItemRow(
    invoice: InvoiceEntity,
    currency: String,
    strings: AppStrings,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFEEECFD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = "Invoice",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = invoice.customerName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${invoice.invoiceNumber} • ${strings.dueDate}: ${DateUtils.formatDate(invoice.dueDateMillis)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatCurrency(invoice.total, currency),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = invoice.status, strings = strings)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, strings: AppStrings? = null) {
    val (color, bg) = when (status.uppercase()) {
        "PAID" -> Pair(IncomeGreen, IncomeGreenBg)
        "OVERDUE" -> Pair(ExpenseRed, ExpenseRedBg)
        else -> Pair(WarningAmber, WarningAmberBg)
    }

    val displayStatus = strings?.status(status) ?: status.uppercase()

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = displayStatus.uppercase(),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyListPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
