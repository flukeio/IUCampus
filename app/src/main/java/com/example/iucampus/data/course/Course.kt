package com.example.iucampus.data.course

data class Course(
    val id: String,
    val name: String,
    val professor: String,
    val lectureHall: String,
    val time: String,
    val syllabusSummary: String,
    val credits: Int = 5,
    val imageUrl: String = "",
    val outcomes: List<String> = emptyList(),
    val contents: List<String> = emptyList(),
    val literature: List<String> = emptyList()
)
