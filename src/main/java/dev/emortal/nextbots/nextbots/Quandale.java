package dev.emortal.nextbots.nextbots;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class Quandale extends Nextbot {
    public Quandale() {
        super(
                Component.text("Quandale"), "\uE002",
                Component.text("You were What's up guys, it's Quandale Dingle here! *REHEHEHE* I have been arrested for multiple crimes *UUAHHHHHH*, including, battery on a police officer *WHAT*, grand theft, declaring war on Italy, and public indecency. *REHEHEHE*"), Component.text("was What's up guys, it's Quandale Dingle here! *REHEHEHE* I have been arrested for multiple crimes *UUAHHHHHH*, including, battery on a police officer *WHAT*, grand theft, declaring war on Italy, and public indecency. *REHEHEHE*"),
                SoundEvent.ENTITY_BAT_DEATH.id(), 199,
                20.0
        );

        setPathSkip(2);
        setAllowChangeOfTarget(true);
    }

    @Override
    public @Nullable Player pickTarget(Set<Player> players) { // make quandale target the furthest away player
        if (getEntity() == null) return null;

        double distanceHighScore = Double.MIN_VALUE;
        Player lowestDistancePlayer = null;

        for (Player player : players) {
            if (player.isDead()) continue;
            // TODO: Maybe give 3 seconds of immunity
            if (player.getGameMode() == GameMode.CREATIVE) continue;

            Integer botTarget = player.getTag(BOT_TARGET_TAG);
            if (botTarget != null && botTarget != getEntity().getEntityId()) continue;

            double dist = player.getDistanceSquared(getEntity());

            if (dist > distanceHighScore) {
                distanceHighScore = dist;
                lowestDistancePlayer = player;
            }
        }

        return lowestDistancePlayer;
    }
}
