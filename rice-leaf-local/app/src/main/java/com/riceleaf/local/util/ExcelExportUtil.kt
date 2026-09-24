package com.riceleaf.local.util

import android.content.Context
import com.riceleaf.local.data.local.entity.RecordEntity
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExportUtil {

    fun export(context: Context, records: List<RecordEntity>): String {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("病害记录")

        val header = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { bold = true })
        }
        val headers = arrayOf(
            "编号", "株号", "叶片位", "病害名称", "置信度",
            "病斑面积(%)", "病情级别", "症状描述", "备注", "记录时间"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, h ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(h)
            cell.cellStyle = header
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        records.forEachIndexed { idx, r ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(r.id.toDouble())
            row.createCell(1).setCellValue(r.plantNo)
            row.createCell(2).setCellValue(r.leafPosition)
            row.createCell(3).setCellValue(r.diseaseName ?: "")
            row.createCell(4).setCellValue(((r.confidence ?: 0f) * 100).toString() + "%")
            row.createCell(5).setCellValue(r.lesionAreaRatio?.toDouble() ?: 0.0)
            row.createCell(6).setCellValue((r.severityLevel ?: 0).toDouble())
            row.createCell(7).setCellValue(r.symptomDesc ?: "")
            row.createCell(8).setCellValue(r.remark)
            row.createCell(9).setCellValue(dateFormat.format(Date(r.createTime)))
        }

        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(dir, "riceleaf_export_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        return file.absolutePath
    }
}
