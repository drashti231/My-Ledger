package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object ForgotPassword : Screen("forgot_password")
    object SetPin : Screen("set_pin")
    object EnterPin : Screen("enter_pin")

    // Main Tabs
    object Main : Screen("main")

    // Sub-screens
    object CreateInvoice : Screen("create_invoice")
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
    object Customers : Screen("customers")
    object Suppliers : Screen("suppliers")
    object Products : Screen("products")
    object Reminders : Screen("reminders")
}

enum class MainTab(val label: String, val route: String) {
    HOME("Home", "tab_home"),
    TRANSACTIONS("Transactions", "tab_transactions"),
    INVOICES("Invoices", "tab_invoices"),
    REPORTS("Reports", "tab_reports"),
    PROFILE("Profile", "tab_profile")
}
