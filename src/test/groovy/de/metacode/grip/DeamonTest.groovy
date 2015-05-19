package de.metacode.grip

import de.metacode.grip.core.JobProcessor
import de.metacode.grip.core.Quartz
import org.hsqldb.Server

/**
 * Created by mloesch on 29.04.15.
 */
class DeamonTest extends GroovyTestCase {

    void testIt() {
/// hsql server /////////////////////////////////////////////////////////////////////////////////////
        println "starting server"
        Server.main()
        println "after starting server"

        Quartz.instance.start()
//// interpred job //////////////////////////////////////////////////////////////////////////////////
        log.info('executing dsl engine')
/// compiler configuration ////////////////////////////////////////////////////////////////////////////
        def scriptFile = new File('./src/test/groovy/de/metacode/grip/hsqlTest.grip')
        JobProcessor.run(scriptFile)

        Quartz.instance.shutdown(true)
    }

}
