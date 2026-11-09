package com.example.data.local

import androidx.room.*
import com.example.data.model.CreditPackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Query("SELECT * FROM credit_packages WHERE isActive = 1")
    fun getActivePackages(): Flow<List<CreditPackageEntity>>

    @Query("SELECT * FROM credit_packages")
    fun getAllPackages(): Flow<List<CreditPackageEntity>>

    @Query("SELECT * FROM credit_packages WHERE id = :packageId")
    suspend fun getPackageById(packageId: String): CreditPackageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<CreditPackageEntity>)

    @Update
    suspend fun updatePackage(pkg: CreditPackageEntity)
}
