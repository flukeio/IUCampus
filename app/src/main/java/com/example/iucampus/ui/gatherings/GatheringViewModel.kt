package com.example.iucampus.ui.gatherings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.iucampus.data.gathering.GatheringRepository

class GatheringViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GatheringRepository(application)
    val gatherings = repository.getGatherings()
    val joinedGatherings = repository.getJoinedGatherings()

    fun addGathering(name: String, description: String, location: String, time: String, maxParticipants: Int) {
        repository.addGathering(name, description, location, time, maxParticipants)
    }

    fun joinGathering(id: String) = repository.joinGathering(id)
    
    fun withdrawFromGathering(id: String) = repository.withdrawFromGathering(id)
    
    fun deleteGathering(id: String) = repository.deleteGathering(id)
    
    fun getGatheringById(id: String) = repository.getGatheringById(id)
}
