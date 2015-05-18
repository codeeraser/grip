package de.metacode.grip

import de.metacode.grip.core.JobProcessor
import de.metacode.grip.core.Quartz
import de.metacode.grip.core.SocketActor
import org.hsqldb.Server

/**
 * Created by mloesch on 06.05.15.
 */

println "starting server"
Server.main()
println "after starting server"

Quartz.instance.start()

new SocketActor().start()

def hsql = new File('../../../../../test/de/metacode/grip/hsqlTest.grip')
JobProcessor.run(hsql)

def longRunning = new File('../../../../../test/de/metacode/grip/longRunningTest.grip')
JobProcessor.run(longRunning)


