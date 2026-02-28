package tungtt.Handler.CommandHandler.Dispatchers;

import tungtt.Envelope.Contexts.*;
import com.google.gson.JsonObject;

public record CommandContext(
        String hostId,
        EnvelopeMetaContext envelopeMetacontext,
        EnvelopeSecureContext envelopeSecurecontext,
        JsonObject root,
        JsonObject data
) {}