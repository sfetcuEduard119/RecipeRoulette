package com.reciperoulette.network

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.reciperoulette.BuildConfig
import com.reciperoulette.data.RecipeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    /**
     * Takes a fridge photo bitmap, randomly selects ingredients from it,
     * and generates a fun recipe — all in one Gemini call.
     */
    suspend fun analyzeAndGenerateRecipe(bitmap: Bitmap): Result<RecipeResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val prompt = """
                    You are a fun and creative chef assistant. Analyze this fridge photo carefully.
                    
                    Step 1 – Detect all visible ingredients in the fridge.
                    Step 2 – Randomly pick 3 to 6 of those ingredients (the more surprising the combo, the better!).
                    Step 3 – Invent a fun, creative recipe using ONLY those selected ingredients plus basic pantry staples (salt, pepper, oil, water).
                    
                    Respond ONLY in the following JSON format, no markdown, no extra text:
                    {
                      "detected_ingredients": ["ingredient1", "ingredient2", ...],
                      "selected_ingredients": ["ingredient1", "ingredient2", ...],
                      "recipe_name": "Fun Recipe Name",
                      "fun_tagline": "A short witty one-liner about the recipe",
                      "difficulty": "Easy|Medium|Hard",
                      "prep_time_minutes": 15,
                      "servings": 2,
                      "steps": [
                        "Step 1: ...",
                        "Step 2: ...",
                        "Step 3: ..."
                      ],
                      "chef_tip": "A fun tip or joke from the chef"
                    }
                """.trimIndent()

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = model.generateContent(inputContent)
                val rawText = response.text ?: throw Exception("Empty response from Gemini")

                // Strip potential markdown fences just in case
                val json = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                parseRecipeJson(json)
            }
        }

    private fun parseRecipeJson(json: String): RecipeResult {
        // Simple manual parsing to avoid needing an extra library in this file.
        // In production you'd use Gson or kotlinx.serialization.
        return try {
            val gson = com.google.gson.Gson()
            gson.fromJson(json, RecipeResult::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to parse recipe: ${e.message}\nRaw: $json")
        }
    }
}