package tungtt.Handler.CommandHandler.EnvelopPublisher;

import tungtt.Console.JsonConsole;
import tungtt.Envelope.Modules.ReportEnvelope;
import tungtt.Envelope.Contexts.ReportModuleContext;

import tungtt.Handler.CommandHandler.Dispatchers.CommandContext;
import tungtt.Handler.CommandHandler.PublishHooker.CommandPublishHooker;

import com.google.gson.JsonObject;

public final class ReportPublisher {

        private final CommandPublishHooker hooker;
        private final String publishTopic;

        public ReportPublisher(
                String publishTopic,
                CommandPublishHooker hooker
        ) {
                this.publishTopic = publishTopic;
                this.hooker = hooker;
        }

        public void publish(
                String commandTitle,
                String command,
                CommandContext ctx,
                JsonObject payload
        ) {

                ReportEnvelope envelope = new ReportEnvelope(
                        ctx.envelopeMetacontext(),
                        ctx.envelopeSecurecontext(),
                        new ReportModuleContext(
                                commandTitle,
                                command,
                                JsonConsole.toJson(payload)
                        )
                );

                this.hooker.reportPublish(envelope, this.publishTopic).run();
        }
}
