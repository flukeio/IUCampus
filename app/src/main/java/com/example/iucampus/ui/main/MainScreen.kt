package com.example.iucampus.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.navigation
import com.example.iucampus.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val items = listOf(
        Screen.Courses,
        Screen.Dining,
        Screen.Gatherings,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            val icon = when (screen.route) {
                                Screen.Courses.route -> Icons.Default.Star
                                Screen.Dining.route -> Icons.Default.LocationOn
                                Screen.Gatherings.route -> Icons.Default.Share
                                Screen.Profile.route -> Icons.Default.Person
                                else -> Icons.Default.Star
                            }
                            Icon(icon, contentDescription = screen.route)
                        },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Courses.route,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        ) {
            navigation(route = Screen.Courses.route, startDestination = "course_list") {
                composable("course_list") {
                    com.example.iucampus.ui.courses.CourseListScreen(
                        onCourseClick = { courseId ->
                            bottomNavController.navigate("course_detail/$courseId")
                        },
                        onAddCourseClick = {
                            bottomNavController.navigate("available_courses")
                        }
                    )
                }
                composable("available_courses") {
                    com.example.iucampus.ui.courses.AvailableCoursesScreen(
                        onCourseClick = { courseId ->
                            bottomNavController.navigate("course_detail/$courseId")
                        },
                        onNavigateBack = { bottomNavController.popBackStack() }
                    )
                }
                composable("course_detail/{courseId}") { backStackEntry ->
                    val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                    com.example.iucampus.ui.courses.CourseDetailScreen(
                        courseId = courseId,
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onEnrollSuccess = { 
                            bottomNavController.popBackStack("course_list", inclusive = false) 
                        }
                    )
                }
            }
            composable(Screen.Dining.route) {
                com.example.iucampus.ui.dining.DiningMapScreen()
            }
            composable(Screen.Gatherings.route) {
                com.example.iucampus.ui.gatherings.GatheringFeedScreen(
                    onNavigateToDetail = { id -> bottomNavController.navigate("gathering_detail/$id") },
                    onNavigateToCreate = { bottomNavController.navigate("gathering_create") }
                )
            }
            composable("gathering_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                com.example.iucampus.ui.gatherings.GatheringDetailScreen(
                    gatheringId = id,
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }
            composable("gathering_create") {
                com.example.iucampus.ui.gatherings.CreateGatheringScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                com.example.iucampus.ui.profile.ProfileScreen()
            }
        }
    }
}
