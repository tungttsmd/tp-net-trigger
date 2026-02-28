package tungtt.Handler.CommandHandler.PublishHooker;

import tungtt.Envelope.Modules.HeartbeatEnvelope;
import tungtt.Envelope.Modules.ReportEnvelope;
import tungtt.Envelope.Modules.CommandEnvelope;
import tungtt.Envelope.Modules.SignalEnvelope;

public interface CommandPublishHooker {

    Runnable heartbeatPublish(HeartbeatEnvelope envelope, String publishTopic);
    Runnable reportPublish(ReportEnvelope envelope, String publishTopic);
    Runnable commandPublish(CommandEnvelope envelope, String publishTopic);
    Runnable signalPublish(SignalEnvelope envelope, String publishTopic);
}
