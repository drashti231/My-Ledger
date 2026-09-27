package com.example.ui.screens.ai

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.currentStrings

@Composable
fun AiWhatsAppReminderDialog(
    viewModel: LedgerViewModel,
    customerName: String,
    customerPhone: String,
    amount: Double,
    onDismiss: () -> Unit
) {
    val strings = currentStrings()
    val context = LocalContext.current

    var selectedTone by remember { mutableStateOf("POLITE") } // POLITE, PROFESSIONAL, URGENT
    var messageText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTone) {
        isGenerating = true
        viewModel.generateWhatsAppReminderWithAi(
            customerName = customerName,
            amount = amount,
            tone = selectedTone,
            onResult = { generated ->
                isGenerating = false
                messageText = generated
            }
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
                .testTag("ai_whatsapp_reminder_dialog"),
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
                                .background(IncomeGreen.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.aiWhatsAppReminderTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Personalized WhatsApp reminder with Gemini",
                                fontSize = 11.sp,
                                color = IncomeGreen,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Customer info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (customerPhone.isNotBlank()) {
                                Text(
                                    text = customerPhone,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            color = PrimaryPurple.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "₹$amount",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tone selection chips
                Text(
                    text = "Message Tone (મેસેજનો ભાવ):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "POLITE" to strings.aiReminderTonePolite,
                        "PROFESSIONAL" to strings.aiReminderToneProfessional,
                        "URGENT" to strings.aiReminderToneUrgent
                    ).forEach { (toneKey, toneLabel) ->
                        FilterChip(
                            selected = selectedTone == toneKey,
                            onClick = { selectedTone = toneKey },
                            label = { Text(toneLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IncomeGreen.copy(alpha = 0.15f),
                                selectedLabelColor = IncomeGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Message Text box
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IncomeGreen)
                    }
                } else {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .testTag("ai_whatsapp_message_input"),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 6
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Send WhatsApp Button
                Button(
                    onClick = {
                        val cleanPhone = customerPhone.replace(Regex("[^0-9]"), "")
                        val targetPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                        try {
                            val uri = if (targetPhone.isNotBlank()) {
                                Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(messageText)}")
                            } else {
                                Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(messageText)}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                            onDismiss()
                        } catch (e: Exception) {
                            // Fallback to generic share
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, messageText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Reminder"))
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("ai_send_whatsapp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.aiSendWhatsApp, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
