package it.wldt.adapter.coap.physical.server.resource;

import it.unimore.dipi.iot.utils.CoreInterfaces;
import it.wldt.adapter.coap.physical.server.model.GenericActuator;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public abstract class CoapSenmlActuatorResource<T_ACTUATOR extends GenericActuator<?, ?>> extends CoapActuatorResource<T_ACTUATOR> {

    protected final String senmlRt;
    protected final String senmlUnit;

    public CoapSenmlActuatorResource(String deviceId, String objectTitle, String name, String senmlRt, String senmlUnit,
                                     Number actuatorVersion, T_ACTUATOR actuator) {
        super(deviceId, objectTitle, name, actuatorVersion, actuator);

        this.senmlRt = senmlRt;
        this.senmlUnit = senmlUnit;

        init();
    }

    private void init() {
        getAttributes().addAttribute("rt", this.senmlRt);
        getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());
        getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
        getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_JSON));
    }
}
