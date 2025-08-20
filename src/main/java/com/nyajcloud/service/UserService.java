package com.nyajcloud.service;

import com.nyajcloud.model.User;
import com.nyajcloud.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SshProvisioningService sshProvisioningService;

    public UserService(UserRepository userRepository,
                       SshProvisioningService sshProvisioningService) {
        this.userRepository = userRepository;
        this.sshProvisioningService = sshProvisioningService;
    }

    public User createUser(User user) {
        // 1. Sauvegarde en base
        User saved = userRepository.save(user);

        // 2. Provisionne le compte Linux via SSH
        sshProvisioningService.createLinuxUser(saved.getNom(), saved.getPassword());

        return saved;
    }
}
