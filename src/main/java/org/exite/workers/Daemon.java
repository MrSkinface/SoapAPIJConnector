package org.exite.workers;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by levitsky on 11.02.19
 */
@Slf4j
public abstract class Daemon implements Runnable {

    protected boolean execute = false;
    protected long sleepTime;

    protected abstract void onInit() throws Exception;
    protected abstract void beforeExecute() throws Exception;
    protected abstract void afterExecute() throws Exception;

    protected abstract void execute() throws Exception;

    @Override
    public void run() {
        try{
            onInit();
            while (execute){
                beforeExecute();
                execute();
                afterExecute();
            }
        } catch (InterruptedException ignore) {
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    protected final void sleep(long seconds){
        try {
            log.info("Sleeping {} sec", seconds);
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
