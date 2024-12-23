package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class TestCommand {
    public static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
    public static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final String STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND = "Structure block entity could not be found";
    private static final TestFinder.Builder<TestCommand.Runner> testFinder = new TestFinder.Builder<>(TestCommand.Runner::new);

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
        ArgumentBuilder<CommandSourceStack, ?> pArgumentBuilder,
        Function<CommandContext<CommandSourceStack>, TestCommand.Runner> pRunnerGetter,
        Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> pModifier
    ) {
        return pArgumentBuilder.executes(p_325991_ -> pRunnerGetter.apply(p_325991_).run())
            .then(
                Commands.argument("numberOfTimes", IntegerArgumentType.integer(0))
                    .executes(
                        p_325975_ -> pRunnerGetter.apply(p_325975_).run(new RetryOptions(IntegerArgumentType.getInteger(p_325975_, "numberOfTimes"), false))
                    )
                    .then(
                        pModifier.apply(
                            Commands.argument("untilFailed", BoolArgumentType.bool())
                                .executes(
                                    p_325980_ -> pRunnerGetter.apply(p_325980_)
                                            .run(
                                                new RetryOptions(
                                                    IntegerArgumentType.getInteger(p_325980_, "numberOfTimes"),
                                                    BoolArgumentType.getBool(p_325980_, "untilFailed")
                                                )
                                            )
                                )
                        )
                    )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
        ArgumentBuilder<CommandSourceStack, ?> pArgumentBuilder, Function<CommandContext<CommandSourceStack>, TestCommand.Runner> pRunnerGetter
    ) {
        return runWithRetryOptions(pArgumentBuilder, pRunnerGetter, p_325997_ -> p_325997_);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(
        ArgumentBuilder<CommandSourceStack, ?> pArgumentBuilder, Function<CommandContext<CommandSourceStack>, TestCommand.Runner> pRunnerGetter
    ) {
        return runWithRetryOptions(
            pArgumentBuilder,
            pRunnerGetter,
            p_325993_ -> p_325993_.then(
                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                        .executes(
                            p_326001_ -> pRunnerGetter.apply(p_326001_)
                                    .run(
                                        new RetryOptions(
                                            IntegerArgumentType.getInteger(p_326001_, "numberOfTimes"), BoolArgumentType.getBool(p_326001_, "untilFailed")
                                        ),
                                        IntegerArgumentType.getInteger(p_326001_, "rotationSteps")
                                    )
                        )
                        .then(
                            Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                .executes(
                                    p_325977_ -> pRunnerGetter.apply(p_325977_)
                                            .run(
                                                new RetryOptions(
                                                    IntegerArgumentType.getInteger(p_325977_, "numberOfTimes"),
                                                    BoolArgumentType.getBool(p_325977_, "untilFailed")
                                                ),
                                                IntegerArgumentType.getInteger(p_325977_, "rotationSteps"),
                                                IntegerArgumentType.getInteger(p_325977_, "testsPerRow")
                                            )
                                )
                        )
                )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        ArgumentBuilder<CommandSourceStack, ?> argumentbuilder = runWithRetryOptionsAndBuildInfo(
            Commands.argument("onlyRequiredTests", BoolArgumentType.bool()),
            p_326015_ -> testFinder.failedTests(p_326015_, BoolArgumentType.getBool(p_326015_, "onlyRequiredTests"))
        );
        ArgumentBuilder<CommandSourceStack, ?> argumentbuilder1 = runWithRetryOptionsAndBuildInfo(
            Commands.argument("testClassName", TestClassNameArgument.testClassName()),
            p_325999_ -> testFinder.allTestsInClass(p_325999_, TestClassNameArgument.getTestClassName(p_325999_, "testClassName"))
        );
        pDispatcher.register(
            Commands.literal("test")
                .then(
                    Commands.literal("run")
                        .then(
                            runWithRetryOptionsAndBuildInfo(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()), p_325988_ -> testFinder.byArgument(p_325988_, "testName"))
                        )
                )
                .then(
                    Commands.literal("runmultiple")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(p_325973_ -> testFinder.byArgument(p_325973_, "testName").run())
                                .then(
                                    Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(
                                            p_325995_ -> testFinder.createMultipleCopies(IntegerArgumentType.getInteger(p_325995_, "amount"))
                                                    .byArgument(p_325995_, "testName")
                                                    .run()
                                        )
                                )
                        )
                )
                .then(runWithRetryOptionsAndBuildInfo(Commands.literal("runall").then(argumentbuilder1), testFinder::allTests))
                .then(runWithRetryOptions(Commands.literal("runthese"), testFinder::allNearby))
                .then(runWithRetryOptions(Commands.literal("runclosest"), testFinder::nearest))
                .then(runWithRetryOptions(Commands.literal("runthat"), testFinder::lookedAt))
                .then(runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then(argumentbuilder), testFinder::failedTests))
                .then(
                    Commands.literal("verify")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(p_341098_ -> testFinder.byArgument(p_341098_, "testName").verify())
                        )
                )
                .then(
                    Commands.literal("verifyclass")
                        .then(
                            Commands.argument("testClassName", TestClassNameArgument.testClassName())
                                .executes(p_341097_ -> testFinder.allTestsInClass(p_341097_, TestClassNameArgument.getTestClassName(p_341097_, "testClassName")).verify())
                        )
                )
                .then(
                    Commands.literal("locate")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(
                                    p_325985_ -> testFinder.locateByName(
                                                p_325985_, "minecraft:" + TestFunctionArgument.getTestFunction(p_325985_, "testName").structureName()
                                            )
                                            .locate()
                                )
                        )
                )
                .then(Commands.literal("resetclosest").executes(p_325984_ -> testFinder.nearest(p_325984_).reset()))
                .then(Commands.literal("resetthese").executes(p_325994_ -> testFinder.allNearby(p_325994_).reset()))
                .then(Commands.literal("resetthat").executes(p_325983_ -> testFinder.lookedAt(p_325983_).reset()))
                .then(
                    Commands.literal("export")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_325998_ -> exportTestStructure(p_325998_.getSource(), "minecraft:" + StringArgumentType.getString(p_325998_, "testName")))
                        )
                )
                .then(Commands.literal("exportclosest").executes(p_326009_ -> testFinder.nearest(p_326009_).export()))
                .then(Commands.literal("exportthese").executes(p_326010_ -> testFinder.allNearby(p_326010_).export()))
                .then(Commands.literal("exportthat").executes(p_326011_ -> testFinder.lookedAt(p_326011_).export()))
                .then(Commands.literal("clearthat").executes(p_325987_ -> testFinder.lookedAt(p_325987_).clear()))
                .then(Commands.literal("clearthese").executes(p_325978_ -> testFinder.allNearby(p_325978_).clear()))
                .then(
                    Commands.literal("clearall")
                        .executes(p_325986_ -> testFinder.radius(p_325986_, 200).clear())
                        .then(
                            Commands.argument("radius", IntegerArgumentType.integer())
                                .executes(
                                    p_325996_ -> testFinder.radius(p_325996_, Mth.clamp(IntegerArgumentType.getInteger(p_325996_, "radius"), 0, 1024))
                                            .clear()
                                )
                        )
                )
                .then(
                    Commands.literal("import")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_128025_ -> importTestStructure(p_128025_.getSource(), StringArgumentType.getString(p_128025_, "testName")))
                        )
                )
                .then(Commands.literal("stop").executes(p_326006_ -> stopTests()))
                .then(
                    Commands.literal("pos")
                        .executes(p_128023_ -> showPos(p_128023_.getSource(), "pos"))
                        .then(
                            Commands.argument("var", StringArgumentType.word())
                                .executes(p_128021_ -> showPos(p_128021_.getSource(), StringArgumentType.getString(p_128021_, "var")))
                        )
                )
                .then(
                    Commands.literal("create")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .suggests(TestFunctionArgument::suggestTestFunction)
                                .executes(p_128019_ -> createNewStructure(p_128019_.getSource(), StringArgumentType.getString(p_128019_, "testName"), 5, 5, 5))
                                .then(
                                    Commands.argument("width", IntegerArgumentType.integer())
                                        .executes(
                                            p_128014_ -> createNewStructure(
                                                    p_128014_.getSource(),
                                                    StringArgumentType.getString(p_128014_, "testName"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width")
                                                )
                                        )
                                        .then(
                                            Commands.argument("height", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("depth", IntegerArgumentType.integer())
                                                        .executes(
                                                            p_128007_ -> createNewStructure(
                                                                    p_128007_.getSource(),
                                                                    StringArgumentType.getString(p_128007_, "testName"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "width"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "height"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "depth")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int resetGameTestInfo(GameTestInfo pGameTestInfo) {
        pGameTestInfo.getLevel().getEntities(null, pGameTestInfo.getStructureBounds()).stream().forEach(p_325989_ -> p_325989_.remove(Entity.RemovalReason.DISCARDED));
        pGameTestInfo.getStructureBlockEntity().placeStructure(pGameTestInfo.getLevel());
        StructureUtils.removeBarriers(pGameTestInfo.getStructureBounds(), pGameTestInfo.getLevel());
        say(pGameTestInfo.getLevel(), "Reset succeded for: " + pGameTestInfo.getTestName(), ChatFormatting.GREEN);
        return 1;
    }

    static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack pSource, RetryOptions pRetryOptions, StructureBlockPosFinder pStructureBlockPosFinder) {
        return pStructureBlockPosFinder.findStructureBlockPos().map(p_326014_ -> createGameTestInfo(p_326014_, pSource.getLevel(), pRetryOptions)).flatMap(Optional::stream);
    }

    static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack pSource, RetryOptions pRetryOptions, TestFunctionFinder pTestFunctionFinder, int pRotationSteps) {
        return pTestFunctionFinder.findTestFunctions()
            .filter(p_326008_ -> verifyStructureExists(pSource.getLevel(), p_326008_.structureName()))
            .map(p_326005_ -> new GameTestInfo(p_326005_, StructureUtils.getRotationForRotationSteps(pRotationSteps), pSource.getLevel(), pRetryOptions));
    }

    private static Optional<GameTestInfo> createGameTestInfo(BlockPos pPos, ServerLevel pLevel, RetryOptions pRetryOptions) {
        StructureBlockEntity structureblockentity = (StructureBlockEntity)pLevel.getBlockEntity(pPos);
        if (structureblockentity == null) {
            say(pLevel, "Structure block entity could not be found", ChatFormatting.RED);
            return Optional.empty();
        } else {
            String s = structureblockentity.getMetaData();
            Optional<TestFunction> optional = GameTestRegistry.findTestFunction(s);
            if (optional.isEmpty()) {
                say(pLevel, "Test function for test " + s + " could not be found", ChatFormatting.RED);
                return Optional.empty();
            } else {
                TestFunction testfunction = optional.get();
                GameTestInfo gametestinfo = new GameTestInfo(testfunction, structureblockentity.getRotation(), pLevel, pRetryOptions);
                gametestinfo.setStructureBlockPos(pPos);
                return !verifyStructureExists(pLevel, gametestinfo.getStructureName()) ? Optional.empty() : Optional.of(gametestinfo);
            }
        }
    }

    private static int createNewStructure(CommandSourceStack pSource, String pStructureName, int pX, int pY, int pZ) {
        if (pX <= 48 && pY <= 48 && pZ <= 48) {
            ServerLevel serverlevel = pSource.getLevel();
            BlockPos blockpos = createTestPositionAround(pSource).below();
            StructureUtils.createNewEmptyStructureBlock(pStructureName.toLowerCase(), blockpos, new Vec3i(pX, pY, pZ), Rotation.NONE, serverlevel);
            BlockPos blockpos1 = blockpos.above();
            BlockPos blockpos2 = blockpos1.offset(pX - 1, 0, pZ - 1);
            BlockPos.betweenClosedStream(blockpos1, blockpos2).forEach(p_325982_ -> serverlevel.setBlockAndUpdate(p_325982_, Blocks.BEDROCK.defaultBlockState()));
            StructureUtils.addCommandBlockAndButtonToStartTest(blockpos, new BlockPos(1, 0, -1), Rotation.NONE, serverlevel);
            return 0;
        } else {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
    }

    private static int showPos(CommandSourceStack pSource, String pVariableName) throws CommandSyntaxException {
        BlockHitResult blockhitresult = (BlockHitResult)pSource.getPlayerOrException().pick(10.0, 1.0F, false);
        BlockPos blockpos = blockhitresult.getBlockPos();
        ServerLevel serverlevel = pSource.getLevel();
        Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockpos, 15, serverlevel);
        if (optional.isEmpty()) {
            optional = StructureUtils.findStructureBlockContainingPos(blockpos, 200, serverlevel);
        }

        if (optional.isEmpty()) {
            pSource.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + blockpos));
            return 0;
        } else {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(optional.get());
            if (structureblockentity == null) {
                say(serverlevel, "Structure block entity could not be found", ChatFormatting.RED);
                return 0;
            } else {
                BlockPos blockpos1 = blockpos.subtract(optional.get());
                String s = blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ();
                String s1 = structureblockentity.getMetaData();
                Component component = Component.literal(s)
                    .setStyle(
                        Style.EMPTY
                            .withBold(true)
                            .withColor(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + pVariableName + " = new BlockPos(" + s + ");"))
                    );
                pSource.sendSuccess(() -> Component.literal("Position relative to " + s1 + ": ").append(component), false);
                DebugPackets.sendGameTestAddMarker(serverlevel, new BlockPos(blockpos), s, -2147418368, 10000);
                return 1;
            }
        }
    }

    static int stopTests() {
        GameTestTicker.SINGLETON.clear();
        return 1;
    }

    static int trackAndStartRunner(CommandSourceStack pSource, ServerLevel pLevel, GameTestRunner pRunner) {
        pRunner.addListener(new TestCommand.TestBatchSummaryDisplayer(pSource));
        MultipleTestTracker multipletesttracker = new MultipleTestTracker(pRunner.getTestInfos());
        multipletesttracker.addListener(new TestCommand.TestSummaryDisplayer(pLevel, multipletesttracker));
        multipletesttracker.addFailureListener(p_127992_ -> GameTestRegistry.rememberFailedTest(p_127992_.getTestFunction()));
        pRunner.start();
        return 1;
    }

    static int saveAndExportTestStructure(CommandSourceStack pSource, StructureBlockEntity pStructureBlockEntity) {
        String s = pStructureBlockEntity.getStructureName();
        if (!pStructureBlockEntity.saveStructure(true)) {
            say(pSource, "Failed to save structure " + s);
        }

        return exportTestStructure(pSource, s);
    }

    private static int exportTestStructure(CommandSourceStack pSource, String pStructurePath) {
        Path path = Paths.get(StructureUtils.testStructuresDir);
        ResourceLocation resourcelocation = ResourceLocation.parse(pStructurePath);
        Path path1 = pSource.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(resourcelocation, ".nbt");
        Path path2 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path1, resourcelocation.getPath(), path);
        if (path2 == null) {
            say(pSource, "Failed to export " + path1);
            return 1;
        } else {
            try {
                FileUtil.createDirectoriesSafe(path2.getParent());
            } catch (IOException ioexception) {
                say(pSource, "Could not create folder " + path2.getParent());
                LOGGER.error("Could not create export folder", (Throwable)ioexception);
                return 1;
            }

            say(pSource, "Exported " + pStructurePath + " to " + path2.toAbsolutePath());
            return 0;
        }
    }

    private static boolean verifyStructureExists(ServerLevel pLevel, String pStructure) {
        if (pLevel.getStructureManager().get(ResourceLocation.parse(pStructure)).isEmpty()) {
            say(pLevel, "Test structure " + pStructure + " could not be found", ChatFormatting.RED);
            return false;
        } else {
            return true;
        }
    }

    static BlockPos createTestPositionAround(CommandSourceStack pSource) {
        BlockPos blockpos = BlockPos.containing(pSource.getPosition());
        int i = pSource.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY();
        return new BlockPos(blockpos.getX(), i + 1, blockpos.getZ() + 3);
    }

    static void say(CommandSourceStack pSource, String pMessage) {
        pSource.sendSuccess(() -> Component.literal(pMessage), false);
    }

    private static int importTestStructure(CommandSourceStack pSource, String pStructurePath) {
        Path path = Paths.get(StructureUtils.testStructuresDir, pStructurePath + ".snbt");
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace(pStructurePath);
        Path path1 = pSource.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(resourcelocation, ".nbt");

        try {
            BufferedReader bufferedreader = Files.newBufferedReader(path);
            String s = IOUtils.toString(bufferedreader);
            Files.createDirectories(path1.getParent());

            try (OutputStream outputstream = Files.newOutputStream(path1)) {
                NbtIo.writeCompressed(NbtUtils.snbtToStructure(s), outputstream);
            }

            pSource.getLevel().getStructureManager().remove(resourcelocation);
            say(pSource, "Imported to " + path1.toAbsolutePath());
            return 0;
        } catch (CommandSyntaxException | IOException ioexception) {
            LOGGER.error("Failed to load structure {}", pStructurePath, ioexception);
            return 1;
        }
    }

    static void say(ServerLevel pServerLevel, String pMessage, ChatFormatting pFormatting) {
        pServerLevel.getPlayers(p_127945_ -> true).forEach(p_308546_ -> p_308546_.sendSystemMessage(Component.literal(pMessage).withStyle(pFormatting)));
    }

    public static class Runner {
        private final TestFinder<TestCommand.Runner> finder;

        public Runner(TestFinder<TestCommand.Runner> pFinder) {
            this.finder = pFinder;
        }

        public int reset() {
            TestCommand.stopTests();
            return TestCommand.toGameTestInfos(this.finder.source(), RetryOptions.noRetries(), this.finder).map(TestCommand::resetGameTestInfo).toList().isEmpty()
                ? 0
                : 1;
        }

        private <T> void logAndRun(Stream<T> pStructureBlockPos, ToIntFunction<T> pTestCounter, Runnable pOnFail, Consumer<Integer> pOnSuccess) {
            int i = pStructureBlockPos.mapToInt(pTestCounter).sum();
            if (i == 0) {
                pOnFail.run();
            } else {
                pOnSuccess.accept(i);
            }
        }

        public int clear() {
            TestCommand.stopTests();
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            GameTestRunner.clearMarkers(serverlevel);
            this.logAndRun(
                this.finder.findStructureBlockPos(),
                p_330831_ -> {
                    StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(p_330831_);
                    if (structureblockentity == null) {
                        return 0;
                    } else {
                        BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(structureblockentity);
                        StructureUtils.clearSpaceForStructure(boundingbox, serverlevel);
                        return 1;
                    }
                },
                () -> TestCommand.say(serverlevel, "Could not find any structures to clear", ChatFormatting.RED),
                p_330244_ -> TestCommand.say(commandsourcestack, "Cleared " + p_330244_ + " structures")
            );
            return 1;
        }

        public int export() {
            MutableBoolean mutableboolean = new MutableBoolean(true);
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            this.logAndRun(
                this.finder.findStructureBlockPos(),
                p_331429_ -> {
                    StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(p_331429_);
                    if (structureblockentity == null) {
                        TestCommand.say(serverlevel, "Structure block entity could not be found", ChatFormatting.RED);
                        mutableboolean.setFalse();
                        return 0;
                    } else {
                        if (TestCommand.saveAndExportTestStructure(commandsourcestack, structureblockentity) != 0) {
                            mutableboolean.setFalse();
                        }

                        return 1;
                    }
                },
                () -> TestCommand.say(serverlevel, "Could not find any structures to export", ChatFormatting.RED),
                p_333553_ -> TestCommand.say(commandsourcestack, "Exported " + p_333553_ + " structures")
            );
            return mutableboolean.getValue() ? 0 : 1;
        }

        int verify() {
            TestCommand.stopTests();
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            BlockPos blockpos = TestCommand.createTestPositionAround(commandsourcestack);
            Collection<GameTestInfo> collection = Stream.concat(
                    TestCommand.toGameTestInfos(commandsourcestack, RetryOptions.noRetries(), this.finder),
                    TestCommand.toGameTestInfo(commandsourcestack, RetryOptions.noRetries(), this.finder, 0)
                )
                .toList();
            int i = 10;
            GameTestRunner.clearMarkers(serverlevel);
            GameTestRegistry.forgetFailedTests();
            Collection<GameTestBatch> collection1 = new ArrayList<>();

            for (GameTestInfo gametestinfo : collection) {
                for (Rotation rotation : Rotation.values()) {
                    Collection<GameTestInfo> collection2 = new ArrayList<>();

                    for (int j = 0; j < 100; j++) {
                        GameTestInfo gametestinfo1 = new GameTestInfo(gametestinfo.getTestFunction(), rotation, serverlevel, new RetryOptions(1, true));
                        collection2.add(gametestinfo1);
                    }

                    GameTestBatch gametestbatch = GameTestBatchFactory.toGameTestBatch(collection2, gametestinfo.getTestFunction().batchName(), (long)rotation.ordinal());
                    collection1.add(gametestbatch);
                }
            }

            StructureGridSpawner structuregridspawner = new StructureGridSpawner(blockpos, 10, true);
            GameTestRunner gametestrunner = GameTestRunner.Builder.fromBatches(collection1, serverlevel)
                .batcher(GameTestBatchFactory.fromGameTestInfo(100))
                .newStructureSpawner(structuregridspawner)
                .existingStructureSpawner(structuregridspawner)
                .haltOnError(true)
                .build();
            return TestCommand.trackAndStartRunner(commandsourcestack, serverlevel, gametestrunner);
        }

        public int run(RetryOptions pRetryOptions, int pRotationSteps, int pTestsPerRow) {
            TestCommand.stopTests();
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            BlockPos blockpos = TestCommand.createTestPositionAround(commandsourcestack);
            Collection<GameTestInfo> collection = Stream.concat(
                    TestCommand.toGameTestInfos(commandsourcestack, pRetryOptions, this.finder),
                    TestCommand.toGameTestInfo(commandsourcestack, pRetryOptions, this.finder, pRotationSteps)
                )
                .toList();
            if (collection.isEmpty()) {
                TestCommand.say(commandsourcestack, "No tests found");
                return 0;
            } else {
                GameTestRunner.clearMarkers(serverlevel);
                GameTestRegistry.forgetFailedTests();
                TestCommand.say(commandsourcestack, "Running " + collection.size() + " tests...");
                GameTestRunner gametestrunner = GameTestRunner.Builder.fromInfo(collection, serverlevel)
                    .newStructureSpawner(new StructureGridSpawner(blockpos, pTestsPerRow, false))
                    .build();
                return TestCommand.trackAndStartRunner(commandsourcestack, serverlevel, gametestrunner);
            }
        }

        public int run(int pRotationSteps, int pTestsPerRow) {
            return this.run(RetryOptions.noRetries(), pRotationSteps, pTestsPerRow);
        }

        public int run(int pRotationSteps) {
            return this.run(RetryOptions.noRetries(), pRotationSteps, 8);
        }

        public int run(RetryOptions pRetryOptions, int pRotationSteps) {
            return this.run(pRetryOptions, pRotationSteps, 8);
        }

        public int run(RetryOptions pRetryOptions) {
            return this.run(pRetryOptions, 0, 8);
        }

        public int run() {
            return this.run(RetryOptions.noRetries());
        }

        public int locate() {
            TestCommand.say(this.finder.source(), "Started locating test structures, this might take a while..");
            MutableInt mutableint = new MutableInt(0);
            BlockPos blockpos = BlockPos.containing(this.finder.source().getPosition());
            this.finder
                .findStructureBlockPos()
                .forEach(
                    p_327721_ -> {
                        StructureBlockEntity structureblockentity = (StructureBlockEntity)this.finder.source().getLevel().getBlockEntity(p_327721_);
                        if (structureblockentity != null) {
                            Direction direction = structureblockentity.getRotation().rotate(Direction.NORTH);
                            BlockPos blockpos1 = structureblockentity.getBlockPos().relative(direction, 2);
                            int j = (int)direction.getOpposite().toYRot();
                            String s = String.format("/tp @s %d %d %d %d 0", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), j);
                            int k = blockpos.getX() - p_327721_.getX();
                            int l = blockpos.getZ() - p_327721_.getZ();
                            int i1 = Mth.floor(Mth.sqrt((float)(k * k + l * l)));
                            Component component = ComponentUtils.wrapInSquareBrackets(
                                    Component.translatable("chat.coordinates", p_327721_.getX(), p_327721_.getY(), p_327721_.getZ())
                                )
                                .withStyle(
                                    p_332540_ -> p_332540_.withColor(ChatFormatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                                );
                            Component component1 = Component.literal("Found structure at: ").append(component).append(" (distance: " + i1 + ")");
                            this.finder.source().sendSuccess(() -> component1, false);
                            mutableint.increment();
                        }
                    }
                );
            int i = mutableint.intValue();
            if (i == 0) {
                TestCommand.say(this.finder.source().getLevel(), "No such test structure found", ChatFormatting.RED);
                return 0;
            } else {
                TestCommand.say(this.finder.source().getLevel(), "Finished locating, found " + i + " structure(s)", ChatFormatting.GREEN);
                return 1;
            }
        }
    }

    static record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener {
        @Override
        public void testBatchStarting(GameTestBatch p_327831_) {
            TestCommand.say(this.source, "Starting batch: " + p_327831_.name());
        }

        @Override
        public void testBatchFinished(GameTestBatch p_335734_) {
        }
    }

    public static record TestSummaryDisplayer(ServerLevel level, MultipleTestTracker tracker) implements GameTestListener {
        @Override
        public void testStructureLoaded(GameTestInfo pTestInfo) {
        }

        @Override
        public void testPassed(GameTestInfo pTest, GameTestRunner pRunner) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testFailed(GameTestInfo pTest, GameTestRunner pRunner) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testAddedForRerun(GameTestInfo pOldTest, GameTestInfo pNewTest, GameTestRunner pRunner) {
            this.tracker.addTestToTrack(pNewTest);
        }

        private static void showTestSummaryIfAllDone(ServerLevel pLevel, MultipleTestTracker pTracker) {
            if (pTracker.isDone()) {
                TestCommand.say(pLevel, "GameTest done! " + pTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
                if (pTracker.hasFailedRequired()) {
                    TestCommand.say(pLevel, pTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
                } else {
                    TestCommand.say(pLevel, "All required tests passed :)", ChatFormatting.GREEN);
                }

                if (pTracker.hasFailedOptional()) {
                    TestCommand.say(pLevel, pTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
                }
            }
        }
    }
}