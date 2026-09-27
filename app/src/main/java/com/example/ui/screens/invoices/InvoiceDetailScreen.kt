package com.example.ui.screens.invoices

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.screens.dashboard.StatusBadge
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.DateUtils
import com.example.util.FormatUtils
import com.example.util.PdfInvoiceGenerator

@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: LedgerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val invoice = allInvoices.find { it.id == invoiceId }
    val currency = user?.currency ?: "$"

    val items = remember { mutableStateListOf<InvoiceItemEntity>() }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        val loaded = viewModel.getInvoiceItems(invoiceId)
        items.clear()
        items.addAll(loaded)
    }

    if (invoice == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Invoice not found")
        }
        return
    }

    val balanceDue = (invoice.total - invoice.paidAmount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = invoice.invoiceNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Invoice Details",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .testTag("delete_invoice_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Invoice",
                    tint = ExpenseRed
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Action Buttons Bar (PDF, Share, Print)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val pdf = PdfInvoiceGenerator.generateInvoicePdf(
                            context = context,
                            user = user ?: UserEntity(),
                            invoice = invoice,
                            items = items
                        )
                        if (pdf != null) {
                            Toast.makeText(context, "PDF saved to ${pdf.name}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_generate_pdf"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val pdf = PdfInvoiceGenerator.generateInvoicePdf(
                            context = context,
                            user = user ?: UserEntity(),
                            invoice = invoice,
                            items = items
                        )
                        if (pdf != null) {
                            PdfInvoiceGenerator.shareInvoicePdf(context, pdf, invoice.invoiceNumber)
                        } else {
                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_share_invoice"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val pdf = PdfInvoiceGenerator.generateInvoicePdf(
                            context = context,
                            user = user ?: UserEntity(),
                            invoice = invoice,
                            items = items
                        )
                        if (pdf != null) {
                            PdfInvoiceGenerator.shareInvoicePdf(context, pdf, invoice.invoiceNumber)
                        }
                    },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_print_invoice"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Invoice Digital Sheet Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header inside invoice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = user?.businessName ?: "Apex Digital Solutions",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user?.businessEmail ?: "billing@business.com",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = user?.businessPhone ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatusBadge(status = invoice.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Billed To Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("BILLED TO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(invoice.customerName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            if (invoice.customerEmail.isNotEmpty()) {
                                Text(invoice.customerEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (invoice.customerPhone.isNotEmpty()) {
                                Text(invoice.customerPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (invoice.customerAddress.isNotEmpty()) {
                                Text(invoice.customerAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Invoice Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatDate(invoice.invoiceDateMillis), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Due Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = DateUtils.formatDate(invoice.dueDateMillis),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (invoice.status == "OVERDUE") ExpenseRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Line Items Table
                    Text("ITEMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${item.quantity} x ${FormatUtils.formatCurrency(item.unitPrice, currency)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = FormatUtils.formatCurrency(item.lineTotal, currency),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Totals
                    DetailRow("Subtotal", FormatUtils.formatCurrency(invoice.subtotal, currency))
                    if (invoice.discount > 0) {
                        DetailRow("Discount", "- " + FormatUtils.formatCurrency(invoice.discount, currency), color = ExpenseRed)
                    }
                    if (invoice.taxRate > 0) {
                        val taxAmt = (invoice.subtotal - invoice.discount) * (invoice.taxRate / 100.0)
                        DetailRow("Tax (${invoice.taxRate}%)", "+ " + FormatUtils.formatCurrency(taxAmt, currency))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow("Total Amount", FormatUtils.formatCurrency(invoice.total, currency), isBold = true, fontSize = 16)

                    if (invoice.paidAmount > 0) {
                        DetailRow("Paid Amount", FormatUtils.formatCurrency(invoice.paidAmount, currency), color = IncomeGreen)
                    }
                    if (balanceDue > 0) {
                        DetailRow("Balance Due", FormatUtils.formatCurrency(balanceDue, currency), isBold = true, color = ExpenseRed, fontSize = 15)
                    }

                    if (invoice.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("NOTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(invoice.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Action Buttons
            if (invoice.status != "PAID") {
                Button(
                    onClick = {
                        viewModel.markInvoiceAsPaid(invoice)
                        Toast.makeText(context, "Invoice marked as PAID!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_mark_paid"),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark as Fully Paid", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_record_partial_pay"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Partial Payment", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Partial Payment Dialog
    if (showPaymentDialog) {
        RecordPaymentDialog(
            balanceDue = balanceDue,
            currency = currency,
            onDismiss = { showPaymentDialog = false },
            onRecord = { amount, method, notes ->
                viewModel.recordPartialPayment(invoice, amount, method, notes)
                showPaymentDialog = false
                Toast.makeText(context, "Payment of ${FormatUtils.formatCurrency(amount, currency)} recorded!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Invoice") },
            text = { Text("Are you sure you want to permanently delete invoice ${invoice.invoiceNumber}? This action cannot be reversed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoice)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: Int = 13,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = fontSize.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = fontSize.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun RecordPaymentDialog(
    balanceDue: Double,
    currency: String,
    onDismiss: () -> Unit,
    onRecord: (amount: Double, method: String, notes: String) -> Unit
) {
    var amountInput by remember { mutableStateOf(balanceDue.toString()) }
    var selectedMethod by remember { mutableStateOf("Bank Transfer") }
    var paymentNotes by remember { mutableStateOf("Client partial payment") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Record Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Balance Due: ${FormatUtils.formatCurrency(balanceDue, currency)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it; errorMessage = null },
                    label = { Text("Payment Amount ($currency)") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                listOf("Bank Transfer", "UPI / Card", "Cash", "Cheque").forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(16.dp),
                            shape = CircleShape,
                            color = if (selectedMethod == method) PrimaryPurple else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(2.dp, if (selectedMethod == method) PrimaryPurple else Color.Gray)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(method, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paymentNotes,
                    onValueChange = { paymentNotes = it },
                    label = { Text("Reference / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = ExpenseRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                            } else {
                                onRecord(amt, selectedMethod, paymentNotes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Record Payment", color = Color.White)
                    }
                }
            }
        }
    }
}
