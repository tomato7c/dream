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

    val tasks: Flow<List<Task>> = repository.getFrequentTasks()
    val infrequentTasks: Flow<List<Task>> = repository.getInfrequentTasks()
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

    private val _showAddRecordDialog = MutableStateFlow(false)
    val showAddRecordDialog: StateFlow<Boolean> = _showAddRecordDialog.asStateFlow()

    private val _newRecordDescription = MutableStateFlow("")
    val newRecordDescription: StateFlow<String> = _newRecordDescription.asStateFlow()

    private val _newRecordPoints = MutableStateFlow("")
    val newRecordPoints: StateFlow<String> = _newRecordPoints.asStateFlow()

    fun showAddRecordDialog() {
        _showAddRecordDialog.value = true
    }

    fun hideAddRecordDialog() {
        _showAddRecordDialog.value = false
        _newRecordDescription.value = ""
        _newRecordPoints.value = ""
    }

    fun updateRecordDescription(description: String) {
        _newRecordDescription.value = description
    }

    fun updateRecordPoints(points: String) {
        _newRecordPoints.value = points
    }

    fun addPointRecord() {
        val points = _newRecordPoints.value.toIntOrNull() ?: return
        viewModelScope.launch {
            val record = PointRecord(
                taskDescription = _newRecordDescription.value,
                points = points
            )
            repository.insertPointRecord(record)
            hideAddRecordDialog()
        }
    }

    suspend fun getAllPointRecords(): List<PointRecord> {
        return repository.getAllPointRecords()
    }

    fun importPointRecords(records: List<PointRecord>) {
        viewModelScope.launch {
            repository.insertAllPointRecords(records)
        }
    }

    // 不常用任务弹窗
    private val _showInfrequentDialog = MutableStateFlow(false)
    val showInfrequentDialog: StateFlow<Boolean> = _showInfrequentDialog.asStateFlow()

    fun showInfrequentDialog() {
        _showInfrequentDialog.value = true
    }

    fun hideInfrequentDialog() {
        _showInfrequentDialog.value = false
    }

    // 添加不常用任务弹窗
    private val _showAddInfrequentDialog = MutableStateFlow(false)
    val showAddInfrequentDialog: StateFlow<Boolean> = _showAddInfrequentDialog.asStateFlow()

    private val _newInfrequentDescription = MutableStateFlow("")
    val newInfrequentDescription: StateFlow<String> = _newInfrequentDescription.asStateFlow()

    private val _newInfrequentPoints = MutableStateFlow(0)
    val newInfrequentPoints: StateFlow<Int> = _newInfrequentPoints.asStateFlow()

    private var _newInfrequentPointsText = MutableStateFlow("")
    val newInfrequentPointsText: StateFlow<String> = _newInfrequentPointsText.asStateFlow()

    fun showAddInfrequentDialog() {
        _showAddInfrequentDialog.value = true
    }

    fun hideAddInfrequentDialog() {
        _showAddInfrequentDialog.value = false
        _newInfrequentDescription.value = ""
        _newInfrequentPoints.value = 0
        _newInfrequentPointsText.value = ""
    }

    fun updateInfrequentDescription(description: String) {
        _newInfrequentDescription.value = description
    }

    fun updateInfrequentPoints(points: String) {
        _newInfrequentPointsText.value = points
        _newInfrequentPoints.value = points.toIntOrNull() ?: 0
    }

    fun addInfrequentTask() {
        viewModelScope.launch {
            val task = Task(
                description = _newInfrequentDescription.value,
                points = _newInfrequentPoints.value,
                frequent = false
            )
            repository.insertTask(task)
            hideAddInfrequentDialog()
        }
    }
}