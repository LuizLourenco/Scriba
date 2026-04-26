package br.dev.lourenco.scriba.core.tenant;

import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.TenantIsolationViolationException;
import br.dev.lourenco.scriba.core.security.SecurityUtils;
import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    public Optional<UUID> instituicaoId() {
        UserDetailsImpl userDetails = SecurityUtils.currentUserDetails();
        return userDetails == null ? Optional.empty() : Optional.ofNullable(userDetails.getInstituicaoId());
    }

    public UUID requireInstituicaoId() {
        return instituicaoId()
            .orElseThrow(() -> new TenantIsolationViolationException("Não foi possível determinar o tenant da sessão atual."));
    }
}
