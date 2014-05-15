package com.google.android.avalon.rules;

/**
 * Created by mikewallstedt on 5/15/14.
 */
public class IllegalConfigurationException extends IllegalStateException {
    public IllegalConfigurationException() {
    }

    public IllegalConfigurationException(String detailMessage) {
        super(detailMessage);
    }

    public IllegalConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalConfigurationException(Throwable cause) {
        super(cause);
    }
}
