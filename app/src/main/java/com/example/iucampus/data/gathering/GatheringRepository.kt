package com.example.iucampus.data.gathering

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.io.InputStreamReader
import java.util.UUID

class GatheringRepository(private val context: Context) {

    companion object {
        private val gatheringsState = MutableStateFlow<List<Gathering>>(emptyList())
        private val joinedGatherings = MutableStateFlow<Set<String>>(emptySet())
        private var isInitialized = false
    }

    init {
        if (!isInitialized) {
            val initialGatherings = loadGatheringsFromJson()
            gatheringsState.value = initialGatherings
            isInitialized = true
        }
    }

    private fun loadGatheringsFromJson(): List<Gathering> {
        val list = mutableListOf<Gathering>()
        try {
            val inputStream = context.assets.open("gatherings.json")
            val jsonText = InputStreamReader(inputStream).readText()
            val jsonArray = JSONArray(jsonText)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Gathering(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.getString("description"),
                        organizer = obj.getString("organizer"),
                        location = obj.getString("location"),
                        time = obj.getString("time"),
                        maxParticipants = obj.getInt("max_participants"),
                        currentParticipants = obj.getInt("current_participants")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getGatherings(): Flow<List<Gathering>> = gatheringsState

    fun getJoinedGatherings(): Flow<Set<String>> = joinedGatherings

    fun getGatheringById(id: String): Gathering? = gatheringsState.value.find { it.id == id }

    fun addGathering(name: String, description: String, location: String, time: String, maxParticipants: Int) {
        val organizer = com.example.iucampus.data.auth.SessionManager.currentUsername ?: "Anonymous"
        val newEvent = Gathering(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            organizer = organizer,
            location = location,
            time = time,
            maxParticipants = maxParticipants,
            currentParticipants = 1
        )
        gatheringsState.update { current -> current + newEvent }
        joinedGatherings.update { it + newEvent.id } // Automatically join the event we create
    }

    fun joinGathering(id: String): Boolean {
        if (joinedGatherings.value.contains(id)) return false // Already joined
        
        var joined = false
        gatheringsState.update { current ->
            current.map {
                if (it.id == id && it.currentParticipants < it.maxParticipants) {
                    joined = true
                    it.copy(currentParticipants = it.currentParticipants + 1)
                } else {
                    it
                }
            }
        }
        if (joined) {
            joinedGatherings.update { it + id }
        }
        return joined
    }

    fun withdrawFromGathering(id: String): Boolean {
        if (!joinedGatherings.value.contains(id)) return false // Haven't joined
        
        val currentUsername = com.example.iucampus.data.auth.SessionManager.currentUsername
        val gathering = gatheringsState.value.find { it.id == id }
        if (gathering?.organizer == currentUsername) return false // Organizer cannot withdraw
        
        var withdrawn = false
        gatheringsState.update { current ->
            current.map {
                if (it.id == id && it.currentParticipants > 0) {
                    withdrawn = true
                    it.copy(currentParticipants = it.currentParticipants - 1)
                } else {
                    it
                }
            }
        }
        if (withdrawn) {
            joinedGatherings.update { it - id }
        }
        return withdrawn
    }

    fun deleteGathering(id: String) {
        val currentUsername = com.example.iucampus.data.auth.SessionManager.currentUsername
        val gathering = gatheringsState.value.find { it.id == id }
        if (gathering?.organizer == currentUsername) {
            gatheringsState.update { current -> current.filter { it.id != id } }
            joinedGatherings.update { it - id }
        }
    }
}
