package com.example.Subscription.Tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.imap")
public class ImapProperties {
    private String host;
    private int port = 993;
    private String username;
    private String password;
    private boolean ssl = true;
    private String folder = "INBOX";

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isSsl() { return ssl; }
    public void setSsl(boolean ssl) { this.ssl = ssl; }

    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }
}
