package de.metacode.grip.core

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

/**
 * Created on 22.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
class JobShell implements Job {
    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        CoreProcessor.run(jobExecutionContext.jobDetail.jobDataMap.getString("script"),
                jobExecutionContext.jobDetail.jobDataMap["context"] as Map)
    }
}