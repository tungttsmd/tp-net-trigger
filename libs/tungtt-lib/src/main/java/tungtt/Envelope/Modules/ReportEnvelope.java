package tungtt.Envelope.Modules;

import tungtt.Envelope.Contexts.*;
import com.google.gson.JsonObject;
import tungtt.Envelope.Abstracts.AbstractEnvelope;


public final class ReportEnvelope extends AbstractEnvelope {

    private final ReportModuleContext reportModuleContext;
    private final String reportTitle;
    private final String reportHandle;
    private final String reportPayload;

    public ReportEnvelope(
            EnvelopeMetaContext envelopeMetaContext,
            EnvelopeSecureContext envelopeSecureContext,
            ReportModuleContext reportModuleContext) {

        super(envelopeMetaContext, envelopeSecureContext);

        this.reportModuleContext = reportModuleContext;
        this.reportTitle = reportModuleContext.reportTitle();
        this.reportHandle = reportModuleContext.reportHandle();
        this.reportPayload = reportModuleContext.reportPayload();
    }

    /* DATA */
    @Override
    protected JsonObject buildData() {

        JsonObject data = super.buildData();

        data.addProperty("report_title", reportTitle);
        data.addProperty("report_handle", reportHandle);
        data.addProperty("report_payload", reportPayload);

        return data;
    }

    /* META */
    @Override
    protected JsonObject buildMeta() {

        JsonObject meta = super.buildMeta();

        meta.addProperty("json_response", this.reportModuleContext.jsonResponse());

        return meta;
    }

    /* SECURE */
    @Override
    protected JsonObject buildSecure() {

        return super.buildSecure();
    }

}
