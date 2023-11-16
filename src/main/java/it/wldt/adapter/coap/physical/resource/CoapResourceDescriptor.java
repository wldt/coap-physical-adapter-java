package it.wldt.adapter.coap.physical.resource;

import it.wldt.adapter.coap.physical.resource.event.ListenableResource;
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
 * @see ListenableResource
 * @see it.wldt.adapter.coap.physical.resource.event.PayloadListener
 */
public class CoapResourceDescriptor extends ListenableResource {
    private transient Logger logger = LoggerFactory.getLogger(CoapResourceDescriptor.class);

    protected final CoapClient client;

    private final String serverUrl;
    private final String resourceUri;

    private int preferredContentType;

    private Boolean observable;
    private CoapObserveRelation observeRelation;

    protected int lastPayloadContentType = MediaTypeRegistry.TEXT_PLAIN;
    protected byte[] lastPayload = "".getBytes();

    protected String lastError = "";
    protected Boolean notifyErrors;

    private Boolean autoUpdated;
    private long autoUpdateTimerPeriod;
    private Timer autoUpdateTimer;


    public CoapResourceDescriptor(String serverUrl, String resourceUri, boolean notifyErrors) {
        this.serverUrl = serverUrl;
        this.resourceUri = resourceUri;
        this.notifyErrors = notifyErrors;

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

    public int getLastPayloadContentType() {
        return lastPayloadContentType;
    }

    private void setLastPayload(byte[] value, int ct) {
        this.lastPayload = value;
        this.lastPayloadContentType = ct;
        notifyPayloadListeners(value);
    }

    private void setLastError(String value) {
        this.lastError = value;
        if (notifyErrors)
            notifyErrorListeners(value);
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

        Request request = this.getRequestOptionsBase(CoAP.Code.GET);

        request.setObserve();

        try {
            observeRelation = client.observe(request, new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    manageResponse(coapResponse);
                }

                @Override
                public void onError() {
                    logger.error("CoapResourceDescriptor - {}: observation error", getResourceUri());
                    setLastError("Notification error");
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

    /**
     * Sets the new auto update timer period.
     * If the resource is already in observation mode this method does not turn off the observation, but it has to be shut down manually.
     * Notice that after invoking this method the timer does NOT start/restart automatically, but it has to be done MANUALLY.
     *
     * @param period the new timer update period
     */
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

    protected Request getRequestOptionsBase(CoAP.Code code) {
        Request request = new Request(code);

        OptionSet options = new OptionSet();

        options.setAccept(this.preferredContentType);

        request.setOptions(options);
        request.setURI(this.client.getURI());
        request.getOptions().setUriPath(this.resourceUri);
        request.setConfirmable(true);

        return request;
    }

    private void manageResponse(CoapResponse response) {
        if (response == null) {
            setLastError("Response is null");
        } else if (!response.isSuccess()) {
            setLastError("Response code: " + response.getCode());
        } else {
            setLastPayload(response.getPayload(), response.getOptions().getContentFormat());
        }
    }

    /**
     * Sends a CoAP GET request to the client.
     * This method is not implemented via a {@code resource.methods} package's interface but in the resource descriptor
     * since it's expected by each CoRE interface
     */
    private void sendGET() {
        Request request = this.getRequestOptionsBase(CoAP.Code.GET);

        try {
            CoapResponse response = client.advanced(request);

            manageResponse(response);
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Add POST, PUT, DELETE for actuators
}
