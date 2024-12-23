package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
    Logger LOGGER = LogUtils.getLogger();

    static TaskChainer immediate(final Executor pExecutor) {
        return new TaskChainer() {
            @Override
            public <T> void append(CompletableFuture<T> p_310200_, Consumer<T> p_310807_) {
                p_310200_.thenAcceptAsync(p_310807_, pExecutor).exceptionally(p_311935_ -> {
                    LOGGER.error("Task failed", p_311935_);
                    return null;
                });
            }
        };
    }

    default void append(Runnable pTask) {
        this.append(CompletableFuture.completedFuture(null), p_308979_ -> pTask.run());
    }

    <T> void append(CompletableFuture<T> pFuture, Consumer<T> pConsumer);
}