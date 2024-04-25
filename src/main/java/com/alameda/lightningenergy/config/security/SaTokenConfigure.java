package com.alameda.lightningenergy.config.security;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Sa-Token 权限认证] 全局配置类
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfigure {

    private final ObjectMapper objectMapper;

    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 指定 [拦截路由]
                .addInclude("/**")
                // 指定 [放行路由]
                .addExclude("/favicon.ico")
                // 指定[认证函数]: 每次请求执行
                .setAuth(obj -> {
                    SaRouter.match("/a/v1/pri/**", r -> StpUtil.checkLogin());

                })
                .setBeforeAuth(obj -> {
                    // ---------- 设置跨域响应头 ----------
                    SaHolder.getResponse()
                            .setHeader("Cache-Control", "no-cache")

                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "*")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                            .setHeader("Access-Control-Allow-Credentials","true")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*");

                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS)
                            .back();
                })

                // 指定[异常处理函数]：每次[认证函数]发生异常时执行此函数
                .setError(e -> {
                    // 设置响应头
                    SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
                    try {
                        return objectMapper.writeValueAsString(SaTokenGlobalExceptionHandler.handlerSaTokenException(e));
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                ;
    }
}
