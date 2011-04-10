package org.openstreetmap.josm.plugins.areacut;

public class AreaCutRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 857926026580277816L;

    public AreaCutRuntimeException() {
        super();
    }
    public AreaCutRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    public AreaCutRuntimeException(String message) {
        super(message);
    }
    public AreaCutRuntimeException(Throwable cause) {
        super(cause);
    }

}
