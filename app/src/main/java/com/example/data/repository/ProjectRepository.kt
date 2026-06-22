package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.model.AudioClip
import com.example.data.model.DrawingPath
import com.example.data.model.TextClip
import com.example.data.model.VideoClip
import com.example.data.model.VideoProject
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Adapters
    private val videoClipsAdapter = moshi.adapter<List<VideoClip>>(
        Types.newParameterizedType(List::class.java, VideoClip::class.java)
    )
    private val audioClipsAdapter = moshi.adapter<List<AudioClip>>(
        Types.newParameterizedType(List::class.java, AudioClip::class.java)
    )
    private val textClipsAdapter = moshi.adapter<List<TextClip>>(
        Types.newParameterizedType(List::class.java, TextClip::class.java)
    )
    private val drawingPathsAdapter = moshi.adapter<List<DrawingPath>>(
        Types.newParameterizedType(List::class.java, DrawingPath::class.java)
    )

    val allProjects: Flow<List<VideoProject>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Long): VideoProject? {
        return projectDao.getProjectById(id)
    }

    suspend fun insertProject(project: VideoProject): Long {
        return projectDao.insertProject(project)
    }

    suspend fun updateProject(project: VideoProject) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: VideoProject) {
        projectDao.deleteProject(project)
    }

    suspend fun deleteProjectById(id: Long) {
        projectDao.deleteProjectById(id)
    }

    // --- JSON Conversion Helpers ---
    fun serializeVideoClips(clips: List<VideoClip>): String {
        return try {
            videoClipsAdapter.toJson(clips)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeVideoClips(json: String): List<VideoClip> {
        return try {
            videoClipsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeAudioClips(clips: List<AudioClip>): String {
        return try {
            audioClipsAdapter.toJson(clips)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeAudioClips(json: String): List<AudioClip> {
        return try {
            audioClipsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeTextClips(clips: List<TextClip>): String {
        return try {
            textClipsAdapter.toJson(clips)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeTextClips(json: String): List<TextClip> {
        return try {
            textClipsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeDrawingPaths(paths: List<DrawingPath>): String {
        return try {
            drawingPathsAdapter.toJson(paths)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeDrawingPaths(json: String): List<DrawingPath> {
        return try {
            drawingPathsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
