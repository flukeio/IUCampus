package com.example.iucampus.data.course

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader

class CourseRepository(private val context: Context) {

    companion object {
        private var allCourses: List<Course>? = null
        private val _enrolledCoursesFlow = MutableStateFlow<List<Course>>(emptyList())
        private var isInitialized = false
    }

    init {
        if (!isInitialized) {
            val courses = loadCoursesFromJson()
            allCourses = courses
            if (courses.size >= 3) {
                _enrolledCoursesFlow.value = courses.take(3)
            }
            isInitialized = true
        }
    }

    private fun loadCoursesFromJson(): List<Course> {
        return try {
            val inputStream = context.assets.open("courses.json")
            val jsonString = InputStreamReader(inputStream).readText()
            val rootObj = JSONObject(jsonString)
            val semestersArray = rootObj.getJSONArray("semesters")
            val list = mutableListOf<Course>()
            
            for (i in 0 until semestersArray.length()) {
                val semesterObj = semestersArray.getJSONObject(i)
                val semesterNum = semesterObj.getInt("semester")
                val modulesArray = semesterObj.getJSONArray("modules")
                
                for (j in 0 until modulesArray.length()) {
                    val moduleObj = modulesArray.getJSONObject(j)
                    val credits = if (moduleObj.has("credits")) moduleObj.getInt("credits") else 5
                    val coordinator = if (moduleObj.has("coordinator")) moduleObj.getString("coordinator") else "N/A"
                    
                    val coursesArray = moduleObj.getJSONArray("courses")
                    for (k in 0 until coursesArray.length()) {
                        val courseObj = coursesArray.getJSONObject(k)
                        val courseCode = courseObj.getString("course_code")
                        val courseName = courseObj.getString("course_name")
                        val description = if (courseObj.has("description")) courseObj.getString("description") else ""
                        val examType = if (courseObj.has("exam_type")) courseObj.getString("exam_type") else "N/A"
                        
                        val outcomes = mutableListOf<String>()
                        if (courseObj.has("outcomes")) {
                            val arr = courseObj.getJSONArray("outcomes")
                            for (x in 0 until arr.length()) outcomes.add(arr.getString(x))
                        }
                        
                        val contents = mutableListOf<String>()
                        if (courseObj.has("contents")) {
                            val arr = courseObj.getJSONArray("contents")
                            for (x in 0 until arr.length()) contents.add(arr.getString(x))
                        }
                        
                        val literature = mutableListOf<String>()
                        if (courseObj.has("literature")) {
                            val arr = courseObj.getJSONArray("literature")
                            for (x in 0 until arr.length()) literature.add(arr.getString(x))
                        }
                        
                        list.add(
                            Course(
                                id = courseCode,
                                name = courseName,
                                professor = coordinator,
                                lectureHall = "Semester $semesterNum",
                                time = examType,
                                syllabusSummary = description,
                                credits = credits,
                                imageUrl = "https://picsum.photos/seed/$courseCode/400/300",
                                outcomes = outcomes,
                                contents = contents,
                                literature = literature
                            )
                        )
                    }
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getCourses(): Flow<List<Course>> = _enrolledCoursesFlow

    fun getAllAvailableCourses(): Flow<List<Course>> = flow {
        emit(allCourses ?: emptyList())
    }

    fun getCourseById(id: String): Flow<Course?> = flow {
        emit(allCourses?.find { it.id == id })
    }

    fun enrollInCourse(courseId: String) {
        val course = allCourses?.find { it.id == courseId }
        val currentList = _enrolledCoursesFlow.value.toMutableList()
        if (course != null && !currentList.contains(course)) {
            val totalCredits = currentList.sumOf { it.credits }
            if (totalCredits + course.credits <= 25) {
                currentList.add(course)
                _enrolledCoursesFlow.value = currentList
            }
        }
    }

    fun dropCourse(courseId: String) {
        val course = allCourses?.find { it.id == courseId }
        val currentList = _enrolledCoursesFlow.value.toMutableList()
        if (course != null && currentList.contains(course)) {
            currentList.remove(course)
            _enrolledCoursesFlow.value = currentList
        }
    }

    fun getProgramInfo(): Pair<String, Int> {
        return try {
            val inputStream = context.assets.open("courses.json")
            val jsonString = InputStreamReader(inputStream).readText()
            val rootObj = JSONObject(jsonString)
            val programObj = rootObj.getJSONObject("program")
            val name = programObj.getString("name")
            val totalCredits = programObj.getInt("total_credits")
            Pair(name, totalCredits)
        } catch (e: Exception) {
            Pair("University Program", 120)
        }
    }

    fun getTotalEnrolledCredits(): Flow<Int> = flow {
        _enrolledCoursesFlow.collect { courses ->
            emit(courses.sumOf { it.credits })
        }
    }
}
