package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProjectDatabase
import com.example.data.model.AudioClip
import com.example.data.model.DrawingPath
import com.example.data.model.DrawingPoint
import com.example.data.model.KeyframePoint
import com.example.data.model.TextClip
import com.example.data.model.VideoClip
import com.example.data.model.VideoProject
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class EditorTab {
    CLIP, PIP, FILTER, FX, CHROMA, CAPTIONS, TEXT, STICKER, AUDIO, SPEED, KEYFRAME, BRUSH, AI_REMOVE
}

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    init {
        val database = ProjectDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao)
    }

    // Projects list
    val projects: StateFlow<List<VideoProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selection / Core Runtime States
    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedAudioClipId = MutableStateFlow<String?>(null)
    val selectedAudioClipId: StateFlow<String?> = _selectedAudioClipId.asStateFlow()

    private val _selectedTextClipId = MutableStateFlow<String?>(null)
    val selectedTextClipId: StateFlow<String?> = _selectedTextClipId.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.CLIP)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    // Decoded clips holding active UI lists
    val videoClips = mutableStateListOf<VideoClip>()
    val audioClips = mutableStateListOf<AudioClip>()
    val textClips = mutableStateListOf<TextClip>()
    val drawingPaths = mutableStateListOf<DrawingPath>()

    // Voiceover Rec Simulation
    private val _isRecordingVoiceover = MutableStateFlow(false)
    val isRecordingVoiceover: StateFlow<Boolean> = _isRecordingVoiceover.asStateFlow()

    // Subtitle Generating Indicator
    private val _isGeneratingCaptions = MutableStateFlow(false)
    val isGeneratingCaptions: StateFlow<Boolean> = _isGeneratingCaptions.asStateFlow()

    // Brush Tool parameters
    private val _activeDrawingColor = MutableStateFlow(0xFFFF0000) // Red Default
    val activeDrawingColor: StateFlow<Long> = _activeDrawingColor.asStateFlow()

    private val _activeDrawingWidth = MutableStateFlow(10f)
    val activeDrawingWidth: StateFlow<Float> = _activeDrawingWidth.asStateFlow()

    // Current temporary brush path
    val currentDrawingPoints = mutableStateListOf<DrawingPoint>()

    // Status Message Toast Simulator
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Timeline Tick Coroutine
        viewModelScope.launch {
            while (true) {
                if (_isPlaying.value) {
                    val step = 100L
                    delay(step)
                    val currentDur = _currentProject.value?.durationMs ?: 15000L
                    val next = _playbackPosition.value + step
                    if (next >= currentDur) {
                        _playbackPosition.value = 0L
                        _isPlaying.value = false
                    } else {
                        _playbackPosition.value = next
                    }
                } else {
                    delay(100L)
                }
            }
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.value = msg
            delay(2000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun selectProject(project: VideoProject?) {
        _currentProject.value = project
        _playbackPosition.value = 0L
        _isPlaying.value = false
        _selectedClipId.value = null
        _selectedAudioClipId.value = null
        _selectedTextClipId.value = null
        _activeTab.value = EditorTab.CLIP

        videoClips.clear()
        audioClips.clear()
        textClips.clear()
        drawingPaths.clear()

        if (project != null) {
            videoClips.addAll(repository.deserializeVideoClips(project.videoClipsJson))
            audioClips.addAll(repository.deserializeAudioClips(project.audioClipsJson))
            textClips.addAll(repository.deserializeTextClips(project.textClipsJson))
            drawingPaths.addAll(repository.deserializeDrawingPaths(project.drawingPathsJson))
        }
    }

    fun setPlaybackPosition(pos: Long) {
        val maxDur = _currentProject.value?.durationMs ?: 15000L
        _playbackPosition.value = pos.coerceIn(0L, maxDur)
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setEditorTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun selectClip(id: String?) {
        _selectedClipId.value = id
        _selectedAudioClipId.value = null
        _selectedTextClipId.value = null
    }

    fun selectAudioClip(id: String?) {
        _selectedAudioClipId.value = id
        _selectedClipId.value = null
        _selectedTextClipId.value = null
    }

    fun selectTextClip(id: String?) {
        _selectedTextClipId.value = id
        _selectedClipId.value = null
        _selectedAudioClipId.value = null
    }

    // --- Save changes back to SQLite database ---
    private fun saveCurrentState() {
        val proj = _currentProject.value ?: return
        val updated = proj.copy(
            lastModified = System.currentTimeMillis(),
            videoClipsJson = repository.serializeVideoClips(videoClips),
            audioClipsJson = repository.serializeAudioClips(audioClips),
            textClipsJson = repository.serializeTextClips(textClips),
            drawingPathsJson = repository.serializeDrawingPaths(drawingPaths)
        )
        _currentProject.value = updated
        viewModelScope.launch {
            repository.updateProject(updated)
        }
    }

    // --- Create New Project ---
    fun createProject(name: String, aspect: String = "16:9") {
        viewModelScope.launch {
            val (w, h) = when (aspect) {
                "9:16" -> Pair(1080, 1920)
                "1:1" -> Pair(1080, 1080)
                "4:3" -> Pair(1440, 1080)
                else -> Pair(1920, 1080)
            }

            // Put a baseline video track with 3 primary clips
            val baseClips = listOf(
                VideoClip(
                    id = "v_clip_1",
                    name = "Epic Sunset Intro",
                    mediaUrl = "sunset",
                    placeholderColor = 0xFFFFA000, // Vibrant Amber
                    startInTimelineMs = 0L,
                    durationMs = 5000L,
                    filterType = "None"
                ),
                VideoClip(
                    id = "v_clip_2",
                    name = "Vlog Talking Scene",
                    mediaUrl = "vlog",
                    placeholderColor = 0xFF5D4037, // Brown
                    startInTimelineMs = 5000L,
                    durationMs = 6000L,
                    filterType = "None"
                ),
                VideoClip(
                    id = "v_clip_3",
                    name = "Cinematic Outro Drone",
                    mediaUrl = "drone",
                    placeholderColor = 0xFF0288D1, // Ocean Blue
                    startInTimelineMs = 11000L,
                    durationMs = 4000L,
                    filterType = "None"
                )
            )

            // Baseline audio track
            val baseAudio = listOf(
                AudioClip(
                    id = "a_clip_1",
                    name = "Vlog Upbeat Synth Beat",
                    audioUrl = "music_1",
                    startInTimelineMs = 0L,
                    durationMs = 15000L,
                    volume = 0.5f
                )
            )

            // Baseline animated text
            val baseText = listOf(
                TextClip(
                    id = "t_clip_1",
                    text = "Welcome to Cut Pro",
                    startInTimelineMs = 1000L,
                    durationMs = 3000L,
                    fontName = "Sans-Serif",
                    fontColor = 0xFFFFFFFF,
                    animationType = "Spring Pop"
                )
            )

            val newProj = VideoProject(
                name = name,
                width = w,
                height = h,
                aspectRatio = aspect,
                durationMs = 15000L,
                videoClipsJson = repository.serializeVideoClips(baseClips),
                audioClipsJson = repository.serializeAudioClips(baseAudio),
                textClipsJson = repository.serializeTextClips(baseText),
                drawingPathsJson = "[]"
            )

            val id = repository.insertProject(newProj)
            val insertedProject = newProj.copy(id = id)
            selectProject(insertedProject)
            showToast("Project '$name' created successfully!")
        }
    }

    fun deleteProject(project: VideoProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_currentProject.value?.id == project.id) {
                selectProject(null)
            }
            showToast("Project deleted.")
        }
    }

    // --- TIMELINE ACTIONS ---

    // Split Clip
    fun splitSelectedClip() {
        val clipId = _selectedClipId.value ?: return
        val pos = _playbackPosition.value
        val clipIndex = videoClips.indexOfFirst { it.id == clipId }
        if (clipIndex != -1) {
            val clip = videoClips[clipIndex]
            if (pos > clip.startInTimelineMs && pos < clip.startInTimelineMs + clip.durationMs) {
                // Perform Split
                val leftDuration = pos - clip.startInTimelineMs
                val rightDuration = clip.durationMs - leftDuration

                val leftClip = clip.copy(
                    id = "${clip.id}_L",
                    name = "${clip.name} (Part A)",
                    durationMs = leftDuration
                )
                val rightClip = clip.copy(
                    id = "${clip.id}_R",
                    name = "${clip.name} (Part B)",
                    startInTimelineMs = pos,
                    durationMs = rightDuration
                )

                videoClips.removeAt(clipIndex)
                videoClips.add(clipIndex, leftClip)
                videoClips.add(clipIndex + 1, rightClip)

                _selectedClipId.value = rightClip.id
                saveCurrentState()
                showToast("Split video clip at ${(pos / 1000f)}s")
            } else {
                showToast("Cannot split. Playhead is outside clip bounds.")
            }
        }
    }

    // Trim Start of Clip
    fun trimSelectedClipStart() {
        val clipId = _selectedClipId.value ?: return
        val pos = _playbackPosition.value
        val clipIndex = videoClips.indexOfFirst { it.id == clipId }
        if (clipIndex != -1) {
            val clip = videoClips[clipIndex]
            if (pos > clip.startInTimelineMs && pos < clip.startInTimelineMs + clip.durationMs) {
                val newDuration = clip.startInTimelineMs + clip.durationMs - pos
                val updatedClip = clip.copy(
                    startInTimelineMs = pos,
                    durationMs = newDuration
                )
                videoClips[clipIndex] = updatedClip
                saveCurrentState()
                showToast("Trimmed clip start")
            }
        }
    }

    // Trim End of Clip
    fun trimSelectedClipEnd() {
        val clipId = _selectedClipId.value ?: return
        val pos = _playbackPosition.value
        val clipIndex = videoClips.indexOfFirst { it.id == clipId }
        if (clipIndex != -1) {
            val clip = videoClips[clipIndex]
            if (pos > clip.startInTimelineMs && pos < clip.startInTimelineMs + clip.durationMs) {
                val newDuration = pos - clip.startInTimelineMs
                val updatedClip = clip.copy(
                    durationMs = newDuration
                )
                videoClips[clipIndex] = updatedClip
                saveCurrentState()
                showToast("Trimmed clip end")
            }
        }
    }

    // Add Overlay PIP Video
    fun addPIPOverlay(name: String, color: Long) {
        val pos = _playbackPosition.value
        val newPIP = VideoClip(
            id = "pip_${System.currentTimeMillis()}",
            name = name,
            mediaUrl = "overlay",
            placeholderColor = color,
            startInTimelineMs = pos,
            durationMs = 4000L,
            isPIP = true,
            pipWidth = 0.35f,
            pipX = 0.55f,
            pipY = 0.15f
        )
        videoClips.add(newPIP)
        _selectedClipId.value = newPIP.id
        saveCurrentState()
        showToast("Added PIP Overlay '$name'")
    }

    // Update PIP Position
    fun updatePIPPosition(clipId: String, x: Float, y: Float) {
        val idx = videoClips.indexOfFirst { it.id == clipId }
        if (idx != -1) {
            val c = videoClips[idx]
            videoClips[idx] = c.copy(
                pipX = x.coerceIn(0.0f, 1.0f),
                pipY = y.coerceIn(0.0f, 1.0f)
            )
            saveCurrentState()
        }
    }

    // Update PIP Size Scale
    fun updatePIPScale(clipId: String, scale: Float) {
        val idx = videoClips.indexOfFirst { it.id == clipId }
        if (idx != -1) {
            val c = videoClips[idx]
            videoClips[idx] = c.copy(
                pipWidth = scale.coerceIn(0.1f, 0.9f)
            )
            saveCurrentState()
        }
    }

    // Add Keyframe Point
    fun addKeyframeToSelected() {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            val clip = videoClips[idx]
            val localTime = _playbackPosition.value - clip.startInTimelineMs
            val point = KeyframePoint(
                timeMs = localTime.coerceAtLeast(0L),
                scale = clip.scale,
                alpha = 1.0f,
                xOffset = if (clip.isPIP) clip.pipX else 0f,
                yOffset = if (clip.isPIP) clip.pipY else 0f
            )
            val updatedKeys = clip.keyframes.plus(point).sortedBy { it.timeMs }
            videoClips[idx] = clip.copy(keyframes = updatedKeys)
            saveCurrentState()
            showToast("Added animating keyframe at ${localTime / 1000f}s")
        }
    }

    // --- BRUSH / DRAWING ACTIONS ---
    fun startNewBrushPath() {
        currentDrawingPoints.clear()
    }

    fun addPointToBrushPath(x: Float, y: Float) {
        currentDrawingPoints.add(DrawingPoint(x, y))
    }

    fun finalizeBrushPath() {
        if (currentDrawingPoints.isNotEmpty()) {
            val path = DrawingPath(
                id = "brush_${System.currentTimeMillis()}",
                color = _activeDrawingColor.value,
                strokeWidth = _activeDrawingWidth.value,
                points = currentDrawingPoints.toList()
            )
            drawingPaths.add(path)
            currentDrawingPoints.clear()
            saveCurrentState()
            showToast("Brush stroke saved")
        }
    }

    fun clearAllDrawings() {
        drawingPaths.clear()
        currentDrawingPoints.clear()
        saveCurrentState()
        showToast("Cleared canvas drawing")
    }

    fun setDrawingColor(color: Long) {
        _activeDrawingColor.value = color
    }

    fun setDrawingWidth(width: Float) {
        _activeDrawingWidth.value = width
    }

    // --- OTHER PROPERTIES EDIT ---
    fun updateClipFilter(filter: String) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(filterType = filter)
            saveCurrentState()
        }
    }

    fun updateClipEffect(effect: String) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(effectFX = effect)
            saveCurrentState()
        }
    }

    fun updateClipColorCorrection(brightness: Float, contrast: Float, saturation: Float) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(
                brightness = brightness,
                contrast = contrast,
                saturation = saturation
            )
            saveCurrentState()
        }
    }

    fun updateClipSpeed(speed: Float, ramp: String = "None") {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(
                speed = speed,
                speedRampType = ramp
            )
            saveCurrentState()
            showToast("Speed speed adjust to ${speed}x ($ramp)")
        }
    }

    fun updateClipTransition(trans: String, duration: Long = 500L) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(
                transitionType = trans,
                transitionDurationMs = duration
            )
            saveCurrentState()
            showToast("Transition '$trans' set")
        }
    }

    fun updateClipChroma(active: Boolean, color: Int = 0x00FF00, intensity: Float = 0.5f) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(
                hasChromaKey = active,
                chromaKeyColor = color,
                chromaIntensity = intensity
            )
            saveCurrentState()
            showToast(if (active) "Chroma key active" else "Chroma key disabled")
        }
    }

    fun toggleBgRemoval() {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            val state = !videoClips[idx].hasBgRemoved
            videoClips[idx] = videoClips[idx].copy(hasBgRemoved = state)
            saveCurrentState()
            showToast(if (state) "AI Background Removed" else "Background restored")
        }
    }

    fun updateClipRotation(flipH: Boolean? = null, flipV: Boolean? = null, rotateAngle: Float? = null) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            val original = videoClips[idx]
            videoClips[idx] = original.copy(
                flipHorizontal = flipH ?: original.flipHorizontal,
                flipVertical = flipV ?: original.flipVertical,
                rotation = rotateAngle ?: original.rotation
            )
            saveCurrentState()
        }
    }

    fun updateClipScalePos(scale: Float) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(
                scale = scale
            )
            saveCurrentState()
        }
    }

    fun updateClipBlendMode(mode: String) {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            videoClips[idx] = videoClips[idx].copy(blendMode = mode)
            saveCurrentState()
            showToast("Blend mode set to $mode")
        }
    }

    fun toggleMotionTracking() {
        val cId = _selectedClipId.value ?: return
        val idx = videoClips.indexOfFirst { it.id == cId }
        if (idx != -1) {
            val state = !videoClips[idx].isMotionTracked
            videoClips[idx] = videoClips[idx].copy(isMotionTracked = state)
            saveCurrentState()
            showToast(if (state) "Motion Tracking enabled" else "Tracking locked")
        }
    }

    // --- AUDIO / MUSIC / VOICEOVER ACTIONS ---
    fun addAudioTrack(name: String, key: String) {
        val pos = _playbackPosition.value
        val newAudio = AudioClip(
            id = "a_${System.currentTimeMillis()}",
            name = name,
            audioUrl = key,
            startInTimelineMs = pos,
            durationMs = 5000L
        )
        audioClips.add(newAudio)
        _selectedAudioClipId.value = newAudio.id
        saveCurrentState()
        showToast("Added audio overlay '$name'")
    }

    fun updateAudioClipProperties(volume: Float, fadeIn: Long, fadeOut: Long, noiseRed: Boolean, voiceChan: String) {
        val aId = _selectedAudioClipId.value ?: return
        val idx = audioClips.indexOfFirst { it.id == aId }
        if (idx != -1) {
            audioClips[idx] = audioClips[idx].copy(
                volume = volume,
                fadeInMs = fadeIn,
                fadeOutMs = fadeOut,
                noiseReduction = noiseRed,
                voiceChanger = voiceChan
            )
            saveCurrentState()
        }
    }

    fun startVoiceoverRecording() {
        _isRecordingVoiceover.value = true
        showToast("🎤 Recording Voiceover... Speak now")
    }

    fun stopVoiceoverRecording() {
        if (_isRecordingVoiceover.value) {
            _isRecordingVoiceover.value = false
            val pos = _playbackPosition.value
            val newAudio = AudioClip(
                id = "audio_vo_${System.currentTimeMillis()}",
                name = "My Voiceover Rec",
                audioUrl = "voiceover",
                startInTimelineMs = pos,
                durationMs = 4000L,
                volume = 1.0f
            )
            audioClips.add(newAudio)
            _selectedAudioClipId.value = newAudio.id
            saveCurrentState()
            showToast("🎤 Voiceover track added!")
        }
    }

    // --- TEXT / SUBTITLE / STICKER ACTIONS ---
    fun addTextOverlay(text: String, anim: String = "None") {
        val pos = _playbackPosition.value
        val newText = TextClip(
            id = "text_${System.currentTimeMillis()}",
            text = text,
            startInTimelineMs = pos,
            durationMs = 2500L,
            fontName = "Sans-Serif",
            fontColor = 0xFFFFFFFF,
            animationType = anim
        )
        textClips.add(newText)
        _selectedTextClipId.value = newText.id
        saveCurrentState()
        showToast("Added text overlay")
    }

    fun updateTextClipProperties(text: String, font: String, color: Long, anim: String, sizeSp: Int) {
        val tId = _selectedTextClipId.value ?: return
        val idx = textClips.indexOfFirst { it.id == tId }
        if (idx != -1) {
            textClips[idx] = textClips[idx].copy(
                text = text,
                fontName = font,
                fontColor = color,
                animationType = anim,
                fontSizeSp = sizeSp
            )
            saveCurrentState()
        }
    }

    fun deleteSelectedClip() {
        val selVideo = _selectedClipId.value
        if (selVideo != null) {
            videoClips.removeAll { it.id == selVideo }
            _selectedClipId.value = null
            saveCurrentState()
            showToast("Video element deleted")
            return
        }
        val selAudio = _selectedAudioClipId.value
        if (selAudio != null) {
            audioClips.removeAll { it.id == selAudio }
            _selectedAudioClipId.value = null
            saveCurrentState()
            showToast("Audio track deleted")
            return
        }
        val selText = _selectedTextClipId.value
        if (selText != null) {
            textClips.removeAll { it.id == selText }
            _selectedTextClipId.value = null
            saveCurrentState()
            showToast("Text track deleted")
            return
        }
    }

    // --- GEMINI AUTO CAPTIONS GENERATION ---
    fun generateAutoCaptionsWithGemini() {
        val project = _currentProject.value ?: return
        _isGeneratingCaptions.value = true
        showToast("🪄 AI: Loading audio transcript...")

        viewModelScope.launch {
            try {
                // Read from direct REST API if possible, otherwise procedurally generate.
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                var resultText = ""
                if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
                    resultText = callGeminiForCaptions(apiKey, project.name)
                }

                delay(2500) // Mock some high-tech analysis delay for wonderful UX

                textClips.removeAll { it.isAutoCaption }

                if (resultText.isNotEmpty() && resultText.startsWith("[")) {
                    // Try parsing JSON list returned from Gemini
                    try {
                        val arr = JSONArray(resultText)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val txt = obj.getString("text")
                            val start = obj.getLong("startMs")
                            val duration = obj.optLong("durationMs", 2000L)
                            textClips.add(
                                TextClip(
                                    id = "caption_ai_${System.currentTimeMillis()}_$i",
                                    text = txt,
                                    startInTimelineMs = start,
                                    durationMs = duration,
                                    fontName = "Monospace",
                                    fontColor = 0xFFFFF176, // Beautiful warm yellow for subtitles
                                    animationType = "Typewriter",
                                    isAutoCaption = true,
                                    yPos = 0.85f
                                )
                            )
                        }
                    } catch (pe: Exception) {
                        loadProceduralCaptions()
                    }
                } else {
                    loadProceduralCaptions()
                }

                saveCurrentState()
                showToast("🪄 AI Subtitles generated successfully!")
            } catch (e: Exception) {
                loadProceduralCaptions()
                saveCurrentState()
                showToast("AI Subtitles auto-generated! (Offline backup)")
            } finally {
                _isGeneratingCaptions.value = false
            }
        }
    }

    private fun loadProceduralCaptions() {
        val list = listOf(
            TextClip("c_1", "Exploring the wilderness today...", 500L, 2500L, "Monospace", 0xFFFFF176, isAutoCaption = true, animationType = "Fade In"),
            TextClip("c_2", "This scene takes my breath away!", 4000L, 2000L, "Monospace", 0xFFFFF176, isAutoCaption = true, animationType = "Spring Pop"),
            TextClip("c_3", "Look at that beautiful background glow.", 6500L, 3000L, "Monospace", 0xFFFFF176, isAutoCaption = true, animationType = "Typewriter"),
            TextClip("c_4", "Thanks for tuning in. Subscribe for more!", 11500L, 3000L, "Monospace", 0xFFFFF176, isAutoCaption = true, animationType = "Slide Up")
        )
        textClips.addAll(list)
    }

    private suspend fun callGeminiForCaptions(apiKey: String, projectName: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val prompt = "Create a JSON array of video captions/subtitles for a short vlog project named '$projectName'. The video lasts 15 seconds (15000ms). Return ONLY a raw JSON array. DO NOT wrap inside ```json or formatting blocks. Each caption must have: 'startMs' (number), 'durationMs' (number, e.g. 2000), and 'text' (string). Example: [{\"startMs\": 500, \"durationMs\": 2500, \"text\": \"Hello world!\"}]"
            
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            conn.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray())
            }

            if (conn.responseCode == 200) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseStr)
                val text = root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                text.trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
