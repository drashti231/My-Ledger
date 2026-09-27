package com.example.ui.screens.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.ScannedBillResult
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.viewmodel.LedgerViewModel
import com.example.util.currentStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiReceiptScannerDialog(
    viewModel: LedgerViewModel,
    onDismiss: () -> Unit
) {
    val strings = currentStrings()
    val context = LocalContext.current

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<ScannedBillResult?>(null) }

    // Editable form state after scan
    var amountText by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") }
    var category by remember { mutableStateOf("Bills") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                isAnalyzing = true
                viewModel.scanBillWithAi(
                    bitmap = bitmap,
                    onResult = { result ->
                        isAnalyzing = false
                        scannedResult = result
                        amountText = if (result.amount > 0) result.amount.toString() else ""
                        partyName = result.partyName
                        transactionType = result.type
                        category = result.category
                        paymentMethod = result.paymentMethod
                        notes = "${result.notes} ${result.itemsSummary}".trim()
                    },
                    onError = {
                        isAnalyzing = false
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                isAnalyzing = false
                Toast.makeText(context, "Could not open image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            isAnalyzing = true
            viewModel.scanBillWithAi(
                bitmap = bitmap,
                onResult = { result ->
                    isAnalyzing = false
                    scannedResult = result
                    amountText = if (result.amount > 0) result.amount.toString() else ""
                    partyName = result.partyName
                    transactionType = result.type
                    category = result.category
                    paymentMethod = result.paymentMethod
                    notes = "${result.notes} ${result.itemsSummary}".trim()
                },
                onError = {
                    isAnalyzing = false
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            )
        }
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
                .testTag("ai_receipt_scanner_dialog"),
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.aiScanBill,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Powered by Gemini 2.5 Flash",
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

                Spacer(modifier = Modifier.height(16.dp))

                // Image Capture / Selection Section
                if (selectedBitmap == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.04f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.aiSelectPhoto,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.aiScanningBillSubtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        galleryLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f).testTag("ai_pick_gallery_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery", fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            cameraLauncher.launch()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Camera not available", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("ai_take_photo_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Preview Image Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Bill",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = strings.aiScanningBillTitle,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            // Retake / Change Button overlay
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedBitmap = null
                                        scannedResult = null
                                    },
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Change", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Extracted Details Form
                if (scannedResult != null && !isAnalyzing) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = IncomeGreen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.aiExtractedDetails,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Type Selector (Income / Expense)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { transactionType = "EXPENSE" },
                            color = if (transactionType == "EXPENSE") ExpenseRed else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = strings.expense,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transactionType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { transactionType = "INCOME" },
                            color = if (transactionType == "INCOME") IncomeGreen else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = strings.income,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transactionType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(strings.amountLabel) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_scanned_amount_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Merchant / Party Name
                    OutlinedTextField(
                        value = partyName,
                        onValueChange = { partyName = it },
                        label = { Text(strings.partyNameLabel) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_scanned_party_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Chips
                    Text(
                        text = strings.categoryLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Bills", "Food", "Shopping", "Transport", "Rent", "Salary", "Business", "Other").forEach { cat ->
                            FilterChip(
                                selected = category.equals(cat, ignoreCase = true),
                                onClick = { category = cat },
                                label = { Text(strings.category(cat), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryPurple.copy(alpha = 0.15f),
                                    selectedLabelColor = PrimaryPurple
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.notesLabel) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_scanned_notes_input"),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                            if (parsedAmount <= 0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalResult = scannedResult!!.copy(
                                amount = parsedAmount,
                                partyName = partyName.ifBlank { "General Store" },
                                type = transactionType,
                                category = category,
                                paymentMethod = paymentMethod,
                                notes = notes
                            )
                            viewModel.saveScannedBillTransaction(finalResult) {
                                Toast.makeText(context, strings.aiSuccessEntrySaved, Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("ai_save_scanned_bill_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.aiSaveToLedger, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
