package com.nyajcloud.service;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SshProvisioningService {

    private final String host = "localhost"; // IP de ton serveur Debian
    private final String user = "root";   // utilisateur unique pour toutes les commandes
    private final int port = 2222;             // port SSH standard
    private final String sshPassword = "123"; // mot de passe ou clé SSH

    /**
     * Exécute une commande Linux sur le serveur via SSH
     */
    private String execCommand(String command) {
        Session session = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(sshPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            channel.connect();

            StringBuilder output = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, tmp.length);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                Thread.sleep(100);
            }
            channel.disconnect();
            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur SSH lors de l'exécution de la commande : " + command, e);
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * Crée un compte Linux sur le serveur Debian
     */
    public void createLinuxUser(String username, String passwordLinux) {
        try {

            // 1. Créer le user
            execCommand(String.format(
                    "sudo useradd -m -d /home/%s -s /bin/bash %s",
                    username, username
            ));

            // 2. Définir son mot de passe
            execCommand(String.format(
                    "echo '%s:%s' | sudo chpasswd",
                    username, passwordLinux
            ));

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur SSH : impossible de créer l’utilisateur Linux", e
            );
        }
    }

    // -------------------------------
    // Gestion des répertoires clients
    // -------------------------------

    public void createClientDirectory(String userId) {
        String path = "/srv/" + userId;
        execCommand(String.format("mkdir -p %s && chown %s:%s %s", path, user, user, path));
    }

    public void deleteClientDirectory(String userId) {
        String path = "/srv/" + userId;
        execCommand(String.format("rm -rf %s", path));
    }

    // -------------------------------
    // Gestion des fichiers
    // -------------------------------

    public void uploadFile(String localPath, String remotePath) {
        // Ici, on pourrait utiliser SFTP via JSch
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(sshPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            sftp.put(localPath, remotePath);
            sftp.disconnect();
            session.disconnect();
        } catch (Exception e) {
            throw new RuntimeException("Erreur upload fichier", e);
        }
    }

    public void downloadFile(String remotePath, String localPath) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(sshPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            sftp.get(remotePath, localPath);
            sftp.disconnect();
            session.disconnect();
        } catch (Exception e) {
            throw new RuntimeException("Erreur téléchargement fichier", e);
        }
    }

    // -------------------------------
    // Gestion des paquets
    // -------------------------------

    public void installPackage(String packageName) {
        execCommand(String.format("sudo apt-get update && sudo apt-get install -y %s", packageName));
    }

    public void removePackage(String packageName) {
        execCommand(String.format("sudo apt-get remove -y %s", packageName));
    }

    public List<String> listInstalledPackages() {
        String result = execCommand("dpkg -l | awk '{print $2}'");
        String[] packages = result.split("\n");
        List<String> packageList = new ArrayList<>();
        for (String p : packages) {
            if (!p.isBlank()) packageList.add(p.trim());
        }
        return packageList;
    }

    // -------------------------------
    // Monitoring système
    // -------------------------------

    public String getDiskUsage() {
        return execCommand("df -h");
    }

    public String getMemoryUsage() {
        return execCommand("free -h");
    }

    public String getCPUUsage() {
        return execCommand("top -bn1 | grep 'Cpu(s)'");
    }

    public String getSystemUptime() {
        return execCommand("uptime -p");
    }

    // -------------------------------
    // Gestion des services
    // -------------------------------

    public void startService(String serviceName) {
        execCommand(String.format("sudo systemctl start %s", serviceName));
    }

    public void stopService(String serviceName) {
        execCommand(String.format("sudo systemctl stop %s", serviceName));
    }

    public void restartService(String serviceName) {
        execCommand(String.format("sudo systemctl restart %s", serviceName));
    }

    public String getServiceStatus(String serviceName) {
        return execCommand(String.format("systemctl status %s", serviceName));
    }
}
