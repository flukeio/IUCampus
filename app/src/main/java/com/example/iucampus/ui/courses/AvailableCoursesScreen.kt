package com.example.iucampus.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableCoursesScreen(
    onCourseClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CourseViewModel = viewModel()
) {
    val enrolledCourses by viewModel.courses.collectAsState()
    val allAvailableCourses by viewModel.availableCourses.collectAsState()
    val unEnrolledCourses = allAvailableCourses.filter { available -> 
        enrolledCourses.none { enrolled -> enrolled.id == available.id } 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Courses") },
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val groupedCourses = unEnrolledCourses.groupBy { it.lectureHall }
            groupedCourses.forEach { (semester, semesterCourses) ->
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = semester,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(semesterCourses) { course ->
                    CourseCard(course = course, onClick = { onCourseClick(course.id) })
                }
            }
        }
    }
}
