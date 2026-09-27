package com.example.ui.screens.profile

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.AppLanguage
import com.example.util.currentStrings

val CURRENCIES = listOf(
    "$" to "USD ($)",
    "€" to "EUR (€)",
    "£" to "GBP (£)",
    "₹" to "INR (₹)",
    "CA$" to "CAD (CA$)",
    "A$" to "AUD (A$)",
    "¥" to "JPY (¥)"
)

@Composable
fun ProfileScreen(
    viewModel: LedgerViewModel,
    onNavigateToSetPin: () -> Unit,
    onLogout: () -> Unit
) {
    val strings = currentStrings()
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currentUser = user ?: UserEntity()

    var showEditBusinessDialog by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = strings.profileTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = strings.manageBusiness,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Profile Avatar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(PrimaryPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.businessName.take(2).uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser.businessName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentUser.fullName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentUser.businessEmail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showEditBusinessDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("btn_edit_profile")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Profile",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Business Information Section
        SectionTitle("Business Info")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Phone", currentUser.businessPhone)
                InfoRow("Address", currentUser.businessAddress)
                InfoRow("Tax ID / GST", currentUser.businessTaxId)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language & Localization Section
        SectionTitle(strings.languageSection)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                val currentLangName = if (currentUser.language == "gu") "🇮🇳 ગુજરાતી (Gujarati)" else "🇬🇧 English"
                SettingActionRow(
                    icon = Icons.Filled.Language,
                    title = strings.appLanguageLabel,
                    subtitle = currentLangName,
                    onClick = { showLanguageDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preferences & Financial Settings
        SectionTitle(strings.regionDefaultsSection)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Currency Selector
                Box {
                    SettingActionRow(
                        icon = Icons.Filled.Paid,
                        title = strings.currencySymbolLabel,
                        subtitle = "Current: ${currentUser.currency}",
                        onClick = { showCurrencyMenu = true }
                    )

                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false }
                    ) {
                        CURRENCIES.forEach { (symbol, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.updateUserProfile(currentUser.copy(currency = symbol))
                                    showCurrencyMenu = false
                                }
                            )
                        }
                    }
                }

                // Default Tax Rate
                SettingActionRow(
                    icon = Icons.Filled.Percent,
                    title = "Default Tax Rate",
                    subtitle = "${currentUser.defaultTaxRate}% standard tax applied to invoices",
                    onClick = { showEditBusinessDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security & App Settings
        SectionTitle("Security & Preferences")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // PIN Lock Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(PrimaryPurpleLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("4-Digit PIN Protection", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Require PIN to open ledger", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Switch(
                        checked = currentUser.isPinEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.togglePinProtection(enabled)
                            Toast.makeText(context, if (enabled) "PIN lock enabled" else "PIN lock disabled", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = PrimaryPurpleLight)
                    )
                }

                // Change PIN
                SettingActionRow(
                    icon = Icons.Filled.Security,
                    title = "Change PIN Code",
                    subtitle = "Set or update your 4-digit code",
                    onClick = onNavigateToSetPin
                )

                // Dark Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.DarkMode, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Dark Theme", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Switch app appearance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Switch(
                        checked = currentUser.isDarkMode,
                        onCheckedChange = { enabled ->
                            viewModel.toggleDarkMode(enabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = Color(0xFFEEECFD))
                    )
                }

                // Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Color(0xFFEEECFD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Payment Notifications", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reminders for overdue invoices", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Switch(
                        checked = currentUser.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateUserProfile(currentUser.copy(notificationsEnabled = enabled))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple, checkedTrackColor = Color(0xFFEEECFD))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { showLogoutDialog = true }
                .testTag("btn_logout"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = ExpenseRed)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign Out of Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Edit Business Info Dialog
    if (showEditBusinessDialog) {
        EditBusinessDialog(
            user = currentUser,
            onDismiss = { showEditBusinessDialog = false },
            onSave = { updated ->
                viewModel.updateUserProfile(updated)
                showEditBusinessDialog = false
                Toast.makeText(context, "Business profile saved", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = strings.selectLanguageTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        AppLanguage.ENGLISH to "🇬🇧 English (English)",
                        AppLanguage.GUJARATI to "🇮🇳 ગુજરાતી (Gujarati)"
                    ).forEach { (lang, label) ->
                        val isSelected = (currentUser.language == lang.code) || (currentUser.language.isBlank() && lang == AppLanguage.ENGLISH)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setLanguage(lang.code)
                                    showLanguageDialog = false
                                    Toast.makeText(
                                        context,
                                        if (lang == AppLanguage.GUJARATI) "ભાષા ગુજરાતી પસંદ થઈ ગઈ" else "Language changed to English",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.GUJARATI) "આખી એપ ગુજરાતીમાં દેખાશે" else "Use the app in English",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(strings.logoutLabel) },
            text = { Text(strings.logoutConfirmMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(strings.logoutLabel, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(strings.cancelButton)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifEmpty { "Not specified" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(36.dp).background(Color(0xFFEEECFD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EditBusinessDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var businessName by remember { mutableStateOf(user.businessName) }
    var ownerName by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf(user.businessPhone) }
    var email by remember { mutableStateOf(user.businessEmail) }
    var address by remember { mutableStateOf(user.businessAddress) }
    var taxId by remember { mutableStateOf(user.businessTaxId) }
    var taxRate by remember { mutableStateOf(user.defaultTaxRate.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Business Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_biz_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner / Manager Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Billing Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taxId,
                        onValueChange = { taxId = it },
                        label = { Text("Tax ID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        label = { Text("Tax Rate %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val rate = taxRate.toDoubleOrNull() ?: user.defaultTaxRate
                        onSave(
                            user.copy(
                                businessName = businessName,
                                fullName = ownerName,
                                businessEmail = email,
                                businessPhone = phone,
                                businessAddress = address,
                                businessTaxId = taxId,
                                defaultTaxRate = rate
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_biz_profile_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
