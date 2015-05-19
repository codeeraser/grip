package de.metacode.grip

import org.hsqldb.Server
import org.quartz.JobBuilder
import org.quartz.impl.StdSchedulerFactory

import static org.quartz.TriggerBuilder.newTrigger

/**
 * Created by mloesch on 14.03.15.
 */

class BasicTest extends GroovyTestCase {

    void testIt() {

/// hsql server /////////////////////////////////////////////////////////////////////////////////////
        println "starting server"
        Server.main()
        println "after starting server"

/// quartz scheduler ////////////////////////////////////////////////////////////////////////////////
        def schedFact = new StdSchedulerFactory();
        def sched = schedFact.getScheduler();
        sched.start();

        def job = JobBuilder.newJob(TestJob.class)
                .withIdentity("basictest", "tests")
                .build();

        def trigger = newTrigger()
                .withIdentity("basictesttrigger", "tests")
                .startNow()
                .build();

        sched.scheduleJob(job, trigger);

        sched.shutdown(true)
    }
}