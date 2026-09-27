package com.example.ui.screens.auth

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple

@Composable
fun SetPinScreen(
    currentPin: String,
    onPinSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: enter new PIN, 2: confirm PIN
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(PrimaryPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Security",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (step == 1) "Set 4-Digit Security PIN" else "Confirm Your PIN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (step == 1) "Create a 4-digit PIN to secure your ledger" else "Re-enter your 4-digit PIN to confirm",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // PIN Dots
                PinDotsIndicator(
                    pinLength = if (step == 1) pin.length else confirmPin.length,
                    hasError = errorMessage != null
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Numeric Keypad
            NumericKeypad(
                onDigitClick = { digit ->
                    errorMessage = null
                    if (step == 1) {
                        if (pin.length < 4) {
                            pin += digit
                            if (pin.length == 4) {
                                step = 2
                            }
                        }
                    } else {
                        if (confirmPin.length < 4) {
                            confirmPin += digit
                            if (confirmPin.length == 4) {
                                if (confirmPin == pin) {
                                    onPinSaved(pin)
                                } else {
                                    errorMessage = "PINs do not match. Try again."
                                    confirmPin = ""
                                    step = 1
                                    pin = ""
                                }
                            }
                        }
                    }
                },
                onBackspace = {
                    errorMessage = null
                    if (step == 1) {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    } else {
                        if (confirmPin.isNotEmpty()) {
                            confirmPin = confirmPin.dropLast(1)
                        } else {
                            step = 1
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EnterPinScreen(
    correctPin: String,
    onSuccess: () -> Unit,
    onForgotPin: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val effectiveCorrectPin = if (correctPin.isEmpty()) "1234" else correctPin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(PrimaryPurple.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Lock",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Enter Your PIN",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome back! Enter your 4-digit security PIN",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // PIN Indicator
                PinDotsIndicator(
                    pinLength = pin.length,
                    hasError = errorMessage != null
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Default Demo PIN: 1234",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Numeric Keypad & Forgot link
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumericKeypad(
                    onDigitClick = { digit ->
                        if (pin.length < 4) {
                            errorMessage = null
                            val newPin = pin + digit
                            pin = newPin
                            if (newPin.length == 4) {
                                if (newPin == effectiveCorrectPin) {
                                    onSuccess()
                                } else {
                                    errorMessage = "Incorrect PIN. Please try again."
                                    pin = ""
                                }
                            }
                        }
                    },
                    onBackspace = {
                        errorMessage = null
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Skip PIN (Enter Directly)",
                    color = PrimaryPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onSuccess() }
                        .padding(8.dp)
                        .testTag("pin_skip_button")
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PinDotsIndicator(pinLength: Int, hasError: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 4) {
            val isFilled = i < pinLength
            val dotColor = when {
                hasError -> ExpenseRed
                isFilled -> PrimaryPurple
                else -> Color.Transparent
            }
            val borderColor = when {
                hasError -> ExpenseRed
                isFilled -> PrimaryPurple
                else -> Color(0xFF475569)
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(dotColor, CircleShape)
                    .border(2.dp, borderColor, CircleShape)
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            Box(modifier = Modifier.size(72.dp))
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable { onBackspace() }
                                    .testTag("keypad_del"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                                    .clickable { onDigitClick(key) }
                                    .testTag("keypad_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
