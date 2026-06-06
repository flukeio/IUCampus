package com.example.iucampus.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onNavigateBack: () -> Unit,
    onEnrollSuccess: () -> Unit = onNavigateBack,
    viewModel: CourseViewModel = viewModel()
) {
    val course by viewModel.selectedCourse.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val isEnrolled = courses.any { it.id == courseId }
    var showDropDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        viewModel.loadCourseById(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        course?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = c.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow("Coordinator", c.professor)
                    DetailRow("Term", c.lectureHall)
                    DetailRow("Exam Type", c.time)
                    DetailRow("Credits", "${c.credits} ECTS")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Syllabus Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = c.syllabusSummary, style = MaterialTheme.typography.bodyMedium)
                    
                    BulletList("Learning Outcomes", c.outcomes)
                    BulletList("Course Contents", c.contents)
                    BulletList("Literature", c.literature)
                }
                
                if (isEnrolled) {
                    Button(
                        onClick = { showDropDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 16.dp)
                    ) {
                        Text("Drop Course", color = MaterialTheme.colorScheme.onError)
                    }
                    
                    if (showDropDialog) {
                        AlertDialog(
                            onDismissRequest = { showDropDialog = false },
                            title = { Text("Drop Course") },
                            text = { Text("Are you sure you want to drop ${c.name}?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDropDialog = false
                                        viewModel.dropCourse(c.id)
                                        onEnrollSuccess()
                                    }
                                ) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDropDialog = false }) {
                                    Text("No")
                                }
                            }
                        )
                    }
                } else {
                    val totalCredits = courses.sumOf { it.credits }
                    val canEnroll = totalCredits + c.credits <= 25
                    
                    Button(
                        onClick = {
                            viewModel.enrollInCourse(c.id)
                            onEnrollSuccess()
                        },
                        enabled = canEnroll,
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 16.dp)
                    ) {
                        Text(if (canEnroll) "Start Course" else "Credit Limit Reached")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BulletList(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Spacer(modifier = Modifier.height(24.dp))
    Text(text = title, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    items.forEach { item ->
        Row(modifier = Modifier.padding(bottom = 6.dp, start = 8.dp)) {
            Text(text = "• ", style = MaterialTheme.typography.bodyMedium)
            Text(text = item, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
