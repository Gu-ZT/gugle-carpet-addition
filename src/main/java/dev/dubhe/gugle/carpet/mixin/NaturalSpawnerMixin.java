package dev.dubhe.gugle.carpet.mixin;

import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerMixin {
    @Inject(method = "spawnCategoryForChunk", at = @At("HEAD"), cancellable = true)
    private static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci) {
        if (GcaSetting.qnmdLC < 0) return;
        ChunkPos chunkPos = levelChunk.getPos();
        double chance = GcaSetting.qnmdLC == 0 ? 1.0 : 1.0 / GcaSetting.qnmdLC;
        int i = chunkPos.getMinBlockX() + serverLevel.getRandom().nextInt(16);
        int j = chunkPos.getMinBlockZ() + serverLevel.getRandom().nextInt(16);
        int k = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, i, j) + 1;
        for (int i1 = serverLevel.getMinBuildHeight(); i1 < k; ++i1) {
            if (serverLevel.getRandom().nextDouble() > chance) continue;
            BlockPos blockPos = new BlockPos(i, i1, j);
            if (blockPos.getY() < serverLevel.getMinBuildHeight() + 1) continue;
            NaturalSpawner.spawnCategoryForPosition(mobCategory, serverLevel, levelChunk, blockPos, spawnPredicate, afterSpawnCallback);
        }
        ci.cancel();
    }
}
