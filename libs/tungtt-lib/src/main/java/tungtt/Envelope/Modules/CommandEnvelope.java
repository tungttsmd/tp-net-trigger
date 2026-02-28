package tungtt.Envelope.Modules;

import tungtt.Envelope.Contexts.*;
import tungtt.Envelope.Abstracts.AbstractEnvelope;
import com.google.gson.JsonObject;

public final class CommandEnvelope extends AbstractEnvelope {

    private final CommandModuleContext commandModuleContext;
    private final String commandTitle;
    private final String command;
    private final String commandContext;

    public CommandEnvelope(
            EnvelopeMetaContext envelopeMetaContext,
            EnvelopeSecureContext envelopeSecureContext,
            CommandModuleContext commandModuleContext) {

        super(envelopeMetaContext, envelopeSecureContext);

        this.commandModuleContext = commandModuleContext;
        this.commandTitle = commandModuleContext.commandTitle();
        this.command = commandModuleContext.command();
        this.commandContext = commandModuleContext.commandContext();
    }

    /* DATA */
    @Override
    protected JsonObject buildData() {

        JsonObject data = super.buildData();

        data.addProperty("command_title", commandTitle);
        data.addProperty("command", command);
        data.addProperty("command_context", commandContext);

        return data;
    }

    /* META */
    @Override
    protected JsonObject buildMeta() {

        JsonObject meta = super.buildMeta();

        meta.addProperty("json_response", this.commandModuleContext.jsonResponse());
        
        return meta;
    }

    /* SECURE */
    @Override
    protected JsonObject buildSecure() {

        return super.buildSecure();
    }
}
