package com.example.batch.consumer;

import com.example.batch.service.ExampleBatchScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * version : 1.0
 * author : JUNSEOB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchConsumer {

    private final ExampleBatchScheduler exampleBatchScheduler;

    @KafkaListener(topics = "batch_example_topic_start", groupId = "batch_consumer_group")
    public void batchExampleStart(@Payload String batchId) {
        exampleBatchScheduler.startScheduler(batchId);
    }

    @KafkaListener(topics = "batch_example_topic_stop", groupId = "batch_consumer_group")
    public void batchExampleStop(@Payload String batchId) {
        exampleBatchScheduler.stopScheduler(batchId);
    }


}
