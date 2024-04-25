package com.alameda.lightningenergy.config.network;


import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class NetworkConfig {



    @Bean
    @Primary
    public WebClient createWebClientH2(){
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.H2)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(60000));
        return  WebClient.builder()
                .filter(logRequest())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Connection", "keep-alive")
                .build();
    }
    @Bean(name = "webClientH1")
    public WebClient createWebClient(){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(60000));
        return  WebClient.builder()
//                .filter(logRequest())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println(name + ":" + value)));
            return Mono.just(clientRequest);
        });
    }


}
