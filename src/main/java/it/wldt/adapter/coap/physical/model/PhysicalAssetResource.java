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
import java.util.function.Function;

public class PhysicalAssetResource {
    Logger logger = LoggerFactory.getLogger(PhysicalAssetResource.class);

    String name;

    private CoapPhysicalAdapterConfiguration configuration;

    private final Map<PhysicalAssetResourceListener, PhysicalAssetResourceListener.ListenerType> listeners;
    private PhysicalAssetResourceListener.ListenerType eventsNotified;

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

    public void addListener(PhysicalAssetResourceListener listener, PhysicalAssetResourceListener.ListenerType type) {
        listeners.put(listener, type);
    }

    public void removeListener(PhysicalAssetResourceListener listener) {
        listeners.remove(listener);
    }

    protected void notifyEvent(String message) {
        listeners.forEach((listener, type) -> {
            if (type == PhysicalAssetResourceListener.ListenerType.EVENT || type == PhysicalAssetResourceListener.ListenerType.ALL) {
                listener.onEvent(this, eventTranslator.apply(
                        resourceType != null && !resourceType.trim().isEmpty() ?
                                String.format("%s.%s", resourceType, name) :
                                name,
                        message));
            }
        });
    }

    protected void notifyPropertyChange(byte[] payload) {
        listeners.forEach((listener, type) -> {
            if (type == PhysicalAssetResourceListener.ListenerType.PROPERTY || type == PhysicalAssetResourceListener.ListenerType.ALL) {
                listener.onPropertyChanged(this, getRequestTranslator.apply(
                        resourceType != null && !resourceType.trim().isEmpty() ?
                                String.format("%s.%s", resourceType, name) :
                                name,
                        payload));
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

    public void stopObservation() {
        if (observeRelation != null) {
            observeRelation.proactiveCancel();
        }
        observeRelation = null;
    }

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

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
        }
        autoUpdateTimer = null;
    }

    public void updateProperty() {
        updateProperty(getBaseRequest(CoAP.Code.GET));
    }

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
            e.printStackTrace();
        }
    }

    public void sendAction(Request request) {
        if (request.getCode() == CoAP.Code.POST && !hasPostSupport ||
            request.getCode() == CoAP.Code.PUT && !hasPutSupport) {
            logger.warn("Invoked unsupported action request to {}/{}", configuration.getServerConnectionString(), name);
            return;
        }
        try {
            CoapResponse coapResponse;

            if (request == null) {
                request = getBaseRequest(CoAP.Code.POST);
            }

            if (configuration.getCustomActionRequestFunction() != null) {
                coapResponse = configuration.getCustomActionRequestFunction().apply(request);
            } else {
                coapResponse = client.advanced(request);
            }

            if (coapResponse == null) {
                notifyEvent("CoAP request got null response");
            } else if (!coapResponse.isSuccess()) {
                notifyEvent("CoAP request failed with code=" + coapResponse.getCode());
            }
        } catch (Exception e) {
            logger.error("CoAP physical adapter failed to send POST request to {}/{}", configuration.getServerConnectionString(), this.name, e);
        }
    }

    private Request getBaseRequest(CoAP.Code code) {
        Request request = new Request(code);
        request.getOptions().setUriPath(name);
        request.getOptions().setAccept(configuration.getPreferredContentFormat());

        return request;
    }
}
