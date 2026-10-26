package com.samuel.farmos.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.HealthCase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FarmPdfExporter {

    fun generateAnimalPassportPdf(
        context: Context,
        farmName: String,
        animal: Animal,
        healthHistory: List<HealthCase>
    ): Uri? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(38, 115, 58) // FarmGreen
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(94, 111, 98)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(38, 115, 58)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(27, 36, 29)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(27, 36, 29)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val boxPaint = Paint().apply {
            color = Color.rgb(240, 247, 240)
            style = Paint.Style.FILL
        }

        val linePaint = Paint().apply {
            color = Color.rgb(216, 230, 218)
            strokeWidth = 1f
        }

        var y = 50f

        // Header
        canvas.drawText("FarmOS - Livestock Passport & Medical Record", 40f, y, titlePaint)
        y += 18f
        canvas.drawText("Farm: ${farmName.ifBlank { "My Farm" }} • Generated on ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", 40f, y, subtitlePaint)
        y += 20f

        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 24f

        // Animal Profile Box
        canvas.drawRoundRect(40f, y, 555f, y + 130f, 12f, 12f, boxPaint)

        val col1 = 55f
        val col2 = 300f
        var profileY = y + 25f

        canvas.drawText("Tag / ID:", col1, profileY, boldTextPaint)
        canvas.drawText(animal.tagNumber.ifBlank { "N/A" }, col1 + 80f, profileY, textPaint)

        canvas.drawText("Species:", col2, profileY, boldTextPaint)
        canvas.drawText(animal.species.name, col2 + 80f, profileY, textPaint)

        profileY += 22f
        canvas.drawText("Name:", col1, profileY, boldTextPaint)
        canvas.drawText(animal.name.ifBlank { "None" }, col1 + 80f, profileY, textPaint)

        canvas.drawText("Breed:", col2, profileY, boldTextPaint)
        canvas.drawText(animal.breed.ifBlank { "Unspecified" }, col2 + 80f, profileY, textPaint)

        profileY += 22f
        canvas.drawText("Gender:", col1, profileY, boldTextPaint)
        canvas.drawText(animal.gender.name, col1 + 80f, profileY, textPaint)

        canvas.drawText("Status:", col2, profileY, boldTextPaint)
        canvas.drawText(animal.status.name, col2 + 80f, profileY, textPaint)

        profileY += 22f
        canvas.drawText("Birth Date:", col1, profileY, boldTextPaint)
        val birthStr = animal.birthDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Unknown"
        canvas.drawText(birthStr, col1 + 80f, profileY, textPaint)

        canvas.drawText("Weight:", col2, profileY, boldTextPaint)
        canvas.drawText(if (animal.weightKg != null) "${animal.weightKg} kg" else "Not weighed", col2 + 80f, profileY, textPaint)

        y += 150f

        // Medical & Veterinary History Section
        canvas.drawText("Veterinary & Health History (${healthHistory.size} records)", 40f, y, sectionPaint)
        y += 16f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        if (healthHistory.isEmpty()) {
            canvas.drawText("No health cases or illnesses reported for this animal. All clear.", 40f, y, subtitlePaint)
        } else {
            // Table Header
            canvas.drawText("Date", 45f, y, boldTextPaint)
            canvas.drawText("Complaint / Signs", 140f, y, boldTextPaint)
            canvas.drawText("Status", 450f, y, boldTextPaint)
            y += 14f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 18f

            healthHistory.take(15).forEach { case ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(case.createdDateMillis))
                val signsStr = (listOf(case.mainComplaint) + case.clinicalSigns.map { it.name.replace("_", " ") })
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString(", ")

                canvas.drawText(dateStr, 45f, y, textPaint)
                canvas.drawText(signsStr.take(45), 140f, y, textPaint)
                canvas.drawText(case.status.name, 450f, y, textPaint)

                y += 20f
                if (y > 780f) return@forEach
            }
        }

        // Footer
        val footerY = 810f
        canvas.drawLine(40f, footerY - 10f, 555f, footerY - 10f, linePaint)
        canvas.drawText("FarmOS Farm Management • Verified Digital Livestock Record", 40f, footerY, subtitlePaint)

        document.finishPage(page)

        // Write to cache file
        return try {
            val file = File(context.cacheDir, "animal_${animal.tagNumber}_passport.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            document.close()
            null
        }
    }
}
