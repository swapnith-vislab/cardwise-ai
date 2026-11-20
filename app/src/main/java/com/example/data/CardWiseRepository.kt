package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.CardBenefitRule
import com.example.model.CreditCard
import com.example.model.PurchaseLog
import com.example.model.RewardType
import com.example.model.SampleDataFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val REPO_TAG = "CardWiseRepository"

object FirestoreProvider {
    fun getDatabase(context: Context): FirebaseFirestore {
        val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        try {
            db.firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings {})
            }
        } catch (_: IllegalStateException) {
            // Firestore settings already applied for this process
        }
        return db
    }
}

class CardWiseRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(FirestoreProvider.getDatabase(context))

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun cardsCollection(uid: String) =
        db.collection("users").document(uid).collection("cards")

    private fun transactionsCollection(uid: String) =
        db.collection("users").document(uid).collection("transactions")

    private fun metaDoc(uid: String) =
        db.collection("users").document(uid).collection("meta").document("settings")

    fun observeCards(userId: String): Flow<List<CreditCard>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/cards"
        emitAll(
            cardsCollection(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CreditCard::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String): Flow<List<PurchaseLog>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/transactions"
        emitAll(
            transactionsCollection(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PurchaseLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun seedSampleDataIfNeeded(userId: String): Result<Boolean> {
        val metaPath = "users/$userId/meta/settings"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }

            val metaSnapshot = metaDoc(uid).get().await()
            if (metaSnapshot.exists() && metaSnapshot.getBoolean("sampleSeeded") == true) {
                return Result.success(false)
            }

            // Also skip seeding if user already has cards
            val existingCards = cardsCollection(uid).limit(1).get().await()
            if (!existingCards.isEmpty) {
                if (!metaSnapshot.exists()) {
                    metaDoc(uid).set(
                        mapOf(
                            "userId" to uid,
                            "sampleSeeded" to true,
                            "preferredCurrency" to "₹",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                return Result.success(false)
            }

            // Seed sample cards + sample transactions + settings doc atomically in a single WriteBatch
            val sampleCards = SampleDataFactory.createSampleCards(uid)
            val sampleTransactions = SampleDataFactory.createSampleTransactions(uid)
            val batch = db.batch()

            for ((cardId, card) in sampleCards) {
                batch.set(cardsCollection(uid).document(cardId), card.toCreateMap(uid))
            }
            for ((txId, tx) in sampleTransactions) {
                batch.set(transactionsCollection(uid).document(txId), tx.toCreateMap(uid))
            }
            if (!metaSnapshot.exists()) {
                batch.set(
                    metaDoc(uid),
                    mapOf(
                        "userId" to uid,
                        "sampleSeeded" to true,
                        "preferredCurrency" to "₹",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    metaDoc(uid),
                    mapOf(
                        "sampleSeeded" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, metaPath)
            Result.failure(e)
        }
    }

    suspend fun addCard(card: CreditCard): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cards"
        return try {
            val uid = requireUserId()
            require(card.issuer.isNotBlank()) { "Issuer name is required." }
            require(card.cardName.isNotBlank()) { "Card variant name is required." }
            val digits = card.lastFour.filter { it.isDigit() }
            require(digits.length == 4) { "Enter exactly the last 4 digits of your card." }

            val docRef = cardsCollection(uid).document()
            docPath = docRef.path
            docRef.set(card.copy(lastFour = digits).toCreateMap(uid)).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateCard(card: CreditCard): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cards/${card.id}"
        return try {
            val uid = requireUserId()
            require(card.id.isNotBlank()) { "Card ID is required." }
            require(card.issuer.isNotBlank()) { "Issuer name is required." }
            require(card.cardName.isNotBlank()) { "Card variant name is required." }
            val digits = card.lastFour.filter { it.isDigit() }
            require(digits.length == 4) { "Last 4 digits must be 4 numbers." }

            val docRef = cardsCollection(uid).document(card.id)
            docPath = docRef.path
            docRef.update(card.copy(lastFour = digits).toFullUpdateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateCardRulesAndTerms(
        cardId: String,
        baseRewardRatePercent: Double,
        rewardType: RewardType,
        pointValueInCurrency: Double,
        aiFetched: Boolean,
        aiSummary: String,
        aiLastUpdatedEpochMs: Long,
        rules: List<CardBenefitRule>
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cards/$cardId"
        return try {
            val uid = requireUserId()
            require(cardId.isNotBlank()) { "Card ID must not be blank." }
            val docRef = cardsCollection(uid).document(cardId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "baseRewardRatePercent" to baseRewardRatePercent.coerceIn(0.0, 50.0),
                    "rewardType" to rewardType.name,
                    "pointValueInCurrency" to pointValueInCurrency.coerceIn(0.0, 100.0),
                    "aiFetched" to aiFetched,
                    "aiSummary" to aiSummary.trim().take(1200),
                    "aiLastUpdatedEpochMs" to aiLastUpdatedEpochMs.coerceAtLeast(0L),
                    "rules" to rules.take(30).map { it.toMap() },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCard(cardId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cards/$cardId"
        return try {
            val uid = requireUserId()
            require(cardId.isNotBlank()) { "Card ID must not be blank." }
            val docRef = cardsCollection(uid).document(cardId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun logPurchaseAndUpdateBalance(
        purchase: PurchaseLog,
        newStatementBalance: Double
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/transactions"
        return try {
            val uid = requireUserId()
            require(purchase.cardId.isNotBlank()) { "Card ID is required." }
            require(purchase.amount > 0.0) { "Purchase amount must be greater than 0." }

            val txRef = transactionsCollection(uid).document()
            val cardRef = cardsCollection(uid).document(purchase.cardId)
            docPath = txRef.path

            val batch = db.batch()
            batch.set(txRef, purchase.toCreateMap(uid))
            batch.update(
                cardRef,
                mapOf(
                    "statementBalance" to newStatementBalance.coerceIn(0.0, 100_000_000.0),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(txRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(
        transactionId: String,
        cardIdForBalanceRollback: String? = null,
        adjustedStatementBalance: Double? = null
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/transactions/$transactionId"
        return try {
            val uid = requireUserId()
            require(transactionId.isNotBlank()) { "Transaction ID is required." }
            val txRef = transactionsCollection(uid).document(transactionId)
            docPath = txRef.path

            if (!cardIdForBalanceRollback.isNullOrBlank() && adjustedStatementBalance != null) {
                val batch = db.batch()
                batch.delete(txRef)
                batch.update(
                    cardsCollection(uid).document(cardIdForBalanceRollback),
                    mapOf(
                        "statementBalance" to adjustedStatementBalance.coerceIn(0.0, 100_000_000.0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            } else {
                txRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun clearSampleData(
        sampleCardIds: List<String>,
        sampleTransactionIds: List<String>
    ): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cards"
        return try {
            val uid = requireUserId()
            val batch = db.batch()
            for (txId in sampleTransactionIds) {
                if (txId.isNotBlank()) {
                    batch.delete(transactionsCollection(uid).document(txId))
                }
            }
            for (cardId in sampleCardIds) {
                if (cardId.isNotBlank()) {
                    batch.delete(cardsCollection(uid).document(cardId))
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(REPO_TAG, "Failed to clear sample data", e)
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun restoreSampleData(): Result<Unit> {
        val path = "users/${auth.currentUser?.uid ?: "unauthenticated"}/cards"
        return try {
            val uid = requireUserId()
            val sampleCards = SampleDataFactory.createSampleCards(uid)
            val sampleTransactions = SampleDataFactory.createSampleTransactions(uid)
            val batch = db.batch()
            for ((cardId, card) in sampleCards) {
                batch.set(cardsCollection(uid).document(cardId), card.toCreateMap(uid))
            }
            for ((txId, tx) in sampleTransactions) {
                batch.set(transactionsCollection(uid).document(txId), tx.toCreateMap(uid))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
