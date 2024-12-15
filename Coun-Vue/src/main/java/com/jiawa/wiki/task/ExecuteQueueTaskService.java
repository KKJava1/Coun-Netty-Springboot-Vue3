package com.jiawa.wiki.task;

/**
 *  @author KangJunJie
 */
@FunctionalInterface
public interface ExecuteQueueTaskService {
    /**
     * 获取任务类型
     *
     * @return 任务类型
     */
    default String getTaskType() {
        return "default-task";
    }
    /**
     * 存放业务
     *
     * @param t 参数
     */
    void execute(String t);

}
