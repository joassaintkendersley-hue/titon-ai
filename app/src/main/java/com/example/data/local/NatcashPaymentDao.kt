package com.example.data.local

import androidx.room.*
import com.example.data.model.NatcashPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NatcashPaymentDao {
    @Query("SELECT * FROM natcash_payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPaymentsForUser(userId: Long): Flow<List<NatcashPaymentEntity>>

    @Query("SELECT * FROM natcash_payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<NatcashPaymentEntity>>

    @Query("SELECT * FROM natcash_payments WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingPayments(): Flow<List<NatcashPaymentEntity>>

    @Query("SELECT COUNT(*) FROM natcash_payments WHERE status = 'PENDING'")
    fun getPendingPaymentsCount(): Flow<Int>

    @Query("SELECT * FROM natcash_payments WHERE status = 'APPROVED' ORDER BY reviewedAt DESC")
    fun getApprovedPayments(): Flow<List<NatcashPaymentEntity>>

    @Query("SELECT * FROM natcash_payments WHERE status = 'REJECTED' ORDER BY reviewedAt DESC")
    fun getRejectedPayments(): Flow<List<NatcashPaymentEntity>>

    @Query("SELECT COUNT(*) FROM natcash_payments WHERE status = 'APPROVED'")
    fun getApprovedPaymentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM natcash_payments WHERE status = 'REJECTED'")
    fun getRejectedPaymentsCount(): Flow<Int>

    @Query("SELECT SUM(creditsAmount) FROM natcash_payments WHERE status = 'APPROVED'")
    fun getTotalCreditsSold(): Flow<Int?>

    @Query("SELECT SUM(amountHtg) FROM natcash_payments WHERE status = 'APPROVED'")
    fun getTotalRevenueHtg(): Flow<Int?>

    @Query("SELECT * FROM natcash_payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: String): NatcashPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: NatcashPaymentEntity)

    @Update
    suspend fun updatePayment(payment: NatcashPaymentEntity)

    @Query("UPDATE natcash_payments SET status = :status, adminNotes = :adminNotes, reviewedAt = :reviewedAt WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: String, status: String, adminNotes: String?, reviewedAt: Long = System.currentTimeMillis())
}
