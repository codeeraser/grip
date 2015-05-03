package de.metacode.grip.core

import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.quartz.*

import static org.quartz.TriggerBuilder.newTrigger

/**
 * Created by mloesch on 29.04.15.
 */

@Slf4j
class JobProcessor {
    static final String JOB = "job"
    final Binding binding
    final Scheduler sched
    final String script

    static class JobShell implements Job {
        @Override
        void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
/// compiler configuration //////////////////////////////////////////////////////////////////////////
            def cc = new CompilerConfiguration()
            cc.addCompilationCustomizers new MoveToTopCustomizer("env")
            cc.scriptBaseClass = DelegatingScript.class.name
            def sh = new GroovyShell(cc)

            def binding = new Binding()
            def core = new CoreProcessor(binding)

            /// grip script /////////////////////////////////////////////////////////////////////////////////////
            def grip = sh.parse(jobExecutionContext.getJobDetail().jobDataMap.getString("script"))
            grip.setDelegate(core)
            grip.run() //run with CoreProcessor to do the actual work
            log.info(binding.properties.toMapString())
        }
    }

    JobProcessor(Binding binding, String script, Scheduler sched) {
        this.binding = binding
        this.script = script
        this.sched = sched
    }

    void schedule(String name) {
        log.info("schedule job $name!")

        def job = JobBuilder.newJob(JobShell.class).usingJobData("script", this.script).withIdentity(name, "tests").build();

        def trigger = newTrigger()
                .withIdentity("basictesttrigger", "tests")
                .startNow()
                .build();

        sched.scheduleJob(job, trigger);
    }
}
