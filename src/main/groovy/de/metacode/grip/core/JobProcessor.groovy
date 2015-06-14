package de.metacode.grip.core

import de.metacode.grip.core.ast.HighlanderCustomizer
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.quartz.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC;

import static org.quartz.CronScheduleBuilder.cronSchedule
import static org.quartz.TriggerBuilder.newTrigger

/**
 * Created by mloesch on 29.04.15.
 */

@Slf4j
class JobProcessor {
    final String script
    final Map context

    JobProcessor(Map context, String script) {
        this.context = new HashMap(context)
        this.script = script
    }

    void schedule(String name, String cronExpression) {
        MDC.put("loggerFileName", name)
        def jobLog = LoggerFactory.getLogger(name)
        def schedMsg = "scheduling job $name for $cronExpression"
        jobLog.info(schedMsg)
        MDC.remove("loggerFileName")
        log.info(schedMsg)

        def map = new JobDataMap()
        map["script"] = this.script
        map["context"] = this.context
        map["name"] = name
        log.trace "setting context[name] to $name"
        this.context["name"] = name

        def job = JobBuilder.newJob(JobShell.class)
                .usingJobData(map)
                .withIdentity(name, name)
                .build();

        def builder = newTrigger()
                .withIdentity(name, name)
        if ("now".equals(cronExpression)) {
            builder.startNow();
        } else {
            builder.withSchedule(cronSchedule(cronExpression))
        }
        def trigger = builder.build()

        Quartz.instance.schedule(job, trigger)
    }

    static void run(File gripScript, Map context) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new HighlanderCustomizer("schedule")
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(cc)

        def script = gripScript.text
        def job = new JobProcessor(context, script)

        def grip = sh.parse(script)
        grip.setDelegate(job)
        grip.run()
    }
}