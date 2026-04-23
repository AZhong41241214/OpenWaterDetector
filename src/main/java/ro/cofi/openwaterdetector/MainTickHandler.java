package ro.cofi.openwaterdetector;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import ro.cofi.openwaterdetector.mixin.FishingBobberAccessor;

import java.util.Optional;
import java.util.Random;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = true;
    private boolean lastPressed = false;
    private final Random random = new Random();

    @Override
    public void onEndTick(MinecraftClient client) {
        try {
            tick(client);
        } catch (Exception e) {
            OpenWaterDetectorClient.LOGGER.error("Unexpected exception in tick", e);
        }
    }

    private void tick(MinecraftClient client) {
        boolean currentlyPressed = OpenWaterDetectorClient.toggleKey.isPressed();
        if (currentlyPressed && !lastPressed) {
            enabled = !enabled;
        }
        lastPressed = currentlyPressed;

        if (!enabled) return;
        if (client == null || client.player == null || client.world == null) return;

        Optional<FishingBobberEntity> optionalBobber = client.world
                .getEntitiesByClass(
                        FishingBobberEntity.class,
                        client.player.getBoundingBox().expand(64),
                        bobber -> bobber.getPlayerOwner() == client.player
                )
                .stream()
                .findFirst();

        if (optionalBobber.isEmpty()) return;

        FishingBobberEntity bobber = optionalBobber.get();
        FishingBobberAccessor accessor = (FishingBobberAccessor) bobber;
        FishingBobberEntity.State state = accessor.getState();

        if (state != FishingBobberEntity.State.BOBBING) return;

        Vec3d bobberPos = bobber.getPos();
        boolean inOpenWater = bobber.isOpenOrWaterAround(bobber.getBlockPos());

        double randomX = bobberPos.x + (random.nextDouble() - 0.5) * 0.5;
        double randomZ = bobberPos.z + (random.nextDouble() - 0.5) * 0.5;
        double y = bobberPos.y + 0.2;

        if (inOpenWater) {
            client.world.addParticle(ParticleTypes.COMPOSTER, randomX, y, randomZ, 0, 0.05, 0);
        } else {
            client.world.addParticle(ParticleTypes.SMALL_FLAME, randomX, y, randomZ, 0, 0.05, 0);
        }
    }
}
