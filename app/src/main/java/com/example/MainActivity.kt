package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.auth.EnterPinScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.SetPinScreen
import com.example.ui.screens.auth.SignInScreen
import com.example.ui.screens.auth.SignUpScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.customers.CustomersScreen
import com.example.ui.screens.invoices.CreateInvoiceScreen
import com.example.ui.screens.invoices.InvoiceDetailScreen
import com.example.ui.screens.products.ProductsScreen
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.screens.reminders.RemindersScreen
import com.example.ui.screens.suppliers.SuppliersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.LocalAppLanguage
import com.example.util.LocalStrings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LedgerViewModel = viewModel()
            val user by viewModel.user.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = user?.isDarkMode ?: systemDark
            val appLanguage = AppLanguage.fromCode(user?.language)
            val appStrings = AppStrings.get(appLanguage)

            CompositionLocalProvider(
                LocalAppLanguage provides appLanguage,
                LocalStrings provides appStrings
            ) {
                MyApplicationTheme(darkTheme = isDarkTheme) {
                    MyLedgerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MyLedgerApp(viewModel: LedgerViewModel) {
    val navController = rememberNavController()
    val user by viewModel.user.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                user = user,
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }

        // 3. Sign In Screen
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    val target = if (user?.isPinEnabled == true && !user?.pin.isNullOrEmpty()) {
                        Screen.EnterPin.route
                    } else {
                        Screen.Main.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onBack = { navController.popBackStack() },
                onLoginClick = { email, pass, onResult ->
                    viewModel.login(email, pass, onResult)
                }
            )
        }

        // 4. Sign Up Screen
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = { navController.navigate(Screen.SignIn.route) },
                onBack = { navController.popBackStack() },
                onSignUpClick = { name, email, pass, bizName, onResult ->
                    viewModel.signUp(name, email, pass, bizName, onResult)
                }
            )
        }

        // 5. Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Enter PIN Screen
        composable(Screen.EnterPin.route) {
            EnterPinScreen(
                correctPin = user?.pin ?: "1234",
                onSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.EnterPin.route) { inclusive = true }
                    }
                },
                onForgotPin = {
                    navController.navigate(Screen.SignIn.route)
                }
            )
        }

        // 7. Set PIN Screen
        composable(Screen.SetPin.route) {
            SetPinScreen(
                currentPin = user?.pin ?: "",
                onPinSaved = { newPin ->
                    viewModel.setPin(newPin)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 8. Main App (Tabs: Home, Transactions, Invoices, Reports, Profile)
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToCreateInvoice = { navController.navigate(Screen.CreateInvoice.route) },
                onNavigateToInvoiceDetail = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                },
                onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
                onNavigateToSuppliers = { navController.navigate(Screen.Suppliers.route) },
                onNavigateToProducts = { navController.navigate(Screen.Products.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToSetPin = { navController.navigate(Screen.SetPin.route) },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 9. Sub-screens
        composable(Screen.CreateInvoice.route) {
            CreateInvoiceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onInvoiceCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceDetailScreen(
                invoiceId = invoiceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Customers.route) {
            CustomersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Suppliers.route) {
            SuppliersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Products.route) {
            ProductsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reminders.route) {
            RemindersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onInvoiceClick = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                }
            )
        }
    }
}
