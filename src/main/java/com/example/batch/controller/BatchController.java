package com.example.batch.controller;

import com.example.batch.service.ExampleBatchScheduler;
import de.huxhorn.sulky.ulid.ULID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * version : 1.0
 * author : JUNSEOB
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/example/batch")
public class BatchController {

    private final ExampleBatchScheduler exampleBatchScheduler;

    @GetMapping("/start")
    public void batchExampleStart() {
        ULID ulid = new ULID();
        String batchId = ulid.nextValue().toString();
        exampleBatchScheduler.startScheduler(batchId);
    }

    @GetMapping("/stop/{batchId}")
    public void batchExampleStop(@PathVariable("batchId") String batchId) {
        exampleBatchScheduler.stopScheduler(batchId);
    }
}
