package tungtt.Handler.CommandHandler.EnvelopPublisher;

import tungtt.Console.JsonConsole;
import tungtt.Envelope.Modules.SignalEnvelope;
import tungtt.Envelope.Contexts.SignalModuleContext;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.PublishHooker.CommandPublishHooker;

import com.google.gson.JsonObject;

public final class SignalPublisher {

        private final CommandPublishHooker hooker;
        private final String publishTopic;

        public SignalPublisher(
                String publishTopic,
                CommandPublishHooker hooker
        ) {
                this.publishTopic = publishTopic;
                this.hooker = hooker;
        }

        public void publish(
                String keyPrefix,
                String commandTitle,
                String command,
                CommandContext ctx,
                JsonObject payload) {

                SignalEnvelope envelope = new SignalEnvelope(
                        ctx.envelopeMetacontext(),
                        ctx.envelopeSecurecontext(),
                        new SignalModuleContext(
                                keyPrefix + ":" + commandTitle + ":" + command,
                                JsonConsole.toJson(payload)
                        )
                );

                this.hooker.signalPublish(envelope, this.publishTopic).run();
        }
}
