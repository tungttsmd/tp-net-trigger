package tungtt.Envelope.Contexts;

public record EnvelopeMetaContext(
    String hostId,
    String nodeFrom,
    String nodeVersion,
    long timestamp) {}
