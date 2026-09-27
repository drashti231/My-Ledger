package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.UserEntity
import java.io.File
import java.io.FileOutputStream

object PdfInvoiceGenerator {

    fun generateInvoicePdf(
        context: Context,
        user: UserEntity,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (pts)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val primaryColor = 0xFF6C4FF5.toInt()
        val darkNavy = 0xFF172554.toInt()
        val textSecondary = 0xFF64748B.toInt()
        val borderLine = 0xFFE2E8F0.toInt()
        val bgLight = 0xFFF8F9FF.toInt()

        // 1. Header Banner / Background bar
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Business Name on Banner
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(user.businessName.ifEmpty { "MyLedger Business" }, 36f, 48f, paint)

        // Subtitle / Tagline
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = 0xFFE0E7FF.toInt()
        canvas.drawText("TAX ID: ${user.businessTaxId} | ${user.businessPhone}", 36f, 68f, paint)

        // Large "INVOICE" Title on the right of banner
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("INVOICE", 559f, 54f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 2. Business & Invoice Info Row
        var y = 120f
        paint.color = darkNavy
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ISSUED BY:", 36f, y, paint)
        canvas.drawText("INVOICE DETAILS:", 360f, y, paint)

        y += 18f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = textSecondary
        canvas.drawText(user.businessAddress, 36f, y, paint)
        canvas.drawText("Invoice #: ${invoice.invoiceNumber}", 360f, y, paint)

        y += 15f
        canvas.drawText("Email: ${user.businessEmail}", 36f, y, paint)
        canvas.drawText("Date: ${DateUtils.formatDate(invoice.invoiceDateMillis)}", 360f, y, paint)

        y += 15f
        canvas.drawText("Phone: ${user.businessPhone}", 36f, y, paint)
        canvas.drawText("Due Date: ${DateUtils.formatDate(invoice.dueDateMillis)}", 360f, y, paint)

        y += 15f
        // Status Badge
        val statusText = "Status: ${invoice.status}"
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        when (invoice.status) {
            "PAID" -> paint.color = 0xFF22C55E.toInt()
            "OVERDUE" -> paint.color = 0xFFEF4444.toInt()
            else -> paint.color = 0xFFF59E0B.toInt()
        }
        canvas.drawText(statusText, 360f, y, paint)

        // 3. Customer "BILL TO" Box
        y += 25f
        paint.color = bgLight
        canvas.drawRoundRect(36f, y, 559f, y + 65f, 8f, 8f, paint)

        paint.color = darkNavy
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("BILLED TO:", 48f, y + 20f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = textSecondary
        canvas.drawText("${invoice.customerName}   |   ${invoice.customerEmail}   |   ${invoice.customerPhone}", 48f, y + 36f, paint)
        canvas.drawText(invoice.customerAddress.ifEmpty { "Customer Address on record" }, 48f, y + 52f, paint)

        // 4. Line Items Table Header
        y += 85f
        paint.color = primaryColor
        canvas.drawRect(36f, y, 559f, y + 24f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("ITEM / DESCRIPTION", 46f, y + 16f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("QTY", 370f, y + 16f, paint)
        canvas.drawText("UNIT PRICE", 460f, y + 16f, paint)
        canvas.drawText("TOTAL", 545f, y + 16f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 5. Line Items Rows
        y += 24f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f

        items.forEachIndexed { index, item ->
            // alternating row background
            if (index % 2 == 1) {
                paint.color = bgLight
                canvas.drawRect(36f, y, 559f, y + 22f, paint)
            }
            paint.color = darkNavy
            canvas.drawText(item.productName, 46f, y + 15f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(item.quantity.toString(), 370f, y + 15f, paint)
            canvas.drawText(FormatUtils.formatCurrency(item.unitPrice, user.currency), 460f, y + 15f, paint)
            canvas.drawText(FormatUtils.formatCurrency(item.lineTotal, user.currency), 545f, y + 15f, paint)
            paint.textAlign = Paint.Align.LEFT

            y += 22f
        }

        // Table bottom border
        paint.color = borderLine
        paint.strokeWidth = 1f
        canvas.drawLine(36f, y, 559f, y, paint)

        // 6. Summary Calculation Block (Right Aligned)
        y += 18f
        val summaryLabelX = 380f
        val summaryValueX = 545f

        fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, color: Int = darkNavy) {
            paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            paint.textSize = if (isBold) 11f else 9.5f
            paint.color = if (isBold) color else textSecondary
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, summaryLabelX, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, summaryValueX, y, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 18f
        }

        drawSummaryRow("Subtotal:", FormatUtils.formatCurrency(invoice.subtotal, user.currency))
        if (invoice.discount > 0) {
            drawSummaryRow("Discount:", "- " + FormatUtils.formatCurrency(invoice.discount, user.currency))
        }
        if (invoice.taxRate > 0) {
            val taxAmount = (invoice.subtotal - invoice.discount) * (invoice.taxRate / 100.0)
            drawSummaryRow("Tax (${invoice.taxRate}%):", FormatUtils.formatCurrency(taxAmount, user.currency))
        }

        // Divider
        paint.color = borderLine
        canvas.drawLine(360f, y - 5f, 559f, y - 5f, paint)

        drawSummaryRow("Grand Total:", FormatUtils.formatCurrency(invoice.total, user.currency), isBold = true, color = primaryColor)

        if (invoice.paidAmount > 0) {
            drawSummaryRow("Paid Amount:", FormatUtils.formatCurrency(invoice.paidAmount, user.currency))
            val balanceDue = (invoice.total - invoice.paidAmount).coerceAtLeast(0.0)
            drawSummaryRow("Balance Due:", FormatUtils.formatCurrency(balanceDue, user.currency), isBold = true, color = if (balanceDue > 0) 0xFFEF4444.toInt() else 0xFF22C55E.toInt())
        }

        // 7. Terms & Notes on Left side of summary
        val notesY = y - 80f
        paint.color = darkNavy
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("NOTES / PAYMENT TERMS:", 36f, notesY, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f
        paint.color = textSecondary
        val noteText = invoice.notes.ifEmpty { "Payment is requested within the due date specified. Thank you for your partnership." }
        canvas.drawText(noteText, 36f, notesY + 16f, paint)
        canvas.drawText("Payment via Wire, ACH or Bank Transfer referencing ${invoice.invoiceNumber}.", 36f, notesY + 30f, paint)

        // 8. Footer
        paint.color = 0xFFF1F5F9.toInt()
        canvas.drawRect(0f, 792f, 595f, 842f, paint)

        paint.color = textSecondary
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Generated by MyLedger – Smart Business Manager", 297f, 814f, paint)
        canvas.drawText("www.myledger.app  •  Confidential & Proprietary", 297f, 828f, paint)
        paint.textAlign = Paint.Align.LEFT

        document.finishPage(page)

        // Save PDF to cache dir
        val invoicesDir = File(context.cacheDir, "invoices")
        if (!invoicesDir.exists()) invoicesDir.mkdirs()
        val file = File(invoicesDir, "${invoice.invoiceNumber}.pdf")

        return try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.close()
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    fun shareInvoicePdf(context: Context, pdfFile: File, invoiceNumber: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice $invoiceNumber")
            putExtra(Intent.EXTRA_TEXT, "Please find attached invoice $invoiceNumber.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
    }
}
