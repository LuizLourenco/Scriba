package br.dev.lourenco.scriba.core.config;

import java.util.Optional;
import java.util.UUID;

import br.dev.lourenco.scriba.core.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    AuditorAware<UUID> auditorAware() {
        return () -> Optional.ofNullable(SecurityUtils.currentUserId());
    }
}
