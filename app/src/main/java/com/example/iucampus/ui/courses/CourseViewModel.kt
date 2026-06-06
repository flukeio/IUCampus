package com.example.iucampus.ui.courses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.iucampus.data.course.Course
import com.example.iucampus.data.course.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CourseRepository(application)

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _availableCourses = MutableStateFlow<List<Course>>(emptyList())
    val availableCourses: StateFlow<List<Course>> = _availableCourses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    init {
        loadCourses()
        loadAvailableCourses()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            repository.getCourses().collect {
                _courses.value = it
            }
        }
    }

    private fun loadAvailableCourses() {
        viewModelScope.launch {
            repository.getAllAvailableCourses().collect {
                _availableCourses.value = it
            }
        }
    }

    fun loadCourseById(id: String) {
        viewModelScope.launch {
            repository.getCourseById(id).collect {
                _selectedCourse.value = it
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        repository.enrollInCourse(courseId)
        loadCourses()
    }

    fun dropCourse(courseId: String) {
        repository.dropCourse(courseId)
        loadCourses()
    }
}
