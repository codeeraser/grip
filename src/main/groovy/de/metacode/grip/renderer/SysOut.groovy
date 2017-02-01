package de.metacode.grip.renderer

import java.sql.ResultSet

/**
 * Created by mloesch on 07.08.15.
 */

class SysOut implements Instantiable {
    private final static int PAD = 15

    def sout(String text) {
        println(text)
    }

    def write(ResultSet rs) {
        println toText(rs)
    }

    String toText(ResultSet rs) {
        def result = ""
        def metadata = rs.metaData
        def head = []
        for (int i = 0; i < metadata.columnCount; i++) {
            def name = metadata.getColumnName(i + 1)
            head <<= name
        }

        Number textLength = metadata.columnCount * PAD
        result <<= '\n'
        result <<= """ that's what comes of it! """.center(textLength, '-')
        result <<= '\n'
        head.each { String s ->
            result <<= s.padRight(PAD)
        }
        result <<= '\n'
        result <<= "-" * textLength
        result <<= '\n'
        while (rs.next()) {
            for (int i = 0; i < metadata.columnCount; i++) {
                def value = rs.getString(i + 1)
                if (value) {
                    if (value.length() >= PAD) {
                        result <<= "$value   "
                    } else {
                        result <<= value.padRight(PAD)
                    }
                } else {
                    result <<= 'null'.padRight(PAD)
                }
            }
            result <<= '\n'
        }
        result <<= '\n'
    }
}