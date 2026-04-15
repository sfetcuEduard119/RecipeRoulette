package com.Recipe

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.*
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val gemini = Gemini()
    val firebase = FirebaseService()

    routing {
        post("/analyze-fridge") {

            val multipart = call.receiveMultipart()
            var imageBytes: ByteArray? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    imageBytes = part.streamProvider().readBytes()
                }
                part.dispose()
            }

            if (imageBytes == null) {
                call.respond(mapOf("error" to "No image received"))
                return@post
            }

            val allIngredients = gemini.getIngredientsFromImage(imageBytes!!)

            if (allIngredients.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "AI couldn't see any food! Try better lighting."))
                return@post
            }

            val picked = allIngredients.shuffled().take(3)
            val recipes = gemini.getRecipesForIngredients(picked)
            firebase.saveSession(picked, recipes)

            call.respond(mapOf(
                "ingredients" to picked,
                "recipes" to recipes
            ))
        }

        get("/history") {
            val history = firebase.getHistory()
            call.respond(history)
        }
    }
}