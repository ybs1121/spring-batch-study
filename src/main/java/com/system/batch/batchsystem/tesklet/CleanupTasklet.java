package com.system.batch.batchsystem.tesklet;


import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CleanupTasklet implements Tasklet {
    private final int killTargetCnt = 10;
    private int killed = 0;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        killed++;
        System.out.println("killed:" + killed + "killTargetCnt:" + killTargetCnt);

        if (killTargetCnt > killTargetCnt) {
            return RepeatStatus.FINISHED; // 처리 완료
        }

        return RepeatStatus.CONTINUABLE; // 아직 처리해야할 수가 남아있다.
    }
}
