package com.alameda.lightningenergy.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;

public class IpUtils {

    public static String getRealClientIpAddress(ServerHttpRequest request) {
        String xForwardedForHeader = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedForHeader != null) {
            // 从 X-Forwarded-For 头部中提取第一个真实的用户 IP 地址
            return xForwardedForHeader.split(",")[0].trim();
        } else {
            // 如果 X-Forwarded-For 头部不存在，则返回请求的远程地址作为 IP 地址
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
    }
}