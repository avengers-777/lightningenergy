package com.alameda.lightningenergy.config.error;


import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component()
@Order(-2) // 确保这个处理器在默认处理器之前运行
@RequiredArgsConstructor
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper mapper;
    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange,@NonNull Throwable ex) {
        return handleApplicationException(ex, exchange);
    }
    public <T> Mono<Void> handleApplicationException(Throwable error, ServerWebExchange exchange) {
        ResModel<T> responseModel;
        if (error instanceof ErrorType.ApplicationException) {
            responseModel = ResModel.error((ErrorType.ApplicationException) error);
            exchange.getResponse().setStatusCode(HttpStatus.OK); // 或其他适当的状态码
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            try {
                byte[] bytes = mapper.writeValueAsBytes(responseModel);
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }
        } else {
            return Mono.error(error);
        }



    }
}
