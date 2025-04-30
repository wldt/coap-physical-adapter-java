package it.wldt.adapter.coap.physical.server.model;

public class TemperatureActuator extends GenericActuator<Double, Integer> {
    public static final int STATUS_OFF = -1;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_COOLING = 1;
    public static final int STATUS_HEATING = 2;

    public static final double MAX_TEMPERATURE = 50;
    public static final double MIN_TEMPERATURE = 0;

    public static final double MAX_DELTA = 2.0;
    public static final double MIN_DELTA = 0.1;

    public static final double DEFAULT_TEMPERATURE_DELTA = 1.0;
    public static final double DEFAULT_WANTED_TEMPERATURE = 20.0;

    private double wantedTemperature;
    private double delta;

    public TemperatureActuator() {
        this(DEFAULT_WANTED_TEMPERATURE, DEFAULT_TEMPERATURE_DELTA);
    }

    public TemperatureActuator(double wantedTemperature) {
        this(wantedTemperature, DEFAULT_TEMPERATURE_DELTA);
    }

    public TemperatureActuator(double wantedTemperature, double temperatureDelta) {
        this.wantedTemperature = wantedTemperature;
        this.delta = temperatureDelta;

        setStatus(STATUS_OFF);
    }

    public double getWantedTemperature() {
        return wantedTemperature;
    }

    public void setWantedTemperature(double wantedTemperature) {
        // Check that the wanted temperature is between the possible values, otherwise change it
        wantedTemperature = Math.min(wantedTemperature, MAX_TEMPERATURE);
        wantedTemperature = Math.max(wantedTemperature, MIN_TEMPERATURE);

        this.wantedTemperature = wantedTemperature;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        // Check that the delta is between the possible values, otherwise change it
        delta = Math.min(delta, MAX_DELTA);     // if (delta > MAX_DELTA) { delta = MAX_DELTA; }
        delta = Math.max(delta, MIN_DELTA);     // if (delta < MIN_DELTA) { delta = MIN_DELTA; }

        this.delta = delta;
    }

    public static String getStatusDescription(int status) {
        switch (status) {
            case STATUS_OFF: {
                return "Off";
            }
            case STATUS_IDLE: {
                return "Idle";
            }
            case STATUS_COOLING: {
                return "Cooling";
            }
            case STATUS_HEATING: {
                return "Heating";
            }
            default: {
                return "Unknown";
            }
        }
    }

    @Override
    public void onDataChanged(Double value) {
        if (value == null) {
            value = wantedTemperature;
        }
        setSavedValue(value);

        if (this.isActive()) {
            if (value > wantedTemperature + delta) {
                setStatus(STATUS_COOLING);
            }
            else if (value < wantedTemperature - delta) {
                setStatus(STATUS_HEATING);
            }
            else {
                setStatus(STATUS_IDLE);
            }
        }
        else {
            setStatus(STATUS_OFF);
        }
    }
}
