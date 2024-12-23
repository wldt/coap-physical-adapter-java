package it.wldt.adapter.coap.physical.server.resource;

import com.google.gson.Gson;
import it.unimore.dipi.iot.utils.SenMLPack;
import it.unimore.dipi.iot.utils.SenMLRecord;
import it.wldt.adapter.coap.physical.server.model.TemperatureActuator;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TemperatureActuatorResource extends CoapSenmlActuatorResource<TemperatureActuator> {
    Logger logger = LoggerFactory.getLogger(TemperatureActuatorResource.class);

    private static final String OBJECT_TITLE = "TemperatureActuator";
    private static final Number ACTUATOR_VERSION = 0.2;
    private static final String SENML_RT = "iot.actuator.temperature";
    private static final String SENML_UNIT = "Cel";

    private Gson gson;

    public TemperatureActuatorResource(String deviceId, String name, TemperatureActuator actuator) {
        super(deviceId, OBJECT_TITLE, name, SENML_RT, SENML_UNIT, ACTUATOR_VERSION, actuator);

        init();
    }

    private void init() {
        this.gson = new Gson();
    }

    private Optional<String> getJsonSenmlResponse() {
        try {
            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord baseRecord = new SenMLRecord();
            baseRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            baseRecord.setBver(actuatorVersion);
            baseRecord.setT(System.currentTimeMillis());

            SenMLRecord valueRecord = new SenMLRecord();
            valueRecord.setN("value");
            valueRecord.setU(this.senmlUnit);
            valueRecord.setV(this.actuator.getWantedTemperature());

            SenMLRecord statusRecord = new SenMLRecord();
            statusRecord.setN("status");
            statusRecord.setVs(TemperatureActuator.getStatusDescription(this.actuator.getStatus()));

            senMLPack.add(baseRecord);
            senMLPack.add(valueRecord);
            senMLPack.add(statusRecord);

            return Optional.of(gson.toJson(senMLPack));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        /*
        GET REQUEST

        Returns wanted value and status
         */
        try {
            if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                    exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {
                Optional<String> payload = getJsonSenmlResponse();

                if (payload.isPresent()) {
                    exchange.respond(CoAP.ResponseCode.CONTENT, payload.get(), exchange.getRequestOptions().getAccept());
                }
                else {
                    exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                }
            }
            else {
                exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(this.actuator.getStatus()), MediaTypeRegistry.TEXT_PLAIN);
            }
        } catch (Exception e) {
            logger.error("Resource '{}' incurred in an error", this.getName(), e);
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        /*
        POST REQUEST

        Toggle between on/off
         */
        try {
            this.actuator.toggleActive();

            exchange.respond(CoAP.ResponseCode.CHANGED);
        } catch (Exception e) {
            logger.error("Resource '{}' incurred in an error", this.getName(), e);
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        /*
        PUT REQUEST

        Change wanted value
         */
        try {
            if (exchange.getRequestPayload() != null) {
                String payload = new String(exchange.getRequestPayload());

                this.actuator.setWantedTemperature(Double.parseDouble(payload));

                exchange.respond(CoAP.ResponseCode.CHANGED);
            } else {
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Resource '{}' incurred in an error", this.getName(), e);
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}
