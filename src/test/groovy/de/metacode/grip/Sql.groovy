package de.metacode.grip

import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.InitProcessor
import de.metacode.grip.core.JobProcessor
import de.metacode.grip.core.Quartz
import org.hsqldb.Server
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by mloesch on 29.01.17.
 */

class Sql extends Specification {

    @Shared
    groovy.sql.Sql sql = groovy.sql.Sql.newInstance(
            "jdbc:hsqldb:mem:testdb"
            , "sa"
            , ""
            , "org.hsqldb.jdbcDriver")

    def setupSpec() {
        Server.main()
    }

    def cleanupSpec() {
        sql.close()
    }

    def "insert data by script HsqlInsertData.grip with explicit commit"() {
        setup:
        sql.execute("""drop table atable if exists;
                    create table atable (
                        name varchar(50),
                        address varchar(100)
                    )"""
        )
        def context = [name: 'gripCli']

        when:
        def scriptFile = new File(getClass().getClassLoader()
                .getResource("de/metacode/grip/HsqlInsertData.grip").toURI())
        CoreProcessor.run(scriptFile.text, context)

        then:
        assert sql.rows("select * from atable").size() == 3
    }

    def "schedule HsqlInsertDataScheduled.grip every second and insert at least 3 rows using autocommit"() {
        setup:
        sql.execute("""drop table atable if exists;
                    create table atable (
                        name varchar(50),
                        address varchar(100)
                    )"""
        )
        def context = [:]
        Quartz.instance.start()

        when:
        def scriptFile = new File(getClass().getClassLoader()
                .getResource("de/metacode/grip/HsqlInsertDataScheduled.grip").toURI())
        InitProcessor.run(scriptFile, context)
        JobProcessor.run(scriptFile, context)
        sleep(2000)

        then:
        assert sql.rows("select * from atable").size() >= 1
    }


}
