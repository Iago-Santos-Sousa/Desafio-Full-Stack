package com.desafio.ingestion.shared;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class MessagingConfig {
  public static final String INGESTION_QUEUE = "ingestion.jobs";
  public static final String INGESTION_ROUTING_KEY = "ingestion.jobs";
  public static final String DEAD_LETTER_EXCHANGE = "ingestion.dlx";
  public static final String DEAD_LETTER_QUEUE = "ingestion.jobs.dlq";
  public static final String DEAD_LETTER_ROUTING_KEY = "ingestion.jobs";

  @Bean
  JacksonJsonMessageConverter rabbitMessageConverter() {
    return new JacksonJsonMessageConverter("com.desafio.ingestion.ingestion.messaging");
  }

  @Bean
  DirectExchange ingestionExchange() {
    return new DirectExchange("ingestion.exchange", true, false);
  }

  @Bean
  Queue ingestionQueue() {
    return QueueBuilder.durable(INGESTION_QUEUE)
        .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
        .build();
  }

  @Bean
  Queue ingestionDeadLetterQueue() {
    return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
  }

  @Bean
  Binding ingestionBinding(Queue ingestionQueue, DirectExchange ingestionExchange) {
    return BindingBuilder.bind(ingestionQueue).to(ingestionExchange).with(INGESTION_ROUTING_KEY);
  }

  @Bean
  DirectExchange deadLetterExchange() {
    return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
  }

  @Bean
  Binding ingestionDeadLetterBinding(
      Queue ingestionDeadLetterQueue, DirectExchange deadLetterExchange) {
    return BindingBuilder.bind(ingestionDeadLetterQueue)
        .to(deadLetterExchange)
        .with(DEAD_LETTER_ROUTING_KEY);
  }

  @Bean
  RetryOperationsInterceptor rabbitRetryInterceptor() {
    RetryTemplate retry = new RetryTemplate();
    retry.setRetryPolicy(new SimpleRetryPolicy(3));
    ExponentialBackOffPolicy backoff = new ExponentialBackOffPolicy();
    backoff.setInitialInterval(1000);
    backoff.setMultiplier(2.0);
    backoff.setMaxInterval(10000);
    retry.setBackOffPolicy(backoff);
    return RetryInterceptorBuilder.stateless().retryOperations(retry).build();
  }

  @Bean
  SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter messageConverter,
      RetryOperationsInterceptor retryInterceptor) {
    var factory = new SimpleRabbitListenerContainerFactory();

    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    factory.setAdviceChain(retryInterceptor);
    factory.setPrefetchCount(1);
    factory.setDefaultRequeueRejected(false);
    return factory;
  }
}
