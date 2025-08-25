package com.nyajcloud.controller;

import com.nyajcloud.model.User;
import com.nyajcloud.repository.UserRepository;
import com.nyajcloud.service.SshProvisioningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UserController {

    private final UserRepository userRepository;
    private final SshProvisioningService sshService;

    public UserController(UserRepository userRepository, SshProvisioningService sshService) {
        this.userRepository = userRepository;
        this.sshService = sshService;
    }

    // -------------------------------
    // Gestion des utilisateurs (DB uniquement)
    // -------------------------------

    @PostMapping
    public ResponseEntity<User> register(@RequestBody User user) {
        User created = userRepository.save(user);
        // Créer un répertoire Linux dédié pour l'utilisateur
        sshService.createClientDirectory(created.getId().toString());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userRepository.deleteById(userId);
        sshService.deleteClientDirectory(userId.toString());
        return ResponseEntity.ok("Utilisateur et répertoire supprimés : " + userId);
    }

    // -------------------------------
    // Gestion des paquets (global Linux)
    // -------------------------------

    @GetMapping("/packages")
    public ResponseEntity<?> listInstalledPackages() {
        return ResponseEntity.ok(sshService.listInstalledPackages());
    }

    @PostMapping("/packages/install")
    public ResponseEntity<String> installPackage(@RequestParam String packageName) {
        sshService.installPackage(packageName);
        return ResponseEntity.ok("Paquet installé : " + packageName);
    }

    @PostMapping("/packages/remove")
    public ResponseEntity<String> removePackage(@RequestParam String packageName) {
        sshService.removePackage(packageName);
        return ResponseEntity.ok("Paquet supprimé : " + packageName);
    }

    // -------------------------------
    // Monitoring système
    // -------------------------------

    @GetMapping("/system/disk")
    public ResponseEntity<String> getDiskUsage() {
        return ResponseEntity.ok(sshService.getDiskUsage());
    }

    @GetMapping("/system/memory")
    public ResponseEntity<String> getMemoryUsage() {
        return ResponseEntity.ok(sshService.getMemoryUsage());
    }

    @GetMapping("/system/cpu")
    public ResponseEntity<String> getCPUUsage() {
        return ResponseEntity.ok(sshService.getCPUUsage());
    }

    @GetMapping("/system/uptime")
    public ResponseEntity<String> getSystemUptime() {
        return ResponseEntity.ok(sshService.getSystemUptime());
    }

    // -------------------------------
    // Gestion des services Linux
    // -------------------------------

    @PostMapping("/service/{serviceName}/start")
    public ResponseEntity<String> startService(@PathVariable String serviceName) {
        sshService.startService(serviceName);
        return ResponseEntity.ok("Service démarré : " + serviceName);
    }

    @PostMapping("/service/{serviceName}/stop")
    public ResponseEntity<String> stopService(@PathVariable String serviceName) {
        sshService.stopService(serviceName);
        return ResponseEntity.ok("Service arrêté : " + serviceName);
    }

    @PostMapping("/service/{serviceName}/restart")
    public ResponseEntity<String> restartService(@PathVariable String serviceName) {
        sshService.restartService(serviceName);
        return ResponseEntity.ok("Service redémarré : " + serviceName);
    }

    @GetMapping("/service/{serviceName}/status")
    public ResponseEntity<String> getServiceStatus(@PathVariable String serviceName) {
        return ResponseEntity.ok(sshService.getServiceStatus(serviceName));
    }
}
