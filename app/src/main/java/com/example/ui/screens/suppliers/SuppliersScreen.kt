package com.example.ui.screens.suppliers

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SupplierEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.FormatUtils

@Composable
fun SuppliersScreen(
    viewModel: LedgerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"
    val suppliers by viewModel.filteredSuppliers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.supplierSearch.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var supplierToDelete by remember { mutableStateOf<SupplierEntity?>(null) }

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
                        text = "Suppliers & Vendors",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${suppliers.size} suppliers active",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSupplierSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("supplier_search_input"),
                placeholder = { Text("Search supplier name, phone, email...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSupplierSearch("") }) {
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

            // Suppliers List
            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No suppliers found", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add your vendors and suppliers using the + button", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(suppliers, key = { it.id }) { supplier ->
                        val pending = (supplier.totalPurchases - supplier.paidAmount).coerceAtLeast(0.0)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("supplier_card_${supplier.id}"),
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
                                                .size(40.dp)
                                                .background(Color(0xFFE0E7FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.LocalShipping,
                                                contentDescription = null,
                                                tint = DarkNavy,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(supplier.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = supplier.phone.ifEmpty { supplier.email.ifEmpty { supplier.address } },
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row {
                                        if (supplier.phone.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supplier.phone}"))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.Call, contentDescription = "Call", tint = IncomeGreen, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        if (supplier.email.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${supplier.email}"))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Filled.Email, contentDescription = "Email", tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                editingSupplier = supplier
                                                showAddEditDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { supplierToDelete = supplier },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Purchases", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(FormatUtils.formatCurrency(supplier.totalPurchases, currency), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Paid Out", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(FormatUtils.formatCurrency(supplier.paidAmount, currency), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Payable Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            FormatUtils.formatCurrency(pending, currency),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pending > 0) ExpenseRed else IncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Supplier FAB
        FloatingActionButton(
            onClick = {
                editingSupplier = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_add_supplier"),
            containerColor = PrimaryPurple,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Supplier")
        }
    }

    // Add / Edit Supplier Dialog
    if (showAddEditDialog) {
        AddEditSupplierDialog(
            initialSupplier = editingSupplier,
            currency = currency,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, phone, email, address, notes, purchases, paid ->
                if (editingSupplier != null) {
                    viewModel.updateSupplier(
                        editingSupplier!!.copy(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes,
                            totalPurchases = purchases,
                            paidAmount = paid
                        )
                    )
                } else {
                    viewModel.addSupplier(name, phone, email, address, notes, purchases, paid)
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation
    if (supplierToDelete != null) {
        AlertDialog(
            onDismissRequest = { supplierToDelete = null },
            title = { Text("Delete Supplier") },
            text = { Text("Are you sure you want to remove ${supplierToDelete!!.name} from suppliers?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSupplier(supplierToDelete!!)
                        supplierToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddEditSupplierDialog(
    initialSupplier: SupplierEntity? = null,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, address: String, notes: String, purchases: Double, paid: Double) -> Unit
) {
    var name by remember { mutableStateOf(initialSupplier?.name ?: "") }
    var phone by remember { mutableStateOf(initialSupplier?.phone ?: "") }
    var email by remember { mutableStateOf(initialSupplier?.email ?: "") }
    var address by remember { mutableStateOf(initialSupplier?.address ?: "") }
    var notes by remember { mutableStateOf(initialSupplier?.notes ?: "") }
    var purchasesText by remember { mutableStateOf(initialSupplier?.totalPurchases?.toString() ?: "0.0") }
    var paidText by remember { mutableStateOf(initialSupplier?.paidAmount?.toString() ?: "0.0") }
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
                        text = if (initialSupplier != null) "Edit Supplier" else "Add Supplier",
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
                    label = { Text("Supplier / Vendor Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("supplier_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / City") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasesText,
                        onValueChange = { purchasesText = it },
                        label = { Text("Total Orders ($currency)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("Paid Amount ($currency)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Service Details") },
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
                        val purchases = purchasesText.toDoubleOrNull() ?: 0.0
                        val paid = paidText.toDoubleOrNull() ?: 0.0
                        if (name.isBlank()) {
                            errorMessage = "Please enter supplier name"
                        } else {
                            onSave(name, phone, email, address, notes, purchases, paid)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("supplier_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Supplier", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
