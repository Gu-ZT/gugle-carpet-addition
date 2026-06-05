package dev.dubhe.gugle.carpet.mixin;

import org.spongepowered.asm.mixin.Mixin;

//#if MC >= 12002 && MC <= 12004
//$$ import com.mojang.authlib.GameProfile;
//$$ import net.minecraft.world.level.block.entity.SkullBlockEntity;
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$ import java.util.Optional;
//$$ import java.util.concurrent.CompletableFuture;
//#else
import dev.dubhe.gugle.carpet.util.mixin.DummyClass;
//#endif

//#if MC >= 12002 && MC <= 12004
//$$ @Mixin(SkullBlockEntity.class)
//#else
@Mixin(DummyClass.class)
//#endif
public interface SkullBlockEntityAccessor {

    //#if MC >= 12002 && MC <= 12004
    //$$ @Invoker
    //$$ static CompletableFuture<Optional<GameProfile>> invokeFetchGameProfile(String string) {
    //$$     throw new AssertionError();
    //$$ }
    //#endif

}
