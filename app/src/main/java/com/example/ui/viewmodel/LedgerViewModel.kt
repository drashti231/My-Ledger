package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.BusinessInsight
import com.example.data.ai.BusinessSummaryForAi
import com.example.data.ai.GeminiAiService
import com.example.data.ai.ParsedTransactionResult
import com.example.data.ai.ScannedBillResult
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.LedgerRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class MonthDataPoint(
    val monthName: String,
    val income: Double,
    val expense: Double
)

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LedgerRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = LedgerRepository(db)
        viewModelScope.launch {
            repository.checkAndSeedIfEmpty()
        }
    }

    // User Profile & Settings
    val user: StateFlow<UserEntity?> = repository.user.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Raw Flows from DB
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSuppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allInvoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI Search & Filter States
    private val _transactionSearch = MutableStateFlow("")
    val transactionSearch = _transactionSearch.asStateFlow()

    private val _transactionTypeFilter = MutableStateFlow("ALL") // ALL, INCOME, EXPENSE
    val transactionTypeFilter = _transactionTypeFilter.asStateFlow()

    private val _transactionCategoryFilter = MutableStateFlow("ALL")
    val transactionCategoryFilter = _transactionCategoryFilter.asStateFlow()

    private val _transactionSortOrder = MutableStateFlow("NEWEST") // NEWEST, OLDEST, HIGH_LOW, LOW_HIGH
    val transactionSortOrder = _transactionSortOrder.asStateFlow()

    private val _customerSearch = MutableStateFlow("")
    val customerSearch = _customerSearch.asStateFlow()

    private val _supplierSearch = MutableStateFlow("")
    val supplierSearch = _supplierSearch.asStateFlow()

    private val _productSearch = MutableStateFlow("")
    val productSearch = _productSearch.asStateFlow()

    private val _invoiceSearch = MutableStateFlow("")
    val invoiceSearch = _invoiceSearch.asStateFlow()

    private val _invoiceStatusFilter = MutableStateFlow("ALL") // ALL, PAID, PENDING, OVERDUE
    val invoiceStatusFilter = _invoiceStatusFilter.asStateFlow()

    private val _reportDateFilter = MutableStateFlow(DateUtils.DateFilter.THIS_MONTH)
    val reportDateFilter = _reportDateFilter.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _transactionSearch,
        _transactionTypeFilter,
        _transactionCategoryFilter,
        _transactionSortOrder
    ) { txns, search, type, category, sort ->
        txns.filter { txn ->
            val matchesSearch = search.isBlank() ||
                    txn.notes.contains(search, ignoreCase = true) ||
                    txn.category.contains(search, ignoreCase = true) ||
                    (txn.partyName?.contains(search, ignoreCase = true) == true)
            val matchesType = type == "ALL" || txn.type.equals(type, ignoreCase = true)
            val matchesCategory = category == "ALL" || txn.category.equals(category, ignoreCase = true)
            matchesSearch && matchesType && matchesCategory
        }.let { list ->
            when (sort) {
                "NEWEST" -> list.sortedByDescending { it.dateMillis }
                "OLDEST" -> list.sortedBy { it.dateMillis }
                "HIGH_LOW" -> list.sortedByDescending { it.amount }
                "LOW_HIGH" -> list.sortedBy { it.amount }
                else -> list
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Customers
    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        allCustomers,
        _customerSearch
    ) { customers, query ->
        if (query.isBlank()) customers
        else customers.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Suppliers
    val filteredSuppliers: StateFlow<List<SupplierEntity>> = combine(
        allSuppliers,
        _supplierSearch
    ) { suppliers, query ->
        if (query.isBlank()) suppliers
        else suppliers.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _productSearch
    ) { products, query ->
        if (query.isBlank()) products
        else products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.sku.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Invoices
    val filteredInvoices: StateFlow<List<InvoiceEntity>> = combine(
        allInvoices,
        _invoiceSearch,
        _invoiceStatusFilter
    ) { invoices, query, status ->
        invoices.filter { inv ->
            val matchesQuery = query.isBlank() ||
                    inv.invoiceNumber.contains(query, ignoreCase = true) ||
                    inv.customerName.contains(query, ignoreCase = true)
            val matchesStatus = status == "ALL" || inv.status.equals(status, ignoreCase = true)
            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Totals
    val totalIncome: StateFlow<Double> = allTransactions.combine(MutableStateFlow(Unit)) { txns, _ ->
        txns.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = allTransactions.combine(MutableStateFlow(Unit)) { txns, _ ->
        txns.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingPaymentsAmount: StateFlow<Double> = allInvoices.combine(MutableStateFlow(Unit)) { invs, _ ->
        invs.filter { it.status == "PENDING" || it.status == "OVERDUE" }
            .sumOf { (it.total - it.paidAmount).coerceAtLeast(0.0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyRevenue: StateFlow<Double> = allTransactions.combine(MutableStateFlow(Unit)) { txns, _ ->
        val (start, end) = DateUtils.getTimeRange(DateUtils.DateFilter.THIS_MONTH)
        txns.filter { it.type == "INCOME" && it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> = allTransactions.combine(MutableStateFlow(Unit)) { txns, _ ->
        val (start, end) = DateUtils.getTimeRange(DateUtils.DateFilter.THIS_MONTH)
        txns.filter { it.type == "EXPENSE" && it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Reports Aggregations
    val reportIncome: StateFlow<Double> = combine(allTransactions, _reportDateFilter) { txns, filter ->
        val (start, end) = DateUtils.getTimeRange(filter)
        txns.filter { it.type == "INCOME" && it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val reportExpense: StateFlow<Double> = combine(allTransactions, _reportDateFilter) { txns, filter ->
        val (start, end) = DateUtils.getTimeRange(filter)
        txns.filter { it.type == "EXPENSE" && it.dateMillis in start..end }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val reportProfit: StateFlow<Double> = combine(reportIncome, reportExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val reportCategoryBreakdown: StateFlow<List<CategoryBreakdown>> = combine(allTransactions, _reportDateFilter) { txns, filter ->
        val (start, end) = DateUtils.getTimeRange(filter)
        val expenses = txns.filter { it.type == "EXPENSE" && it.dateMillis in start..end }
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) {
            emptyList()
        } else {
            expenses.groupBy { it.category }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.amount }
                    CategoryBreakdown(cat, sum, (sum / total).toFloat())
                }
                .sortedByDescending { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6-Month Trend Data
    val monthlyTrendData: StateFlow<List<MonthDataPoint>> = allTransactions.combine(MutableStateFlow(Unit)) { txns, _ ->
        val calendar = Calendar.getInstance()
        val dataPoints = mutableListOf<MonthDataPoint>()

        for (i in 5 downTo 0) {
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.MONTH, -i)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis

            val monthName = DateUtils.formatMonth(start)
            val inc = txns.filter { it.type == "INCOME" && it.dateMillis in start..end }.sumOf { it.amount }
            val exp = txns.filter { it.type == "EXPENSE" && it.dateMillis in start..end }.sumOf { it.amount }
            dataPoints.add(MonthDataPoint(monthName, inc, exp))
        }
        dataPoints
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter Setters
    fun setTransactionSearch(query: String) { _transactionSearch.value = query }
    fun setTransactionTypeFilter(type: String) { _transactionTypeFilter.value = type }
    fun setTransactionCategoryFilter(category: String) { _transactionCategoryFilter.value = category }
    fun setTransactionSortOrder(sort: String) { _transactionSortOrder.value = sort }

    fun setCustomerSearch(query: String) { _customerSearch.value = query }
    fun setSupplierSearch(query: String) { _supplierSearch.value = query }
    fun setProductSearch(query: String) { _productSearch.value = query }
    fun setInvoiceSearch(query: String) { _invoiceSearch.value = query }
    fun setInvoiceStatusFilter(status: String) { _invoiceStatusFilter.value = status }
    fun setReportDateFilter(filter: DateUtils.DateFilter) { _reportDateFilter.value = filter }

    // Database Actions: Transactions
    fun addTransaction(
        type: String,
        amount: Double,
        category: String,
        notes: String,
        paymentMethod: String,
        partyName: String?,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                type = type,
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                notes = notes,
                paymentMethod = paymentMethod,
                partyName = partyName
            )
            repository.insertTransaction(entity)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Database Actions: Customers
    fun addCustomer(name: String, phone: String, email: String, address: String, notes: String) {
        viewModelScope.launch {
            val customer = CustomerEntity(
                name = name,
                phone = phone,
                email = email,
                address = address,
                notes = notes
            )
            repository.insertCustomer(customer)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Database Actions: Suppliers
    fun addSupplier(name: String, phone: String, email: String, address: String, notes: String, totalPurchases: Double = 0.0, paidAmount: Double = 0.0) {
        viewModelScope.launch {
            val supplier = SupplierEntity(
                name = name,
                phone = phone,
                email = email,
                address = address,
                notes = notes,
                totalPurchases = totalPurchases,
                paidAmount = paidAmount
            )
            repository.insertSupplier(supplier)
        }
    }

    fun updateSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.updateSupplier(supplier)
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    // Database Actions: Products
    fun addProduct(name: String, sku: String, price: Double, stockQuantity: Int, category: String, taxRate: Double, description: String) {
        viewModelScope.launch {
            val product = ProductEntity(
                name = name,
                sku = sku,
                price = price,
                stockQuantity = stockQuantity,
                category = category,
                taxRate = taxRate,
                description = description
            )
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Database Actions: Invoices
    fun createInvoice(
        customerId: Long,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        customerAddress: String,
        dueDateMillis: Long,
        discount: Double,
        taxRate: Double,
        items: List<Pair<ProductEntity?, Pair<Int, Double>>>, // (Product?, (quantity, unitPrice))
        notes: String
    ) {
        viewModelScope.launch {
            val invoiceCount = (allInvoices.value.size + 1)
            val invNumber = String.format("INV-2026-%03d", invoiceCount)
            val subtotal = items.sumOf { it.second.first * it.second.second }
            val discounted = (subtotal - discount).coerceAtLeast(0.0)
            val taxAmount = discounted * (taxRate / 100.0)
            val total = discounted + taxAmount

            val invoice = InvoiceEntity(
                invoiceNumber = invNumber,
                customerId = customerId,
                customerName = customerName,
                customerEmail = customerEmail,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                invoiceDateMillis = System.currentTimeMillis(),
                dueDateMillis = dueDateMillis,
                discount = discount,
                taxRate = taxRate,
                subtotal = subtotal,
                total = total,
                paidAmount = 0.0,
                status = "PENDING",
                notes = notes
            )
            val invoiceId = repository.insertInvoice(invoice)

            // Insert line items
            val itemEntities = items.map { (prod, qtyPrice) ->
                InvoiceItemEntity(
                    invoiceId = invoiceId,
                    productId = prod?.id,
                    productName = prod?.name ?: "Service / Item",
                    quantity = qtyPrice.first,
                    unitPrice = qtyPrice.second,
                    lineTotal = qtyPrice.first * qtyPrice.second
                )
            }
            repository.insertAllInvoiceItems(itemEntities)
        }
    }

    fun markInvoiceAsPaid(invoice: InvoiceEntity) {
        viewModelScope.launch {
            val updated = invoice.copy(
                paidAmount = invoice.total,
                status = "PAID"
            )
            repository.updateInvoice(updated)

            // Also record a payment and an Income transaction
            repository.insertPayment(
                PaymentEntity(
                    invoiceId = invoice.id,
                    amount = invoice.total - invoice.paidAmount,
                    paymentDateMillis = System.currentTimeMillis(),
                    paymentMethod = "Bank Transfer",
                    notes = "Marked as paid for ${invoice.invoiceNumber}"
                )
            )
            repository.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    amount = invoice.total - invoice.paidAmount,
                    category = "Business",
                    dateMillis = System.currentTimeMillis(),
                    notes = "Payment received for invoice ${invoice.invoiceNumber}",
                    paymentMethod = "Bank Transfer",
                    referenceId = invoice.invoiceNumber,
                    partyName = invoice.customerName
                )
            )
        }
    }

    fun recordPartialPayment(invoice: InvoiceEntity, paymentAmount: Double, paymentMethod: String, notes: String) {
        viewModelScope.launch {
            val newPaid = invoice.paidAmount + paymentAmount
            val newStatus = if (newPaid >= invoice.total - 0.01) "PAID" else "PENDING"
            val updated = invoice.copy(paidAmount = newPaid, status = newStatus)
            repository.updateInvoice(updated)

            repository.insertPayment(
                PaymentEntity(
                    invoiceId = invoice.id,
                    amount = paymentAmount,
                    paymentDateMillis = System.currentTimeMillis(),
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )

            repository.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    amount = paymentAmount,
                    category = "Business",
                    dateMillis = System.currentTimeMillis(),
                    notes = "Payment received for invoice ${invoice.invoiceNumber}: $notes",
                    paymentMethod = paymentMethod,
                    referenceId = invoice.invoiceNumber,
                    partyName = invoice.customerName
                )
            )
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            repository.deleteItemsForInvoice(invoice.id)
        }
    }

    suspend fun getInvoiceItems(invoiceId: Long): List<InvoiceItemEntity> {
        return repository.getItemsForInvoiceSync(invoiceId)
    }

    // Database Actions: User & Profile
    fun updateUserProfile(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                repository.updateUser(currentUser.copy(isDarkMode = enabled))
            }
        }
    }

    fun togglePinProtection(enabled: Boolean) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                repository.updateUser(currentUser.copy(isPinEnabled = enabled))
            }
        }
    }

    fun setPin(newPin: String) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                repository.updateUser(currentUser.copy(pin = newPin, isPinEnabled = true))
            }
        }
    }

    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                repository.updateUser(currentUser.copy(language = langCode))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                repository.updateUser(currentUser.copy(isLoggedIn = false))
            }
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = user.value ?: repository.getUserSync()
            if (currentUser != null) {
                // If user exists, log them in
                val updated = currentUser.copy(isLoggedIn = true)
                repository.updateUser(updated)
                onResult(true)
            } else {
                val newUser = UserEntity(
                    email = email,
                    passwordHash = pass,
                    isLoggedIn = true
                )
                repository.insertUser(newUser)
                onResult(true)
            }
        }
    }

    fun signUp(name: String, email: String, pass: String, bizName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val newUser = (user.value ?: UserEntity()).copy(
                fullName = name,
                email = email,
                passwordHash = pass,
                businessName = bizName,
                isLoggedIn = true
            )
            repository.insertUser(newUser)
            onResult(true)
        }
    }

    // --- AI Gemini Features ---
    private val aiService = GeminiAiService.getInstance()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiInsights = MutableStateFlow<List<BusinessInsight>>(emptyList())
    val aiInsights: StateFlow<List<BusinessInsight>> = _aiInsights.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    fun loadAiInsights(forceRefresh: Boolean = false) {
        if (!forceRefresh && _aiInsights.value.isNotEmpty()) return

        viewModelScope.launch {
            _isGeneratingInsights.value = true
            val currentUser = user.value
            val lang = currentUser?.language ?: "gu"
            val curr = currentUser?.currency ?: "$"
            val bizName = currentUser?.businessName ?: "My Business"

            val topCategoryItem = reportCategoryBreakdown.value.firstOrNull()
            val topCategory: String = topCategoryItem?.category ?: "Bills"
            val topCategoryAmount: Double = topCategoryItem?.amount ?: 0.0

            val overdueCount = allInvoices.value.count { it.status.uppercase() == "OVERDUE" || (it.status.uppercase() == "PENDING" && it.dueDateMillis < System.currentTimeMillis()) }

            val totalSupplierPayables = allSuppliers.value.sumOf { (it.totalPurchases - it.paidAmount).coerceAtLeast(0.0) }

            val summary = BusinessSummaryForAi(
                businessName = bizName,
                currency = curr,
                totalRevenue = totalIncome.value,
                totalExpense = totalExpense.value,
                netProfit = totalBalance.value,
                pendingReceivables = pendingPaymentsAmount.value,
                pendingPayables = totalSupplierPayables,
                topExpenseCategory = topCategory,
                topExpenseAmount = topCategoryAmount,
                overdueCustomersCount = overdueCount
            )

            val result = aiService.generateBusinessAudit(summary, lang)
            _aiInsights.value = result.getOrElse { emptyList() }
            _isGeneratingInsights.value = false
        }
    }

    fun scanBillWithAi(bitmap: Bitmap, onResult: (ScannedBillResult) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val lang = user.value?.language ?: "gu"
            val result = aiService.scanReceipt(bitmap, lang)
            _isAiLoading.value = false
            result.onSuccess {
                onResult(it)
            }.onFailure {
                onError(it.message ?: "Failed to scan receipt")
            }
        }
    }

    fun parseNaturalLanguageWithAi(input: String, onResult: (ParsedTransactionResult) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val lang = user.value?.language ?: "gu"
            val result = aiService.parseNaturalLanguageEntry(input, lang)
            _isAiLoading.value = false
            result.onSuccess {
                onResult(it)
            }.onFailure {
                onError(it.message ?: "Failed to parse entry")
            }
        }
    }

    fun generateWhatsAppReminderWithAi(
        customerName: String,
        amount: Double,
        tone: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val lang = user.value?.language ?: "gu"
            val curr = user.value?.currency ?: "$"
            val bizName = user.value?.businessName ?: "My Ledger"
            val result = aiService.generatePaymentReminderMessage(
                customerName = customerName,
                amount = amount,
                currency = curr,
                businessName = bizName,
                tone = tone,
                languageCode = lang
            )
            _isAiLoading.value = false
            onResult(result.getOrDefault(""))
        }
    }

    fun saveParsedAiTransaction(parsed: ParsedTransactionResult, onSaved: () -> Unit) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                type = parsed.type,
                amount = parsed.amount,
                category = parsed.category,
                dateMillis = System.currentTimeMillis(),
                notes = parsed.notes,
                paymentMethod = parsed.paymentMethod,
                partyName = parsed.partyName
            )
            repository.insertTransaction(entity)
            onSaved()
        }
    }

    fun saveScannedBillTransaction(bill: ScannedBillResult, onSaved: () -> Unit) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                type = bill.type,
                amount = bill.amount,
                category = bill.category,
                dateMillis = bill.dateMillis,
                notes = "${bill.notes} ${bill.itemsSummary}".trim(),
                paymentMethod = bill.paymentMethod,
                partyName = bill.partyName
            )
            repository.insertTransaction(entity)
            onSaved()
        }
    }
}

