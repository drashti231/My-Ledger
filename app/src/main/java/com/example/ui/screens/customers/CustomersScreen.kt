package com.example.ui.screens.customers

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CustomerEntity
import com.example.ui.screens.ai.AiWhatsAppReminderDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.FormatUtils
import com.example.util.currentStrings

@Composable
fun CustomersScreen(
    viewModel: LedgerViewModel,
    onBack: () -> Unit,
    initialAddCustomer: Boolean = false,
    onClearInitialAdd: () -> Unit = {}
) {
    val strings = currentStrings()
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"
    val customers by viewModel.filteredCustomers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.customerSearch.collectAsStateWithLifecycle()
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(initialAddCustomer) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var viewingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var reminderCustomer by remember { mutableStateOf<Pair<CustomerEntity, Double>?>(null) }

    if (initialAddCustomer) {
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
                        text = strings.customersTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${customers.size} records",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setCustomerSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("customer_search_input"),
                placeholder = { Text(strings.searchCustomersHint) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setCustomerSearch("") }) {
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

            Spacer(modifier = Modifier.height(14.dp))

            // Customer List
            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No customers found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add a new customer with the + button below",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        val customerInvoices = allInvoices.filter { it.customerId == customer.id }
                        val totalInvoiced = customerInvoices.sumOf { it.total }
                        val totalPaid = customerInvoices.sumOf { it.paidAmount }
                        val totalPending = (totalInvoiced - totalPaid).coerceAtLeast(0.0)

                        CustomerCard(
                            customer = customer,
                            invoicesCount = customerInvoices.size,
                            totalInvoiced = totalInvoiced,
                            totalPaid = totalPaid,
                            totalPending = totalPending,
                            currency = currency,
                            onClick = { viewingCustomer = customer },
                            onEdit = {
                                editingCustomer = customer
                                showAddEditDialog = true
                            },
                            onDelete = { customerToDelete = customer },
                            onCall = {
                                if (customer.phone.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onEmail = {
                                if (customer.email.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${customer.email}"))
                                    context.startActivity(intent)
                                }
                            },
                            onAiReminder = {
                                reminderCustomer = Pair(customer, totalPending)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Customer FAB
        FloatingActionButton(
            onClick = {
                editingCustomer = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_add_customer"),
            containerColor = PrimaryPurple,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Customer")
        }
    }

    if (reminderCustomer != null) {
        AiWhatsAppReminderDialog(
            viewModel = viewModel,
            customerName = reminderCustomer!!.first.name,
            customerPhone = reminderCustomer!!.first.phone,
            amount = reminderCustomer!!.second,
            onDismiss = { reminderCustomer = null }
        )
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditCustomerDialog(
            initialCustomer = editingCustomer,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, phone, email, address, notes ->
                if (editingCustomer != null) {
                    viewModel.updateCustomer(
                        editingCustomer!!.copy(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addCustomer(name, phone, email, address, notes)
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer") },
            text = { Text("Are you sure you want to delete ${customerToDelete!!.name}? Invoices linked to this customer will remain in records.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(customerToDelete!!)
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Customer Details Dialog
    if (viewingCustomer != null) {
        CustomerDetailsDialog(
            customer = viewingCustomer!!,
            currency = currency,
            viewModel = viewModel,
            onDismiss = { viewingCustomer = null }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: CustomerEntity,
    invoicesCount: Int,
    totalInvoiced: Double,
    totalPaid: Double,
    totalPending: Double,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onAiReminder: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
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
                            .size(42.dp)
                            .background(Color(0xFFEEECFD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(customer.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = customer.phone.ifEmpty { customer.email.ifEmpty { "No contact info" } },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    if (customer.phone.isNotEmpty()) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Call, contentDescription = "Call", tint = IncomeGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (totalPending > 0 && onAiReminder != null) {
                        IconButton(
                            onClick = onAiReminder,
                            modifier = Modifier.size(32.dp).testTag("cust_ai_reminder_${customer.id}")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "AI WhatsApp Reminder", tint = IncomeGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (customer.email.isNotEmpty()) {
                        IconButton(onClick = onEmail, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Email, contentDescription = "Email", tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invoices", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$invoicesCount", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paid", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FormatUtils.formatCurrency(totalPaid, currency), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        FormatUtils.formatCurrency(totalPending, currency),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalPending > 0) WarningAmber else IncomeGreen
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditCustomerDialog(
    initialCustomer: CustomerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var email by remember { mutableStateOf(initialCustomer?.email ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialCustomer != null) "Edit Customer" else "Add Customer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Customer / Company Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("cust_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth().testTag("cust_phone_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth().testTag("cust_email_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Relationship") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = ExpenseRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Please enter customer name"
                        } else {
                            onSave(name, phone, email, address, notes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("cust_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Customer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CustomerDetailsDialog(
    customer: CustomerEntity,
    currency: String,
    viewModel: LedgerViewModel,
    onDismiss: () -> Unit
) {
    val allInvoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val invoices = allInvoices.filter { it.customerId == customer.id }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Customer Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(customer.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                if (customer.email.isNotEmpty()) Text("Email: ${customer.email}", fontSize = 13.sp)
                if (customer.phone.isNotEmpty()) Text("Phone: ${customer.phone}", fontSize = 13.sp)
                if (customer.address.isNotEmpty()) Text("Address: ${customer.address}", fontSize = 13.sp)
                if (customer.notes.isNotEmpty()) Text("Notes: ${customer.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Invoices History (${invoices.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (invoices.isEmpty()) {
                    Text("No invoices recorded for this customer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    invoices.forEach { inv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${inv.invoiceNumber} (${inv.status})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatCurrency(inv.total, currency), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
