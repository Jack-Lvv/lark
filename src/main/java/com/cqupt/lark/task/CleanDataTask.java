package com.cqupt.lark.task;

import com.cqupt.lark.vector.repository.VectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CleanDataTask {

    private final VectorRepository vectorRepository;
    @Scheduled(cron = "0 */5 * * * *") // 每5分钟执行一次
    public void cleanupData() {
        vectorRepository.cleanUselessVectors();
    }
}
