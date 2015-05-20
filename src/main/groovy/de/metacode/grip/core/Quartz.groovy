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
    Scheduler sched = new StdSchedulerFactory().getScheduler()

    def start() {
        sched.start()
    }

    def schedule(JobDetail job, Trigger trigger) {
        sched.scheduleJob(job, trigger);
    }
}