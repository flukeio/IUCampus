package com.example.iucampus.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    
    // Bottom Nav screens
    object Courses : Screen("courses")
    object Dining : Screen("dining")
    object Gatherings : Screen("gatherings")
    object Profile : Screen("profile")
}
