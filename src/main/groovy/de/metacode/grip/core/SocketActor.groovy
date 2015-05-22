package de.metacode.grip.core

import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DefaultActor
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher

/**
 * Created by mloesch on 07.05.15.
 */

@Slf4j
class SocketActor extends DefaultActor {
    @Override
    protected void act() {
        def listenPort = 4242
        def server = new ServerSocket(listenPort)
        loop {
            server.accept { socket ->
                socket.withStreams { input, output ->
                    def reader = input.newReader()
                    def buffer = reader.readLine()

                    def sched = Quartz.instance.sched
                    if (buffer.startsWith("call:")) {
                        def cmd = buffer.substring(5)
                        output << sched."$cmd"
                    }
                    if (buffer.startsWith("allstat")) {
                        output << listJobsAndStat()
                    }
                    if (buffer.startsWith("running")) {
                        sched.getJobKeys(GroupMatcher.anyGroup()).each {
                            output << sched.getCurrentlyExecutingJobs()
                        }
                    }
                    if (buffer.startsWith("schedule")) {
                        sched.getJobKeys(GroupMatcher.anyGroup()).each {
                            output << sched.getMetaData()
                        }
                    }
                }
            }
        }
    }

    static def listJobsAndStat() {
        def sched = Quartz.instance.sched
        def sb = new StringBuilder()
        for (String groupName : sched.jobGroupNames) {
            for (JobKey jobKey : sched.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.name;
                String jobGroup = jobKey.group;

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) sched.getTriggersOfJob(jobKey);
                Date nextFireTime = triggers[0].nextFireTime;

                sb.append("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime)
                sb.append('\n')
            }
        }
        sb.toString()
    }

}
