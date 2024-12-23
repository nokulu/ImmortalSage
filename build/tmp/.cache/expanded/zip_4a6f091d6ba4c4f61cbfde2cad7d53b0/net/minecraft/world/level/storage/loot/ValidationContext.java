package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

/**
 * Context for validating loot tables. Loot tables are validated recursively by checking that all functions, conditions,
 * etc. (implementing {@link LootContextUser}) are valid according to their LootTable's {@link LootContextParamSet}.
 */
public class ValidationContext {
    private final ProblemReporter reporter;
    private final LootContextParamSet params;
    private final Optional<HolderGetter.Provider> resolver;
    private final Set<ResourceKey<?>> visitedElements;

    public ValidationContext(ProblemReporter pReporter, LootContextParamSet pParams, HolderGetter.Provider pResolver) {
        this(pReporter, pParams, Optional.of(pResolver), Set.of());
    }

    public ValidationContext(ProblemReporter pReporter, LootContextParamSet pParams) {
        this(pReporter, pParams, Optional.empty(), Set.of());
    }

    private ValidationContext(
        ProblemReporter pReporter, LootContextParamSet pParams, Optional<HolderGetter.Provider> pResolver, Set<ResourceKey<?>> pVisitedElements
    ) {
        this.reporter = pReporter;
        this.params = pParams;
        this.resolver = pResolver;
        this.visitedElements = pVisitedElements;
    }

    public ValidationContext forChild(String pChildName) {
        return new ValidationContext(this.reporter.forChild(pChildName), this.params, this.resolver, this.visitedElements);
    }

    public ValidationContext enterElement(String pName, ResourceKey<?> pKey) {
        Set<ResourceKey<?>> set = ImmutableSet.<ResourceKey<?>>builder().addAll(this.visitedElements).add(pKey).build();
        return new ValidationContext(this.reporter.forChild(pName), this.params, this.resolver, set);
    }

    public boolean hasVisitedElement(ResourceKey<?> pKey) {
        return this.visitedElements.contains(pKey);
    }

    public void reportProblem(String pProblem) {
        this.reporter.report(pProblem);
    }

    public void validateUser(LootContextUser pLootContextUser) {
        this.params.validateUser(this, pLootContextUser);
    }

    public HolderGetter.Provider resolver() {
        return this.resolver.orElseThrow(() -> new UnsupportedOperationException("References not allowed"));
    }

    public boolean allowsReferences() {
        return this.resolver.isPresent();
    }

    public ValidationContext setParams(LootContextParamSet pParams) {
        return new ValidationContext(this.reporter, pParams, this.resolver, this.visitedElements);
    }

    public ProblemReporter reporter() {
        return this.reporter;
    }
}