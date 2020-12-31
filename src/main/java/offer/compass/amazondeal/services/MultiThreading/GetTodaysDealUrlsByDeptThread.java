package offer.compass.amazondeal.services.MultiThreading;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetTodaysDealUrlsByDeptThread extends Thread {

    private final GetUrlsByDeptTask task;
    private final String deptName;

    public GetTodaysDealUrlsByDeptThread(GetUrlsByDeptTask task, String deptName) {
        this.task = task;
        this.deptName = deptName;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("::: " + Thread.currentThread().getName() + " is started...");
        try {
            Thread.sleep(2000);
            task.getUrlsProcess(deptName);
        } catch (Exception ex) {
            log.info("::: Error Occurred in " + Thread.currentThread().getName() + ". Exception is " + ex.getMessage());
            log.info("::: So retrying the " + deptName);
            task.getUrlsProcess(deptName);
        }
    }
}
