package com.Recipe

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Gemini {
    private val client = Client.builder()
        .apiKey(System.getenv("GEMINI_API_KEY") ?: "AIzaSyAU73yte7INT_rY5vgr7zh-BEdY8HyzLw0")
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getIngredientsFromImage(imageBytes: ByteArray): List<String> =
        withContext(Dispatchers.IO) {
            val content = Content.fromParts(
                Part.fromText(
                    "List every food ingredient you see in this fridge. " +
                            "Return ONLY a comma-separated list, nothing else. " +
                            "Example: eggs, milk, cheese, tomatoes"
                ),
                Part.fromBytes(imageBytes, "image/jpeg")
            )

            val response = client.models.generateContent(
                "gemini-2.0-flash",
                content,
                null
            )

            response.text()
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    suspend fun getRecipesForIngredients(ingredients: List<String>): List<Recipe> =
        withContext(Dispatchers.IO) {
            val content = Content.fromParts(
                Part.fromText("""
                    You have exactly these 3 ingredients: ${ingredients.joinToString(separator = ", ")}.
                    Generate 3 creative recipes that use all of them.
                    Respond ONLY as a JSON array, no markdown, no extra text:
                    [{"name":"...","description":"...","steps":["...","..."],"time_minutes":30}]
                """.trimIndent())
            )

            val response = client.models.generateContent(
                "gemini-2.0-flash",
                content,
                null
            )

            val raw = response.text()
                ?.replace("```json", "")
                ?.replace("```", "")
                ?.trim() ?: "[]"

            json.decodeFromString<List<Recipe>>(raw)
        }
}