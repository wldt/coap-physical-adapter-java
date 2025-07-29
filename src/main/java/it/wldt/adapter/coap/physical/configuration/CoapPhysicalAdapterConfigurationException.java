package it.wldt.adapter.coap.physical.configuration;

/**
 * Exception thrown when there is a configuration error in the CoAP physical adapter configuration.
 */
public class CoapPhysicalAdapterConfigurationException extends Exception{
    public CoapPhysicalAdapterConfigurationException(String message) {
        super(message);
    }
}
