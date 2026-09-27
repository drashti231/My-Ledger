package com.example.ui.screens.reminders

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InvoiceEntity
import com.example.ui.screens.dashboard.StatusBadge
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.DateUtils
import com.example.util.FormatUtils

@Composable
fun RemindersScreen(
    viewModel: LedgerViewModel,
    onBack: () -> Unit,
    onInvoiceClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val allSuppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Overdue, 2: Upcoming

    val now = System.currentTimeMillis()
    val overdueInvoices = allInvoices.filter {
        (it.status == "OVERDUE" || (it.status == "PENDING" && it.dueDateMillis < now)) && it.paidAmount < it.total
    }
    val upcomingInvoices = allInvoices.filter {
        it.status == "PENDING" && it.dueDateMillis >= now && it.paidAmount < it.total
    }

    val displayInvoices = when (selectedTab) {
        1 -> overdueInvoices
        2 -> upcomingInvoices
        else -> overdueInvoices + upcomingInvoices
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Payment Due Reminders",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${overdueInvoices.size} overdue, ${upcomingInvoices.size} upcoming",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Alerts
        if (overdueInvoices.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ExpenseRedBg)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).background(ExpenseRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.NotificationImportant, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${overdueInvoices.size} Overdue Invoices",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text(
                            text = "Total ${FormatUtils.formatCurrency(overdueInvoices.sumOf { it.total - it.paidAmount }, currency)} requires collection follow-up",
                            fontSize = 12.sp,
                            color = ExpenseRed.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All Alerts (${overdueInvoices.size + upcomingInvoices.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Overdue (${overdueInvoices.size})", color = if (selectedTab == 1) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Upcoming (${upcomingInvoices.size})", color = if (selectedTab == 2) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayInvoices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("All payments settled!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No overdue or pending invoices requiring reminders", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayInvoices, key = { it.id }) { invoice ->
                    val isOverdue = invoice.dueDateMillis < now || invoice.status == "OVERDUE"
                    val dueBalance = (invoice.total - invoice.paidAmount).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_card_${invoice.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (isOverdue) ExpenseRedBg else WarningAmberBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Alarm,
                                            contentDescription = null,
                                            tint = if (isOverdue) ExpenseRed else WarningAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(invoice.customerName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${invoice.invoiceNumber} • Due ${DateUtils.formatDate(invoice.dueDateMillis)}",
                                            fontSize = 11.sp,
                                            color = if (isOverdue) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = FormatUtils.formatCurrency(dueBalance, currency),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverdue) ExpenseRed else WarningAmber
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${invoice.customerEmail}")).apply {
                                            putExtra(Intent.EXTRA_SUBJECT, "Friendly Payment Reminder: ${invoice.invoiceNumber}")
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Dear ${invoice.customerName},\n\nThis is a friendly reminder that invoice ${invoice.invoiceNumber} for ${FormatUtils.formatCurrency(dueBalance, currency)} is due on ${DateUtils.formatDate(invoice.dueDateMillis)}.\n\nThank you,\n${user?.businessName}"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Send Email Reminder"))
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Notice", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.markInvoiceAsPaid(invoice)
                                        Toast.makeText(context, "${invoice.invoiceNumber} marked as Paid!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Mark Paid", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
