package com.connectsphere.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "connectsphere.admin.bootstrap")
public class AdminBootstrapProperties {
    private boolean enabled = true;
    private String email = "admin@connectsphere.local";
    private String username = "admin";
    private String password = "Admin@12345";
    private String fullName = "ConnectSphere Admin";
    private boolean syncPasswordOnStartup = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isSyncPasswordOnStartup() {
        return syncPasswordOnStartup;
    }

    public void setSyncPasswordOnStartup(boolean syncPasswordOnStartup) {
        this.syncPasswordOnStartup = syncPasswordOnStartup;
    }
}
