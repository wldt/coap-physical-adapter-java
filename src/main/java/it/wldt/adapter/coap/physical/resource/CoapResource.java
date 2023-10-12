package it.wldt.adapter.coap.physical.resource;

import it.wldt.adapter.coap.physical.resource.event.ListenablePayloadResource;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class CoapResource extends ListenablePayloadResource {

    protected final CoapClient client;

    private final String serverUrl;
    private final String resourceUri;

    private Boolean observable;
    private CoapObserveRelation observeRelation;

    protected byte[] lastPayload = "".getBytes();

    private Boolean autoUpdated;
    private long autoUpdateTimerPeriod;
    private Timer autoUpdateTimer;


    private CoapResource(String serverUrl, String resourceUri) {
        this.serverUrl = serverUrl;
        this.resourceUri = resourceUri;
        this.client = new CoapClient(serverUrl);
    }

    public CoapResource(String serverUrl, String resourceUri, long autoUpdatePeriod) {
        this(serverUrl, resourceUri);

        this.observable = false;
        this.autoUpdated = true;

        this.autoUpdateTimerPeriod = autoUpdatePeriod;
    }

    public CoapResource(String serverUrl, String resourceUri, boolean observable) {
        this(serverUrl, resourceUri);

        this.observable = observable;
        this.autoUpdated = !this.observable;

        init();
    }

    private void init() {
        if (observable) {
            Request request = new Request(CoAP.Code.GET);

            request.setOptions(this.createOptionSet());
            request.setURI(String.format("%s%s", this.client.getURI(), this.resourceUri));
            request.setObserve();
            request.setConfirmable(true);

            try {
                observeRelation = client.observe(request, new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        if (coapResponse != null) {
                            setLastPayload(coapResponse.getPayload());
                        } else {
                            setLastPayload("".getBytes());
                        }
                    }

                    @Override
                    public void onError() {
                        setLastPayload("".getBytes());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (autoUpdated && !observable) {
            setAutoUpdate(0L, this.autoUpdateTimerPeriod);
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
        String oldPayloadString = new String(lastPayload);
        String newPayloadString = new String(value);

        if (!oldPayloadString.equals(newPayloadString)) {
            this.lastPayload = value;
            notifyListeners(value);
        }
    }

    public void setAutoUpdate(long delay, long period) {
        if (this.autoUpdateTimer == null) {
            this.autoUpdateTimer = new Timer();
        } else {
            stopAutoUpdate();
        }

        autoUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // sendGET();
            }
        }, delay, period);
    }

    public void stopAutoUpdate() {
        autoUpdateTimer.cancel();
        autoUpdateTimer.purge();
    }

    protected OptionSet createOptionSet() {
        OptionSet options = new OptionSet();

        return options;
    }

    /*
    private void sendGET() {
        Request request = new Request(CoAP.Code.GET);
        request.setOptions(this.createOptionSet());
        request.setConfirmable(true);

        try {
            CoapResponse response = client.advanced(request);

            if (response != null) {
                setLastPayload(response.getPayload());
            } else {
                setLastPayload("".getBytes());
            }
        } catch (ConnectorException | IOException e) {
            setLastPayload("".getBytes());
            e.printStackTrace();
        }
    }
     */
}
