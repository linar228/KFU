package com.example.demo.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createPermissions();
        createRoles();
        createAdminUser();
    }

    private void createPermissions() {
        List<Permission> permissions = List.of(
                new Permission("user", "read"),
                new Permission("user", "write"),
                new Permission("sensor", "read"),
                new Permission("sensor", "write"),
                new Permission("sensor", "delete"),
                new Permission("incident", "read"),
                new Permission("incident", "write"),
                new Permission("incident", "assign"),
                new Permission("incident", "status"),
                new Permission("incident", "photo")
        );

        for (Permission permission : permissions) {
            boolean exists = permissionRepository
                    .findByResourceAndOperation(permission.getResource(), permission.getOperation())
                    .isPresent();
            if (!exists) {
                permissionRepository.save(permission);
            }
        }
    }

    private void createRoles() {
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        Role admin = getOrCreateRole("ADMIN");
        admin.setPermissions(allPermissions);
        roleRepository.save(admin);

        Role operator = getOrCreateRole("OPERATOR");
        operator.setPermissions(new HashSet<>(List.of(
                getPermission("user", "read"),
                getPermission("sensor", "read"),
                getPermission("sensor", "write"),
                getPermission("incident", "read"),
                getPermission("incident", "write"),
                getPermission("incident", "assign"),
                getPermission("incident", "status"),
                getPermission("incident", "photo")
        )));
        roleRepository.save(operator);

        Role viewer = getOrCreateRole("VIEWER");
        viewer.setPermissions(new HashSet<>(List.of(
                getPermission("sensor", "read"),
                getPermission("incident", "read")
        )));
        roleRepository.save(viewer);
    }

    private void createAdminUser() {
        User admin = userRepository.findByUsername("admin").orElse(new User());
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setEnabled(true);
        admin.setRole(roleRepository.findByName("ADMIN").orElseThrow());
        userRepository.save(admin);
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name).orElse(new Role(name));
    }

    private Permission getPermission(String resource, String operation) {
        return permissionRepository.findByResourceAndOperation(resource, operation).orElseThrow();
    }
}
