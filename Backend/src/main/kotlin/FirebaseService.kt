package com.Recipe

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

class FirebaseService {
    private val db: Firestore

    init {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = FileInputStream("serviceAccountKey.json")
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
        }
        db = FirestoreClient.getFirestore()
    }

    suspend fun saveSession(ingredients: List<String>, recipes: List<Recipe>) =
        withContext(Dispatchers.IO) {
            db.collection("sessions").add(
                mapOf(
                    "ingredients" to ingredients,
                    "recipes" to recipes.map {
                        mapOf(
                            "name" to it.name,
                            "description" to it.description,
                            "steps" to it.steps,
                            "time_minutes" to it.time_minutes
                        )
                    },
                    "timestamp" to System.currentTimeMillis()
                )
            ).get()
        }

    suspend fun getHistory(): List<Map<String, Any>> =
        withContext(Dispatchers.IO) {
            db.collection("sessions")
                .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get().get()
                .documents
                .mapNotNull { it.data }
        }
}