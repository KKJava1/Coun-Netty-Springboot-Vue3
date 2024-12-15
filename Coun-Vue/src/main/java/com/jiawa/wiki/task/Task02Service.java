package com.jiawa.wiki.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Task02Service implements ExecuteQueueTaskService {
    @Override
    public String getTaskType() {
        return "redisson-task02";
    }

    @Override
    public void execute(String params) {
        log.info("执行 Task02, 参数: {}", params);
    }
}
