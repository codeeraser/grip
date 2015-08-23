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
        def metadata = rs.metaData
        def head = []
        for (int i = 0; i < metadata.columnCount; i++) {
            def name = metadata.getColumnName(i + 1)
            head << name
        }

        Number textLength = metadata.columnCount * PAD
        println()
        println """ that's what comes of it! """.center(textLength, '-')
        head.each {String s ->
            print s.padRight(PAD)
        }
        println()
        println "-" * textLength
        while (rs.next()) {
            for (int i = 0; i < metadata.columnCount; i++) {
                def value = rs.getString(i + 1)
                if (value) {
                    if (value.length() >= PAD) {
                        print "$value   "
                    } else {
                        print value.padRight(PAD)
                    }
                } else {
                    print 'null'.padRight(PAD)
                }
            }
            println ""
        }
        println()
    }
}