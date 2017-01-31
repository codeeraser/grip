import de.metacode.grip.GripService
import de.metacode.grip.core.Quartz
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher

import static ratpack.groovy.Groovy.ratpack

/**
 * Created by mloesch on 30.01.17.
 */

ratpack {
    bindings {
        bind GripService
    }
    handlers {
        prefix("jobs"){
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
    }
}