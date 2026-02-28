package tungtt.Envelope.Modules;

import tungtt.Envelope.Contexts.*;
import com.google.gson.JsonObject;
import tungtt.Envelope.Abstracts.AbstractEnvelope;

public final class SignalEnvelope extends AbstractEnvelope {

    private final SignalModuleContext signalModuleContext;
    private final String signalKey;
    private final String signalContext;

    public SignalEnvelope(
            EnvelopeMetaContext envelopeMetaContext,
            EnvelopeSecureContext envelopeSecureContext,
            SignalModuleContext signalModuleContext) {

        super(envelopeMetaContext, envelopeSecureContext);

        this.signalModuleContext = signalModuleContext;
        this.signalKey = signalModuleContext.signalKey();
        this.signalContext = signalModuleContext.signalContext();
    }

    /* DATA */
    @Override
    protected JsonObject buildData() {

        JsonObject data = super.buildData();

        data.addProperty("signal_key", signalKey);
        data.addProperty("signal_context", signalContext);

        return data;
    }

    /* META */
    @Override
    protected JsonObject buildMeta() {

        JsonObject meta = super.buildMeta();

        meta.addProperty("json_response", this.signalModuleContext.jsonResponse());

        return meta;
    }

    /* SECURE */
    @Override
    protected JsonObject buildSecure() {

        return super.buildSecure();
    }
}
