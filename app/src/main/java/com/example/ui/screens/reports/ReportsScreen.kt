package com.example.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ai.AiBusinessAdvisorDialog
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.LedgerViewModel
import com.example.ui.viewmodel.MonthDataPoint
import com.example.util.DateUtils
import com.example.util.FormatUtils
import com.example.util.currentStrings

@Composable
fun ReportsScreen(
    viewModel: LedgerViewModel
) {
    val strings = currentStrings()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"
    val dateFilter by viewModel.reportDateFilter.collectAsStateWithLifecycle()

    val reportIncome by viewModel.reportIncome.collectAsStateWithLifecycle()
    val reportExpense by viewModel.reportExpense.collectAsStateWithLifecycle()
    val reportProfit by viewModel.reportProfit.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.reportCategoryBreakdown.collectAsStateWithLifecycle()
    val monthlyTrendData by viewModel.monthlyTrendData.collectAsStateWithLifecycle()

    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val allSuppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()

    val totalReceivables = allInvoices.sumOf { (it.total - it.paidAmount).coerceAtLeast(0.0) }
    val totalPayables = allSuppliers.sumOf { (it.totalPurchases - it.paidAmount).coerceAtLeast(0.0) }

    val profitMargin = if (reportIncome > 0) (reportProfit / reportIncome) * 100.0 else 0.0

    var showAiAdvisorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = strings.reportsTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = strings.manageBusiness,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Date Range Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                DateUtils.DateFilter.THIS_WEEK,
                DateUtils.DateFilter.THIS_MONTH,
                DateUtils.DateFilter.LAST_MONTH,
                DateUtils.DateFilter.THIS_YEAR
            ).forEach { filter ->
                val isSelected = dateFilter == filter
                val label = when (filter) {
                    DateUtils.DateFilter.THIS_WEEK -> strings.filterThisWeek
                    DateUtils.DateFilter.THIS_MONTH -> strings.filterThisMonth
                    DateUtils.DateFilter.LAST_MONTH -> strings.filterLastMonth
                    DateUtils.DateFilter.THIS_YEAR -> strings.filterThisYear
                    else -> filter.label
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setReportDateFilter(filter) }
                        .testTag("report_filter_${filter.name}"),
                    color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Net Profit Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavy)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.netProfit.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = if (reportProfit >= 0) IncomeGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Margin: ${FormatUtils.formatPercent(profitMargin)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reportProfit >= 0) IncomeGreen else ExpenseRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = FormatUtils.formatCurrency(reportProfit, currency),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (reportProfit >= 0) Color.White else ExpenseRed
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(IncomeGreen, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.totalIncome, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCurrency(reportIncome, currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(ExpenseRed, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.totalExpenses, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCurrency(reportExpense, currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ✨ AI Financial Advisor & Audit Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { showAiAdvisorDialog = true }
                .testTag("reports_ai_advisor_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF312E81)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.aiAdvisorTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = strings.aiAdvisorSubtitle,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6-Month Revenue vs Expense Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.monthlyRevenueTrend,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.incomeVsExpense,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendPill("Inc", IncomeGreen)
                        LegendPill("Exp", ExpenseRed)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                MonthlyBarChart(data = monthlyTrendData)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expense Category Breakdown Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = strings.expenseByCategory,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (categoryBreakdown.isEmpty()) {
                    Text(
                        text = strings.noTransactionsYet,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    categoryBreakdown.forEach { catItem ->
                        CategoryBarItem(
                            category = strings.category(catItem.category),
                            amount = catItem.amount,
                            percentage = catItem.percentage,
                            currency = currency
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pending Payment Summary: Receivables vs Payables
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = strings.pendingSummary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(IncomeGreenBg.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(strings.receivablesLabel, fontSize = 11.sp, color = DarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalReceivables, currency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalReceivables > 0) WarningAmber else IncomeGreen
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(ExpenseRedBg.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(strings.payablesLabel, fontSize = 11.sp, color = DarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalPayables, currency),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text("Due to vendors", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }

    if (showAiAdvisorDialog) {
        AiBusinessAdvisorDialog(
            viewModel = viewModel,
            onDismiss = { showAiAdvisorDialog = false },
            onNavigateToReminders = {},
            onNavigateToTransactions = {}
        )
    }
}

@Composable
private fun LegendPill(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MonthlyBarChart(data: List<MonthDataPoint>) {
    val maxVal = data.maxOfOrNull { maxOf(it.income, it.expense) }?.takeIf { it > 0 } ?: 1000.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height - 20f
            val count = data.size
            if (count == 0) return@Canvas

            val slotWidth = totalWidth / count
            val barWidth = slotWidth * 0.28f

            data.forEachIndexed { i, pt ->
                val centerX = (i * slotWidth) + (slotWidth / 2f)

                // Income bar
                val incomeHeight = ((pt.income / maxVal) * totalHeight).toFloat().coerceAtLeast(4f)
                val incomeLeft = centerX - barWidth - 2f
                val incomeTop = totalHeight - incomeHeight
                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(incomeLeft, incomeTop),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Expense bar
                val expenseHeight = ((pt.expense / maxVal) * totalHeight).toFloat().coerceAtLeast(4f)
                val expenseLeft = centerX + 2f
                val expenseTop = totalHeight - expenseHeight
                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(expenseLeft, expenseTop),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        // Labels Row below canvas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { pt ->
                Text(
                    text = pt.monthName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CategoryBarItem(
    category: String,
    amount: Double,
    percentage: Float,
    currency: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${FormatUtils.formatCurrency(amount, currency)} (${(percentage * 100).toInt()}%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when (category) {
                "Rent" -> PrimaryPurple
                "Salary" -> Color(0xFF3B82F6)
                "Marketing" -> WarningAmber
                "Bills" -> Color(0xFFEC4899)
                "Food" -> Color(0xFF10B981)
                else -> Color(0xFF8B5CF6)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
