package de.metacode.grip.core

import groovy.util.logging.Slf4j
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.MDC

/**
 * Created on 22.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@Slf4j
class JobShell implements Job {
    @Override
    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        def context = jobExecutionContext.jobDetail.jobDataMap["context"] as Map
        MDC.put("loggerFileName", context['name'])
        try {
            CoreProcessor.run(jobExecutionContext.jobDetail.jobDataMap.getString("script"),
                    context)
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        } finally {
            MDC.remove("loggerFileName");
        }
    }
}