package it.wldt.adapter.coap.physical.model;

import it.wldt.adapter.coap.physical.CoapPhysicalAdapterConfiguration;
import it.wldt.core.event.WldtEvent;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Represents a CoAP resource.
 * It is used to send requests and receive responses from the server.
 */
public class PhysicalAssetResource {
    Logger logger = LoggerFactory.getLogger(PhysicalAssetResource.class);

    String name;

    private final CoapPhysicalAdapterConfiguration configuration;

    private final Map<PhysicalAssetResourceListener, PhysicalAssetResourceListener.ListenerType> listeners;

    private CoapClient client;

    private String resourceType;

    private boolean observable;

    private BiFunction<String, byte[], List<? extends WldtEvent<?>>> getRequestTranslator;

    private boolean hasPostSupport;
    private boolean hasPutSupport;

    private BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator;

    private int contentType;

    private CoapObserveRelation observeRelation;
    private Timer autoUpdateTimer;

    public PhysicalAssetResource(CoapPhysicalAdapterConfiguration configuration,
                                 String name,
                                 String resourceType,
                                 int contentType,
                                 BiFunction<String, byte[], List<? extends WldtEvent<?>>> getRequestTranslator,
                                 BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator) {
        this(configuration, name, resourceType, contentType, getRequestTranslator, false, false, eventTranslator, false);
    }
    public PhysicalAssetResource(CoapPhysicalAdapterConfiguration configuration,
                                 String name,
                                 String resourceType,
                                 int contentType,
                                 BiFunction<String, byte[], List<? extends WldtEvent<?>>> getRequestTranslator,
                                 BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator,
                                 boolean observable) {
        this(configuration, name, resourceType, contentType, getRequestTranslator, false, false, eventTranslator, observable);
    }

    public PhysicalAssetResource(CoapPhysicalAdapterConfiguration configuration,
                                 String name,
                                 String resourceType,
                                 int contentType,
                                 BiFunction<String, byte[], List<? extends WldtEvent<?>>> getRequestTranslator,
                                 boolean hasPostSupport,
                                 boolean hasPutSupport,
                                 BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator) {
        this(configuration, name, resourceType, contentType, getRequestTranslator, hasPostSupport, hasPutSupport, eventTranslator, false);
    }

    public PhysicalAssetResource(CoapPhysicalAdapterConfiguration configuration,
                                 String name,
                                 String resourceType,
                                 int contentType,
                                 BiFunction<String, byte[], List<? extends WldtEvent<?>>> getRequestTranslator,
                                 boolean hasPostSupport,
                                 boolean hasPutSupport,
                                 BiFunction<String, String, List<? extends WldtEvent<?>>> eventTranslator,
                                 boolean observable) {
        this.configuration = configuration;

        this.name = name;
        this.resourceType = resourceType;
        this.contentType = contentType;

        this.getRequestTranslator = getRequestTranslator;
        this.hasPostSupport = hasPostSupport;
        this.hasPutSupport = hasPutSupport;

        this.eventTranslator = eventTranslator;

        this.observable = observable;

        this.client = new CoapClient(String.format("%s/%s", configuration.getServerConnectionString(), name));

        this.listeners = new HashMap<>();
    }

    /**
     * Adds a listener to the resource.
     * The listener must be an implementation of {@link PhysicalAssetResource}.
     * @param listener The listener to add.
     * @param type     The type of data the listener is interested in.
     */
    public void addListener(PhysicalAssetResourceListener listener, PhysicalAssetResourceListener.ListenerType type) {
        listeners.put(listener, type);
    }

    /**
     * Removes a listener from the resource.
     * @param listener The listener to remove.
     */
    public void removeListener(PhysicalAssetResourceListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners of an event.
     * @param message The event message.
     */
    protected void notifyEvent(String message) {
        listeners.forEach((listener, type) -> {
            if (type == PhysicalAssetResourceListener.ListenerType.EVENT || type == PhysicalAssetResourceListener.ListenerType.ALL) {
                listener.onEvent(this, eventTranslator.apply(
                        this.resourceType.trim().isEmpty() ? this.name : this.resourceType.concat(".").concat(this.name),
                        message));
            }
        });
    }

    /**
     * Notifies all listeners of a property change.
     * @param payload The new value of the property.
     */
    protected void notifyPropertyChange(byte[] payload) {
        listeners.forEach((listener, type) -> {
            if (type == PhysicalAssetResourceListener.ListenerType.PROPERTY || type == PhysicalAssetResourceListener.ListenerType.ALL) {
                listener.onPropertyChanged(this, getRequestTranslator.apply(
                        this.resourceType.trim().isEmpty() ? this.name : this.resourceType.concat(".").concat(this.name),
                        payload
                ));
            }
        });
    }

    public String getName() {
        return name;
    }

    public BiFunction<String, byte[], List<? extends WldtEvent<?>>> getGetRequestTranslator() {
        return getRequestTranslator;
    }

    public boolean isPostSupported() {
        return hasPostSupport;
    }

    public boolean isPutSupported() {
        return hasPutSupport;
    }

    public BiFunction<String, String, List<? extends WldtEvent<?>>> getEventTranslator() {
        return eventTranslator;
    }

    public int getContentType() {
        return contentType;
    }

    public boolean isObservable() {
        return observable;
    }

    public String getResourceType() {
        return resourceType;
    }

    /**
     * Starts observing the resource.
     * If any polling or previous observation is active, they will be cancelled.
     * This method will send an observe request to the resource.
     * After the observe relation is established, the onLoad method will be called at any new property update.
     * If an error happens at any point during the observation process, it will get logged as a warning.
     * If an error occurs while establishing the observe relation, it will be logged as an error.
     */
    public void startObservation() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }

        if (observeRelation != null)
            observeRelation.proactiveCancel();

        Request request = getBaseRequest(CoAP.Code.GET);
        request.setObserve();

        try {
            observeRelation = client.observe(request, new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    if (coapResponse == null) {
                        notifyEvent("Observed null CoAP response");
                    } else if (!coapResponse.isSuccess()) {
                        notifyEvent("Observed CoAP response with code=" + coapResponse.getCode());
                    } else {
                        notifyPropertyChange(coapResponse.getPayload());
                    }
                }

                @Override
                public void onError() {
                    logger.warn("CoAP physical adapter got a resource observation error from {}/{}", configuration.getServerConnectionString(), name);
                }
            });
        } catch (Exception e) {
            logger.error("CoAP physical adapter failed to establish observe relation with {}/{}", configuration.getServerConnectionString(), this.name, e);
        }
    }

    /**
     * Stops observing the resource.
     */
    public void stopObservation() {
        if (observeRelation != null) {
            observeRelation.proactiveCancel();
        }
        observeRelation = null;
    }

    /**
     * Starts a timer that will periodically update the property.
     * If any observation or previous polling is active, they will be cancelled.
     * @param autoUpdateInterval The interval in milliseconds between each update call.
     */
    public void startAutoUpdate(long autoUpdateInterval) {
        if (observeRelation != null) {
            observeRelation.proactiveCancel();
            observeRelation = null;
        }

        if (autoUpdateTimer != null)
            autoUpdateTimer.cancel();

        autoUpdateTimer = new Timer();
        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateProperty();
            }
        }, 0, autoUpdateInterval);
    }

    /**
     * Stops the timer that updates the property.
     */
    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
        }
        autoUpdateTimer = null;
    }

    public void updateProperty() {
        updateProperty(getBaseRequest(CoAP.Code.GET));
    }

    /**
     * Sends a GET request to the resource.
     * If a custom request method is provided in the configuration, it will be used to send the request, otherwise the default method will be used.
     * If an error occurs during the communication, it gets logged as an error.
     * @param request The request to send, if null a default GET request will be created instead.
     */
    public void updateProperty(Request request) {
        try {

            CoapResponse coapResponse;

            if (request == null) {
                request = getBaseRequest(CoAP.Code.GET);
            }

            if (configuration.getCustomPropertyRequestFunction() != null) {
                coapResponse = configuration.getCustomPropertyRequestFunction().apply(request);
            } else {
                coapResponse = client.advanced(request);
            }

            if (coapResponse == null) {
                notifyEvent("CoAP request got null response");
            } else if (!coapResponse.isSuccess()) {
                notifyEvent("CoAP request failed with code=" + coapResponse.getCode());
            } else {
                notifyPropertyChange(coapResponse.getPayload());
            }
        } catch (Exception e) {
            logger.error("CoAP physical adapter failed to send GET request to {}/{}", configuration.getServerConnectionString(), this.name, e);
        }
    }

    /**
     * Sends an action request to the resource.
     * If the response is not successful an event will be notified.
     * If an error occurs during the communication, it gets logged as an error.
     * @param request The request to send, if null a POST request will be created instead.
     */
    public void sendAction(Request request) {
        if (request == null) {
            request = getBaseRequest(CoAP.Code.POST);
        }

        if (request.getCode() == CoAP.Code.POST && !hasPostSupport ||
            request.getCode() == CoAP.Code.PUT && !hasPutSupport) {
            logger.warn("Invoked unsupported action request to {}/{}", configuration.getServerConnectionString(), name);
            return;
        }
        try {
            CoapResponse coapResponse;

            if (configuration.getCustomActionRequestFunction() != null) {
                coapResponse = configuration.getCustomActionRequestFunction().apply(request);
            } else {
                request.getOptions().setUriPath(name);
                coapResponse = client.advanced(request);
            }

            if (coapResponse == null) {
                logger.warn("CoAP request got null response");
                notifyEvent("CoAP request got null response");
            } else if (!coapResponse.isSuccess()) {
                logger.warn("CoAP request failed with code={}", coapResponse.getCode());
                notifyEvent("CoAP request failed with code=" + coapResponse.getCode());
            } else {
                logger.info("CoAP request succeeded");
            }
        } catch (Exception e) {
            logger.error("CoAP physical adapter failed to send request to {}/{}", configuration.getServerConnectionString(), this.name, e);
        }
    }

    /**
     * Creates a base request with the given code setting the URI path and the Accept options.
     * @param code The CoAP request code.
     * @return The base request.
     */
    private Request getBaseRequest(CoAP.Code code) {
        Request request = new Request(code);
        request.getOptions().setUriPath(name);
        request.getOptions().setAccept(configuration.getPreferredContentFormat());

        return request;
    }
}
