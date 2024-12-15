package com.jiawa.wiki.task;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *  @author KangJunJie
 */
@Slf4j
@Configuration
public class QueueTaskConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private QueueTaskService queueTaskService;

    @Autowired
    private List<ExecuteQueueTaskService> taskServices;

    private final Executor executor = Executors.newSingleThreadExecutor();  // 单线程执行任务

    @PostConstruct
    public void queueTask() {
        executor.execute(() -> {
            try {
                // 获取所有任务类型
                List<String> taskTypes = taskServices.stream()
                        .map(ExecuteQueueTaskService::getTaskType)
                        .collect(Collectors.toList());

                // 初始化所有队列
                Map<String, RBlockingQueue<QueueTask>> blockingQueues = taskTypes.stream()
                        .collect(Collectors.toMap(
                                taskType -> taskType,
                                taskType -> redissonClient.getBlockingQueue(taskType)
                        ));

                // 单线程轮询所有队列
                while (true) {
                    for (Map.Entry<String, RBlockingQueue<QueueTask>> entry : blockingQueues.entrySet()) {
                        String queueName = entry.getKey();
                        RBlockingQueue<QueueTask> queue = entry.getValue();

                        QueueTask task = queue.poll(1, TimeUnit.SECONDS);  // 尝试获取任务
                        if (task != null) {
                            queueTaskService.executeTask(task);  // 分发并执行任务
                        }
                    }
                }
            } catch (Exception e) {
                log.error("队列监听线程出错", e);
            }
        });
    }
}

