package com.alameda.lightningenergy.config.tron;

import com.alameda.lightningenergy.config.security.RASUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.tron.trident.core.ApiWrapper;

@Configuration
public class TronConfig {
    private final String apiKey;
    private final String defaultPrivate;
    @Autowired
    public TronConfig(Environment environment) {
        this.apiKey = environment.getProperty("tron.api-key");
        this.defaultPrivate = environment.getProperty("tron.default-private-key");
    }

    @Bean
    public ApiWrapper initApiWrapper(){
        if (apiKey.equals("Shasta")) {
            return  ApiWrapper.ofShasta(defaultPrivate);
        }else {
            return  ApiWrapper.ofMainnet(defaultPrivate,apiKey);
        }
    }
}
