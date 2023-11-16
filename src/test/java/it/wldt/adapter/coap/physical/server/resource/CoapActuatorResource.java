package it.wldt.adapter.coap.physical.server.resource;

import it.wldt.adapter.coap.physical.server.model.GenericActuator;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;

public abstract class CoapActuatorResource<T_ACTUATOR extends GenericActuator<?, ?>> extends CoapResource {
    protected final String objectTitle;

    protected final Number actuatorVersion;

    protected String deviceId;

    protected T_ACTUATOR actuator;

    protected boolean observable;

    public CoapActuatorResource(String deviceId, String objectTitle, String name,
                                Number actuatorVersion, T_ACTUATOR actuator) {
        this(deviceId, objectTitle, name, actuatorVersion, actuator, true);
    }

    public CoapActuatorResource(String deviceId, String objectTitle, String name,
                                Number actuatorVersion, T_ACTUATOR actuator, boolean observable) {
        super(name);
        this.objectTitle = objectTitle;
        this.actuatorVersion = actuatorVersion;
        this.deviceId = deviceId;
        this.actuator = actuator;
        this.observable = observable;

        init();
    }

    private void init() {
        getAttributes().setTitle(objectTitle);

        if (observable) {
            setObservable(true);
            setObserveType(CoAP.Type.CON);
            getAttributes().setObservable();
        }

    }
}
