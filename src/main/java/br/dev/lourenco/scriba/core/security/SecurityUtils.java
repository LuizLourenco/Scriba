package br.dev.lourenco.scriba.core.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserDetailsImpl currentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return null;
        }
        return userDetails;
    }

    public static UUID currentUserId() {
        UserDetailsImpl userDetails = currentUserDetails();
        return userDetails != null ? userDetails.getId() : null;
    }
}
