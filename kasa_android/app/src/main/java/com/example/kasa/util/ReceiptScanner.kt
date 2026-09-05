package com.example.kasa.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ScannedReceipt(
    val totalAmount: Double?,
    val merchantName: String?,
    val date: Date?,
    val suggestedCategoryType: String?, // "provisions", "health", "transport", "restaurant", "bills", "general"
    val rawText: String,
    val lineCount: Int
)

object ReceiptScanner {

    fun scanReceipt(
        context: Context,
        imageUri: Uri,
        onSuccess: (ScannedReceipt) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(context, imageUri)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    val scanned = parseReceiptText(rawText)
                    onSuccess(scanned)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun parseReceiptText(text: String): ScannedReceipt {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return ScannedReceipt(null, null, null, null, text, 0)
        }

        val totalAmount = extractTotalAmount(lines)
        val merchantName = extractMerchantName(lines)
        val date = extractDate(text)
        val categoryType = detectCategory(text, merchantName)

        return ScannedReceipt(
            totalAmount = totalAmount,
            merchantName = merchantName,
            date = date,
            suggestedCategoryType = categoryType,
            rawText = text,
            lineCount = lines.size
        )
    }

    private fun extractTotalAmount(lines: List<String>): Double? {
        val priceRegex = Pattern.compile("""(\d{1,5}[,\.]\d{2})""")
        val numberRegex = Pattern.compile("""\b\d+([,\.]\d{2})?\b""")

        // Pass 1: Search for explicit TOTAL lines (from bottom to top)
        for (i in lines.indices.reversed()) {
            val line = lines[i].uppercase()
            if (line.contains("TOTAL") || line.contains("NET") || line.contains("MONTANT") || line.contains("A PAYER") || line.contains("EUR") || line.contains("USD") || line.contains("CB")) {
                val matcher = priceRegex.matcher(lines[i])
                var lastFound: Double? = null
                while (matcher.find()) {
                    val strVal = matcher.group(1)?.replace(",", ".") ?: continue
                    strVal.toDoubleOrNull()?.let { lastFound = it }
                }
                if (lastFound != null && lastFound > 0.0) {
                    return lastFound
                }
            }
        }

        // Pass 2: Search for any price matches and take the maximum or prominent amount near the end
        val candidates = mutableListOf<Double>()
        for (line in lines) {
            val matcher = priceRegex.matcher(line)
            while (matcher.find()) {
                val str = matcher.group(1)?.replace(",", ".") ?: continue
                str.toDoubleOrNull()?.let {
                    if (it in 0.5..99999.0) {
                        candidates.add(it)
                    }
                }
            }
        }

        return if (candidates.isNotEmpty()) candidates.maxOrNull() else null
    }

    private fun extractMerchantName(lines: List<String>): String? {
        val noiseWords = listOf("TICKET", "FACTURE", "DATE", "HEURE", "BIENVENUE", "MERCI", "TEL", "SIRET", "CLIENT", "CAISSE", "DUPLICATA", "TVA")
        for (line in lines.take(5)) {
            val clean = line.trim()
            val upper = clean.uppercase()
            val isNoise = noiseWords.any { upper.startsWith(it) || upper == it } || clean.length < 3 || clean.matches(Regex("""[\d\W]+"""))
            if (!isNoise) {
                return clean.take(30)
            }
        }
        return lines.firstOrNull()?.take(30)
    }

    private fun extractDate(text: String): Date? {
        val dateRegex = Pattern.compile("""\b(\d{1,2})[/\.-](\d{1,2})[/\.-](\d{2,4})\b""")
        val matcher = dateRegex.matcher(text)
        if (matcher.find()) {
            val day = matcher.group(1)?.toIntOrNull() ?: return null
            val month = matcher.group(2)?.toIntOrNull() ?: return null
            var year = matcher.group(3)?.toIntOrNull() ?: return null
            if (year < 100) year += 2000

            if (day in 1..31 && month in 1..12 && year in 2000..2099) {
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, day)
                return cal.time
            }
        }
        return null
    }

    private fun detectCategory(text: String, merchant: String?): String {
        val full = (text + " " + (merchant ?: "")).uppercase()
        return when {
            full.contains("CARREFOUR") || full.contains("AUCHAN") || full.contains("LECLERC") ||
            full.contains("LIDL") || full.contains("ALDI") || full.contains("INTERMARCHE") ||
            full.contains("MONOPRIX") || full.contains("SUPERMARCHE") || full.contains("SUPER") ||
            full.contains("EPICERIE") || full.contains("PROVISION") || full.contains("ALIMENTATION") ||
            full.contains("FRUITS") || full.contains("LEGUMES") || full.contains("BOULANGERIE") ||
            full.contains("BOUCHERIE") -> "provisions"

            full.contains("PHARMACIE") || full.contains("DOCTEUR") || full.contains("MEDECIN") ||
            full.contains("CLINIQUE") || full.contains("HOPITAL") || full.contains("SANTE") ||
            full.contains("DENTISTE") || full.contains("MEDICAMENT") -> "health"

            full.contains("TOTAL") || full.contains("SHELL") || full.contains("BP") ||
            full.contains("ESSENCE") || full.contains("CARBURANT") || full.contains("STATION") ||
            full.contains("UBER") || full.contains("TAXI") || full.contains("PARKING") ||
            full.contains("PEAGE") || full.contains("SNCF") || full.contains("METRO") -> "transport"

            full.contains("RESTAURANT") || full.contains("CAFE") || full.contains("BISTRO") ||
            full.contains("BURGER") || full.contains("PIZZA") || full.contains("MCDONALD") ||
            full.contains("KFC") || full.contains("SUBWAY") || full.contains("SUSHI") ||
            full.contains("BRASSERIE") || full.contains("BAR") -> "restaurant"

            full.contains("EDF") || full.contains("ENGIE") || full.contains("EAU") ||
            full.contains("LOYER") || full.contains("ORANGE") || full.contains("SFR") ||
            full.contains("BOUYGUES") || full.contains("FREE") || full.contains("INTERNET") ||
            full.contains("ELECTRICITE") || full.contains("ASSURANCE") -> "bills"

            else -> "general"
        }
    }
}
