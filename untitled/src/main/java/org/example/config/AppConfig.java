package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String name = "视频网站";
    private String version = "1.0.0";
    private Upload upload = new Upload();
    private Security security = new Security();
    
    public static class Upload {
        private String path = "/uploads";
        private long maxFileSize = 100 * 1024 * 1024; // 100MB
        private String[] allowedTypes = {"mp4", "avi", "mov", "wmv"};
        
        // getters and setters
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public long getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
        public String[] getAllowedTypes() { return allowedTypes; }
        public void setAllowedTypes(String[] allowedTypes) { this.allowedTypes = allowedTypes; }
    }
    
    public static class Security {
        private int sessionTimeout = 3600; // 1 hour
        private int maxLoginAttempts = 5;
        private boolean enableCaptcha = false;
        
        // getters and setters
        public int getSessionTimeout() { return sessionTimeout; }
        public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }
        public int getMaxLoginAttempts() { return maxLoginAttempts; }
        public void setMaxLoginAttempts(int maxLoginAttempts) { this.maxLoginAttempts = maxLoginAttempts; }
        public boolean isEnableCaptcha() { return enableCaptcha; }
        public void setEnableCaptcha(boolean enableCaptcha) { this.enableCaptcha = enableCaptcha; }
    }
    
    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Upload getUpload() { return upload; }
    public void setUpload(Upload upload) { this.upload = upload; }
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
}