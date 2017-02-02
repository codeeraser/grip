import de.metacode.grip.GripService
import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.InitProcessor
import de.metacode.grip.core.Quartz
import org.apache.commons.io.IOUtils
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher
import ratpack.http.TypedData

import javax.activation.DataSource

import static ratpack.groovy.Groovy.ratpack

/**
 * Created by mloesch on 30.01.17.
 */

ratpack {
    bindings {
        bind GripService
    }
    handlers {
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
        prefix("exec") {
            post() {
                context.request.body.then { TypedData td ->
                    def script = td.text
                    def ctx = [name: 'RESTexec']
/// init script /////////////////////////////////////////////////////////////////////////////////////
                    def home = System.getProperty("user.home")
                    def initScript = new File("""$home/.grip/init.grip""")
                    if (initScript.exists()) {
                        InitProcessor.run(initScript, ctx)
                    }
/// grip script /////////////////////////////////////////////////////////////////////////////////////
                    CoreProcessor.run(script, ctx)
                    if (ctx['responseText']) {
                        context.response.send(ctx['responseText'] as String)
                    } else if (ctx['responseDataSource']) {
                        def ds = ctx['responseDataSource'] as DataSource
                        context.response.send(ds.contentType, IOUtils.toByteArray(ds.inputStream))
                    } else {
                        context.response.send()
                    }
                }
            }
        }
    }
}