package com.example.dreamsystem.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.dreamsystem.data.PointRecord
import com.example.dreamsystem.data.Task
import com.example.dreamsystem.ui.components.*
import com.example.dreamsystem.utils.ExportUtils
import com.example.dreamsystem.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishPointScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val pointRecords: LazyPagingItems<PointRecord> = viewModel.pointRecords.collectAsLazyPagingItems()
    val totalPoints by viewModel.totalPoints.collectAsState(initial = 0)
    
    val showAddTaskDialog by viewModel.showAddTaskDialog.collectAsState()
    val taskDescription by viewModel.newTaskDescription.collectAsState()
    val taskPoints by viewModel.newTaskPoints.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()

    val showAddRecordDialog by viewModel.showAddRecordDialog.collectAsState()
    val recordDescription by viewModel.newRecordDescription.collectAsState()
    val recordPoints by viewModel.newRecordPoints.collectAsState()

    val infrequentTasks by viewModel.infrequentTasks.collectAsState(initial = emptyList())
    val showInfrequentDialog by viewModel.showInfrequentDialog.collectAsState()
    val showAddInfrequentDialog by viewModel.showAddInfrequentDialog.collectAsState()
    val infrequentDescription by viewModel.newInfrequentDescription.collectAsState()
    val infrequentPointsText by viewModel.newInfrequentPointsText.collectAsState()
    
    var showCompleteConfirmDialog by remember { mutableStateOf(false) }
    var taskToComplete by remember { mutableStateOf<Task?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    
    var pointsText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.viewModelScope.launch {
                try {
                    val records = withContext(Dispatchers.IO) {
                        val tempFile = File(context.cacheDir, "import_temp.xls")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        ExportUtils.importFromExcel(tempFile)
                    }
                    if (records.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "未找到有效的积分流水数据", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        viewModel.importPointRecords(records)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "成功导入 ${records.size} 条记录", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    LaunchedEffect(taskPoints) {
        pointsText = taskPoints.toString()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("dream point") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFA8E6CF),
                    titleContentColor = androidx.compose.ui.graphics.Color.Black
                ),
                actions = {
                    IconButton(onClick = { viewModel.showSettingsDialog() }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置", tint = androidx.compose.ui.graphics.Color.Black)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "总积分",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalPoints.toString(),
                        fontSize = 36.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "任务列表",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = { viewModel.showAddTaskDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "添加任务")
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.showInfrequentDialog() }) {
                    Text("更多", fontSize = 13.sp)
                }
            }

            if (tasks.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                      Text(
                          text = "暂无任务，点击右侧按钮添加任务",
                          modifier = Modifier.padding(16.dp),
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(max = 400.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            TaskItem(
                                task = task,
                                onComplete = {
                                    taskToComplete = task
                                    showCompleteConfirmDialog = true
                                },
                                onDelete = {
                                    taskToDelete = task
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "积分流水",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                IconButton(onClick = { viewModel.showAddRecordDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "添加流水")
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(
                        count = pointRecords.itemCount,
                        key = { index -> pointRecords[index]?.id ?: index }
                    ) { index ->
                        val record = pointRecords[index]
                        record?.let {
                            PointRecordItem(
                                taskDescription = it.taskDescription,
                                points = it.points,
                                timestamp = it.timestamp,
                                onDelete = { viewModel.deletePointRecord(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    AddTaskDialog(
        showDialog = showAddTaskDialog,
        description = taskDescription,
        points = pointsText,
        onDescriptionChange = { viewModel.updateTaskDescription(it) },
        onPointsChange = {
            pointsText = it
            viewModel.updateTaskPoints(it)
        },
        onDismiss = { viewModel.hideAddTaskDialog() },
        onConfirm = { viewModel.addTask() }
    )

    if (showAddRecordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddRecordDialog() },
            title = { Text("添加流水") },
            text = {
                Column {
                    OutlinedTextField(
                        value = recordDescription,
                        onValueChange = { viewModel.updateRecordDescription(it) },
                        label = { Text("描述") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = recordPoints,
                        onValueChange = { viewModel.updateRecordPoints(it) },
                        label = { Text("积分（正数增加，负数扣减）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.addPointRecord() }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddRecordDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    SettingsDialog(
        showDialog = showSettingsDialog,
        onDismiss = { viewModel.hideSettingsDialog() },
        onExportToExcel = {
            viewModel.viewModelScope.launch {
                val records = viewModel.getAllPointRecords()
                ExportUtils.exportToExcel(context, records)
            }
        },
        onImportFromExcel = {
            importLauncher.launch(arrayOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "*/*"))
        }
    )

    if (showCompleteConfirmDialog && taskToComplete != null) {
        AlertDialog(
            onDismissRequest = {
                showCompleteConfirmDialog = false
                taskToComplete = null
            },
            title = { Text("确认完成任务") },
            text = {
                Text("确定要完成\"${taskToComplete?.description}\"并获得${taskToComplete?.points}积分吗？")
            },
            confirmButton = {
                Button(onClick = {
                    taskToComplete?.let { viewModel.completeTask(it) }
                    showCompleteConfirmDialog = false
                    taskToComplete = null
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteConfirmDialog = false
                    taskToComplete = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteConfirmDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                taskToDelete = null
            },
            title = { Text("确认删除任务") },
            text = {
                Text("确定要删除任务\"${taskToDelete?.description}\"吗？")
            },
            confirmButton = {
                Button(onClick = {
                    taskToDelete?.let { viewModel.deleteTask(it) }
                    showDeleteConfirmDialog = false
                    taskToDelete = null
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    taskToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // 不常用任务列表弹窗
    if (showInfrequentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideInfrequentDialog() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("不常用任务", modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.showAddInfrequentDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "添加不常用任务")
                    }
                }
            },
            text = {
                if (infrequentTasks.isEmpty()) {
                    Text("暂无不常用任务，点击右上角添加")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(infrequentTasks) { task ->
                            TaskItem(
                                task = task,
                                onComplete = {
                                    taskToComplete = task
                                    showCompleteConfirmDialog = true
                                },
                                onDelete = {
                                    taskToDelete = task
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideInfrequentDialog() }) {
                    Text("关闭")
                }
            }
        )
    }

    // 添加不常用任务弹窗
    if (showAddInfrequentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddInfrequentDialog() },
            title = { Text("添加不常用任务") },
            text = {
                Column {
                    OutlinedTextField(
                        value = infrequentDescription,
                        onValueChange = { viewModel.updateInfrequentDescription(it) },
                        label = { Text("任务描述") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = infrequentPointsText,
                        onValueChange = { viewModel.updateInfrequentPoints(it) },
                        label = { Text("积分") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.addInfrequentTask() }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddInfrequentDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}