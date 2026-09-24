package com.riceleaf.local.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.riceleaf.local.data.local.AppDatabase
import com.riceleaf.local.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val db: AppDatabase,
    private val context: Context
) {
    private val dao get() = db.recordDao()

    private fun getImageDir(): File {
        val dir = File(context.filesDir, "app_images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveImage(bitmap: Bitmap): String {
        val dir = getImageDir()
        val filename = "${UUID.randomUUID()}.jpg"
        val file = File(dir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun deleteImage(path: String) {
        try { File(path).delete() } catch (_: Exception) {}
    }

    suspend fun insertRecord(record: RecordEntity): Long = dao.insert(record)

    suspend fun updateRecord(record: RecordEntity) = dao.update(record)

    suspend fun deleteRecord(id: Long) {
        val record = dao.getById(id) ?: return
        deleteImage(record.imagePath)
        dao.deleteById(id)
    }

    fun getAllRecords(): Flow<List<RecordEntity>> = dao.getAll()

    suspend fun getRecordById(id: Long): RecordEntity? = dao.getById(id)

    fun getRecordCount(): Flow<Int> = dao.getCount()

    suspend fun getPagedRecords(limit: Int, offset: Int): List<RecordEntity> =
        dao.getPaged(limit, offset)

    suspend fun getAllForExport(): List<RecordEntity> = dao.getAllForExport()
}
