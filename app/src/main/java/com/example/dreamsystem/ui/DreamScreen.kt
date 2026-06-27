package com.example.dreamsystem.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dreamsystem.data.Wish
import com.example.dreamsystem.viewmodel.DreamViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val TARGET_DATE = LocalDate.of(2029, 10, 24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamScreen(viewModel: DreamViewModel) {
    val wishes by viewModel.wishes.collectAsState(initial = emptyList())
    val showAddWishDialog by viewModel.showAddWishDialog.collectAsState()
    val wishDescription by viewModel.newWishDescription.collectAsState()
    val wishPoints by viewModel.newWishPoints.collectAsState()

    var pointsText by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var wishToDelete by remember { mutableStateOf<Wish?>(null) }

    LaunchedEffect(wishPoints) {
        pointsText = wishPoints.toString()
    }

    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), TARGET_DATE)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Dream") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD1C4E9),
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$daysLeft days",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7E57C2)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "心愿列表",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.showAddWishDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "添加心愿")
                }
            }

            if (wishes.isEmpty()) {
                Text(
                    text = "todo",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false
                ) {
                    items(wishes) { wish ->
                        WishItem(
                            wish = wish,
                            onDelete = {
                                wishToDelete = wish
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    AddWishDialog(
        showDialog = showAddWishDialog,
        description = wishDescription,
        points = pointsText,
        onDescriptionChange = { viewModel.updateWishDescription(it) },
        onPointsChange = {
            pointsText = it
            viewModel.updateWishPoints(it)
        },
        onDismiss = { viewModel.hideAddWishDialog() },
        onConfirm = { viewModel.addWish() }
    )

    if (showDeleteConfirmDialog && wishToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                wishToDelete = null
            },
            title = { Text("确认删除心愿") },
            text = {
                Text("确定要删除心愿\"${wishToDelete?.description}\"吗？")
            },
            confirmButton = {
                Button(onClick = {
                    wishToDelete?.let { viewModel.deleteWish(it) }
                    showDeleteConfirmDialog = false
                    wishToDelete = null
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog = false
                    wishToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun WishItem(
    wish: Wish,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wish.points} 积分",
                    fontSize = 11.sp,
                    color = Color(0xFF7E57C2)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除心愿",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AddWishDialog(
    showDialog: Boolean,
    description: String,
    points: String,
    onDescriptionChange: (String) -> Unit,
    onPointsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("添加心愿") },
            text = {
                Column {
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("心愿描述") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = points,
                        onValueChange = onPointsChange,
                        label = { Text("所需积分") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}
