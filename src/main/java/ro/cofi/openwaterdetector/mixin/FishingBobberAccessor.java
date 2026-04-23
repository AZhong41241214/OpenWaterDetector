package ro.cofi.openwaterdetector.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberAccessor {

    @Invoker("isOpenOrWaterAround")
    boolean invokeIsOpenOrWaterAround(net.minecraft.util.math.BlockPos pos);
}
