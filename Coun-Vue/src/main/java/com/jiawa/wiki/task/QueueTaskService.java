package com.jiawa.wiki.task;


import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *  @author KangJunJie
 */
@Slf4j
@Component
public class QueueTaskService {
    @Resource
    private RedissonClient redissonClient;

//    @Resource
//    private Map<String, ExecuteQueueTaskService> executeQueueTaskMap;

    private final Map<String, ExecuteQueueTaskService> executeQueueTaskMap = new ConcurrentHashMap<>();

    @Autowired
    public QueueTaskService(List<ExecuteQueueTaskService> taskServices) {
        for (ExecuteQueueTaskService service : taskServices) {
            executeQueueTaskMap.put(service.getTaskType(), service);
        }
    }

    /**
     * 分发并执行任务
     *
     * @param queueTask 队列任务
     */
    public void executeTask(QueueTask queueTask) {
        try {
            String type = queueTask.getType();
            ExecuteQueueTaskService service = executeQueueTaskMap.get(type);
            if (service == null) {
                log.error("未找到任务类型 [{}] 的处理器", type);
                return;
            }
            service.execute(queueTask.getTask());
        } catch (Exception e) {
            log.error("任务执行出错", e);
        }
    }

    public void addTask(QueueTask queueTask) {
        log.info("添加任务到队列中...时间:{},参数:{}", queueTask.getStartTime(), queueTask.getTask());
        RBlockingQueue<QueueTask> blockingFairQueue = redissonClient.getBlockingQueue(queueTask.getType());
        RDelayedQueue<QueueTask> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);

        //计算延时时间
        long between = ChronoUnit.SECONDS.between(LocalDateTime.now(), queueTask.getStartTime());
        delayedQueue.offer(queueTask, between, TimeUnit.SECONDS);
    }

    public void removeTask(QueueTask queueTask) {
        RBlockingQueue<QueueTask> blockingFairQueue = redissonClient.getBlockingQueue(queueTask.getTask());
        RDelayedQueue<QueueTask> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
        delayedQueue.remove(queueTask);
    }

    public void addTaskWithDelay(QueueTask queueTask, long delayInSec) {
        log.info("将任务添加到延迟队列，延迟时间：{}秒，任务类型：{}，任务内容：{}", delayInSec, queueTask.getType(), queueTask.getTask());

        RBlockingQueue<QueueTask> blockingFairQueue = redissonClient.getBlockingQueue(queueTask.getType());
        /**
         * RDelayedQueue：是一个延迟队列，它允许你将任务放入队列中，并设置一个延迟时间。
         * 在延迟时间到达之前，任务不会被消费，只有当延迟时间到期后，任务才会被移到 RBlockingQueue 中进行处理。
         */
        RDelayedQueue<QueueTask> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);

        // 将任务放入延迟队列中，延迟时间为传入的 delayInSec 秒
        delayedQueue.offer(queueTask, delayInSec, TimeUnit.SECONDS);
    }
}
