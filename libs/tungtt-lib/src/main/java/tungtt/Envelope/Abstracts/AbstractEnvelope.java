package tungtt.Envelope.Abstracts;

import com.google.gson.JsonObject;
import tungtt.Envelope.Contexts.EnvelopeMetaContext;
import tungtt.Envelope.Contexts.EnvelopeSecureContext;

public abstract class AbstractEnvelope {

    protected EnvelopeMetaContext envelopeMetaContext;
    protected EnvelopeSecureContext envelopeSecureContext;

    protected AbstractEnvelope(
        EnvelopeMetaContext envelopeMetaContext,
        EnvelopeSecureContext envelopeSecureContext) {

        this.envelopeMetaContext = envelopeMetaContext;
        this.envelopeSecureContext = envelopeSecureContext;
    }

    public final JsonObject build() {

        JsonObject root = new JsonObject();

        root.add("meta", this.buildMeta());
        root.add("data", this.buildData());
        root.add("secure", this.buildSecure());

        return root;
    }

    protected JsonObject buildMeta() {

        JsonObject meta = new JsonObject();

        meta.addProperty("host_id", this.envelopeMetaContext.hostId());
        meta.addProperty("node_from", this.envelopeMetaContext.nodeFrom());
        meta.addProperty("node_version", this.envelopeMetaContext.nodeVersion());
        meta.addProperty("timestamp", this.envelopeMetaContext.timestamp());

        return meta;
    };

    protected JsonObject buildData() {
        
        JsonObject data = new JsonObject();

        return data;
    };

    protected JsonObject buildSecure() {

        JsonObject secure = new JsonObject();

        secure.addProperty("nonce", this.envelopeSecureContext.nonce());
        secure.addProperty("token_id", this.envelopeSecureContext.tokenId());

        return secure;
    };
}
