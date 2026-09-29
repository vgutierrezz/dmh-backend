package com.dmh.auth.config;

import com.dmh.auth.model.RolAuth;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RolesInitializer implements CommandLineRunner {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        createRoleIfNotExists(ROLE_USER);
        createRoleIfNotExists(ROLE_ADMIN);
    }

    private void createRoleIfNotExists(String roleName) {

        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM RolAuth r WHERE r.name = :name",
                        Long.class
                )
                .setParameter("name", roleName)
                .getSingleResult();

        if (count == 0) {
            RolAuth role = new RolAuth();
            role.setName(roleName);
            entityManager.persist(role);
        }
    }
}