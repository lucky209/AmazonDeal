package offer.compass.amazondeal.config;

import lombok.extern.slf4j.Slf4j;
import offer.compass.amazondeal.entities.PropertiesRepo;
import offer.compass.amazondeal.entities.SchedulerControl;
import offer.compass.amazondeal.entities.SchedulerRepo;
import offer.compass.amazondeal.schedulers.TodaysDealScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
        SchedulerControl todaysDealSchedulerEntity = schedulerRepo
                .findByScheduler(TODAYS_DEAL_SCEDULER_PROPERTY_NAME);
        taskRegistrar.addTriggerTask(
                () -> {
                    if (todaysDealSchedulerEntity.isEnabled()) {
                        try {
                            todaysDealScheduler.getTodaysDealProducts();
                        } catch (Exception e) {
                            log.info("Exception occurred, " + e.getMessage());
                        }
                    }
                }, triggerContext -> {
                    String cron = todaysDealSchedulerEntity.getCron();
                    CronTrigger trigger = new CronTrigger(cron);
                    Date nextExecutionTime = trigger
                            .nextExecutionTime(triggerContext);
                    todaysDealSchedulerEntity.setNextExecutionTime(nextExecutionTime);
                    schedulerRepo.save(todaysDealSchedulerEntity);
                    log.info("next Execution Time is " + nextExecutionTime);
                    return nextExecutionTime;
                }
        );
    }
}
