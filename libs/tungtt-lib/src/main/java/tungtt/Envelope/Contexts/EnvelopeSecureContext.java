package tungtt.Envelope.Contexts;

public record EnvelopeSecureContext(
    String nonce,
    String tokenId) {}