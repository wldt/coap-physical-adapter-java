package it.wldt.adapter.coap.physical.resource;

import it.wldt.adapter.coap.physical.resource.event.ListenablePayloadResource;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
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

    private Boolean observable;
    private CoapObserveRelation observeRelation;

    protected byte[] lastPayload = "".getBytes();

    private Boolean autoUpdated;
    private long autoUpdateTimerPeriod;
    private Timer autoUpdateTimer;


    private CoapResourceDescriptor(String serverUrl, String resourceUri) {
        this.serverUrl = serverUrl;
        this.resourceUri = resourceUri;
        this.client = new CoapClient(serverUrl);

    }

    public CoapResourceDescriptor(String serverUrl, String resourceUri, long autoUpdatePeriod) {
        this(serverUrl, resourceUri);

        this.observable = false;
        this.autoUpdated = true;

        this.autoUpdateTimerPeriod = autoUpdatePeriod;

        init();
    }

    public CoapResourceDescriptor(String serverUrl, String resourceUri, boolean observable) {
        this(serverUrl, resourceUri);

        this.observable = observable;
        this.autoUpdated = !this.observable;

        init();
    }

    private void init() {
        if (observable) {
            Request request = this.createRequest(CoAP.Code.GET);

            request.setObserve();

            try {
                observeRelation = client.observe(request, new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {

                        if (coapResponse != null && coapResponse.isSuccess()) {
                            setLastPayload(coapResponse.getPayload());
                            logger.info("Received new payload");
                        } else {
                            logger.info("Payload is null or unsuccessful");
                        }
                    }

                    @Override
                    public void onError() {
                        logger.error("Observation error");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (autoUpdated && !observable) {
            setAutoUpdate(0L, this.autoUpdateTimerPeriod);
        } else {
            autoUpdated = false;
        }
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

    private void setLastPayload(byte[] value) {
        this.lastPayload = value;
        notifyListeners(value);
    }

    public void setAutoUpdate(long delay, long period) {
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
        }, delay, period);
    }

    public void stopAutoUpdate() {
        autoUpdateTimer.cancel();
        autoUpdateTimer.purge();
    }

    protected Request createRequest(CoAP.Code code) {
        Request request = new Request(code);

        OptionSet options = new OptionSet();

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
                setLastPayload(response.getPayload());
            }
        } catch (ConnectorException | IOException e) {
            e.printStackTrace();
        }
    }
}
