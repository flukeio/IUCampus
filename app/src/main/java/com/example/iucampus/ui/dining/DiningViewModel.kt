package com.example.iucampus.ui.dining

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.iucampus.data.dining.DiningFacility
import com.example.iucampus.data.dining.DiningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiningViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiningRepository(application)

    private val _facilities = MutableStateFlow<List<DiningFacility>>(emptyList())
    val facilities: StateFlow<List<DiningFacility>> = _facilities.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFacilities().collect {
                _facilities.value = it
            }
        }
    }
}
