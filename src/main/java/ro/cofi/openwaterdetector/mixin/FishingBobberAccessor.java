package ro.cofi.openwaterdetector.mixin;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberAccessor {

    @Shadow
    private FishingBobberEntity.State state;

    public FishingBobberEntity.State getState() {
        return this.state;
    }
}
