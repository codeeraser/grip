package de.metacode.grip.renderer

import groovy.util.logging.Slf4j

import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource
import java.sql.ResultSet

/**
 * Created by mloesch on 07.08.15.
 */

@Slf4j
class SysOut implements Instantiable, DataSourceDistributor {
    private final static int PAD = 15

    def out = "" 

    def write(ResultSet rs) {
        def metadata = rs.metaData
        def head = []
        for (int i = 0; i < metadata.columnCount; i++) {
            def name = metadata.getColumnName(i + 1)
            head <<= name
        }

        Number textLength = metadata.columnCount * PAD
        out <<= '\n'
        out <<= """ that's what comes of it! """.center(textLength, '-')
        out <<= '\n'
        head.each { String s ->
            out <<= s.padRight(PAD)
        }
        out <<= '\n'
        out <<= "-" * textLength
        out <<= '\n'
        while (rs.next()) {
            for (int i = 0; i < metadata.columnCount; i++) {
                def value = rs.getString(i + 1)
                if (value) {
                    if (value.length() >= PAD) {
                        out <<= "$value   "
                    } else {
                        out <<= value.padRight(PAD)
                    }
                } else {
                    out <<= 'null'.padRight(PAD)
                }
            }
            out <<= '\n'
        }
        out <<= '\n'
        return this
    }

    @Override
    DataSource toDataSource() {
        log.trace("writing out.bytes to datasource ${out}")
        def ds = new ByteArrayDataSource(out.toString().bytes, "text/plain")
        log.trace("closing outputstream")
        return ds
    }
}