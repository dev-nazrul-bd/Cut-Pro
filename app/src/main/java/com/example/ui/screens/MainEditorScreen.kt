package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import kotlinx.coroutines.delay
import com.example.data.model.AudioClip
import com.example.data.model.TextClip
import com.example.data.model.VideoClip
import com.example.data.model.VideoProject
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorTab
import com.example.ui.viewmodel.VideoEditorViewModel

@Composable
fun MainEditorScreen(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (currentProject == null) {
            // Project Lobby / Landing
            ProjectLobbyView(
                projects = projects,
                onCreateProject = { name, aspect -> viewModel.createProject(name, aspect) },
                onSelectProject = { proj -> viewModel.selectProject(proj) },
                onDeleteProject = { proj -> viewModel.deleteProject(proj) }
            )
        } else {
            // Workspace Application Editor
            VideoWorkspaceView(viewModel = viewModel, project = currentProject!!)
        }

        // Action Toast banner overlay
        toastMessage?.let { msg ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
                    .testTag("toast_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// 1. PROJECT LOBBY VIEW (LANDING BOARD)
// ==========================================
@Composable
fun ProjectLobbyView(
    projects: List<VideoProject>,
    onCreateProject: (String, String) -> Unit,
    onSelectProject: (VideoProject) -> Unit,
    onDeleteProject: (VideoProject) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Futuristic Brand Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        CutProPrimaryNeonPink,
                                        CutProSecondaryNeonCyan
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Cut Pro Logo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cut Pro",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "PRO VIDEO STUDIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 2.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("add_project_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Project",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Main Creative CTA Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        CutProPrimaryNeonPink.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.9f,
                                center = Offset(size.width * 0.85f, size.height * 0.5f)
                            )
                        }
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = CutProPrimaryNeonPink.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "NEW RELEASE V1.0",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CutProPrimaryNeonPink,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Create a New Studio Video Project",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Design TikToks, YouTube shorts, or cinema films with our multi-track real-time workbench.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Start Editing",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CutProSecondaryNeonCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = CutProSecondaryNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Project List Section Title
        item {
            Text(
                text = "Recent Drafts (${projects.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (projects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No drafts yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Tap the plus button above to begin your first project.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(projects) { proj ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectProject(proj) }
                        .testTag("project_draft_item"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aspect Ratio Text Badge
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proj.aspectRatio,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = proj.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(proj.aspectRatio, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${proj.durationMs / 1000}s duration",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteProject(proj) },
                            modifier = Modifier.testTag("delete_project_${proj.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Mock tutorial guides
        item {
            Text(
                text = "Learn to Edit like a Pro",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    MockTutorialCard(
                        title = "Mastering Speech with AI",
                        duration = "3 min read",
                        gradient = listOf(Color(0xFF8E24AA), Color(0xFFE1BEE7))
                    )
                }
                item {
                    MockTutorialCard(
                        title = "Speed Ramping Secrets",
                        duration = "5 min read",
                        gradient = listOf(Color(0xFFE64A19), Color(0xFFFFCC80))
                    )
                }
                item {
                    MockTutorialCard(
                        title = "Chroma Key & PIP overlays",
                        duration = "4 min read",
                        gradient = listOf(Color(0xFF00796B), Color(0xFFB2DFDB))
                    )
                }
            }
        }
    }

    // CREATE DRAFT PROJECT DIALOG
    if (showCreateDialog) {
        var projName by remember { mutableStateOf("") }
        var selectedAspect by remember { mutableStateOf("16:9") }

        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Initialize Project",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = projName,
                        onValueChange = { projName = it },
                        placeholder = { Text("E.g., Summer Vlog 2026") },
                        label = { Text("Project Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_project_name_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Choose Aspect Ratio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row of aspect ratio chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair("16:9", "Wide"),
                            Pair("9:16", "TikTok"),
                            Pair("1:1", "Square"),
                            Pair("4:3", "Tablet")
                        ).forEach { (aspect, desc) ->
                            FilterChip(
                                selected = selectedAspect == aspect,
                                onClick = { selectedAspect = aspect },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .border(1.dp, if (selectedAspect == aspect) Color.White else MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                    )
                                },
                                label = { Column {
                                    Text(aspect, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(desc, fontSize = 8.sp)
                                } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val finalName = projName.ifEmpty { "My Video ${System.currentTimeMillis() % 1000}" }
                            onCreateProject(finalName, selectedAspect)
                            showCreateDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_confirm_create_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Launch Canvas")
                    }
                }
            }
        }
    }
}

@Composable
fun MockTutorialCard(
    title: String,
    duration: String,
    gradient: List<Color>
) {
    Card(
        modifier = Modifier
            .size(150.dp, 120.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
            Text(
                text = duration,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}


// ==========================================
// 2. TIMELINE EDITOR ACTIVE STUDIO WORKSPACE
// ==========================================
@Composable
fun VideoWorkspaceView(
    viewModel: VideoEditorViewModel,
    project: VideoProject
) {
    val playbackPos by viewModel.playbackPosition.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val selectedClipId by viewModel.selectedClipId.collectAsStateWithLifecycle()
    val selectedAudioClipId by viewModel.selectedAudioClipId.collectAsStateWithLifecycle()
    val selectedTextClipId by viewModel.selectedTextClipId.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isRecordingVoiceover by viewModel.isRecordingVoiceover.collectAsStateWithLifecycle()
    val autoCaptionsActive by viewModel.isGeneratingCaptions.collectAsStateWithLifecycle()

    var showExportModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // TOP RETRO HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.selectProject(null) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Save and Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = project.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aspect: ${project.aspectRatio} • ${project.width}x${project.height}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Export Video Action Button
            Button(
                onClick = { showExportModal = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CutProPrimaryNeonPink
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("export_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // MAIN PREVIEW PLAYER COMPONENT (35% height layout weight)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            ResponsiveCanvasPlayer(
                aspectRatioStr = project.aspectRatio,
                playbackMs = playbackPos,
                viewModel = viewModel
            )

            // Dynamic mic overlay of Voiceover Active
            if (isRecordingVoiceover) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🎤 RECORDING VOICEOVER SIMULATOR",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // AI Captions Processing overlay
            if (autoCaptionsActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CutProSecondaryNeonCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "🪄 Gemini AI: Isolating speech frequencies...",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // PLAYBACK AND QUICK TRIMMING BAR CONTROLS
        PlaybackControlsToolbar(
            playbackMs = playbackPos,
            durationMs = project.durationMs,
            isPlaying = isPlaying,
            onPlayPause = { viewModel.togglePlay() },
            onSeek = { seek -> viewModel.setPlaybackPosition(seek) },
            onSplit = { viewModel.splitSelectedClip() },
            onTrimStart = { viewModel.trimSelectedClipStart() },
            onTrimEnd = { viewModel.trimSelectedClipEnd() },
            isClipSelected = selectedClipId != null
        )

        // DRILL TRACKS VISUAL TIMELINE MULTI-LANE CONSTRUCT (30% weight)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.32f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            TimelineTracksWorkspace(
                viewModel = viewModel,
                project = project,
                playbackMs = playbackPos,
                selectedClipId = selectedClipId,
                selectedAudioClipId = selectedAudioClipId,
                selectedTextClipId = selectedTextClipId
            )
        }

        // CONTEXTUAL PROPERTY ACCENTS BOTTOM TABS BAR (23% weight)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.23f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            BottomContextEditWorkspace(
                activeTab = activeTab,
                selectedClipId = selectedClipId,
                selectedAudioClipId = selectedAudioClipId,
                selectedTextClipId = selectedTextClipId,
                viewModel = viewModel
            )
        }

        // THE PRIMARY ACCESSORIES OPTIONS SELECTOR TABBAR
        StudioToolbarTabs(
            activeTab = activeTab,
            onTabSelected = { viewModel.setEditorTab(it) }
        )
    }

    // EXPORT COMPILATION MODAL WINDOW
    if (showExportModal) {
        var progressState by remember { mutableStateOf(0f) }
        var isExportFinished by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // Simulate professional 60fps frame compilation process
            for (i in 1..100) {
                delay(40)
                progressState = i / 100f
            }
            isExportFinished = true
        }

        Dialog(onDismissRequest = { if (isExportFinished) showExportModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isExportFinished) {
                        Text(
                            text = "Compiling Cut Pro Video",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Merging layers, speed ramping, transitions and LUT overlays in high resolution...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            CircularProgressIndicator(
                                progress = { progressState },
                                modifier = Modifier.size(90.dp),
                                strokeWidth = 8.dp,
                                color = CutProPrimaryNeonPink,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Text(
                                text = "${(progressState * 100).toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Do not minimize or lock screen.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        // EXPORT COMPLETE SHEET
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Export Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Export Completed!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The finished masterpiece MP4 has been compiles in 1080p Full HD & is saved in your local gallery.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Share Directly To",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            listOf("TikTok", "YouTube", "Instagram").forEach { social ->
                                OutlinedButton(
                                    onClick = { viewModel.showToast("Shared to $social!") },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(social, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showExportModal = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to Studio")
                        }
                    }
                }
            }
        }
    }
}

// Custom recording scale glow animation
@Composable
fun Modifier.animateInfiniteMicGlow(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAnim"
    )
    return this.drawBehind {
        drawCircle(
            color = Color.Red.copy(alpha = alpha * 0.25f),
            radius = size.maxDimension * 0.7f
        )
    }
}


// ==========================================
// 3. RESPONSIVE VIDEO CANVAS PLAYER
// ==========================================
@Composable
fun ResponsiveCanvasPlayer(
    aspectRatioStr: String,
    playbackMs: Long,
    viewModel: VideoEditorViewModel
) {
    // Collect active clips
    val videoClips = viewModel.videoClips
    val drawingPaths = viewModel.drawingPaths
    val activeColor = viewModel.activeDrawingColor.collectAsStateWithLifecycle()
    val activeWidth = viewModel.activeDrawingWidth.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    // Determine the sizing based on project configuration
    val boxAspect = when (aspectRatioStr) {
        "9:16" -> 9f / 16f
        "1:1" -> 1f
        "4:3" -> 4f / 3f
        else -> 16f / 9f
    }

    // Capture dragging and drawing on the player when BRUSH tab is active
    val brushModifier = if (activeTab == EditorTab.BRUSH) {
        Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    viewModel.startNewBrushPath()
                    viewModel.addPointToBrushPath(offset.x, offset.y)
                },
                onDrag = { change, _ ->
                    change.consume()
                    viewModel.addPointToBrushPath(change.position.x, change.position.y)
                },
                onDragEnd = {
                    viewModel.finalizeBrushPath()
                }
            )
        }
    } else Modifier

    // Outer framing card bounding box
    Card(
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .aspectRatio(boxAspect)
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .then(brushModifier)
            .testTag("preview_player_canvas"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111115))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // 1. COMPOSITION AND COLOR LAYERS RENDERING
            
            // Render Background Base track clip
            val currentBaseClip = videoClips.firstOrNull {
                !it.isPIP && playbackMs >= it.startInTimelineMs && playbackMs < (it.startInTimelineMs + it.durationMs)
            }

            if (currentBaseClip != null) {
                RenderClipLayer(clip = currentBaseClip, modifier = Modifier.fillMaxSize())
            } else {
                // Empty background state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No segment on track",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp
                    )
                }
            }

            // Render Overlay/PIP layer clips
            val currentPipClips = videoClips.filter {
                it.isPIP && playbackMs >= it.startInTimelineMs && playbackMs < (it.startInTimelineMs + it.durationMs)
            }

            currentPipClips.forEach { pipClip ->
                // Apply pip coordinates
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    RenderClipLayer(
                        clip = pipClip,
                        modifier = Modifier
                            .fillMaxSize(pipClip.pipWidth)
                            .align(Alignment.Center)
                            .offset(
                                x = ((pipClip.pipX - 0.5f) * 150).dp,
                                y = ((pipClip.pipY - 0.5f) * 180).dp
                            )
                            .border(1.dp, CutProSecondaryNeonCyan, RoundedCornerShape(4.dp))
                    )
                }
            }

            // 2. LIVE DRAWING/BRUSH OVERLAYS CANVAS LAYER
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw existing saved drawings
                drawingPaths.forEach { drawPath ->
                    if (drawPath.points.isNotEmpty()) {
                        val path = Path().apply {
                            drawPath.points.firstOrNull()?.let { moveTo(it.x, it.y) }
                            drawPath.points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = path,
                            color = Color(drawPath.color),
                            style = Stroke(
                                width = drawPath.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Temporary active pointer path being drawn
                if (viewModel.currentDrawingPoints.isNotEmpty()) {
                    val activePath = Path().apply {
                        viewModel.currentDrawingPoints.firstOrNull()?.let { moveTo(it.x, it.y) }
                        viewModel.currentDrawingPoints.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = activePath,
                        color = Color(activeColor.value),
                        style = Stroke(
                            width = activeWidth.value,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // 3. TEXT / SUBTITLE AND AUTO-CAPTIONS RENDERING Overlays
            val activeTextClips = viewModel.textClips.filter {
                playbackMs >= it.startInTimelineMs && playbackMs < (it.startInTimelineMs + it.durationMs)
            }

            activeTextClips.forEach { textOverlay ->
                // Render with preset type-animations style
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp)
                ) {
                    RenderAnimatedText(
                        textClip = textOverlay,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RenderClipLayer(
    clip: VideoClip,
    modifier: Modifier = Modifier
) {
    // Generate simulated effects color matrices
    val filterColorFilter = when (clip.filterType) {
        "Noir" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        "Vintage" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            0.95f, 0.05f, 0f, 0f, 20f,
            0.05f, 0.85f, 0f, 0f, 15f,
            0f, 0.05f, 0.65f, 0f, 10f,
            0f, 0f, 0f, 1.0f, 0f
        )))
        "Cyberpunk" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            1.2f, 0f, 0.5f, 0f, 30f,
            0f, 0.8f, 1.0f, 0f, -20f,
            1.0f, 0f, 1.5f, 0f, 40f,
            0f, 0f, 0f, 1.0f, 0f
        )))
        "Forest" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            0.6f, 0.2f, 0f, 0f, 0f,
            0.1f, 1.3f, 0.1f, 0f, 10f,
            0f, 0.2f, 0.7f, 0f, 0f,
            0f, 0f, 0f, 1.0f, 0f
        )))
        "Warm Gold" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            1.1f, 0.1f, 0f, 0f, 25f,
            0.1f, 1.0f, 0f, 0f, 15f,
            0f, 0f, 0.8f, 0f, -10f,
            0f, 0f, 0f, 1.0f, 0f
        )))
        else -> null
    }

    // Reconcile scale, flips, rotations
    val rotationMatrix = GraphicsLayerScope().apply {
        rotationZ = clip.rotation
        scaleX = clip.scale * (if (clip.flipHorizontal) -1f else 1f)
        scaleY = clip.scale * (if (clip.flipVertical) -1f else 1f)
    }

    // Overlay Chroma Green Screen Simulator background removal
    val bgModifier = if (clip.hasChromaKey) {
        // Chroma green is substituted with a cool futuristic pattern
        Modifier.background(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0D47A1), Color(0xFF001529))
            )
        )
    } else if (clip.hasBgRemoved) {
        // Transparent back replaced with premium checker mesh
        Modifier.background(Color(0xFF2E0854)) // Fancy AI Dark Violet glow
    } else {
        Modifier.background(Color(clip.placeholderColor))
    }

    Box(
        modifier = modifier
            .then(bgModifier)
            .graphicsLayer {
                rotationZ = clip.rotation
                scaleX = clip.scale * (if (clip.flipHorizontal) -1f else 1f)
                scaleY = clip.scale * (if (clip.flipVertical) -1f else 1f)
            },
        contentAlignment = Alignment.Center
    ) {
        // Base illustrative canvas simulating active video camera recording frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            
            // Draw Chroma removal simulator mesh
            if (clip.hasChromaKey) {
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.15f),
                    radius = size.minDimension * 0.4f,
                    center = Offset(size.width * 0.3f, size.height * 0.4f)
                )
            } else if (clip.hasBgRemoved) {
                // Glow mesh representing AI cutout silhouette
                drawRect(
                    color = Color.White.copy(alpha = 0.08f),
                    size = size,
                )
            } else {
                // Plain layout rendering colors
            }
        }

        // Clip label
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
            val visualFXLabel = if (clip.effectFX != "None") " [FX: ${clip.effectFX}]" else ""
            Text(
                text = "${clip.name}$visualFXLabel",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 4f))
            )

            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (clip.isMotionTracked) {
                    Icon(Icons.Default.Settings, null, tint = CutProSecondaryNeonCyan, modifier = Modifier.size(12.dp))
                }
                if (clip.keyframes.isNotEmpty()) {
                    Icon(Icons.Default.Lock, null, tint = CutProTertiaryGold, modifier = Modifier.size(12.dp))
                }
                if (clip.hasChromaKey) {
                    Text("CHROMA", fontSize = 8.sp, color = Color.Green, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Black, RoundedCornerShape(2.dp)).padding(horizontal = 2.dp))
                }
                if (clip.hasBgRemoved) {
                    Text("AI CUTOUT", fontSize = 8.sp, color = CutProPrimaryNeonPink, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Black, RoundedCornerShape(2.dp)).padding(horizontal = 2.dp))
                }
            }
        }
    }
}

@Composable
fun RenderAnimatedText(
    textClip: TextClip,
    modifier: Modifier = Modifier
) {
    // Implement preset transitions
    val duration = textClip.durationMs
    val animationType = textClip.animationType

    var visible by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "anim")
    val sizeScale by infiniteTransition.animateFloat(
        initialValue = if (animationType == "Spring Pop") 0.9f else 1.0f,
        targetValue = if (animationType == "Spring Pop") 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.65f)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .padding(horizontal = 12.dp)
            .graphicsLayer {
                scaleX = sizeScale
                scaleY = sizeScale
            }
    ) {
        Text(
            text = textClip.text,
            color = Color(textClip.fontColor),
            fontSize = textClip.fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = when (textClip.fontName) {
                "Monospace" -> FontFamily.Monospace
                "Serif" -> FontFamily.Serif
                else -> FontFamily.Default
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}


// ==========================================
// 4. TIMELINE PLAYBACK SECTOR CONTROL METADATA
// ==========================================
@Composable
fun PlaybackControlsToolbar(
    playbackMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSplit: () -> Unit,
    onTrimStart: () -> Unit,
    onTrimEnd: () -> Unit,
    isClipSelected: Boolean
) {
    // Math to convert ms -> format (00:00.0)
    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val deci = (ms % 1000) / 100
        return String.format("%02d:%02d.%d", min, sec, deci)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Seek Bar Slider
        Slider(
            value = playbackMs.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("timeline_main_slider"),
            colors = SliderDefaults.colors(
                thumbColor = CutProPrimaryNeonPink,
                activeTrackColor = CutProPrimaryNeonPink.copy(alpha = 0.5f),
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Time Labels
            Text(
                text = "${formatTime(playbackMs)} / ${formatTime(durationMs)}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Playback buttons Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSeek(0L) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, "Back start", tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = { onPlayPause() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("studio_play_button")
                ) {
                    if (isPlaying) {
                        Text("‖", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                    }
                }

                IconButton(onClick = { onSeek(durationMs) }) {
                    Icon(Icons.Default.KeyboardArrowRight, "To end", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Quick timeline edit buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Split Action Button
                IconButton(
                    onClick = onSplit,
                    enabled = isClipSelected,
                    modifier = Modifier
                        .background(
                            if (isClipSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .size(34.dp)
                        .testTag("action_split_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Split",
                        tint = if (isClipSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Trim Start Action Button
                IconButton(
                    onClick = onTrimStart,
                    enabled = isClipSelected,
                    modifier = Modifier
                        .background(
                            if (isClipSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .size(34.dp)
                        .testTag("action_trim_start")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Trim Left",
                        tint = if (isClipSelected) CutProSecondaryNeonCyan else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Trim End Action Button
                IconButton(
                    onClick = onTrimEnd,
                    enabled = isClipSelected,
                    modifier = Modifier
                        .background(
                            if (isClipSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .size(34.dp)
                        .testTag("action_trim_end")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Trim Right",
                        tint = if (isClipSelected) CutProSecondaryNeonCyan else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


// ==========================================
// 5. THE MULTI-TRACK TIMELINE CHASSIS GRID
// ==========================================
@Composable
fun TimelineTracksWorkspace(
    viewModel: VideoEditorViewModel,
    project: VideoProject,
    playbackMs: Long,
    selectedClipId: String?,
    selectedAudioClipId: String?,
    selectedTextClipId: String?
) {
    val totalMs = project.durationMs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        // Inner workspace stretching proportionate to scale (1px per 1ms, total 15000px, scoped to 900dp visually)
        val timelineWidthDp = 900.dp

        Column(
            modifier = Modifier
                .width(timelineWidthDp)
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Video and Overlay PIP Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎥 VIDEO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(60.dp).padding(start = 6.dp)
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Draw base timeline clips
                    viewModel.videoClips.forEach { clip ->
                        val startFraction = clip.startInTimelineMs.toFloat() / totalMs
                        val durFraction = clip.durationMs.toFloat() / totalMs

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(durFraction)
                                .fillMaxHeight(0.9f)
                                .align(Alignment.CenterStart)
                                .offset(x = (startFraction * 840).dp) // absolute offset proportion mapping
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (clip.id == selectedClipId) 2.dp else 0.dp,
                                    color = if (clip.id == selectedClipId) CutProPrimaryNeonPink else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (clip.isPIP) Color(clip.placeholderColor).copy(alpha = 0.8f)
                                    else Color(clip.placeholderColor)
                                )
                                .clickable { viewModel.selectClip(clip.id) }
                                .padding(6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = clip.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 2. Animated Text overlay Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 TEXT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(60.dp).padding(start = 6.dp)
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    viewModel.textClips.forEach { text ->
                        val startFract = text.startInTimelineMs.toFloat() / totalMs
                        val durFract = text.durationMs.toFloat() / totalMs

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(durFract)
                                .fillMaxHeight(0.85f)
                                .align(Alignment.CenterStart)
                                .offset(x = (startFract * 840).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (text.id == selectedTextClipId) 2.dp else 0.dp,
                                    color = if (text.id == selectedTextClipId) CutProSecondaryNeonCyan else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (text.isAutoCaption) Color(0x35FFEB3B)
                                    else Color(0xFF3F51B5)
                                )
                                .clickable { viewModel.selectTextClip(text.id) }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = text.text,
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 3. Audio Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎵 AUDIO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(60.dp).padding(start = 6.dp)
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    viewModel.audioClips.forEach { audio ->
                        val startFract = audio.startInTimelineMs.toFloat() / totalMs
                        val durFract = audio.durationMs.toFloat() / totalMs

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(durFract)
                                .fillMaxHeight(0.85f)
                                .align(Alignment.CenterStart)
                                .offset(x = (startFract * 840).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (audio.id == selectedAudioClipId) 2.dp else 1.dp,
                                    color = if (audio.id == selectedAudioClipId) CutProTertiaryGold else Color(0x3000E676),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (audio.voiceChanger != "None") Color(0x906200EA) // Purple voice changer decoration
                                    else Color(0xFF00C853)
                                )
                                .clickable { viewModel.selectAudioClip(audio.id) }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = audio.name,
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Draggable Playhead vertical cursor line overlay
        val playheadFrac = playbackMs.toFloat() / totalMs
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .offset(x = (playheadFrac * 840 + 60).dp) // offsets past label margins
                .background(CutProPrimaryNeonPink)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(CutProPrimaryNeonPink, CircleShape)
                    .align(Alignment.TopCenter)
            )
        }
    }
}


// ==========================================
// 6. DETAILED BOTTOM CONTEXT EDIT DESKTOP
// ==========================================
@Composable
fun BottomContextEditWorkspace(
    activeTab: EditorTab,
    selectedClipId: String?,
    selectedAudioClipId: String?,
    selectedTextClipId: String?,
    viewModel: VideoEditorViewModel
) {
    val context = LocalContext.current
    val videoClips = viewModel.videoClips
    val audioClips = viewModel.audioClips
    val textClips = viewModel.textClips

    // Select the currently targeted objects
    val currentClip = videoClips.firstOrNull { it.id == selectedClipId }
    val currentAudio = audioClips.firstOrNull { it.id == selectedAudioClipId }
    val currentText = textClips.firstOrNull { it.id == selectedTextClipId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (activeTab) {
            EditorTab.CLIP -> {
                if (currentClip != null) {
                    Text("Transform Segment: ${currentClip.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { viewModel.updateClipRotation(rotateAngle = currentClip.rotation + 90f) }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rotate 90°", fontSize = 11.sp)
                        }
                        Button(onClick = { viewModel.updateClipRotation(flipH = !currentClip.flipHorizontal) }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flip Horiz", fontSize = 11.sp)
                        }
                        Button(onClick = { viewModel.updateClipRotation(flipV = !currentClip.flipVertical) }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flip Vert", fontSize = 11.sp)
                        }
                    }
                    // Zoom Crop slider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Crop & Zoom: ", fontSize = 11.sp)
                        Slider(
                            value = currentClip.scale,
                            onValueChange = { viewModel.updateClipScalePos(it) },
                            valueRange = 0.5f..3.0f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format("%.1fx", currentClip.scale), fontSize = 11.sp)
                    }
                } else {
                    SelectClipHelperTip("video segment")
                }
            }

            EditorTab.PIP -> {
                Text("Overlays & PIP layers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { viewModel.addPIPOverlay("Cute Sticker Layer", 0xFFFF4081) }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sticker PIP", fontSize = 11.sp)
                    }
                    Button(onClick = { viewModel.addPIPOverlay("Webcam Face Cam", 0xFF607D8B) }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Facecam PIP", fontSize = 11.sp)
                    }
                    if (currentClip?.isPIP == true) {
                        Button(onClick = { viewModel.deleteSelectedClip() }) {
                            Text("Remove PIP", fontSize = 11.sp)
                        }
                    }
                }
                if (currentClip?.isPIP == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Selected PIP blending: ${currentClip.blendMode}", fontSize = 10.sp)
                }
            }

            EditorTab.FILTER -> {
                if (currentClip != null) {
                    Text("Select Beautiful Filter / Color Presets", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Noir", "Vintage", "Cyberpunk", "Forest", "Warm Gold").forEach { filter ->
                            FilterChip(
                                selected = currentClip.filterType == filter,
                                onClick = { viewModel.updateClipFilter(filter) },
                                label = { Text(filter, fontSize = 10.sp) }
                            )
                        }
                    }
                } else {
                    SelectClipHelperTip("video track clip")
                }
            }

            EditorTab.FX -> {
                if (currentClip != null) {
                    Text("Select Advanced Video FX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Glitch", "VHS", "Blur", "Film Grain", "RGB Split", "Pixelate").forEach { fx ->
                            FilterChip(
                                selected = currentClip.effectFX == fx,
                                onClick = { viewModel.updateClipEffect(fx) },
                                label = { Text(fx, fontSize = 10.sp) }
                            )
                        }
                    }
                } else {
                    SelectClipHelperTip("video track clip")
                }
            }

            EditorTab.CHROMA -> {
                if (currentClip != null) {
                    Text("Green Screen / Chroma Keying", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Chroma Key Active", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = currentClip.hasChromaKey,
                            onCheckedChange = { viewModel.updateClipChroma(it) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    var isBlueScreen by remember { mutableStateOf(false) }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                isBlueScreen = false
                                viewModel.updateClipChroma(true, 0x00FF00)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                        ) {
                            Text("Select Green", fontSize = 10.sp, color = Color.Black)
                        }
                        Button(
                            onClick = {
                                isBlueScreen = true
                                viewModel.updateClipChroma(true, 0x0000FF)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                        ) {
                            Text("Select Blue", fontSize = 10.sp, color = Color.White)
                        }
                    }
                } else {
                    SelectClipHelperTip("overlays background clip")
                }
            }

            EditorTab.CAPTIONS -> {
                Text("Auto Captions / Subtitles (Gemini AI Powered)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CutProPrimaryNeonPink)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.generateAutoCaptionsWithGemini() },
                    colors = ButtonDefaults.buttonColors(containerColor = CutProPrimaryNeonPink)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI: Generate Subtitles", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Analyzes project audio metadata and populates styled subtitles.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            EditorTab.TEXT -> {
                var newTextValue by remember { mutableStateOf("") }
                Text("Insert Styled Animated Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newTextValue,
                        onValueChange = { newTextValue = it },
                        placeholder = { Text("Enter Overlay Title...") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTextValue.isNotEmpty()) {
                                viewModel.addTextOverlay(newTextValue, "Spring Pop")
                                newTextValue = ""
                            }
                        }
                    ) {
                        Text("Add", fontSize = 11.sp)
                    }
                }
            }

            EditorTab.STICKER -> {
                Text("Stickers & Quick Emojis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("🔥", "🎉", "🎥", "🎬", "⚡", "❤️", "🤩", "🤔", "👑", "🚀").forEach { emoji ->
                        IconButton(
                            onClick = { viewModel.addTextOverlay(emoji, "Spring Pop") },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            }

            EditorTab.AUDIO -> {
                Text("Simulated Mic Voiceover & Tracks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CutProTertiaryGold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Record trigger
                    Button(
                        onClick = { viewModel.startVoiceoverRecording() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rec Voiceover", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { viewModel.stopVoiceoverRecording() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Save Rec", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { viewModel.addAudioTrack("Sound Effect Sweep", "sfx_sweep") }
                    ) {
                        Text("+ SFX Sync", fontSize = 11.sp)
                    }
                }
                
                if (currentAudio != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Active Audio Voice Changer: ${currentAudio.voiceChanger}", fontSize = 10.sp)
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("None", "Robot", "Chipmunk", "Deep Echo", "Helium").forEach { changer ->
                            FilterChip(
                                selected = currentAudio.voiceChanger == changer,
                                onClick = { viewModel.updateAudioClipProperties(currentAudio.volume, currentAudio.fadeInMs, currentAudio.fadeOutMs, currentAudio.noiseReduction, changer) },
                                label = { Text(changer, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            }

            EditorTab.SPEED -> {
                if (currentClip != null) {
                    Text("Speed Controls (Current: ${currentClip.speed}x)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf(0.25f, 0.5f, 1.0f, 2.0f, 4.0f).forEach { speed ->
                            FilterChip(
                                selected = currentClip.speed == speed,
                                onClick = { viewModel.updateClipSpeed(speed, currentClip.speedRampType) },
                                label = { Text("${speed}x", fontSize = 10.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Creative Speed Ramp Curves", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("None", "Hero Curve", "Bullet", "Montage").forEach { ramp ->
                            FilterChip(
                                selected = currentClip.speedRampType == ramp,
                                onClick = { viewModel.updateClipSpeed(currentClip.speed, ramp) },
                                label = { Text(ramp, fontSize = 9.sp) }
                            )
                        }
                    }
                } else {
                    SelectClipHelperTip("video segment")
                }
            }

            EditorTab.KEYFRAME -> {
                if (currentClip != null) {
                    Text("Keyframe Animations Control", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { viewModel.addKeyframeToSelected() }) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Keyframe Point", fontSize = 11.sp)
                        }
                        Button(onClick = { viewModel.toggleMotionTracking() }) {
                            Icon(Icons.Default.Settings, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentClip.isMotionTracked) "Lock Tracking" else "Motion Track Object", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total keyframes set: ${currentClip.keyframes.size}", fontSize = 10.sp)
                } else {
                    SelectClipHelperTip("video segment")
                }
            }

            EditorTab.BRUSH -> {
                Text("Real-Time Drawing Brush Canvas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CutProPrimaryNeonPink)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Color palette: ", fontSize = 10.sp)
                    listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFFFFFF).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (viewModel.activeDrawingColor.collectAsStateWithLifecycle().value == color) 2.dp else 0.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                                .background(Color(color))
                                .clickable { viewModel.setDrawingColor(color) }
                                .padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { viewModel.clearAllDrawings() }) {
                        Text("Clear Drawings", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Draw directly on the preview video above!", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            EditorTab.AI_REMOVE -> {
                if (currentClip != null) {
                    Text("AI Background Removal (Intelligent Cutout)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CutProPrimaryNeonPink)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.toggleBgRemoval() },
                        colors = ButtonDefaults.buttonColors(containerColor = CutProPrimaryNeonPink)
                    ) {
                        Icon(Icons.Default.Settings, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentClip.hasBgRemoved) "Restore Original Background" else "Isolate and Remove Background", fontSize = 12.sp)
                    }
                } else {
                    SelectClipHelperTip("video segment")
                }
            }
        }
    }
}

@Composable
fun SelectClipHelperTip(trackName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap a $trackName on the timeline to configure parameters.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}


// ==========================================
// 7. STUDIO TOOLBAR TABS SECTOR SYSTEM
// ==========================================
@Composable
fun StudioToolbarTabs(
    activeTab: EditorTab,
    onTabSelected: (EditorTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        tonalElevation = 8.dp
    ) {
        listOf(
            Triple(EditorTab.CLIP, Icons.Default.Edit, "Clip"),
            Triple(EditorTab.PIP, Icons.Default.Settings, "PIP"),
            Triple(EditorTab.FILTER, Icons.Default.Star, "Filter"),
            Triple(EditorTab.FX, Icons.Default.Settings, "FX"),
            Triple(EditorTab.CHROMA, Icons.Default.Settings, "Chroma"),
            Triple(EditorTab.AUDIO, Icons.Default.PlayArrow, "Audio"),
            Triple(EditorTab.TEXT, Icons.Default.Edit, "Text"),
            Triple(EditorTab.CAPTIONS, Icons.Default.Settings, "AI Captions"),
            Triple(EditorTab.SPEED, Icons.Default.Settings, "Speed"),
            Triple(EditorTab.BRUSH, Icons.Default.Edit, "Brush"),
            Triple(EditorTab.AI_REMOVE, Icons.Default.Settings, "AI BG")
        ).forEach { (tab, icon, label) ->
            NavigationBarItem(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag("toolbar_tab_${tab.name.lowercase()}")
            )
        }
    }
}
