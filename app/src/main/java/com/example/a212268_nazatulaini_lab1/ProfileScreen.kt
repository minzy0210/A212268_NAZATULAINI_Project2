package com.example.a212268_nazatulaini_lab1

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileScreen(
    onBack           : () -> Unit,
    onHomeClick      : () -> Unit = {},
    profileViewModel : ProfileViewModel,
    locationViewModel: LocationViewModel                          // ← NEW
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()

    // GPS state
    val placeName    by locationViewModel.placeName.collectAsStateWithLifecycle()
    val isLocating   by locationViewModel.isLoading.collectAsStateWithLifecycle()

    var displayName  by remember(profile) { mutableStateOf(profile?.displayName ?: "") }
    var bio          by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var location     by remember(profile) { mutableStateOf(profile?.location ?: "") }
    var phoneNumber  by remember(profile) { mutableStateOf(profile?.phoneNumber ?: "") }
    var saved        by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // When GPS resolves a new place name, auto-fill the location field
    LaunchedEffect(placeName) {
        if (placeName != null && location.isBlank()) {
            location = placeName!!
            saved = false
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) locationViewModel.fetchLocation()
    }

    fun requestGps() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) locationViewModel.fetchLocation()
        else permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.70f)))

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                CustomBottomNavigation(
                    onHomeClick   = onHomeClick,
                    onSearchClick = onHomeClick,
                    onEmailClick  = onHomeClick,
                    onAddClick    = onHomeClick
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                // Top bar
                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
                    Row(
                        modifier = Modifier.padding(top = 48.dp, start = 8.dp, end = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            "My Profile",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (displayName.isNotBlank()) {
                                Text(
                                    displayName.first().uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Member since badge
                    if (profile?.joinDate != null) {
                        Surface(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                "Member since ${profile!!.joinDate}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // ── Edit card ─────────────────────────────────────
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Personal Info", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                            ProfileField(
                                value = displayName, onValueChange = { displayName = it; saved = false },
                                label = "Display Name", icon = Icons.Default.Person
                            )
                            ProfileField(
                                value = bio, onValueChange = { bio = it; saved = false },
                                label = "Bio", icon = Icons.Default.Edit,
                                singleLine = false, minLines = 2
                            )

                            // ── Location field + GPS button ───────────
                            ProfileField(
                                value = location, onValueChange = { location = it; saved = false },
                                label = "Location", icon = Icons.Default.LocationOn
                            )

                            // GPS fill button
                            OutlinedButton(
                                onClick  = { requestGps() },
                                enabled  = !isLocating,
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                border   = BorderStroke(
                                    1.dp,
                                    if (isLocating) MaterialTheme.colorScheme.outlineVariant
                                    else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isLocating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Getting location…")
                                } else {
                                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (placeName != null) "Update with GPS" else "Use My GPS Location")
                                }
                            }

                            // Show the resolved place name as a hint under the button
                            if (placeName != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle, null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "GPS: $placeName",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(Modifier.weight(1f))
                                        // Tap to apply it to the field
                                        TextButton(
                                            onClick = { location = placeName!!; saved = false },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text("Use", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            ProfileField(
                                value = phoneNumber, onValueChange = { phoneNumber = it; saved = false },
                                label = "Phone Number", icon = Icons.Default.Phone
                            )
                        }
                    }

                    // ── Save button ───────────────────────────────────
                    Button(
                        onClick = {
                            profileViewModel.saveProfile(
                                displayName = displayName,
                                bio         = bio,
                                location    = location,
                                phoneNumber = phoneNumber
                            )
                            saved = true
                        },
                        enabled  = displayName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (saved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(if (saved) Icons.Default.CheckCircle else Icons.Default.Person, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (saved) "Profile Saved!" else "Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Info note
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Your profile is saved privately on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    value        : String,
    onValueChange: (String) -> Unit,
    label        : String,
    icon         : androidx.compose.ui.graphics.vector.ImageVector,
    singleLine   : Boolean = true,
    minLines     : Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label) },
        leadingIcon   = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        shape         = RoundedCornerShape(12.dp),
        singleLine    = singleLine,
        minLines      = minLines,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}