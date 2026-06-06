package com.example.iucampus.data.gathering

data class Gathering(
    val id: String,
    val name: String,
    val description: String,
    val organizer: String,
    val location: String,
    val time: String,
    val maxParticipants: Int,
    var currentParticipants: Int
)
