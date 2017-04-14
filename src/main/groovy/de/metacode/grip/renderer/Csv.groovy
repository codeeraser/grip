package de.metacode.grip.renderer

import com.opencsv.CSVWriter
import de.metacode.grip.env.SqlEnv
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource
import java.sql.ResultSet

/**
 * Created by mloesch on 07.08.15.
 */

@Slf4j
class Csv implements DataSourceDistributor, Instantiable {

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream()
    final CSVWriter writer

    private Csv(Map m) {
        Writer out = new BufferedWriter(new OutputStreamWriter(bos))
        this.writer = new CSVWriter(out, (m?.separator ?: ',') as char, (m?.quote ?: '\0') as char)
    }

    Csv writeAll(Sql sql, String query) {
        sql.query(query, {ResultSet rs ->
            this.writer.writeAll(rs, true)
        })
        return this
    }

    Csv writeAll(ResultSet rs) {
        this.writer.writeAll(rs, true)
        return this
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