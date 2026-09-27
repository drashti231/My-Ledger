package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.MainTab
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.invoices.InvoicesScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PrimaryPurple
import com.example.ui.viewmodel.LedgerViewModel

import com.example.util.currentStrings

@Composable
fun MainScreen(
    viewModel: LedgerViewModel,
    onNavigateToCreateInvoice: () -> Unit,
    onNavigateToInvoiceDetail: (Long) -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSetPin: () -> Unit,
    onLogout: () -> Unit
) {
    val strings = currentStrings()
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var pendingAddIncome by remember { mutableStateOf<Boolean?>(null) }
    var pendingAddCustomer by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                listOf(
                    MainTab.HOME to (Icons.Filled.Home to Icons.Outlined.Home),
                    MainTab.TRANSACTIONS to (Icons.Filled.ReceiptLong to Icons.Outlined.ReceiptLong),
                    MainTab.INVOICES to (Icons.Filled.Description to Icons.Outlined.Description),
                    MainTab.REPORTS to (Icons.Filled.Analytics to Icons.Outlined.Analytics),
                    MainTab.PROFILE to (Icons.Filled.Person to Icons.Outlined.Person)
                ).forEach { (tab, icons) ->
                    val isSelected = currentTab == tab
                    val tabLabel = when (tab) {
                        MainTab.HOME -> strings.navHome
                        MainTab.TRANSACTIONS -> strings.navTransactions
                        MainTab.INVOICES -> strings.navInvoices
                        MainTab.REPORTS -> strings.navReports
                        MainTab.PROFILE -> strings.navProfile
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = tabLabel
                            )
                        },
                        label = {
                            Text(
                                text = tabLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryPurple,
                            selectedTextColor = PrimaryPurple,
                            indicatorColor = PrimaryPurple.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentTab = MainTab.TRANSACTIONS },
                        onNavigateToInvoices = { currentTab = MainTab.INVOICES },
                        onNavigateToCustomers = onNavigateToCustomers,
                        onNavigateToSuppliers = onNavigateToSuppliers,
                        onNavigateToProducts = onNavigateToProducts,
                        onNavigateToReminders = onNavigateToReminders,
                        onNavigateToCreateInvoice = onNavigateToCreateInvoice,
                        onNavigateToInvoiceDetail = onNavigateToInvoiceDetail,
                        onOpenAddTransaction = { isIncome ->
                            pendingAddIncome = isIncome
                            currentTab = MainTab.TRANSACTIONS
                        },
                        onOpenAddCustomer = {
                            onNavigateToCustomers()
                        }
                    )
                }
                MainTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        initialAddIncome = pendingAddIncome,
                        onClearInitialAdd = { pendingAddIncome = null }
                    )
                }
                MainTab.INVOICES -> {
                    InvoicesScreen(
                        viewModel = viewModel,
                        onCreateInvoiceClick = onNavigateToCreateInvoice,
                        onInvoiceClick = onNavigateToInvoiceDetail
                    )
                }
                MainTab.REPORTS -> {
                    ReportsScreen(viewModel = viewModel)
                }
                MainTab.PROFILE -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToSetPin = onNavigateToSetPin,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
