package com.nyajcloud.service;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class SshProvisioningService {

    private final String host = "localhost"; // IP de ton serveur Debian
    private final String user = "root";      // utilisateur SSH avec droits sudo
    private final int port = 2222;

    // Mot de passe SSH du user root
    private final String sshPassword = "123";

    /**
     * Crée un compte Linux sur le serveur Debian
     */
    public void createLinuxUser(String username, String passwordLinux) {
        Session session = null;
        try {
            JSch jsch = new JSch();

            session = jsch.getSession(user, host, port);
            session.setPassword(sshPassword);  // Utilisation du mot de passe
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 1. Créer le user
            execCommand(session,
                    String.format("sudo useradd -m -d /home/%s -s /bin/bash %s", username, username));

            // 2. Définir son mot de passe
            execCommand(session,
                    String.format("echo '%s:%s' | sudo chpasswd", username, passwordLinux));

        } catch (Exception e) {
            throw new RuntimeException("Erreur SSH : impossible de créer l’utilisateur Linux", e);
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void execCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setErrStream(System.err);
        channel.setInputStream(null);

        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, tmp.length);
                if (i < 0) break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                System.out.println("Exit-status: " + channel.getExitStatus());
                break;
            }
            Thread.sleep(100);
        }

        channel.disconnect();
    }
}
