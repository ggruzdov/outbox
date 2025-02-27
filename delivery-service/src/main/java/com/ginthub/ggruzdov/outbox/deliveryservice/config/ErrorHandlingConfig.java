package com.ginthub.ggruzdov.outbox.deliveryservice.config;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MessageRetryProperties.class)
public class ErrorHandlingConfig implements KafkaListenerConfigurer {

    private final LocalValidatorFactoryBean validator;
    private final MessageRetryProperties retryProperties;

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        var backOff = new ExponentialBackOff(retryProperties.initInterval(), retryProperties.multiplier());
        backOff.setMaxInterval(retryProperties.maxInterval());
        var errorHandler = new DefaultErrorHandler(consumerRecordRecoverer(), backOff);
        errorHandler.setSeekAfterError(false);
        errorHandler.setResetStateOnRecoveryFailure(false);
        errorHandler.addNotRetryableExceptions(JsonParseException.class, DataIntegrityViolationException.class);

        return errorHandler;
    }

    private ConsumerRecordRecoverer consumerRecordRecoverer() {
        return (consumerRecord, exception) -> {
            switch (exception.getCause()) {
                case MethodArgumentNotValidException ife -> logValidationErrors(ife);
                default -> log.error("Exception occurred", exception.getCause());
            }
        };
    }

    // Human-readable logging about validation failures
    private void logValidationErrors(MethodArgumentNotValidException ife) {
        List<FieldError> fieldErrors = Objects.requireNonNull(ife.getBindingResult()).getFieldErrors();
        StringBuilder errorMsgBuilder = new StringBuilder("Validation exception while deserializing Json, ");
        if (!fieldErrors.isEmpty()) {
            for (FieldError error : fieldErrors) {
                errorMsgBuilder.append(error.getField()).append(" ");
                errorMsgBuilder.append(error.getDefaultMessage()).append("; ");
            }
        } else {
            errorMsgBuilder.append("null message body received");
        }
        log.error(errorMsgBuilder.toString());
    }
}
