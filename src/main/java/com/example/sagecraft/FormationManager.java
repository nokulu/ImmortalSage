/*
package main.java.com.example.sagecraft;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent; // Ensure this is the correct import
import net.minecraftforge.event.entity.player.PlayerEvent; // Check if this is needed
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class FormationManager {
    private boolean isFormationActive = false;
    private Map<Player, BlockPos> activeFormations = new HashMap<>();
    private static final String FORMATION_NAME = "Weak Qi Gathering Formation";

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        Block block = blockState.getBlock();

        // Check if the block clicked is an orange banner
        if (block == Blocks.ORANGE_BANNER) {
            BlockState belowBlockState = event.getLevel().getBlockState(event.getPos().below());
            Block belowBlock = belowBlockState.getBlock();

            // Check if the block below the banner is an emerald block
            if (belowBlock == Blocks.EMERALD_BLOCK) {
                if (!isFormationActive) {
                    isFormationActive = true;
                    activeFormations.put(player, event.getPos().below());
                    player.sendSystemMessage(FORMATION_NAME + " has been activated!");
                } else {
                    // Deactivate the formation
                    isFormationActive = false;
                    activeFormations.remove(player);
                    player.sendSystemMessage(FORMATION_NAME + " has been deactivated!");
                }
            }
        }
    }

    private boolean isWithinRange(Player player, BlockPos pos) {
        for (BlockPos activePos : activeFormations.values()) {
            if (activePos.distSqr(pos) <= 100) { // 10 blocks range
                return true;
            }
        }
        return false;
    }

    public int getQiBonus(int baseQi) {
        int bonus = isFormationActive ? baseQi + 10 : baseQi;
        // Check for overlapping formations and apply triple effect
        for (BlockPos activePos : activeFormations.values()) {
            if (activePos.distSqr(pos) <= 100) { // 10 blocks range
                bonus += baseQi * 2; // Triple effect
            }
        }
        return bonus;
    }
    
    // Add logic to handle formation shutdown when the banner or emerald block is broken
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockPos pos = event.getPos();
        if (activeFormations.containsValue(pos)) {
            Player player = event.getPlayer();
            isFormationActive = false;
            activeFormations.remove(player);
            player.sendSystemMessage(FORMATION_NAME + " has been deactivated due to block break!");
        }
    }
}
*/ 
