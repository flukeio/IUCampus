package com.example.iucampus.ui.dining

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iucampus.data.dining.DiningFacility
import com.example.iucampus.BuildConfig
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.example.iucampus.R
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiningMapScreen(viewModel: DiningViewModel = viewModel()) {
    val facilities by viewModel.facilities.collectAsState()
    var selectedFacility by remember { mutableStateOf<DiningFacility?>(null) }
    
    val iuCampusLatLng = LatLng(50.64197955454996, 7.228797445839009)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(iuCampusLatLng, 15f)
    }
    
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { facilities.size })
    val coroutineScope = rememberCoroutineScope()

    // Sync Map Camera when Pager is scrolled
    LaunchedEffect(pagerState.settledPage) {
        if (facilities.isNotEmpty()) {
            val facility = facilities[pagerState.settledPage]
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(LatLng(facility.latitude, facility.longitude), 18f),
                durationMs = 500
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapClick = { showBottomSheet = false }
        ) {
            facilities.forEachIndexed { index, facility ->
                val iconRes = when (facility.cuisine) {
                    "Hotel" -> R.drawable.ic_pin_hotel
                    "Restaurant" -> R.drawable.ic_pin_restaurant
                    "Cafe" -> R.drawable.ic_pin_cafe
                    "Bakery" -> R.drawable.ic_pin_bakery
                    else -> null
                }
                
                Marker(
                    state = MarkerState(position = LatLng(facility.latitude, facility.longitude)),
                    icon = iconRes?.let { BitmapDescriptorFactory.fromResource(it) },
                    title = facility.name,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                        selectedFacility = facility
                        showBottomSheet = true
                        true
                    }
                )
            }

            Marker(
                state = MarkerState(position = iuCampusLatLng),
                title = "IU University",
                anchor = Offset(x = 0.5f, y = 0.5f),
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_iu_university_pin)
            )
        }
        
        // FAB to center to University
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(iuCampusLatLng, 17f),
                        durationMs = 500
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 185.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_gps_fixed),
                contentDescription = "Center to IU",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }
        
        // Horizontal Card List
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp
            ) { page ->
                val facility = facilities[page]
                DiningCard(
                    facility = facility,
                    onClick = {
                        selectedFacility = facility
                        showBottomSheet = true
                    }
                )
            }
        }
        
        if (showBottomSheet && selectedFacility != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                DiningDetail(selectedFacility!!)
            }
        }
    }
}

@Composable
fun DiningCard(facility: DiningFacility, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (facility.image.isNotEmpty()) {
                val apiKey = BuildConfig.MAPS_API_KEY
                val imageUrl = "https://places.googleapis.com/v1/${facility.image}/media?maxHeightPx=400&maxWidthPx=400&key=$apiKey"
                AsyncImage(
                    model = imageUrl,
                    contentDescription = facility.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = facility.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = facility.address,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${facility.rate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFBC02D)
                    )
                    Text(
                        text = " • ${facility.cuisine}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun DiningDetail(facility: DiningFacility) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (facility.image.isNotEmpty()) {
            val apiKey = BuildConfig.MAPS_API_KEY
            val imageUrl = "https://places.googleapis.com/v1/${facility.image}/media?maxHeightPx=400&maxWidthPx=400&key=$apiKey"
            AsyncImage(
                model = imageUrl,
                contentDescription = facility.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(bottom = 16.dp)
            )
        }
        Text(text = facility.name, style = MaterialTheme.typography.titleLarge)
        Text(text = "${facility.cuisine} • ★ ${facility.rate}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Address: ${facility.address}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(text = "Phone: ${facility.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (facility.opening.isNotEmpty()) {
            Text(text = "Opening Hours", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            facility.opening.forEach { item ->
                Text(text = "• $item", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                val uri = "geo:${facility.latitude},${facility.longitude}?q=${facility.latitude},${facility.longitude}(${facility.name})"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Get Directions")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
