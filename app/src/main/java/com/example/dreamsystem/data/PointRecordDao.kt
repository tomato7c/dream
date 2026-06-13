package com.example.dreamsystem.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PointRecordDao {
    @Query("SELECT * FROM point_records ORDER BY timestamp DESC")
    fun getPointRecordsPaged(): PagingSource<Int, PointRecord>

    @Query("SELECT * FROM point_records ORDER BY timestamp DESC")
    suspend fun getAllPointRecords(): List<PointRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPointRecord(record: PointRecord): Long

    @Delete
    suspend fun deletePointRecord(record: PointRecord)

    @Query("DELETE FROM point_records WHERE id = :recordId")
    suspend fun deletePointRecordById(recordId: Long)

    @Query("SELECT SUM(points) FROM point_records")
    suspend fun getTotalPoints(): Int?

    @Query("SELECT SUM(points) FROM point_records")
    fun getTotalPointsFlow(): Flow<Int?>
}