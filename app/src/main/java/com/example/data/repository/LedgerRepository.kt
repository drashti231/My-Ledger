package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class LedgerRepository(private val db: AppDatabase) {
    // User
    val user: Flow<UserEntity?> = db.userDao().getUser()
    suspend fun getUserSync(): UserEntity? = db.userDao().getUserSync()
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun insertUser(user: UserEntity) = db.userDao().insertUser(user)

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> = db.transactionDao().getTransactionsByType(type)
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>> = db.transactionDao().getTransactionsBetween(startMillis, endMillis)
    suspend fun insertTransaction(transaction: TransactionEntity): Long = db.transactionDao().insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = db.transactionDao().updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = db.transactionDao().deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = db.transactionDao().deleteTransactionById(id)

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = db.customerDao().getAllCustomers()
    suspend fun getCustomerById(id: Long): CustomerEntity? = db.customerDao().getCustomerById(id)
    fun getCustomerByIdFlow(id: Long): Flow<CustomerEntity?> = db.customerDao().getCustomerByIdFlow(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = db.customerDao().insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = db.customerDao().updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = db.customerDao().deleteCustomer(customer)
    suspend fun deleteCustomerById(id: Long) = db.customerDao().deleteCustomerById(id)

    // Suppliers
    val allSuppliers: Flow<List<SupplierEntity>> = db.supplierDao().getAllSuppliers()
    suspend fun getSupplierById(id: Long): SupplierEntity? = db.supplierDao().getSupplierById(id)
    suspend fun insertSupplier(supplier: SupplierEntity): Long = db.supplierDao().insertSupplier(supplier)
    suspend fun updateSupplier(supplier: SupplierEntity) = db.supplierDao().updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = db.supplierDao().deleteSupplier(supplier)
    suspend fun deleteSupplierById(id: Long) = db.supplierDao().deleteSupplierById(id)

    // Products
    val allProducts: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    suspend fun getProductById(id: Long): ProductEntity? = db.productDao().getProductById(id)
    suspend fun insertProduct(product: ProductEntity): Long = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = db.productDao().deleteProduct(product)
    suspend fun deleteProductById(id: Long) = db.productDao().deleteProductById(id)

    // Invoices
    val allInvoices: Flow<List<InvoiceEntity>> = db.invoiceDao().getAllInvoices()
    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceEntity>> = db.invoiceDao().getInvoicesForCustomer(customerId)
    suspend fun getInvoiceById(id: Long): InvoiceEntity? = db.invoiceDao().getInvoiceById(id)
    fun getInvoiceByIdFlow(id: Long): Flow<InvoiceEntity?> = db.invoiceDao().getInvoiceByIdFlow(id)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long = db.invoiceDao().insertInvoice(invoice)
    suspend fun updateInvoice(invoice: InvoiceEntity) = db.invoiceDao().updateInvoice(invoice)
    suspend fun deleteInvoice(invoice: InvoiceEntity) = db.invoiceDao().deleteInvoice(invoice)
    suspend fun deleteInvoiceById(id: Long) = db.invoiceDao().deleteInvoiceById(id)

    // Invoice Items
    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItemEntity>> = db.invoiceDao().getItemsForInvoice(invoiceId)
    suspend fun getItemsForInvoiceSync(invoiceId: Long): List<InvoiceItemEntity> = db.invoiceDao().getItemsForInvoiceSync(invoiceId)
    suspend fun insertInvoiceItem(item: InvoiceItemEntity): Long = db.invoiceDao().insertInvoiceItem(item)
    suspend fun insertAllInvoiceItems(items: List<InvoiceItemEntity>) = db.invoiceDao().insertAllInvoiceItems(items)
    suspend fun deleteItemsForInvoice(invoiceId: Long) = db.invoiceDao().deleteItemsForInvoice(invoiceId)

    // Payments
    val allPayments: Flow<List<PaymentEntity>> = db.paymentDao().getAllPayments()
    fun getPaymentsForInvoice(invoiceId: Long): Flow<List<PaymentEntity>> = db.paymentDao().getPaymentsForInvoice(invoiceId)
    suspend fun insertPayment(payment: PaymentEntity): Long = db.paymentDao().insertPayment(payment)

    suspend fun checkAndSeedIfEmpty() {
        if (db.userDao().getUserSync() == null) {
            AppDatabase.seedInitialData(db)
        }
    }
}
