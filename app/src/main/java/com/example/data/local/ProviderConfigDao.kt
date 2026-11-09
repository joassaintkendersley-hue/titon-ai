package com.example.data.local

import androidx.room.*
import com.example.data.model.ProviderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {
    @Query("SELECT * FROM provider_configs")
    fun getAllProviders(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM provider_configs WHERE id = :id")
    suspend fun getProviderById(id: String): ProviderConfigEntity?

    @Query("SELECT * FROM provider_configs WHERE isDefault = 1 LIMIT 1")
    fun getDefaultProvider(): Flow<ProviderConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderConfigEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderConfigEntity)

    @Query("DELETE FROM provider_configs WHERE id = 'seedance' OR apiEndpoint LIKE '%api.seedance.ai%'")
    suspend fun deleteLegacySeedance()

    @Query("SELECT COUNT(*) FROM provider_configs")
    suspend fun getProviderCount(): Int
}
