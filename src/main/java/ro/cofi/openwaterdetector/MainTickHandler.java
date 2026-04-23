package ro.cofi.openwaterdetector;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
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

    // 用反射找 state 欄位，只找一次
    private Field stateField = null;
    private Object bobbingState = null;
    private boolean reflectionReady = false;

    private void initReflection(FishingBobberEntity bobber) {
        if (reflectionReady) return;
        try {
            // 找名為 "state" 的欄位（yarn 映射名）
            for (Field f : bobber.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(bobber);
                if (val != null && val.getClass().isEnum()
                        && val.getClass().getSimpleName().equals("State")) {
                    stateField = f;
                    // 找 BOBBING 這個 enum 常數
                    for (Object constant : val.getClass().getEnumConstants()) {
                        if (constant.toString().equals("BOBBING")) {
                            bobbingState = constant;
                            break;
                        }
                    }
                    break;
                }
            }
            // 若找不到 State 欄位，從父類找
            if (stateField == null) {
                Class<?> cls = bobber.getClass().getSuperclass();
                while (cls != null && stateField == null) {
                    for (Field f : cls.getDeclaredFields()) {
                        f.setAccessible(true);
                        try {
                            Object val = f.get(bobber);
                            if (val != null && val.getClass().isEnum()
                                    && val.getClass().getSimpleName().equals("State")) {
                                stateField = f;
                                for (Object constant : val.getClass().getEnumConstants()) {
                                    if (constant.toString().equals("BOBBING")) {
                                        bobbingState = constant;
                                        break;
                                    }
                                }
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                    cls = cls.getSuperclass();
                }
            }
            reflectionReady = true;
        } catch (Exception e) {
            OpenWaterDetectorClient.LOGGER.error("Failed to init reflection", e);
        }
    }

    private boolean isBobbing(FishingBobberEntity bobber) {
        initReflection(bobber);
        if (stateField == null || bobbingState == null) return true; // 找不到就假設 bobbing
        try {
            Object state = stateField.get(bobber);
            return bobbingState.equals(state);
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

        if (!isBobbing(bobber)) return;

        FishingBobberAccessor accessor = (FishingBobberAccessor) bobber;
        boolean inOpenWater = accessor.invokeIsOpenOrWaterAround(bobber.getBlockPos());

        Vec3d bobberPos = bobber.getPos();
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
