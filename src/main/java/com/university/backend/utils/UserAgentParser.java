package com.university.backend.utils;

import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public String extractDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    public String extractBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();

        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("opr/") || ua.contains("opera")) return "Opera";
        if (ua.contains("chrome") && !ua.contains("chromium")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Safari";
        if (ua.contains("msie") || ua.contains("trident")) return "Internet Explorer";
        if (ua.contains("postmanruntime")) return "Postman";
        return "Unknown";
    }

    public String extractOperatingSystem(String userAgent) {
        if (userAgent == null) return "Unknown";
        String ua = userAgent.toLowerCase();

        if (ua.contains("windows nt 10")) return "Windows 10";
        if (ua.contains("windows nt 11")) return "Windows 11";
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os x")) return "macOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("linux")) return "Linux";
        return "Unknown";
    }
}