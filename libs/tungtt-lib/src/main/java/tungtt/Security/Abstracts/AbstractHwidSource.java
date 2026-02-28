package tungtt.Security.Abstracts;

import tungtt.Security.Contexts.HwidProfileContext;

public abstract class AbstractHwidSource {

    protected final HwidProfileContext hwProfileContext;

    protected AbstractHwidSource(HwidProfileContext hwProfileContext) {

        if (hwProfileContext == null) {
            throw new IllegalArgumentException("HwidProfileContext is null");
        }

        this.hwProfileContext = hwProfileContext;
    }

    public final String build() {
        
        return hwidEncode();
    }

    protected abstract String hwidEncode();
}
