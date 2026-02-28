package tungtt.Envelope.Modules;

import tungtt.Envelope.Contexts.*;
import com.google.gson.JsonObject;
import tungtt.Envelope.Abstracts.AbstractEnvelope;

public final class HeartbeatEnvelope extends AbstractEnvelope {

    private final HeartbeatModuleContext heartbeatModuleContext;
    private final String heartbeatNote;
    private final String heartbeatType;

    public HeartbeatEnvelope(
            EnvelopeMetaContext envelopeMetaContext,
            EnvelopeSecureContext envelopeSecureContext,
            HeartbeatModuleContext heartbeatModuleContext) {

        super(envelopeMetaContext, envelopeSecureContext);

        this.heartbeatModuleContext = heartbeatModuleContext;
        this.heartbeatNote = this.heartbeatModuleContext.heartbeatNote();
        this.heartbeatType = this.heartbeatModuleContext.heartbeatType();
    }

    /* DATA */
    @Override
    protected JsonObject buildData() {

        JsonObject data = super.buildData();

        data.addProperty("heartbeatType", this.heartbeatType);
        data.addProperty("heartbeatNote", this.heartbeatNote);

        return data;
    }

    /* META */
    @Override
    protected JsonObject buildMeta() {

        JsonObject meta = super.buildMeta();

        meta.addProperty("json_response", this.heartbeatModuleContext.jsonResponse());

        return meta;
    }

    /* SECURE */
    @Override
    protected JsonObject buildSecure() {

        return super.buildSecure();
    }
}
