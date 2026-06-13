package com.example.dreamsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreamsystem.data.AppDatabase
import com.example.dreamsystem.data.PointRecord
import com.example.dreamsystem.data.Task
import com.example.dreamsystem.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val database: AppDatabase = AppDatabase.getDatabase(application)

    init {
        val taskDao = database.taskDao()
        val pointRecordDao = database.pointRecordDao()
        repository = TaskRepository(taskDao, pointRecordDao)
    }

    val tasks: Flow<List<Task>> = repository.getAllTasks()
    val pointRecords = repository.getPointRecordsPaged()
    val totalPoints: Flow<Int> = repository.getTotalPoints()

    private val _showAddTaskDialog = MutableStateFlow(false)
    val showAddTaskDialog: StateFlow<Boolean> = _showAddTaskDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _newTaskDescription = MutableStateFlow("")
    val newTaskDescription: StateFlow<String> = _newTaskDescription.asStateFlow()

    private val _newTaskPoints = MutableStateFlow(0)
    val newTaskPoints: StateFlow<Int> = _newTaskPoints.asStateFlow()

    fun showAddTaskDialog() {
        _showAddTaskDialog.value = true
    }

    fun hideAddTaskDialog() {
        _showAddTaskDialog.value = false
        _newTaskDescription.value = ""
        _newTaskPoints.value = 0
    }

    fun showSettingsDialog() {
        _showSettingsDialog.value = true
    }

    fun hideSettingsDialog() {
        _showSettingsDialog.value = false
    }

    fun updateTaskDescription(description: String) {
        _newTaskDescription.value = description
    }

    fun updateTaskPoints(points: String) {
        _newTaskPoints.value = points.toIntOrNull() ?: 0
    }

    fun addTask() {
        viewModelScope.launch {
            val task = Task(
                description = _newTaskDescription.value,
                points = _newTaskPoints.value
            )
            repository.insertTask(task)
            hideAddTaskDialog()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            val record = PointRecord(
                taskDescription = task.description,
                points = task.points
            )
            repository.insertPointRecord(record)
        }
    }

    fun deletePointRecord(record: PointRecord) {
        viewModelScope.launch {
            repository.deletePointRecord(record)
        }
    }

    suspend fun getAllPointRecords(): List<PointRecord> {
        return repository.getAllPointRecords()
    }
}