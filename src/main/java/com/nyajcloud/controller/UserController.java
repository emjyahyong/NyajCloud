package com.nyajcloud.controller;

import com.nyajcloud.model.User;
import com.nyajcloud.service.SshProvisioningService;
import com.nyajcloud.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
public class UserController {

    private final UserService userService;
    private final SshProvisioningService sshService;

    public UserController(UserService userService, SshProvisioningService sshService) {
        this.userService = userService;
        this.sshService = sshService;
    }

    // Crée un utilisateur dans la base et son répertoire dédié sur le serveur
    @PostMapping
    public ResponseEntity<User> register(@RequestBody User user) {
        User created = userService.createUser(user);

        // Créer le répertoire Linux pour l'utilisateur
        sshService.createClientDirectory(created.getId().toString());

        return ResponseEntity.ok(created);
    }

    // Supprime le répertoire Linux de l'utilisateur
    @DeleteMapping("/{userId}/directory")
    public ResponseEntity<String> deleteUserDirectory(@PathVariable Long userId) {
        sshService.deleteClientDirectory(userId.toString());
        return ResponseEntity.ok("Répertoire supprimé pour l'utilisateur " + userId);
    }

    // Lister les fichiers ou paquets de l'utilisateur
    @GetMapping("/{userId}/packages")
    public ResponseEntity<?> listUserPackages(@PathVariable Long userId) {
        // Dans ce cas, c'est global puisque tous passent par le même compte Linux
        return ResponseEntity.ok(sshService.listInstalledPackages());
    }

    // Installer un paquet pour l'utilisateur
    @PostMapping("/{userId}/packages/install")
    public ResponseEntity<String> installPackage(@PathVariable Long userId,
                                                 @RequestParam String packageName) {
        sshService.installPackage(packageName);
        return ResponseEntity.ok("Paquet installé : " + packageName);
    }

    // Supprimer un paquet pour l'utilisateur
    @PostMapping("/{userId}/packages/remove")
    public ResponseEntity<String> removePackage(@PathVariable Long userId,
                                                @RequestParam String packageName) {
        sshService.removePackage(packageName);
        return ResponseEntity.ok("Paquet supprimé : " + packageName);
    }

    // Monitoring système
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

    // Gestion des services
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
