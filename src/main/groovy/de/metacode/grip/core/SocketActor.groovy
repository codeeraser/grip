package de.metacode.grip.core

import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DefaultActor
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
                    if (buffer.startsWith("call:")) {
                        def cmd = buffer.substring(5)
                        output << Quartz.instance.sched."$cmd"
                    }
                    if (buffer.startsWith("allstat")) {
                        Quartz.instance.sched.getJobKeys(GroupMatcher.anyGroup()).each {
                            output << Quartz.instance.sched.getJobDetail(it)
                        }
                    }
                    if (buffer.startsWith("running")) {
                        Quartz.instance.sched.getJobKeys(GroupMatcher.anyGroup()).each {
                            output << Quartz.instance.sched.getCurrentlyExecutingJobs()
                        }
                    }
                    if (buffer.startsWith("schedule")) {
                        Quartz.instance.sched.getJobKeys(GroupMatcher.anyGroup()).each {
                            output << Quartz.instance.sched.getMetaData()
                        }
                    }
                }
            }
        }
    }
}
