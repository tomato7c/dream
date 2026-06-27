package com.example.dreamsystem.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dreamsystem.data.PointRecord
import com.example.dreamsystem.data.PointRecordDao
import com.example.dreamsystem.data.Task
import com.example.dreamsystem.data.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val taskDao: TaskDao, private val pointRecordDao: PointRecordDao) {

    fun getFrequentTasks(): Flow<List<Task>> {
        return taskDao.getFrequentTasks()
    }

    fun getInfrequentTasks(): Flow<List<Task>> {
        return taskDao.getInfrequentTasks()
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    fun getPointRecordsPaged(): Flow<PagingData<PointRecord>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { pointRecordDao.getPointRecordsPaged() }
        ).flow
    }

    suspend fun insertPointRecord(record: PointRecord): Long {
        return pointRecordDao.insertPointRecord(record)
    }

    suspend fun insertAllPointRecords(records: List<PointRecord>) {
        pointRecordDao.insertAll(records)
    }

    suspend fun deletePointRecord(record: PointRecord) {
        pointRecordDao.deletePointRecord(record)
    }

    suspend fun deletePointRecordById(recordId: Long) {
        pointRecordDao.deletePointRecordById(recordId)
    }

    fun getTotalPoints(): Flow<Int> {
        return pointRecordDao.getTotalPointsFlow().map { it ?: 0 }
    }

    suspend fun getAllPointRecords(): List<PointRecord> {
        return pointRecordDao.getAllPointRecords()
    }
}