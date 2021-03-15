package com.spring.quartz.model;

import lombok.Data;
import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.springframework.util.StringUtils.isEmpty;

@Data
public class TriggerDescriptor {

    private String name;
    private String group;
    private LocalDateTime fireTime;
    private String cron;
    private TriggerState triggerState;

    public TriggerDescriptor setName(final String name) {
        this.name = name;
        return this;
    }
    public TriggerDescriptor setGroup(final String group) {
        this.group = group;
        return this;
    }
    public TriggerDescriptor setFireTime(final LocalDateTime fireTime) {
        this.fireTime = fireTime;
        return this;
    }
    public TriggerDescriptor setCron(final String cron) {
        this.cron = cron;
        return this;
    }
    public TriggerDescriptor setTriggerState(final TriggerState triggerState) {
        this.triggerState = triggerState;
        return this;
    }
    private String buildName() {
        return isEmpty(name) ? randomUUID().toString() : name;
    }

    public Trigger buildTrigger() {
        if (!isEmpty(cron)) {
            if (!isValidExpression(cron))
                throw new IllegalArgumentException("Provided expression " + cron + " is not a valid cron expression");
            return newTrigger()
                    .withIdentity(buildName(), group)
                    .withSchedule(cronSchedule(cron)
                            //Ignore misfires so multiple jobs don't run at once if the server goes down
                            .withMisfireHandlingInstructionDoNothing()
                            .inTimeZone(TimeZone.getTimeZone(systemDefault())))
                    .usingJobData("cron", cron)
                    .build();
        } else if (!isEmpty(fireTime)) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("fireTime", fireTime);
            return newTrigger()
                    .withIdentity(buildName(), group)
                    .withSchedule(simpleSchedule()
                            .withMisfireHandlingInstructionNextWithExistingCount())
                    .startAt(Date.from(fireTime.atZone(systemDefault()).toInstant()))
                    .usingJobData(jobDataMap)
                    .build();
        }

        throw new IllegalStateException("unsupported trigger descriptor " + this);
    }
    /**
     *
     * @param trigger
     *            the Trigger used to build this descriptor
     * @return the TriggerDescriptor
     */
    public static TriggerDescriptor buildDescriptor(Trigger trigger, TriggerState triggerState) {
        return new TriggerDescriptor()
                .setName(trigger.getKey().getName())
                .setGroup(trigger.getKey().getGroup())
                .setFireTime((LocalDateTime) trigger.getJobDataMap().get("fireTime"))
                .setCron(trigger.getJobDataMap().getString("cron"))
                .setTriggerState(triggerState);
    }
}