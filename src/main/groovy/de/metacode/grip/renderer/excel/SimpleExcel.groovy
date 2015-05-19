package de.metacode.grip.renderer.excel

import de.metacode.grip.util.AttachmentProvider
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook

import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource
import java.sql.ResultSet

/**
 * Created by mloesch on 15.03.15.
 */
class SimpleExcel implements AttachmentProvider {

    HSSFWorkbook wb;

    SimpleExcel() {
        this.wb = new HSSFWorkbook()
    }

    HSSFSheet newSheet(String name) {
        return this.wb.createSheet(name)
    }

    SimpleExcel writeToSheet(HSSFSheet sheet, ResultSet rs) {
        def row = 0;
        writeHead(sheet.createRow(row++), rs)
        while (rs.next()) {
            writeRow(sheet.createRow(row++), rs)
        }
        return this
    }


    static def writeHead(HSSFRow row, ResultSet rs) {
        def metadata = rs.getMetaData()
        for (int i = 0; i < metadata.columnCount; i++) {
            row.createCell(i).setCellValue(metadata.getColumnName(i + 1))
        }
    }

    static def writeRow(HSSFRow row, ResultSet rs) {
        def metadata = rs.getMetaData()
        for (int i = 0; i < metadata.columnCount; i++) {
            row.createCell(i).setCellValue(rs.getString(i + 1))
        }
    }

    DataSource toDataSource() {
        def osExl = new ByteArrayOutputStream()
        this.wb.write(osExl)
        def isExl = new ByteArrayInputStream(osExl.toByteArray())
        def dsExl = new ByteArrayDataSource(isExl, "application/vnd.ms-excel")
        osExl.close()
        return dsExl;
    }

}
