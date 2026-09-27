package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myledger_database.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedInitialData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val oneDayMillis = 86_400_000L

            // 1. Initial User
            val defaultUser = UserEntity(
                id = 1,
                fullName = "Alex Morgan",
                email = "alex.morgan@myledger.biz",
                passwordHash = "password123",
                pin = "1234",
                isPinEnabled = true,
                businessName = "Apex Digital Systems",
                businessPhone = "+1 (555) 382-9011",
                businessEmail = "billing@apexdigital.biz",
                businessAddress = "104 Innovation Way, Tech Park, Suite 400",
                businessTaxId = "US-TAX-8891240-B",
                currency = "$",
                defaultTaxRate = 8.5,
                notificationsEnabled = true,
                isDarkMode = false,
                isLoggedIn = true,
                language = "en"
            )
            db.userDao().insertUser(defaultUser)

            // 2. Customers
            val customers = listOf(
                CustomerEntity(1, "Acme Corporation", "+1 (555) 123-4567", "finance@acmecorp.com", "742 Evergreen Terr, Springfield", "Key enterprise client for SaaS consulting", now - 40 * oneDayMillis),
                CustomerEntity(2, "Starlight Retailers", "+1 (555) 987-6543", "contact@starlight.store", "88 Market Street, New York", "Wholesale client, bi-weekly billing", now - 35 * oneDayMillis),
                CustomerEntity(3, "Nexus Media Lab", "+1 (555) 456-7890", "accounts@nexusmedia.io", "32 Silicon Ave, San Francisco", "Monthly design and IT retainer", now - 25 * oneDayMillis),
                CustomerEntity(4, "Horizon Logistics", "+1 (555) 321-7654", "billing@horizonlog.com", "500 Harbor Blvd, Seattle", "Fleet maintenance and portal development", now - 15 * oneDayMillis),
                CustomerEntity(5, "BrightPath EdTech", "+1 (555) 789-0123", "orders@brightpathed.org", "12 University Plaza, Boston", "E-learning software licenses", now - 5 * oneDayMillis)
            )
            db.customerDao().insertAll(customers)

            // 3. Suppliers
            val suppliers = listOf(
                SupplierEntity(1, "CloudHost Global Services", "+1 (800) 555-0199", "sales@cloudhostglobal.net", "Austin, TX", "AWS & Google Cloud infrastructure provider", 4200.0, 4200.0, now - 60 * oneDayMillis),
                SupplierEntity(2, "Apex Hardware & Office", "+1 (800) 555-0144", "order@apexsupplies.com", "Chicago, IL", "Laptops, workstations, and office consumables", 3150.0, 2000.0, now - 45 * oneDayMillis),
                SupplierEntity(3, "PixelCraft Studio Agency", "+1 (800) 555-0177", "team@pixelcraft.design", "Portland, OR", "Outsourced 3D animation and branding assets", 1800.0, 1800.0, now - 30 * oneDayMillis),
                SupplierEntity(4, "LegalPro Advisory LLP", "+1 (800) 555-0122", "info@legalproadvisors.com", "New York, NY", "Corporate compliance & trademark audit", 1250.0, 500.0, now - 10 * oneDayMillis)
            )
            db.supplierDao().insertAll(suppliers)

            // 4. Products / Items
            val products = listOf(
                ProductEntity(1, "Enterprise ERP Suite License", "SKU-ERP-01", 1250.0, 45, "Software", 8.5, "Annual user seat for business operations"),
                ProductEntity(2, "Cloud Cloud Architecture Consulting (hr)", "SKU-CNS-HR", 150.0, 120, "Services", 0.0, "Senior DevOps and Cloud infrastructure engineering"),
                ProductEntity(3, "UI/UX Design Sprint Package", "SKU-DSN-SPR", 850.0, 25, "Design", 5.0, "Complete web & mobile UX prototyping and validation"),
                ProductEntity(4, "Dedicated VPS Server Hosting (mo)", "SKU-SRV-VPS", 120.0, 80, "Hosting", 8.5, "High-performance NVMe cloud compute instance"),
                ProductEntity(5, "Database Backup & Security Audit", "SKU-SEC-ADT", 600.0, 30, "Security", 8.5, "Full compliance scan and automated backup setup"),
                ProductEntity(6, "Custom API Integration Service", "SKU-API-INT", 950.0, 15, "Development", 8.5, "Custom REST & Webhook webhook middleware integration")
            )
            db.productDao().insertAll(products)

            // 5. Invoices & Items
            val inv1 = InvoiceEntity(
                id = 1,
                invoiceNumber = "INV-2026-001",
                customerId = 1,
                customerName = "Acme Corporation",
                customerEmail = "finance@acmecorp.com",
                customerPhone = "+1 (555) 123-4567",
                customerAddress = "742 Evergreen Terr, Springfield",
                invoiceDateMillis = now - 20 * oneDayMillis,
                dueDateMillis = now - 5 * oneDayMillis,
                discount = 100.0,
                taxRate = 8.5,
                subtotal = 3750.0,
                total = 3960.63,
                paidAmount = 3960.63,
                status = "PAID",
                notes = "Net 15 terms. Thank you for your business!"
            )
            val inv2 = InvoiceEntity(
                id = 2,
                invoiceNumber = "INV-2026-002",
                customerId = 2,
                customerName = "Starlight Retailers",
                customerEmail = "contact@starlight.store",
                customerPhone = "+1 (555) 987-6543",
                customerAddress = "88 Market Street, New York",
                invoiceDateMillis = now - 14 * oneDayMillis,
                dueDateMillis = now + 7 * oneDayMillis,
                discount = 50.0,
                taxRate = 8.5,
                subtotal = 1700.0,
                total = 1790.25,
                paidAmount = 1000.0,
                status = "PENDING",
                notes = "First milestone paid ($1,000). Remaining balance due upon sprint completion."
            )
            val inv3 = InvoiceEntity(
                id = 3,
                invoiceNumber = "INV-2026-003",
                customerId = 3,
                customerName = "Nexus Media Lab",
                customerEmail = "accounts@nexusmedia.io",
                customerPhone = "+1 (555) 456-7890",
                customerAddress = "32 Silicon Ave, San Francisco",
                invoiceDateMillis = now - 28 * oneDayMillis,
                dueDateMillis = now - 7 * oneDayMillis,
                discount = 0.0,
                taxRate = 8.5,
                subtotal = 2550.0,
                total = 2766.75,
                paidAmount = 0.0,
                status = "OVERDUE",
                notes = "Payment was due 7 days ago. Reminder sent."
            )
            val inv4 = InvoiceEntity(
                id = 4,
                invoiceNumber = "INV-2026-004",
                customerId = 4,
                customerName = "Horizon Logistics",
                customerEmail = "billing@horizonlog.com",
                customerPhone = "+1 (555) 321-7654",
                customerAddress = "500 Harbor Blvd, Seattle",
                invoiceDateMillis = now - 3 * oneDayMillis,
                dueDateMillis = now + 18 * oneDayMillis,
                discount = 0.0,
                taxRate = 8.5,
                subtotal = 1200.0,
                total = 1302.00,
                paidAmount = 0.0,
                status = "PENDING",
                notes = "Monthly cloud VPS and backup retainer."
            )
            db.invoiceDao().insertAllInvoices(listOf(inv1, inv2, inv3, inv4))

            // Invoice Items
            val items = listOf(
                InvoiceItemEntity(1, 1, 1, "Enterprise ERP Suite License", 2, 1250.0, 2500.0),
                InvoiceItemEntity(2, 1, 2, "Cloud Cloud Architecture Consulting (hr)", 8, 150.0, 1200.0),
                InvoiceItemEntity(3, 2, 3, "UI/UX Design Sprint Package", 2, 850.0, 1700.0),
                InvoiceItemEntity(4, 3, 3, "UI/UX Design Sprint Package", 3, 850.0, 2550.0),
                InvoiceItemEntity(5, 4, 4, "Dedicated VPS Server Hosting (mo)", 5, 120.0, 600.0),
                InvoiceItemEntity(6, 4, 5, "Database Backup & Security Audit", 1, 600.0, 600.0)
            )
            db.invoiceDao().insertAllInvoiceItems(items)

            // 6. Payments
            val payments = listOf(
                PaymentEntity(1, 1, 3960.63, now - 6 * oneDayMillis, "Bank Transfer", "Full payment wire received - Ref #TXN9921"),
                PaymentEntity(2, 2, 1000.00, now - 10 * oneDayMillis, "UPI / Card", "Deposit for design sprint")
            )
            db.paymentDao().insertAll(payments)

            // 7. Transactions (Income and Expenses)
            val transactions = listOf(
                TransactionEntity(1, "INCOME", 3960.63, "Business", now - 6 * oneDayMillis, "Payment from Acme Corp (INV-2026-001)", "Bank Transfer", "INV-2026-001", "Acme Corporation"),
                TransactionEntity(2, "INCOME", 1000.00, "Business", now - 10 * oneDayMillis, "Advance Deposit Starlight Retailers", "UPI / Card", "INV-2026-002", "Starlight Retailers"),
                TransactionEntity(3, "INCOME", 4500.00, "Salary", now - 22 * oneDayMillis, "Client monthly software maintenance retainer", "Bank Transfer", null, "BluePeak Tech"),
                TransactionEntity(4, "INCOME", 1800.00, "Other", now - 27 * oneDayMillis, "Affiliate partnership commission", "Online", null, "TechAffiliates Network"),
                
                TransactionEntity(5, "EXPENSE", 1450.00, "Rent", now - 4 * oneDayMillis, "Office space lease - Tech Park Unit 400", "Bank Transfer", null, "Metro Commercial Realty"),
                TransactionEntity(6, "EXPENSE", 420.00, "Bills", now - 7 * oneDayMillis, "High-speed fiber internet and electric utility", "UPI / Card", null, "PowerGrid & Telecom"),
                TransactionEntity(7, "EXPENSE", 850.00, "Marketing", now - 9 * oneDayMillis, "Search Engine Ad campaign & social outreach", "Online", null, "Google & Meta Ads"),
                TransactionEntity(8, "EXPENSE", 320.00, "Food", now - 11 * oneDayMillis, "Team project launch celebration luncheon", "UPI / Card", null, "Bistro Gourmet"),
                TransactionEntity(9, "EXPENSE", 650.00, "Shopping", now - 16 * oneDayMillis, "Dual 4K monitors for development workstation", "UPI / Card", null, "BestTech Store"),
                TransactionEntity(10, "EXPENSE", 180.00, "Transport", now - 19 * oneDayMillis, "Airport rides & client visit fuel reimbursement", "Cash", null, "City Transit"),
                TransactionEntity(11, "EXPENSE", 1200.00, "Salary", now - 24 * oneDayMillis, "Contract front-end developer compensation", "Bank Transfer", null, "DevContractor Pro")
            )
            db.transactionDao().insertAll(transactions)
        }
    }
}
