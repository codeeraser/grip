package de.metacode.grip.renderer

import com.opencsv.CSVWriter
import groovy.util.logging.Slf4j

import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource

/**
 * Created by mloesch on 07.08.15.
 */

@Slf4j
class Csv implements DataSourceDistributor, Instantiable {

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream()
    final CSVWriter writer

    static {
        Csv.metaClass.constructor = { Map m ->
            new Csv((m.separator ?: ',') as char, (m.quote ?: '\0') as char)
        }
    }

    private Csv(char separator, char quote) {
        Writer out = new BufferedWriter(new OutputStreamWriter(bos))
        this.writer = new CSVWriter(out, separator, quote)
    }

    @Override
    DataSource toDataSource() {
        log.trace("calling flush on CSVWriter")
        this.writer.flush()
        log.trace("writing outputstream to inputstream")
        def is = new ByteArrayInputStream(bos.toByteArray())
        log.trace("writing inputstream to datasource")
        def ds = new ByteArrayDataSource(is, "application/vnd.ms-excel")
        log.trace("closing outputstream")
        bos.close()
        return ds;
    }
}