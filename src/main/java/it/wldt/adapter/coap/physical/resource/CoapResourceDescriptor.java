package it.wldt.adapter.coap.physical.resource;

import it.wldt.adapter.coap.physical.resource.event.ListenablePayloadResource;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents a remote CoAP resource descriptor whose payload gets constantly updated by resource observability or automatic requests sent once every a set time period.
 * Payload changes can be listened via the use of a {@code PayloadListener} implementation.
 *
 * @see ListenablePayloadResource
 * @see it.wldt.adapter.coap.physical.resource.event.PayloadListener
 */
public class CoapResourceDescriptor extends ListenablePayloadResource {
    private transient Logger logger = LoggerFactory.getLogger(CoapResourceDescriptor.class);

    protected final CoapClient client;

    private final String serverUrl;
    private final String resourceUri;

    private int preferredContentType;

    private Boolean observable;
    private CoapObserveRelation observeRelation;

    protected int lastPayloadContentType = MediaTypeRegistry.TEXT_PLAIN;
    protected byte[] lastPayload = "".getBytes();

    private Boolean autoUpdated;
    private long autoUpdateTimerPeriod;
    private Timer autoUpdateTimer;


    public CoapResourceDescriptor(String serverUrl, String resourceUri) {
        this.serverUrl = serverUrl;
        this.resourceUri = resourceUri;
        this.client = new CoapClient(serverUrl);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public byte[] getLastPayload() {
        return lastPayload;
    }

    private void setLastPayload(byte[] value, int ct) {
        this.lastPayload = value;
        this.lastPayloadContentType = ct;
        notifyListeners(value);
    }

    public void setPreferredContentType(int preferredContentType) {
        this.preferredContentType = preferredContentType;
    }

    public int getPreferredContentType() {
        return this.preferredContentType;
    }

    public void startObserving() {
        this.stopObserving();
        this.stopAutoUpdate();

        this.observable = true;

        Request request = this.createRequest(CoAP.Code.GET);

        request.setObserve();

        try {
            observeRelation = client.observe(request, new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {

                    if (coapResponse != null && coapResponse.isSuccess()) {
                        logger.info("CoapResourceDescriptor - {} received new payload", getResourceUri());
                        setLastPayload(coapResponse.getPayload(), coapResponse.getOptions().getContentFormat());
                    } else {
                        logger.info("CoapResourceDescriptor - {} received null or unsuccessful payload", getResourceUri());
                    }
                }

                @Override
                public void onError() {
                    logger.error("CoapResourceDescriptor - {}: observation error", getResourceUri());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopObserving() {
        if (this.observeRelation != null) {
            observeRelation.proactiveCancel();
        }
        this.observable = false;
    }

    public void setAutoUpdatePeriod(long period) {
        this.autoUpdateTimerPeriod = period;
    }

    public void startAutoUpdate() {
        if (this.autoUpdateTimer == null) {
            this.autoUpdateTimer = new Timer();
        } else {
            stopAutoUpdate();
        }

        this.autoUpdated = true;

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendGET();
            }
        }, this.autoUpdateTimerPeriod, 0L);
    }

    public void stopAutoUpdate() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer.purge();
        }
        this.autoUpdated = false;
    }

    protected Request createRequest(CoAP.Code code) {
        Request request = new Request(code);

        OptionSet options = new OptionSet();

        options.setAccept(this.preferredContentType);

        request.setOptions(options);
        request.setURI(this.client.getURI());
        request.getOptions().setUriPath(this.resourceUri);
        request.setConfirmable(true);

        return request;
    }

    private void sendGET() {
        Request request = this.createRequest(CoAP.Code.GET);

        try {
            CoapResponse response = client.advanced(request);

            if (response != null) {
                setLastPayload(response.getPayload(), response.getOptions().getContentFormat());
            }
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Add POST, PUT, DELETE for actuators
}
