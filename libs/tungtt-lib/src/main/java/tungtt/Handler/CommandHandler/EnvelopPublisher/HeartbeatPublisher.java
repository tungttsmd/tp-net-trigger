package tungtt.Handler.CommandHandler.EnvelopPublisher;

import tungtt.Console.JsonConsole;

import tungtt.Envelope.Modules.HeartbeatEnvelope;
import tungtt.Envelope.Contexts.HeartbeatModuleContext;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.PublishHooker.CommandPublishHooker;

import com.google.gson.JsonObject;

public final class HeartbeatPublisher {

        private final String publishTopic;
        private final CommandPublishHooker hooker;

        public HeartbeatPublisher(
                String publishTopic,
                CommandPublishHooker hooker
        ) {
                this.publishTopic = publishTopic;
                this.hooker = hooker;
        }

        public void publish(
                CommandContext ctx,
                String heartbeatType,
                JsonObject heartbeatNote
        ) {

                HeartbeatEnvelope envelope = new HeartbeatEnvelope(
                        ctx.envelopeMetacontext(),
                        ctx.envelopeSecurecontext(),
                        new HeartbeatModuleContext(
                                heartbeatType,
                                JsonConsole.toJson(heartbeatNote)
                        )
                );

                this.hooker.heartbeatPublish(envelope, this.publishTopic).run();
        }
}
