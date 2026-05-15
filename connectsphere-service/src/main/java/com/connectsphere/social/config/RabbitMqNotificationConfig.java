package com.connectsphere.social.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqNotificationConfig {

    @Bean
    DirectExchange notificationExchange(
            @Value("${connectsphere.rabbitmq.notification-exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Queue notificationQueue(
            @Value("${connectsphere.rabbitmq.notification-queue}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding notificationBinding(
            Queue notificationQueue,
            DirectExchange notificationExchange,
            @Value("${connectsphere.rabbitmq.notification-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
