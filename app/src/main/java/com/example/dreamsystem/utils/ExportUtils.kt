package com.example.dreamsystem.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.dreamsystem.data.PointRecord
import jxl.Workbook
import jxl.format.Colour
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    suspend fun exportToExcel(context: Context, records: List<PointRecord>): String? = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fileNameDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: return@withContext null
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = "积分流水_${fileNameDate.format(Date())}.xls"
            val file = File(downloadsDir, fileName)
            
            val workbook = Workbook.createWorkbook(file)
            val sheet = workbook.createSheet("积分流水", 0)

            val headerFont = WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD)
            val headerFormat = WritableCellFormat(headerFont)
            headerFormat.setBackground(Colour.GRAY_25)

            val headers = arrayOf("序号", "任务描述", "积分", "时间")
            headers.forEachIndexed { index, title ->
                sheet.addCell(Label(index, 0, title, headerFormat))
            }

            records.forEachIndexed { index, record ->
                sheet.addCell(Label(0, index + 1, (index + 1).toString()))
                sheet.addCell(Label(1, index + 1, record.taskDescription))
                sheet.addCell(Label(2, index + 1, record.points.toString()))
                sheet.addCell(Label(3, index + 1, dateFormat.format(Date(record.timestamp))))
            }

            sheet.setColumnView(0, 10)
            sheet.setColumnView(1, 30)
            sheet.setColumnView(2, 10)
            sheet.setColumnView(3, 25)

            workbook.write()
            workbook.close()

            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.ms-excel")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                
                val chooser = Intent.createChooser(intent, "选择应用打开Excel文件")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "文件已保存: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
            null
        }
    }
}