package de.metacode.grip.core

import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory

/**
 * Created by mloesch on 07.05.15.
 */
@Singleton
class Quartz {
    private final static Properties props = new Properties();

    static {
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "4");
    }

    Scheduler sched = new StdSchedulerFactory().scheduler

    def start() {
        sched.start()
    }

    def schedule(JobDetail job, Trigger trigger) {
        sched.scheduleJob(job, trigger);
    }
}