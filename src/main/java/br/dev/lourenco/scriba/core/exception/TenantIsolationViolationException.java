package br.dev.lourenco.scriba.core.exception;

public class TenantIsolationViolationException extends RuntimeException {

    public TenantIsolationViolationException(String message) {
        super(message);
    }
}
