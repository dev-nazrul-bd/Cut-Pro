package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_projects")
data class VideoProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val width: Int = 1920,
    val height: Int = 1080,
    val aspectRatio: String = "16:9", // "16:9", "9:16", "1:1", "4:3"
    val durationMs: Long = 15000L,
    val lastModified: Long = System.currentTimeMillis(),
    val videoClipsJson: String = "[]",
    val audioClipsJson: String = "[]",
    val textClipsJson: String = "[]",
    val drawingPathsJson: String = "[]"
)

data class VideoClip(
    val id: String,
    val name: String,
    val mediaUrl: String, // Or identifier
    val placeholderColor: Long, // Color for rendering the preview block
    val startInTimelineMs: Long,
    val durationMs: Long,
    val scale: Float = 1.0f, // Crop & Zoom
    val rotation: Float = 0.0f, // Rotate & Flip
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0.0f, // Color correction (-1f to 1f)
    val contrast: Float = 1.0f,   // Color correction (0.5f to 2.0f)
    val saturation: Float = 1.0f, // Color correction (0.0f to 2.0f)
    val filterType: String = "None", // "Noir", "Vintage", "Cinematic", "Cyberpunk", "Forest", "Warm Gold"
    val effectFX: String = "None",    // "Glitch", "VHS", "Blur", "Film Grain", "RGB Split", "Pixelate"
    val speed: Float = 1.0f,      // Speed Control (0.25x to 4.0x)
    val speedRampType: String = "None", // "None", "Montage", "Hero Curve", "Bullet"
    val blendMode: String = "Normal",   // Blending modes
    val hasChromaKey: Boolean = false,
    val chromaKeyColor: Int = 0x00FF00, // Hex of greenscreen color
    val chromaIntensity: Float = 0.5f,
    val hasBgRemoved: Boolean = false,   // AI Background Removal
    val transitionType: String = "None", // "Wipe", "Fade to Black", "Zoom In", "Cross Dissolve"
    val transitionDurationMs: Long = 500L,
    val isPIP: Boolean = false, // Overlay / PIP layer indicator
    val pipX: Float = 0.1f, // Relative coordinates (0.0 to 1.0)
    val pipY: Float = 0.1f,
    val pipWidth: Float = 0.35f,
    val isMotionTracked: Boolean = false, // Motion tracking simulation
    val keyframes: List<KeyframePoint> = emptyList() // Keyframe animation
)

data class KeyframePoint(
    val timeMs: Long,
    val scale: Float = 1.0f,
    val alpha: Float = 1.0f,
    val xOffset: Float = 0.0f,
    val yOffset: Float = 0.0f
)

data class AudioClip(
    val id: String,
    val name: String,
    val audioUrl: String,
    val startInTimelineMs: Long,
    val durationMs: Long,
    val volume: Float = 0.8f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val noiseReduction: Boolean = false,
    val voiceChanger: String = "None" // "None", "Robot", "Chipmunk", "Deep Echo", "Helium"
)

data class TextClip(
    val id: String,
    val text: String,
    val startInTimelineMs: Long,
    val durationMs: Long,
    val fontName: String = "Default", // "Default", "Sans-Serif", "Serif", "Monospace"
    val fontColor: Long = 0xFFFFFFFF,
    val fontSizeSp: Int = 24,
    val animationType: String = "None", // "None", "Fade In", "Spring Pop", "Slide Up", "Typewriter"
    val isAutoCaption: Boolean = false, // To differentiate auto-generated subtitles
    val xPos: Float = 0.5f, // Relative centered x
    val yPos: Float = 0.8f // Relative centered y
)

data class DrawingPath(
    val id: String,
    val color: Long = 0xFFFF0000, // Red Brush
    val strokeWidth: Float = 8f,
    val points: List<DrawingPoint> = emptyList()
)

data class DrawingPoint(
    val x: Float,
    val y: Float
)
