import de.metacode.grip.GripService
import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.InitProcessor
import de.metacode.grip.core.Quartz
import org.apache.commons.io.IOUtils
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratpack.groovy.handling.GroovyContext
import ratpack.handling.RequestLogger
import ratpack.http.TypedData

import javax.activation.DataSource

import static ratpack.groovy.Groovy.ratpack

/**
 * Created by mloesch on 30.01.17.
 */

ratpack {

    final Logger log = LoggerFactory.getLogger(getClass().getName())

    bindings {
        bind GripService
    }

    handlers {

        all(RequestLogger.ncsa(log))

        prefix("jobs") {
            get() {
                def jobs = ""
                def scheduler = Quartz.instance.sched
                for (String groupName : scheduler.getJobGroupNames()) {
                    for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                        def jobName = jobKey.getName()
                        def jobGroup = jobKey.getGroup()
                        //get job's trigger
                        def triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey)
                        def nextFireTime = triggers.get(0).getNextFireTime()
                        jobs += "[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime + "\n"
                    }
                }
                render jobs
            }
        }
        prefix("run") {
            post() {
                context.request.body.then { TypedData td ->
                    def scheduler = Quartz.instance.sched
                    def jobName = td.text
                    log.debug("searching for jobname $jobName")
                    for (String groupName : scheduler.getJobGroupNames()) {
                        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                            if (jobKey.name == jobName) {
                                log.debug("found job $jobName")
                                def map = scheduler.getJobDetail(jobKey).jobDataMap
                                def ctx = map["context"] as Map
                                def script = map["script"] as String
                                executeScript(ctx, script, context)
                            }
                        }
                    }
                }
            }
        }
        prefix("exec") {
            post() {
                context.request.body.then { TypedData td ->
                    def script = td.text
                    def ctx = [name: 'gripRESTExec']
                    executeScript(ctx, script, context)
                }
            }
        }
    }
}

private static void executeScript(Map gripContext, String script, GroovyContext groovyContext) {
/// init script /////////////////////////////////////////////////////////////////////////////////////
    def home = System.getProperty("user.home")
    def initScript = new File("""$home/.grip/init.grip""")
    if (initScript.exists()) {
        InitProcessor.run(initScript, gripContext)
    }
/// actual script /////////////////////////////////////////////////////////////////////////////////////
    CoreProcessor.run(script, gripContext)

/// sending response if available
    if (gripContext['response']) {
        def ds = gripContext['response'] as DataSource
        groovyContext.response.send(ds.contentType, IOUtils.toByteArray(ds.inputStream))
    } else {
        groovyContext.response.send()
    }
}