package ro.cofi.openwaterdetector;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import ro.cofi.openwaterdetector.mixin.FishingBobberAccessor;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Random;

public class MainTickHandler implements ClientTickEvents.EndTick {

    private boolean enabled = true;
    private boolean lastPressed = false;
    private final Random random = new Random();

    private Field stateField = null;
    private Object bobbingState = null;
    private boolean reflectionReady = false;

    private void initReflection(FishingBobberEntity bobber) {
        if (reflectionReady) return;
        try {
            Class<?> cls = bobber.getClass();
            while (cls != null && stateField == null) {
                for (Field f : cls.getDeclaredFields()) {
                    if (f.getType().isEnum() && f.getType().getSimpleName().equals("State")) {
                        f.setAccessible(true);
                        stateField = f;
                        for (Object constant : f.getType().getEnumConstants()) {
                            if (constant.toString().equals("BOBBING")) {
                                bobbingState = constant;
                                break;
                            }
                        }
                        break;
                    }
                }
                cls = cls.getSuperclass();
            }
            reflectionReady = true;
        } catch (Exception e) {
            OpenWaterDetectorClient.LOGGER.error("Failed to init reflection", e);
        }
    }

    private boolean isBobbing(FishingBobberEntity bobber) {
        initReflection(bobber);
        if (stateField == null || bobbingState == null) return true;
        try {
            return bobbingState.equals(stateField.get(bobber));
        } catch (Exception e) {
            return true;
        }
    }

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

        ClientWorld world = client.world;

        Optional<FishingBobberEntity> optionalBobber = world
                .getEntitiesByClass(
                        FishingBobberEntity.class,
                        client.player.getBoundingBox().expand(64),
                        bobber -> bobber.getPlayerOwner() == client.player
                )
                .stream()
                .findFirst();

        if (optionalBobber.isEmpty()) return;

        FishingBobberEntity bobber = optionalBobber.get();
        if (!isBobbing(bobber)) return;

        FishingBobberAccessor accessor = (FishingBobberAccessor) bobber;
        boolean inOpenWater = accessor.invokeIsOpenOrWaterAround(bobber.getBlockPos());

        Vec3d pos = bobber.getPos();
        double rx = pos.x + (random.nextDouble() - 0.5) * 0.5;
        double rz = pos.z + (random.nextDouble() - 0.5) * 0.5;
        double y  = pos.y + 0.2;

        // 1.21.x addParticle(ParticleEffect, double, double, double, double, double, double)
        if (inOpenWater) {
            world.addParticle(rx, y, rz, 0.0, 0.05, ParticleTypes.COMPOSTER);
        } else {
            world.addParticle(rx, y, rz, 0.0, 0.05, ParticleTypes.SMALL_FLAME);
        }
    }
}
