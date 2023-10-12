package it.wldt.adapter.coap.physical.server.model;

import it.wldt.adapter.coap.physical.server.event.DataListenerManager;

import java.util.Timer;
import java.util.TimerTask;

public abstract class GenericSensor<T> extends DataListenerManager<T> {
    protected T value;
    private Timer timer = null;

    protected long timerUpdatePeriod;
    protected long timerTaskDelayTime;

    public static final long DEFAULT_UPDATE_PERIOD = 5000;
    public static final long DEFAULT_TASK_DELAY_TIME = 5000;

    public GenericSensor() {
        this(DEFAULT_UPDATE_PERIOD, DEFAULT_TASK_DELAY_TIME);
    }

    public GenericSensor(long timerUpdatePeriod, long timerTaskDelayTime) {
        this.timerUpdatePeriod = timerUpdatePeriod;
        this.timerTaskDelayTime = timerTaskDelayTime;

        init();
        startTimer();
    }

    protected void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public long getTimerUpdatePeriod() {
        return timerUpdatePeriod;
    }

    public void setTimerUpdatePeriod(long timerUpdatePeriod) {
        this.timerUpdatePeriod = timerUpdatePeriod;
    }

    public long getTimerTaskDelayTime() {
        return timerTaskDelayTime;
    }

    public void setTimerTaskDelayTime(long timerTaskDelayTime) {
        this.timerTaskDelayTime = timerTaskDelayTime;
    }

    private void startTimer() {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMeasurement();

                notifyListeners(value);
            }
        }, timerTaskDelayTime, timerUpdatePeriod);
    }

    protected abstract void init();

    protected abstract void updateMeasurement();
}
