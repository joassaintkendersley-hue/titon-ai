package com.example.data.local

import androidx.room.*
import com.example.data.model.MerchantConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantConfigDao {
    @Query("SELECT * FROM merchant_config WHERE id = 'default_merchant' LIMIT 1")
    fun getMerchantConfig(): Flow<MerchantConfigEntity?>

    @Query("SELECT * FROM merchant_config WHERE id = 'default_merchant' LIMIT 1")
    suspend fun getMerchantConfigOnce(): MerchantConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: MerchantConfigEntity)
}
