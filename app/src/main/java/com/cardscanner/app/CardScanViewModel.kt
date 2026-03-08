package com.cardscanner.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    data class Success(val contact: ContactInfo) : ScanState()
    data class Error(val message: String) : ScanState()
}

class CardScanViewModel : ViewModel() {

    private val _scanState = MutableLiveData<ScanState>(ScanState.Idle)
    val scanState: LiveData<ScanState> = _scanState

    fun resetState() { _scanState.value = ScanState.Idle }

    fun scanCard(context: Context, imageUri: Uri) {
        _scanState.value = ScanState.Loading
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { loadBitmap(context, imageUri) }
                val contact = withContext(Dispatchers.IO) { callGemini(bitmap) }
                _scanState.value = ScanState.Success(contact)
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Scan failed")
            }
        }
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        val stream = context.contentResolver.openInputStream(uri)!!
        var bmp = BitmapFactory.decodeStream(stream)
        stream.close()
        // Resize to max 1024px to keep it fast
        val max = 1024
        if (bmp.width > max || bmp.height > max) {
            val ratio = minOf(max.toFloat() / bmp.width, max.toFloat() / bmp.height)
            bmp = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width * ratio).toInt(),
                (bmp.height * ratio).toInt(),
                true
            )
        }
        return bmp
    }

    private suspend fun callGemini(bitmap: Bitmap): ContactInfo {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) throw Exception("Gemini API key not set")

        val model = GenerativeModel(
            modelName = "gemini-2.0-flash",   // Free tier model
            apiKey = apiKey
        )

        val prompt = """
            Look at this business card image carefully.
            Extract ALL contact information and return ONLY a valid JSON object.
            No explanation, no markdown, no code blocks — just the raw JSON.
            
            Use this exact format:
            {"firstName":"","lastName":"","organization":"","title":"","phones":[],"emails":[],"website":"","address":""}
            
            Rules:
            - phones and emails must be JSON arrays e.g. ["9167147881","8691942999"]
            - Include country code in phones if visible
            - website should be the full URL or domain
            - address should be the full address on one line
            - If a field is not found, leave it as empty string or empty array
            - Read carefully — include ALL phone numbers and emails found
        """.trimIndent()

        val response = model.generateContent(
            content {
                image(bitmap)
                text(prompt)
            }
        )

        val raw = response.text?.trim() ?: throw Exception("No response from Gemini")
        // Strip any accidental markdown
        val json = raw
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()

        return parseJson(json)
    }

    private fun parseJson(json: String): ContactInfo {
        val j = JSONObject(json)

        fun arr(key: String): List<String> = try {
            val a = j.getJSONArray(key)
            (0 until a.length()).map { a.getString(it) }.filter { it.isNotBlank() }
        } catch (e: Exception) { emptyList() }

        return ContactInfo(
            firstName    = j.optString("firstName"),
            lastName     = j.optString("lastName"),
            organization = j.optString("organization"),
            title        = j.optString("title"),
            phones       = arr("phones"),
            emails       = arr("emails"),
            website      = j.optString("website"),
            address      = j.optString("address")
        )
    }
}
