package com.example.ui.screens.invoices

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ProductEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.FormatUtils

data class InvoiceLineItemState(
    val product: ProductEntity? = null,
    val name: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

@Composable
fun CreateInvoiceScreen(
    viewModel: LedgerViewModel,
    onBack: () -> Unit,
    onInvoiceCreated: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(customers.firstOrNull()) }
    var customerNameInput by remember { mutableStateOf(selectedCustomer?.name ?: "") }
    var customerEmailInput by remember { mutableStateOf(selectedCustomer?.email ?: "") }
    var customerPhoneInput by remember { mutableStateOf(selectedCustomer?.phone ?: "") }
    var customerAddressInput by remember { mutableStateOf(selectedCustomer?.address ?: "") }
    var showCustomerPicker by remember { mutableStateOf(false) }

    var dueDaysOffset by remember { mutableStateOf(15) } // 7, 15, 30 days
    var discountInput by remember { mutableStateOf("0") }
    var taxRateInput by remember { mutableStateOf(user?.defaultTaxRate?.toString() ?: "8.5") }
    var notesInput by remember { mutableStateOf("Payment due within terms. Thank you for your business!") }

    val lineItems = remember {
        mutableStateListOf(
            InvoiceLineItemState(
                product = products.firstOrNull(),
                name = products.firstOrNull()?.name ?: "Professional Services",
                quantity = 1,
                unitPrice = products.firstOrNull()?.price ?: 250.0
            )
        )
    }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculations
    val subtotal = lineItems.sumOf { it.total }
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val taxRate = taxRateInput.toDoubleOrNull() ?: 0.0
    val discountedSubtotal = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount = discountedSubtotal * (taxRate / 100.0)
    val grandTotal = discountedSubtotal + taxAmount

    val now = System.currentTimeMillis()
    val dueDateMillis = now + (dueDaysOffset * 86_400_000L)

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
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Create Invoice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Customer Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "Customer Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box {
                            Button(
                                onClick = { showCustomerPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("select_customer_dropdown_btn")
                            ) {
                                Text("Select Existing", fontSize = 12.sp, color = PrimaryPurple, fontWeight = FontWeight.SemiBold)
                            }

                            DropdownMenu(
                                expanded = showCustomerPicker,
                                onDismissRequest = { showCustomerPicker = false }
                            ) {
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text(cust.name) },
                                        onClick = {
                                            selectedCustomer = cust
                                            customerNameInput = cust.name
                                            customerEmailInput = cust.email
                                            customerPhoneInput = cust.phone
                                            customerAddressInput = cust.address
                                            showCustomerPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it; errorMessage = null },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("invoice_customer_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerEmailInput,
                        onValueChange = { customerEmailInput = it },
                        label = { Text("Customer Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customerPhoneInput,
                            onValueChange = { customerPhoneInput = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = customerAddressInput,
                            onValueChange = { customerAddressInput = it },
                            label = { Text("Address / City") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Terms / Due Date
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Due Date",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7 to "Net 7", 15 to "Net 15", 30 to "Net 30", 45 to "Net 45").forEach { (days, label) ->
                            val isSelected = dueDaysOffset == days
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { dueDaysOffset = days },
                                color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Items Section
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "Line Items (${lineItems.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { showAddItemDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_add_line_item")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    lineItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${item.quantity} x ${FormatUtils.formatCurrency(item.unitPrice, currency)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = FormatUtils.formatCurrency(item.total, currency),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (lineItems.size > 1) {
                                            lineItems.removeAt(index)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove item",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial Summary & Adjustments
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Totals & Taxes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Discount ($currency)") },
                            modifier = Modifier.weight(1f).testTag("invoice_discount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = taxRateInput,
                            onValueChange = { taxRateInput = it },
                            label = { Text("Tax Rate (%)") },
                            modifier = Modifier.weight(1f).testTag("invoice_tax_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Totals Breakdown
                    SummaryLine("Subtotal", FormatUtils.formatCurrency(subtotal, currency))
                    if (discount > 0) {
                        SummaryLine("Discount", "- ${FormatUtils.formatCurrency(discount, currency)}", color = ExpenseRed)
                    }
                    if (taxRate > 0) {
                        SummaryLine("Tax ($taxRate%)", "+ ${FormatUtils.formatCurrency(taxAmount, currency)}")
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = FormatUtils.formatCurrency(grandTotal, currency),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes / Payment Terms") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorMessage ?: "", color = ExpenseRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (customerNameInput.isBlank()) {
                        errorMessage = "Please specify a customer name"
                    } else if (lineItems.isEmpty()) {
                        errorMessage = "Please add at least one line item"
                    } else {
                        viewModel.createInvoice(
                            customerId = selectedCustomer?.id ?: 1L,
                            customerName = customerNameInput,
                            customerEmail = customerEmailInput,
                            customerPhone = customerPhoneInput,
                            customerAddress = customerAddressInput,
                            dueDateMillis = dueDateMillis,
                            discount = discount,
                            taxRate = taxRate,
                            items = lineItems.map { Pair(it.product, Pair(it.quantity, it.unitPrice)) },
                            notes = notesInput
                        )
                        onInvoiceCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_invoice"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Generate & Save Invoice", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemToInvoiceDialog(
            products = products,
            currency = currency,
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                lineItems.add(item)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
private fun SummaryLine(label: String, value: String, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun AddItemToInvoiceDialog(
    products: List<ProductEntity>,
    currency: String,
    onDismiss: () -> Unit,
    onAdd: (InvoiceLineItemState) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(products.firstOrNull()) }
    var itemName by remember { mutableStateOf(selectedProduct?.name ?: "") }
    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember { mutableStateOf(selectedProduct?.price?.toString() ?: "100.0") }
    var showProductMenu by remember { mutableStateOf(false) }

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
                    Text("Add Line Item", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional select from catalog
                if (products.isNotEmpty()) {
                    Box {
                        Button(
                            onClick = { showProductMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📦 Pick from Products Catalog", fontSize = 12.sp, color = PrimaryPurple)
                        }

                        DropdownMenu(
                            expanded = showProductMenu,
                            onDismissRequest = { showProductMenu = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (${FormatUtils.formatCurrency(prod.price, currency)})") },
                                    onClick = {
                                        selectedProduct = prod
                                        itemName = prod.name
                                        unitPriceText = prod.price.toString()
                                        showProductMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name / Description") },
                    modifier = Modifier.fillMaxWidth().testTag("add_item_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f).testTag("add_item_qty_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Unit Price ($currency)") },
                        modifier = Modifier.weight(1f).testTag("add_item_price_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull() ?: 1
                        val price = unitPriceText.toDoubleOrNull() ?: 0.0
                        if (itemName.isNotBlank() && qty > 0) {
                            onAdd(
                                InvoiceLineItemState(
                                    product = selectedProduct,
                                    name = itemName,
                                    quantity = qty,
                                    unitPrice = price
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add to Invoice", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
