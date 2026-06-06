package com.example.iucampus.ui.gatherings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatheringDetailScreen(
    gatheringId: String,
    onNavigateBack: () -> Unit,
    viewModel: GatheringViewModel = viewModel()
) {
    val gathering = viewModel.getGatheringById(gatheringId)
    val joinedGatherings by viewModel.joinedGatherings.collectAsState(initial = emptySet())
    val hasJoined = joinedGatherings.contains(gatheringId)
    val isOrganizer = gathering?.organizer == com.example.iucampus.data.auth.SessionManager.currentUsername
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && gathering != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Gathering") },
            text = { Text("Are you sure you want to delete '${gathering.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGathering(gathering.id)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Yes, Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No, Keep it")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gathering Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        gathering?.let { g ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(text = g.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = g.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow("Organizer", g.organizer)
                DetailRow("Location", g.location)
                DetailRow("Time", g.time)
                DetailRow("Participants", "${g.currentParticipants} out of ${g.maxParticipants}")

                Spacer(modifier = Modifier.height(32.dp))
                
                if (hasJoined) {
                    if (isOrganizer) {
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = false
                        ) {
                            Text("You are the Organizer")
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.withdrawFromGathering(g.id)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Withdraw from Gathering")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.joinGathering(g.id)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = g.currentParticipants < g.maxParticipants
                    ) {
                        Text(if (g.currentParticipants >= g.maxParticipants) "Full" else "Join Gathering")
                    }
                }

                if (isOrganizer) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete Gathering")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Gathering not found.")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
