package de.metacode.grip.core

import de.metacode.grip.core.ast.HighlanderCustomizer
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.quartz.*

import static org.quartz.CronScheduleBuilder.cronSchedule
import static org.quartz.TriggerBuilder.newTrigger

/**
 * Created by mloesch on 29.04.15.
 */

@Slf4j
class JobProcessor {
    final Binding binding
    final String script

    static class JobShell implements Job {
        @Override
        void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            CoreProcessor.run(jobExecutionContext.jobDetail.jobDataMap.getString("script"),
                    jobExecutionContext.jobDetail.jobDataMap["binding"] as Binding)
        }
    }

    JobProcessor(Binding binding, String script) {
        this.binding = binding
        this.script = script
    }

    void schedule(String name, String cronExpression) {
        log.info("scheduling job $name!")

        def map = new JobDataMap()
        map["script"] = this.script
        map["binding"] = this.binding

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
        def trigger = builder.build();

        Quartz.instance.schedule(job, trigger);
    }

    static void run(File gripScript, Binding binding) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new HighlanderCustomizer("schedule")
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(cc)

        def script = gripScript.text
        def job = new JobProcessor(binding, script)

        def grip = sh.parse(script)
        grip.setDelegate(job)
        grip.run()
    }

}
