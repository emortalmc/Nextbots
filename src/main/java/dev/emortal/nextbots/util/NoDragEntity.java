package dev.emortal.nextbots.util;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntitySoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

public class NoDragEntity extends Entity {

    private final int soundId;
    public NoDragEntity(EntityType type, int soundId) {
        super(type);

        this.soundId = soundId;
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        player.stopSound(SoundStop.named(SoundEvent.fromId(soundId)));
        ServerPacket packet = new EntitySoundEffectPacket(soundId, Sound.Source.MASTER, getEntityId(), 1f, 1f, 1);
        player.sendPacket(packet);
    }

    @Override
    protected void updateVelocity(boolean wasOnGround, boolean flying, Pos positionBeforeMove, Vec newVelocity) {
        double gravity = flying ? 0 : gravityAcceleration;

        this.velocity = newVelocity
                // Apply gravity and drag
                .apply((x, y, z) -> new Vec(
                        x,
                        !hasNoGravity() ? (y - gravity) : y,
                        z
                ))
                // Convert from block/tick to block/sec
                .mul(MinecraftServer.TICK_PER_SECOND)
                // Prevent infinitely decreasing velocity
                .apply(Vec.Operator.EPSILON);
    }
}
