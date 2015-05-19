package de.metacode.grip

import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import de.metacode.grip.env.SqlEnv
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * Created by mloesch on 27.04.15.
 */
@Slf4j
class TestJob implements Job {

    TestJob() {
    }

    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info('executing dsl engine')
/// compiler configuration //////////////////////////////////////////////////////////////////////////
        def cc = new CompilerConfiguration()
        cc.scriptBaseClass = DelegatingScript.class.name
        cc.addCompilationCustomizers new MoveToTopCustomizer("env")
        cc.addCompilationCustomizers new RemoveCustomizer("schedule")
        def sh = new GroovyShell(cc)

//This is how create-methods for plugin environments could be injected
        CoreProcessor.metaClass.createSql = { Closure c ->
            def sql = new SqlEnv()
            c.delegate = sql
            c.resolveStrategy = DELEGATE_ONLY
            c()
            sql
        }

        def binding = new Binding()
        def core = new CoreProcessor(binding)

        /// grip script /////////////////////////////////////////////////////////////////////////////////////
        def scriptFile = new File('./test/groovy/de/metacode/grip/hsqlTest.grip')
        def script = scriptFile.text
        def grip = sh.parse(script)
        grip.setDelegate(core)
        grip.run() //run with CoreProcessor to do the actual work
        log.info(binding.properties.toMapString())
    }
}
