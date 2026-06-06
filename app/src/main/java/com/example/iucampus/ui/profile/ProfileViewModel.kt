package com.example.iucampus.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iucampus.data.auth.SessionManager
import com.example.iucampus.data.course.CourseRepository
import kotlinx.coroutines.flow.*

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val programName: String = "",
    val enrolledCredits: Int = 0,
    val totalProgramCredits: Int = 120
)

class ProfileViewModel(private val repository: CourseRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val username = SessionManager.currentUsername ?: "Guest"
        val email = username.lowercase().replace(" ", ".") + "@iu-study.org"
        val (programName, totalCredits) = repository.getProgramInfo()
        
        _uiState.update { it.copy(
            username = username,
            email = email,
            programName = programName,
            totalProgramCredits = totalCredits
        ) }

        repository.getTotalEnrolledCredits()
            .onEach { enrolled ->
                _uiState.update { it.copy(enrolledCredits = enrolled) }
            }.launchIn(viewModelScope)
    }
}
