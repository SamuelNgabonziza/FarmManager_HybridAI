package com.samuel.farmos.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.HealthCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FarmShareUtils {

    fun sharePdf(context: Context, pdfUri: Uri, title: String = "Share Animal Passport") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun shareWhatsAppSummary(
        context: Context,
        farmName: String,
        animal: Animal,
        healthHistory: List<HealthCase>
    ) {
        val sb = StringBuilder()
        sb.appendLine("📋 *FarmOS Animal Health Summary*")
        sb.appendLine("🏡 Farm: ${farmName.ifBlank { "My Farm" }}")
        sb.appendLine("🏷️ Tag: ${animal.tagNumber}")
        if (animal.name.isNotBlank()) sb.appendLine("📛 Name: ${animal.name}")
        sb.appendLine("🐄 Species: ${animal.species.name} (${animal.breed.ifBlank { "Unspecified" }})")
        sb.appendLine("⚧ Gender: ${animal.gender.name}")
        if (animal.weightKg != null) sb.appendLine("⚖️ Weight: ${animal.weightKg} kg")

        if (healthHistory.isNotEmpty()) {
            sb.appendLine("\n🩺 *Recent Health Events (${healthHistory.size}):*")
            healthHistory.take(3).forEach { case ->
                val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(case.createdDateMillis))
                sb.appendLine("• $dateStr: ${case.mainComplaint} [${case.status.name}]")
            }
        } else {
            sb.appendLine("\n✅ *Health Status:* Healthy, no active cases.")
        }

        sb.appendLine("\n_Shared from FarmOS Mobile_")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        val chooser = Intent.createChooser(intent, "Share Animal Summary via WhatsApp / Message").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
