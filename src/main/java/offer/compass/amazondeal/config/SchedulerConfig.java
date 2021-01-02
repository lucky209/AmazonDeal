package offer.compass.amazondeal.config;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.entities.SchedulerControl;
import offer.compass.amazondeal.entities.SchedulerRepo;
import offer.compass.amazondeal.schedulers.TodaysDealScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulerConfig implements SchedulingConfigurer {

    private static final String TODAYS_DEAL_SCEDULER_PROPERTY_NAME="todays.deal.cron";

    @Autowired
    private TodaysDealScheduler todaysDealScheduler;
    @Autowired
    private SchedulerRepo schedulerRepo;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.triggerTodaysDealProductTask(taskRegistrar);
    }

    private void triggerTodaysDealProductTask(ScheduledTaskRegistrar taskRegistrar) {
        SchedulerControl todaysDealEntity = schedulerRepo
                .findByScheduler(TODAYS_DEAL_SCEDULER_PROPERTY_NAME);
        taskRegistrar.addTriggerTask(
                () -> {
                    if (todaysDealEntity.isEnabled()) {
                        try {
                            todaysDealScheduler.getTodaysDealProducts();
                        } catch (Exception e) {
                            log.info("Exception occurred, " + e.getMessage());
                        }
                    }
                }, triggerContext -> {
                    Date nextExecutionTime = this.saveNextExecutionTime(
                            todaysDealEntity, triggerContext);
                    log.info("next Execution Time of todays deal scheduler is " + nextExecutionTime);
                    return nextExecutionTime;
                }
        );
    }

    private Date saveNextExecutionTime(SchedulerControl schedulerEntity, TriggerContext triggerContext) {
        CronTrigger trigger = new CronTrigger(schedulerEntity.getCron());
        Date nextExecutionTime = trigger
                .nextExecutionTime(triggerContext);
        schedulerEntity.setNextExecutionTime(nextExecutionTime);
        schedulerRepo.save(schedulerEntity);
        return nextExecutionTime;
    }
}
