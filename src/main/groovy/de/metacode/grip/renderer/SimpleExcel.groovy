package de.metacode.grip.renderer

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook

import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource
import java.sql.ResultSet

/**
 * Created by mloesch on 15.03.15.
 */
@Slf4j
class SimpleExcel implements DataSourceDistributor, Instantiable {

    HSSFWorkbook wb;

    private SimpleExcel(Map m) {
        this.wb = new HSSFWorkbook()
    }

    HSSFSheet newSheet(String name) {
        log.debug("creating sheet $name")
        return this.wb.createSheet(name)
    }

    SimpleExcel writeToSheet(String sheetName, Sql sql, String query) {
        sql.query(query, {ResultSet rs ->
            writeToSheet(sheetName, rs)
        })
        return this
    }

    SimpleExcel writeToSheet(String sheetName, ResultSet rs) {
        return writeToSheet(newSheet(sheetName), rs)
    }

    SimpleExcel writeToSheet(HSSFSheet sheet, String text) {
        def cell = sheet.createRow(0).createCell(0)
        cell.setCellValue(text)
        return this
    }

    SimpleExcel writeToSheet(HSSFSheet sheet, ResultSet rs) {
        log.debug("writing to sheet $sheet.sheetName")
        log.trace(rs.metaData.toString())
        def row = 0;
        writeHead(sheet.createRow(row++), rs)
        while (rs.next()) {
            writeRow(sheet.createRow(row++), rs)
        }
        log.debug("wrote $row rows to sheet")
        return this
    }

    static def writeHead(HSSFRow row, ResultSet rs) {
        def metadata = rs.getMetaData()
        for (int i = 0; i < metadata.columnCount; i++) {
            def columnName = metadata.getColumnName(i + 1)
            log.trace("writing head col $columnName")
            row.createCell(i).setCellValue(columnName)
        }
    }

    static def writeRow(HSSFRow row, ResultSet rs) {
        def metadata = rs.getMetaData()
        for (int i = 0; i < metadata.columnCount; i++) {
            def cellValue = rs.getString(i + 1)
            log.trace("writing cell value $cellValue")
            row.createCell(i).setCellValue(cellValue)
        }
    }

    DataSource toDataSource() {
        def osExl = new ByteArrayOutputStream()
        log.trace("writing workbook to ByteArrayOutputStream")
        this.wb.write(osExl)
        log.trace("writing workbook to ByteArrayOutputStream done")
        def isExl = new ByteArrayInputStream(osExl.toByteArray())
        log.trace("writing ByteArrayOutputStream into ByteArrayDataSource")
        def dsExl = new ByteArrayDataSource(isExl, "application/vnd.ms-excel")
        osExl.close()
        return dsExl
    }
}