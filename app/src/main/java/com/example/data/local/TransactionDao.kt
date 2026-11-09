package com.example.data.local

import androidx.room.*
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM credit_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM credit_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}
