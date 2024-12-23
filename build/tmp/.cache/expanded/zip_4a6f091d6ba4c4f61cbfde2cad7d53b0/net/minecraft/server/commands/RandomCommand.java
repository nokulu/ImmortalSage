package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.RandomSequences;

public class RandomCommand {
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_LARGE = new SimpleCommandExceptionType(Component.translatable("commands.random.error.range_too_large"));
    private static final SimpleCommandExceptionType ERROR_RANGE_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("commands.random.error.range_too_small"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(
            Commands.literal("random")
                .then(drawRandomValueTree("value", false))
                .then(drawRandomValueTree("roll", true))
                .then(
                    Commands.literal("reset")
                        .requires(p_301232_ -> p_301232_.hasPermission(2))
                        .then(
                            Commands.literal("*")
                                .executes(p_300657_ -> resetAllSequences(p_300657_.getSource()))
                                .then(
                                    Commands.argument("seed", IntegerArgumentType.integer())
                                        .executes(p_300850_ -> resetAllSequencesAndSetNewDefaults(p_300850_.getSource(), IntegerArgumentType.getInteger(p_300850_, "seed"), true, true))
                                        .then(
                                            Commands.argument("includeWorldSeed", BoolArgumentType.bool())
                                                .executes(
                                                    p_299490_ -> resetAllSequencesAndSetNewDefaults(
                                                            p_299490_.getSource(),
                                                            IntegerArgumentType.getInteger(p_299490_, "seed"),
                                                            BoolArgumentType.getBool(p_299490_, "includeWorldSeed"),
                                                            true
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("includeSequenceId", BoolArgumentType.bool())
                                                        .executes(
                                                            p_299589_ -> resetAllSequencesAndSetNewDefaults(
                                                                    p_299589_.getSource(),
                                                                    IntegerArgumentType.getInteger(p_299589_, "seed"),
                                                                    BoolArgumentType.getBool(p_299589_, "includeWorldSeed"),
                                                                    BoolArgumentType.getBool(p_299589_, "includeSequenceId")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.argument("sequence", ResourceLocationArgument.id())
                                .suggests(RandomCommand::suggestRandomSequence)
                                .executes(p_301142_ -> resetSequence(p_301142_.getSource(), ResourceLocationArgument.getId(p_301142_, "sequence")))
                                .then(
                                    Commands.argument("seed", IntegerArgumentType.integer())
                                        .executes(
                                            p_299433_ -> resetSequence(
                                                    p_299433_.getSource(),
                                                    ResourceLocationArgument.getId(p_299433_, "sequence"),
                                                    IntegerArgumentType.getInteger(p_299433_, "seed"),
                                                    true,
                                                    true
                                                )
                                        )
                                        .then(
                                            Commands.argument("includeWorldSeed", BoolArgumentType.bool())
                                                .executes(
                                                    p_297409_ -> resetSequence(
                                                            p_297409_.getSource(),
                                                            ResourceLocationArgument.getId(p_297409_, "sequence"),
                                                            IntegerArgumentType.getInteger(p_297409_, "seed"),
                                                            BoolArgumentType.getBool(p_297409_, "includeWorldSeed"),
                                                            true
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("includeSequenceId", BoolArgumentType.bool())
                                                        .executes(
                                                            p_299326_ -> resetSequence(
                                                                    p_299326_.getSource(),
                                                                    ResourceLocationArgument.getId(p_299326_, "sequence"),
                                                                    IntegerArgumentType.getInteger(p_299326_, "seed"),
                                                                    BoolArgumentType.getBool(p_299326_, "includeWorldSeed"),
                                                                    BoolArgumentType.getBool(p_299326_, "includeSequenceId")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> drawRandomValueTree(String pSubcommand, boolean pDisplayResult) {
        return Commands.literal(pSubcommand)
            .then(
                Commands.argument("range", RangeArgument.intRange())
                    .executes(p_297453_ -> randomSample(p_297453_.getSource(), RangeArgument.Ints.getRange(p_297453_, "range"), null, pDisplayResult))
                    .then(
                        Commands.argument("sequence", ResourceLocationArgument.id())
                            .suggests(RandomCommand::suggestRandomSequence)
                            .requires(p_299079_ -> p_299079_.hasPermission(2))
                            .executes(
                                p_297834_ -> randomSample(
                                        p_297834_.getSource(),
                                        RangeArgument.Ints.getRange(p_297834_, "range"),
                                        ResourceLocationArgument.getId(p_297834_, "sequence"),
                                        pDisplayResult
                                    )
                            )
                    )
            );
    }

    private static CompletableFuture<Suggestions> suggestRandomSequence(CommandContext<CommandSourceStack> p_297521_, SuggestionsBuilder p_299165_) {
        List<String> list = Lists.newArrayList();
        p_297521_.getSource().getLevel().getRandomSequences().forAllSequences((p_299978_, p_298386_) -> list.add(p_299978_.toString()));
        return SharedSuggestionProvider.suggest(list, p_299165_);
    }

    private static int randomSample(CommandSourceStack pSource, MinMaxBounds.Ints pRange, @Nullable ResourceLocation pSequence, boolean pDisplayResult) throws CommandSyntaxException {
        RandomSource randomsource;
        if (pSequence != null) {
            randomsource = pSource.getLevel().getRandomSequence(pSequence);
        } else {
            randomsource = pSource.getLevel().getRandom();
        }

        int i = pRange.min().orElse(Integer.MIN_VALUE);
        int j = pRange.max().orElse(Integer.MAX_VALUE);
        long k = (long)j - (long)i;
        if (k == 0L) {
            throw ERROR_RANGE_TOO_SMALL.create();
        } else if (k >= 2147483647L) {
            throw ERROR_RANGE_TOO_LARGE.create();
        } else {
            int l = Mth.randomBetweenInclusive(randomsource, i, j);
            if (pDisplayResult) {
                pSource.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("commands.random.roll", pSource.getDisplayName(), l, i, j), false);
            } else {
                pSource.sendSuccess(() -> Component.translatable("commands.random.sample.success", l), false);
            }

            return l;
        }
    }

    private static int resetSequence(CommandSourceStack pSource, ResourceLocation pSequence) throws CommandSyntaxException {
        pSource.getLevel().getRandomSequences().reset(pSequence);
        pSource.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg(pSequence)), false);
        return 1;
    }

    private static int resetSequence(CommandSourceStack pSource, ResourceLocation pSequence, int pSeed, boolean pIncludeWorldSeed, boolean pIncludeSequenceId) throws CommandSyntaxException {
        pSource.getLevel().getRandomSequences().reset(pSequence, pSeed, pIncludeWorldSeed, pIncludeSequenceId);
        pSource.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg(pSequence)), false);
        return 1;
    }

    private static int resetAllSequences(CommandSourceStack pSource) {
        int i = pSource.getLevel().getRandomSequences().clear();
        pSource.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", i), false);
        return i;
    }

    private static int resetAllSequencesAndSetNewDefaults(CommandSourceStack pSource, int pSeed, boolean pIncludeWorldSeed, boolean pIncludeSequenceId) {
        RandomSequences randomsequences = pSource.getLevel().getRandomSequences();
        randomsequences.setSeedDefaults(pSeed, pIncludeWorldSeed, pIncludeSequenceId);
        int i = randomsequences.clear();
        pSource.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", i), false);
        return i;
    }
}