package com.example.dreamsystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.dreamsystem.data.PointRecord
import com.example.dreamsystem.data.Task
import com.example.dreamsystem.ui.components.*
import com.example.dreamsystem.viewmodel.TaskViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishPointScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val pointRecords: LazyPagingItems<PointRecord> = viewModel.pointRecords.collectAsLazyPagingItems()
    val totalPoints by viewModel.totalPoints.collectAsState(initial = 0)
    
    val showAddTaskDialog by viewModel.showAddTaskDialog.collectAsState()
    val taskDescription by viewModel.newTaskDescription.collectAsState()
    val taskPoints by viewModel.newTaskPoints.collectAsState()
    
    var pointsText by remember { mutableStateOf("") }
    
    LaunchedEffect(taskPoints) {
        pointsText = taskPoints.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心愿积分") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddTaskDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
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
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

            Text(
                text = "任务列表",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (tasks.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "暂无任务，点击右下角按钮添加任务",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onComplete = { viewModel.completeTask(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "积分流水",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
}