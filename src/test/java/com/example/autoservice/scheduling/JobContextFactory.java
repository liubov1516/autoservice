package com.example.autoservice.scheduling;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;

import java.util.Date;

// Збирає справжній JobExecutionContext так, як це робить Quartz під час запуску,
// але без планувальника й без Spring. Тому getMergedJobDataMap() працює "по-справжньому":
// значення JobDetail, поверх них - значення тригера.
final class JobContextFactory {

    private JobContextFactory() {
    }

    static JobExecutionContextImpl create(JobDetail jobDetail, Trigger trigger, Job job) {
        Date now = new Date();
        TriggerFiredBundle bundle = new TriggerFiredBundle(
                jobDetail, (OperableTrigger) trigger, null, false, now, now, null, null);
        return new JobExecutionContextImpl(null, bundle, job);
    }
}
