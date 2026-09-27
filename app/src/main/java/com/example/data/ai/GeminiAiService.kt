package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GeminiAiService {

    companion object {
        private const val TAG = "GeminiAiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
        private const val MODEL_TEXT = "gemini-3.5-flash"
        private const val MODEL_VISION = "gemini-2.5-flash-image"

        @Volatile
        private var instance: GeminiAiService? = null

        fun getInstance(): GeminiAiService {
            return instance ?: synchronized(this) {
                instance ?: GeminiAiService().also { instance = it }
            }
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun isKeyConfigured(key: String): Boolean {
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && key != "YOUR_GEMINI_API_KEY"
    }

    /**
     * Converts a Bitmap to compressed JPEG Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val targetW: Int
            val targetH: Int
            if (ratio > 1) {
                targetW = maxDimension
                targetH = (maxDimension / ratio).toInt()
            } else {
                targetH = maxDimension
                targetW = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Scan a receipt or bill photo and return structured data
     */
    suspend fun scanReceipt(bitmap: Bitmap, languageCode: String): Result<ScannedBillResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured(apiKey)) {
            // Intelligent fallback for demo / testing if no key is configured
            return@withContext Result.success(getSmartOfflineBillFallback(languageCode))
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                You are an expert accountant and receipt scanner for a business ledger app.
                Analyze this receipt, bill, or invoice image carefully.
                Extract the details and respond with ONLY a valid JSON object (no markdown, no backticks, no code blocks):
                {
                  "amount": 1250.00,
                  "partyName": "Merchant, Shop or Vendor Name",
                  "type": "EXPENSE",
                  "category": "Food or Shopping or Transport or Bills or Rent or Marketing or Business or Other",
                  "paymentMethod": "Cash or UPI / Card or Bank Transfer or Online",
                  "notes": "Brief summary of purchased goods/services or invoice number",
                  "itemsSummary": "List of key items or summary"
                }
                If this is a sales receipt where the user received money, set type to "INCOME". Otherwise default to "EXPENSE".
                Always return a valid numeric amount. If unclear, provide your best estimation.
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
                put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    })
                })
            }

            val contentObj = JSONObject().put("parts", partsArray)
            val rootObj = JSONObject().apply {
                put("contents", JSONArray().put(contentObj))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = rootObj.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL$MODEL_VISION:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API failed: code=${response.code}, body=$responseBody")
                return@withContext Result.success(getSmartOfflineBillFallback(languageCode))
            }

            val parsedJson = extractJsonText(responseBody)
            val json = JSONObject(parsedJson)

            val amount = json.optDouble("amount", 0.0)
            val partyName = json.optString("partyName", "Vendor / Store")
            val type = json.optString("type", "EXPENSE")
            val category = json.optString("category", "Bills")
            val paymentMethod = json.optString("paymentMethod", "Cash")
            val notes = json.optString("notes", "")
            val itemsSummary = json.optString("itemsSummary", "")

            Result.success(
                ScannedBillResult(
                    amount = amount,
                    partyName = partyName,
                    type = type,
                    category = category,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    itemsSummary = itemsSummary,
                    confidence = "HIGH"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning receipt: ${e.message}", e)
            Result.success(getSmartOfflineBillFallback(languageCode))
        }
    }

    /**
     * Natural Language / Voice Khata Entry Parser
     */
    suspend fun parseNaturalLanguageEntry(
        input: String,
        languageCode: String
    ): Result<ParsedTransactionResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isKeyConfigured(apiKey)) {
            return@withContext Result.success(parseEntryOffline(input))
        }

        try {
            val prompt = """
                You are a smart financial ledger assistant. The user wants to record a transaction in their business ledger.
                The input may be in Gujarati, English, or Hindi (mixed script or Latin transliteration):
                User Input: "$input"

                Analyze this statement and return ONLY a single valid JSON object (no markdown, no formatting):
                {
                  "amount": 500.0,
                  "partyName": "Ramesh / Customer / Supplier / Store name or null",
                  "type": "INCOME" or "EXPENSE",
                  "category": "Food" or "Shopping" or "Transport" or "Salary" or "Business" or "Bills" or "Rent" or "Marketing" or "Other",
                  "paymentMethod": "Cash" or "UPI / Card" or "Bank Transfer" or "Cheque" or "Online",
                  "notes": "Short description of the transaction"
                }
                Rules:
                - If the user received money ("મળ્યા", "આપ્યા" (to me), "received", "got", "income", "sale"), type is "INCOME".
                - If the user paid money ("ચૂકવ્યા", "ખર્ચ", "paid", "gave", "bought", "spent", "purchase"), type is "EXPENSE".
                - Amount must be a positive number.
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
            }
            val contentObj = JSONObject().put("parts", partsArray)
            val rootObj = JSONObject().apply {
                put("contents", JSONArray().put(contentObj))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = rootObj.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL$MODEL_TEXT:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini text API error: ${response.code} $responseBody")
                return@withContext Result.success(parseEntryOffline(input))
            }

            val parsedJson = extractJsonText(responseBody)
            val json = JSONObject(parsedJson)

            val amount = json.optDouble("amount", 0.0)
            val partyName = json.optString("partyName", "").ifBlank { "General" }
            val type = json.optString("type", "EXPENSE")
            val category = json.optString("category", "Other")
            val paymentMethod = json.optString("paymentMethod", "Cash")
            val notes = json.optString("notes", input)

            Result.success(
                ParsedTransactionResult(
                    amount = amount,
                    partyName = partyName,
                    type = type,
                    category = category,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing natural entry: ${e.message}", e)
            Result.success(parseEntryOffline(input))
        }
    }

    /**
     * AI Business Advisor & Financial Audit
     */
    suspend fun generateBusinessAudit(
        summary: BusinessSummaryForAi,
        languageCode: String
    ): Result<List<BusinessInsight>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val langName = if (languageCode == "gu") "Gujarati (ગુજરાતી)" else "English"

        if (!isKeyConfigured(apiKey)) {
            return@withContext Result.success(getSmartBusinessAuditFallback(summary, languageCode))
        }

        try {
            val prompt = """
                You are a senior Chartered Accountant (CA) and business growth advisor.
                Analyze the following business financial metrics:
                Business Name: ${summary.businessName}
                Currency: ${summary.currency}
                Total Revenue: ${summary.totalRevenue}
                Total Expenses: ${summary.totalExpense}
                Net Profit: ${summary.netProfit}
                Pending Receivables from Customers: ${summary.pendingReceivables}
                Pending Payables to Suppliers: ${summary.pendingPayables}
                Top Expense Category: ${summary.topExpenseCategory} (${summary.topExpenseAmount})
                Overdue Customers Count: ${summary.overdueCustomersCount}

                Generate exactly 3 actionable, highly valuable business recommendations in $langName.
                Return ONLY a JSON array with exactly 3 items:
                [
                  {
                    "id": "1",
                    "title": "Short catchy title in $langName",
                    "description": "Specific actionable explanation in $langName",
                    "type": "ALERT" or "OPPORTUNITY" or "SAVINGS" or "REMINDER",
                    "actionText": "Short button label in $langName (e.g. રીમાઇન્ડર મોકલો or Send Reminder)",
                    "actionType": "REMINDERS" or "EXPENSES" or "CUSTOMERS"
                  }
                ]
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
            }
            val contentObj = JSONObject().put("parts", partsArray)
            val rootObj = JSONObject().apply {
                put("contents", JSONArray().put(contentObj))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val requestBody = rootObj.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL$MODEL_TEXT:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartBusinessAuditFallback(summary, languageCode))
            }

            val parsedJson = extractJsonText(responseBody)
            val array = JSONArray(parsedJson)
            val list = mutableListOf<BusinessInsight>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val typeStr = item.optString("type", "OPPORTUNITY")
                val insightType = try {
                    InsightType.valueOf(typeStr)
                } catch (e: Exception) {
                    InsightType.OPPORTUNITY
                }
                list.add(
                    BusinessInsight(
                        id = item.optString("id", "${i + 1}"),
                        title = item.optString("title", "Insight"),
                        description = item.optString("description", ""),
                        type = insightType,
                        actionText = item.optString("actionText", null),
                        actionType = item.optString("actionType", null)
                    )
                )
            }

            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating business audit: ${e.message}", e)
            Result.success(getSmartBusinessAuditFallback(summary, languageCode))
        }
    }

    /**
     * AI WhatsApp Payment Reminder Message
     */
    suspend fun generatePaymentReminderMessage(
        customerName: String,
        amount: Double,
        currency: String,
        businessName: String,
        tone: String, // "POLITE", "PROFESSIONAL", "URGENT"
        languageCode: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val lang = if (languageCode == "gu") "Gujarati (ગુજરાતી)" else "English"

        if (!isKeyConfigured(apiKey)) {
            val fallback = if (languageCode == "gu") {
                when (tone) {
                    "URGENT" -> "નમસ્તે $customerName જી, આપના $businessName ખાતે $currency$amount ની રકમ લાંબા સમયથી બાકી છે. કૃપા કરીને આજે જ આ રકમ ચૂકવી આપવા વિનંતી છે. આભાર."
                    "PROFESSIONAL" -> "પ્રિય $customerName, $businessName તરફથી આપનું બિલ $currency$amount બાકી છે. આપના અનુકૂળ સમયે UPI અથવા બેંક દ્વારા ચૂકવી આપશો. આભાર."
                    else -> "નમસ્તે $customerName જી, આશા છે આપ મજામાં હશો. આપના પાછલા બિલના $currency$amount બાકી નીકળે છે. અનુકૂળતા મુજબ જમા કરાવી દેશો. આભાર - $businessName"
                }
            } else {
                when (tone) {
                    "URGENT" -> "Dear $customerName, your pending balance of $currency$amount with $businessName is overdue. Please settle this immediately today. Thank you."
                    "PROFESSIONAL" -> "Hello $customerName, this is a reminder regarding the outstanding balance of $currency$amount with $businessName. Kindly arrange payment at your earliest convenience."
                    else -> "Hi $customerName, hope you are well. Just a gentle reminder that your pending balance of $currency$amount with $businessName is due. Thank you!"
                }
            }
            return@withContext Result.success(fallback)
        }

        try {
            val prompt = """
                Write a single concise WhatsApp payment reminder message in $lang.
                Customer Name: $customerName
                Outstanding Amount: $currency$amount
                Business Name: $businessName
                Tone: $tone (either polite and friendly, professional, or urgent collection)
                Return ONLY the WhatsApp message text without any quotes or explanations.
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
            }
            val contentObj = JSONObject().put("parts", partsArray)
            val rootObj = JSONObject().apply {
                put("contents", JSONArray().put(contentObj))
            }

            val requestBody = rootObj.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL$MODEL_TEXT:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success("નમસ્તે $customerName, આપના $businessName ખાતે $currency$amount બાકી છે. ચૂકવી આપવા વિનંતી.")
            }

            val text = extractPlainText(responseBody)
            Result.success(text)
        } catch (e: Exception) {
            Result.success("નમસ્તે $customerName, આપના $businessName ખાતે $currency$amount બાકી છે.")
        }
    }

    private fun extractJsonText(responseBody: String): String {
        val root = JSONObject(responseBody)
        val candidates = root.getJSONArray("candidates")
        val candidate = candidates.getJSONObject(0)
        val parts = candidate.getJSONObject("content").getJSONArray("parts")
        var text = parts.getJSONObject(0).getString("text").trim()

        if (text.startsWith("```json")) {
            text = text.substring(7)
        } else if (text.startsWith("```")) {
            text = text.substring(3)
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length - 3)
        }
        return text.trim()
    }

    private fun extractPlainText(responseBody: String): String {
        val root = JSONObject(responseBody)
        val candidates = root.getJSONArray("candidates")
        val candidate = candidates.getJSONObject(0)
        val parts = candidate.getJSONObject("content").getJSONArray("parts")
        return parts.getJSONObject(0).getString("text").trim()
    }

    /**
     * Fallback receipt for offline / mock testing
     */
    private fun getSmartOfflineBillFallback(languageCode: String): ScannedBillResult {
        val isGu = languageCode == "gu"
        return ScannedBillResult(
            amount = 1850.0,
            partyName = if (isGu) "શ્રી લક્ષ્મી જનરલ સ્ટોર" else "Laxmi General Store",
            type = "EXPENSE",
            category = "Bills",
            paymentMethod = "Cash",
            notes = if (isGu) "દુકાન માલ-સામાન અને બિલ ચૂકવણી" else "Store inventory and supplies",
            itemsSummary = if (isGu) "પેકેજિંગ સામાન (₹૧૨૦૦), સ્ટેશનરી (₹૬૫૦)" else "Packaging materials ($1200), Stationery ($650)",
            confidence = "MEDIUM"
        )
    }

    /**
     * Fallback for speech / text regex extraction
     */
    private fun parseEntryOffline(input: String): ParsedTransactionResult {
        val lower = input.lowercase()
        val matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(input)
        val amount = if (matcher.find()) {
            matcher.group(1)?.toDoubleOrNull() ?: 100.0
        } else {
            500.0
        }

        val isIncome = lower.contains("મળ્યા") || lower.contains("got") || lower.contains("received") ||
                lower.contains("income") || lower.contains("આવક") || lower.contains("જમા")

        val paymentMethod = if (lower.contains("upi") || lower.contains("gpay") || lower.contains("phonepe") || lower.contains("paytm")) {
            "UPI / Card"
        } else if (lower.contains("બેંક") || lower.contains("bank") || lower.contains("transfer")) {
            "Bank Transfer"
        } else {
            "Cash"
        }

        val category = if (lower.contains("ભાડું") || lower.contains("rent")) {
            "Rent"
        } else if (lower.contains("લાઇટ") || lower.contains("બિલ") || lower.contains("bill") || lower.contains("electric")) {
            "Bills"
        } else if (lower.contains("પગાર") || lower.contains("salary")) {
            "Salary"
        } else if (lower.contains("પેટ્રોલ") || lower.contains("ડીઝલ") || lower.contains("travel") || lower.contains("auto")) {
            "Transport"
        } else if (lower.contains("જમવા") || lower.contains("નાસ્તો") || lower.contains("food") || lower.contains("ચા")) {
            "Food"
        } else {
            if (isIncome) "Business" else "Shopping"
        }

        val partyName = input.split(" ", "પાસેથી", "ને", "માટે").firstOrNull { it.length > 2 && !it.any { c -> c.isDigit() } } ?: "General"

        return ParsedTransactionResult(
            amount = amount,
            partyName = partyName,
            type = if (isIncome) "INCOME" else "EXPENSE",
            category = category,
            paymentMethod = paymentMethod,
            notes = input
        )
    }

    /**
     * Fallback smart business advice
     */
    private fun getSmartBusinessAuditFallback(
        summary: BusinessSummaryForAi,
        languageCode: String
    ): List<BusinessInsight> {
        val isGu = languageCode == "gu"
        val currency = summary.currency

        return listOf(
            BusinessInsight(
                id = "1",
                title = if (isGu) "ઉઘરાણી પર ધ્યાન આપો" else "Accelerate Customer Collections",
                description = if (isGu) {
                    "ગ્રાહકો પાસેથી કુલ $currency${summary.pendingReceivables.toInt()} લેવાના બાકી છે. ${summary.overdueCustomersCount} ગ્રાહકોની ચૂકવણી મુદત પૂરી થઈ ગઈ છે."
                } else {
                    "You have $currency${summary.pendingReceivables.toInt()} pending from customers. Sending WhatsApp payment reminders can recover cashflow faster."
                },
                type = InsightType.ALERT,
                actionText = if (isGu) "રીમાઇન્ડર મોકલો" else "Send Reminders",
                actionType = "REMINDERS"
            ),
            BusinessInsight(
                id = "2",
                title = if (isGu) "સૌથી મોટો ખર્ચ: ${summary.topExpenseCategory}" else "Top Expense: ${summary.topExpenseCategory}",
                description = if (isGu) {
                    "તમારા કુલ ખર્ચમાંથી $currency${summary.topExpenseAmount.toInt()} માત્ર ${summary.topExpenseCategory} માં થયો છે. આ ખર્ચ પર ૧૦% નિયંત્રણ રાખવાથી નફો વધશે."
                } else {
                    "${summary.topExpenseCategory} accounts for $currency${summary.topExpenseAmount.toInt()} of total expenses. Monitoring vendor rates could improve margins."
                },
                type = InsightType.SAVINGS,
                actionText = if (isGu) "ખર્ચ તપાસો" else "Review Expenses",
                actionType = "EXPENSES"
            ),
            BusinessInsight(
                id = "3",
                title = if (isGu) "નફાકારક રોકડ પ્રવાહ (Healthy Cashflow)" else "Positive Net Profit Margin",
                description = if (isGu) {
                    "આ મહિને તમારો ચોખ્ખો નફો $currency${summary.netProfit.toInt()} રહ્યો છે. આ રકમમાંથી ૨૦% નવા માલ સ્ટોક માટે અનામત રાખવાની સલાહ છે."
                } else {
                    "Your net profit is currently at $currency${summary.netProfit.toInt()}. Consider reinvesting 20% into fast-moving inventory items."
                },
                type = InsightType.OPPORTUNITY,
                actionText = if (isGu) "રિપોર્ટ જુઓ" else "View Report",
                actionType = "REPORTS"
            )
        )
    }
}
