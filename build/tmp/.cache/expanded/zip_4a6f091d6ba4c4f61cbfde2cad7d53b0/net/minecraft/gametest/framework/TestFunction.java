package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public record TestFunction(
    String batchName,
    String testName,
    String structureName,
    Rotation rotation,
    int maxTicks,
    long setupTicks,
    boolean required,
    boolean manualOnly,
    int maxAttempts,
    int requiredSuccesses,
    boolean skyAccess,
    Consumer<GameTestHelper> function
) {
    public TestFunction(
        String pBatchName, String pTestName, String pStructureName, int pMaxTicks, long pSetupTicks, boolean pRequired, Consumer<GameTestHelper> pFunction
    ) {
        this(pBatchName, pTestName, pStructureName, Rotation.NONE, pMaxTicks, pSetupTicks, pRequired, false, 1, 1, false, pFunction);
    }

    public TestFunction(
        String pBatchName,
        String pTestName,
        String pStructureName,
        Rotation pRotation,
        int pMaxTicks,
        long pSetupTicks,
        boolean pRequired,
        Consumer<GameTestHelper> pFunction
    ) {
        this(pBatchName, pTestName, pStructureName, pRotation, pMaxTicks, pSetupTicks, pRequired, false, 1, 1, false, pFunction);
    }

    public void run(GameTestHelper pGameTestHelper) {
        this.function.accept(pGameTestHelper);
    }

    @Override
    public String toString() {
        return this.testName;
    }

    public boolean isFlaky() {
        return this.maxAttempts > 1;
    }
}