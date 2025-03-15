package vn.eledevo.vksbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean(name = "customTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Number of threads to keep in the pool
        executor.setMaxPoolSize(50); // Maximum number of threads in the pool
        executor.setQueueCapacity(100); // Size of the queue to hold tasks
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
