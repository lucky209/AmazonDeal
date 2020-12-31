package offer.compass.amazondeal.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class SchedulerControl {
    @Id
    private Integer schedulerId;
    private String cron;
    private boolean enabled;
    private Date nextExecutionTime;
    private int node;
    private String scheduler;
}
