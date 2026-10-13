package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EngineProfileEntity
import com.example.engine.DownloadProgress
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EnginesScreen(
    engineProfiles: List<EngineProfileEntity>,
    activeEngineId: String,
    downloadProgressMap: Map<String, DownloadProgress>,
    isConnected: Boolean,
    uciTestOutput: List<String>?,
    onSelectEngine: (String) -> Unit,
    onDownloadEngine: (EngineProfileEntity) -> Unit,
    onCancelDownload: (String) -> Unit,
    onDeleteEngine: (String) -> Unit,
    onImportEngine: (String, Uri) -> Unit,
    onTestEngineUci: (String) -> Unit,
    onDismissTestDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var importNameInput by remember { mutableStateOf("") }
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImportUri = uri
            showImportDialog = true
        }
    }

    val builtInEngine = engineProfiles.firstOrNull { it.isBuiltIn }
    val externalProfiles = engineProfiles.filter { !it.isBuiltIn }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Engine Management",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "UCI Protocol & Offline Neural Networks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Connectivity status pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isConnected) EmeraldAccent.copy(alpha = 0.2f) else SurfaceHighlight,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Connection",
                            tint = if (isConnected) EmeraldAccent else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isConnected) "Online" else "Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) EmeraldAccent else TextSecondary
                        )
                    }
                }
            }
        }

        // Section 1: Built-in Engine
        item {
            Text(
                text = "1. BUILT-IN ENGINE (OFFLINE)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth().testTag("builtin_engine_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = "Engine",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = builtInEngine?.name ?: "Stockfish Embedded (UCI)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Version: ${builtInEngine?.version ?: "16.1 Native Mini"} • Bundled In-App",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (activeEngineId == "builtin_stockfish") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bundled directly inside the app. Works 100% offline from first launch. Automatically serves as safety fallback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTestEngineUci("builtin_stockfish") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("test_builtin_uci_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test UCI Handshake")
                        }

                        if (activeEngineId != "builtin_stockfish") {
                            Button(
                                onClick = { onSelectEngine("builtin_stockfish") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF090D16))
                            ) {
                                Text("Select as Primary")
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Downloaded Engines and Engine Packs
        item {
            Text(
                text = "2. DOWNLOADED ENGINES & PACKS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AmberAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!isConnected) {
                Text(
                    text = "Offline Mode: Showing installed packs. Connect to internet to download additional neural evaluation networks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        items(externalProfiles.filter { !it.isImported }) { profile ->
            val progress = downloadProgressMap[profile.id]
            val isDownloading = progress?.isDownloading == true
            val isDownloaded = profile.isDownloaded
            val sizeMb = "%.1f MB".format(profile.sizeBytes / (1024f * 1024f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "v${profile.version} • $sizeMb",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        if (isDownloaded) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "INSTALLED",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = profile.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Download Progress Bar
                    if (isDownloading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (progress?.progressPercent ?: 0) / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = CyanAccent,
                            trackColor = SurfaceHighlight
                        )
                        Text(
                            text = "Downloading... ${progress?.progressPercent ?: 0}%",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isDownloading) {
                            OutlinedButton(
                                onClick = { onCancelDownload(profile.id) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel")
                            }
                        } else if (!isDownloaded) {
                            Button(
                                onClick = { onDownloadEngine(profile) },
                                enabled = isConnected,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF090D16))
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download")
                            }
                        } else {
                            if (activeEngineId != profile.id) {
                                Button(
                                    onClick = { onSelectEngine(profile.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF090D16))
                                ) {
                                    Text("Apply Profile")
                                }
                            }
                            IconButton(
                                onClick = { onDeleteEngine(profile.id) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Import Engine
        item {
            Text(
                text = "3. IMPORT ENGINE / DATA FILE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth().testTag("import_engine_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Android 10+ Writable Storage Security",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "In compliance with Android security policies, downloaded/imported items are loaded as data (NNUE nets, opening books, engine configs) by the bundled engine.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().testTag("pick_engine_file_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight, contentColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick File from Device Storage", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // User Imported Engines List
        val userImports = externalProfiles.filter { it.isImported }
        if (userImports.isNotEmpty()) {
            item {
                Text(
                    text = "Imported Files",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(userImports) { imported ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = imported.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "SHA-256: ${imported.checksum.take(12)}...", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row {
                            OutlinedButton(onClick = { onTestEngineUci(imported.id) }) {
                                Text("Test UCI")
                            }
                            IconButton(onClick = { onDeleteEngine(imported.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    // Import confirmation dialog
    if (showImportDialog && selectedImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Register Imported Engine File") },
            text = {
                Column {
                    Text(
                        text = "Enter a label for this engine file/network:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importNameInput,
                        onValueChange = { importNameInput = it },
                        placeholder = { Text("e.g. Custom NNUE Net v1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedImportUri ?: return@Button
                        onImportEngine(importNameInput, uri)
                        showImportDialog = false
                        importNameInput = ""
                        selectedImportUri = null
                    }
                ) {
                    Text("Register & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // UCI Test Dialog
    if (uciTestOutput != null) {
        AlertDialog(
            onDismissRequest = onDismissTestDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldAccent)
                    Text("UCI Test Handshake OK")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Engine responded to 'uci' command:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceDark,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uciTestOutput.joinToString("\n"),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            color = CyanAccent
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismissTestDialog) {
                    Text("Close")
                }
            }
        )
    }
}
