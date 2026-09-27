package com.example.ui.screens.ai

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.ParsedTransactionResult
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.currentStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiVoiceEntryDialog(
    viewModel: LedgerViewModel,
    onDismiss: () -> Unit
) {
    val strings = currentStrings()
    val context = LocalContext.current
    val currentLanguage = LocalAppLanguage.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currency = user?.currency ?: "$"

    var voicePromptText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedTransactionResult?>(null) }

    // Speech-to-text Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                voicePromptText = spokenText
                // Auto trigger AI parsing
                isProcessing = true
                viewModel.parseNaturalLanguageWithAi(
                    input = spokenText,
                    onResult = { parsed ->
                        isProcessing = false
                        parsedResult = parsed
                    },
                    onError = {
                        isProcessing = false
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    val quickExamples = if (currentLanguage == AppLanguage.GUJARATI) {
        listOf(
            "રમેશભાઈ પાસેથી ૫૦૦ રોકડા મળ્યા",
            "દુકાન ભાડું ૧૫૦૦૦ ચૂકવ્યું",
            "લાઇટબિલ ૧૨૫૦ UPI થી ભર્યું",
            "મહેશભાઈને ૨૦૦૦ નો માલ આપ્યો"
        )
    } else {
        listOf(
            "Received 500 from Ramesh cash",
            "Paid shop rent 15000",
            "Electric bill 1250 via UPI",
            "Sold goods to Mahesh for 2000"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("ai_voice_entry_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryPurple.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.aiVoiceDialogTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Speak or type in Gujarati / English",
                                fontSize = 11.sp,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.cancelButton,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Mic Tap Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(PrimaryPurple, CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, strings.aiVoiceListening)
                                            // Prefer current language (Gujarati or English)
                                            if (currentLanguage == AppLanguage.GUJARATI) {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "gu-IN")
                                            } else {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                                            }
                                        }
                                        speechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Voice recognition not supported on this device. You can type below.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                .testTag("ai_mic_record_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Tap to Speak",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.aiVoiceListening,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Field for manual editing or typing
                OutlinedTextField(
                    value = voicePromptText,
                    onValueChange = { voicePromptText = it },
                    placeholder = { Text(strings.aiVoiceHint, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_voice_text_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple
                    ),
                    trailingIcon = {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (voicePromptText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    isProcessing = true
                                    viewModel.parseNaturalLanguageWithAi(
                                        input = voicePromptText,
                                        onResult = { parsed ->
                                            isProcessing = false
                                            parsedResult = parsed
                                        },
                                        onError = {
                                            isProcessing = false
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                modifier = Modifier.testTag("ai_parse_text_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Parse", tint = PrimaryPurple)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Example Chips
                Text(
                    text = "Quick Examples (ટેપ કરીને ચકાસો):",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickExamples.forEach { ex ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    voicePromptText = ex
                                    isProcessing = true
                                    viewModel.parseNaturalLanguageWithAi(
                                        input = ex,
                                        onResult = { parsed ->
                                            isProcessing = false
                                            parsedResult = parsed
                                        },
                                        onError = {
                                            isProcessing = false
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = ex,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Parsed Result Card
                AnimatedVisibility(visible = parsedResult != null) {
                    parsedResult?.let { result ->
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(strings.aiExtractedDetails, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                                        }

                                        // Income / Expense Badge
                                        Surface(
                                            color = if (result.type == "INCOME") IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (result.type == "INCOME") strings.income else strings.expense,
                                                color = if (result.type == "INCOME") IncomeGreen else ExpenseRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Big Amount Display
                                    Text(
                                        text = "$currency${result.amount}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.type == "INCOME") IncomeGreen else ExpenseRed
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Party / Person: ${result.partyName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Category: ${strings.category(result.category)}  •  Method: ${result.paymentMethod}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (result.notes.isNotBlank()) {
                                        Text(
                                            text = "Note: ${result.notes}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // One tap save
                            Button(
                                onClick = {
                                    viewModel.saveParsedAiTransaction(result) {
                                        Toast.makeText(context, strings.aiSuccessEntrySaved, Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("ai_save_voice_entry_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.aiSaveToLedger, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
