package com.riceleaf.util;

import com.riceleaf.entity.DiseaseDict;
import com.riceleaf.entity.Record;
import com.riceleaf.repository.DiseaseDictRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelUtil {

    public static byte[] generateExcel(List<Record> records,
                                        DiseaseDictRepository diseaseRepo) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("病害记录");
            Row header = sheet.createRow(0);
            String[] headers = {"样点", "株号", "叶片位", "病害名称",
                    "病斑面积占比(%)", "病情级别(0-9)", "备注"};
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Record r : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getSamplePoint());
                row.createCell(1).setCellValue(r.getPlantNo());
                row.createCell(2).setCellValue(r.getLeafPosition());

                String diseaseName = "";
                if (r.getDiseaseId() != null) {
                    diseaseName = diseaseRepo.findById(r.getDiseaseId())
                            .map(DiseaseDict::getName).orElse("");
                }
                row.createCell(3).setCellValue(diseaseName);
                row.createCell(4).setCellValue(r.getLesionRatio() != null ?
                        r.getLesionRatio().toString() : "");
                row.createCell(5).setCellValue(r.getSeverityLevel() != null ?
                        r.getSeverityLevel() : 0);
                row.createCell(6).setCellValue(r.getRemark() != null ? r.getRemark() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
    }
}
