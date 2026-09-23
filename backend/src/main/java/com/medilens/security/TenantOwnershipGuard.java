package com.medilens.security;

import com.medilens.model.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantOwnershipGuard {

    /**
     * Enforces strict tenant ownership isolation.
     * Prevents Insecure Direct Object Reference (IDOR) attacks across patient records.
     *
     * @param resourceOwnerId The UUID of the user who owns the medical resource.
     * @param principal The currently authenticated UserPrincipal.
     */
    public void verifyOwnership(UUID resourceOwnerId, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated access attempt.");
        }

        // Admins have supervisory oversight; patients can strictly only access their own data
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !principal.getId().equals(resourceOwnerId)) {
            throw new AccessDeniedException("Access denied: You are not authorized to view or modify health records of another patient.");
        }
    }
}
