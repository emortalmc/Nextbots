package dev.emortal.nextbots.nextbots;

import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;

public class AutismCreature extends Nextbot {
    public AutismCreature() {
        super(
                Component.text("Autism Creature"), "\uE003",
                Component.empty(), Component.empty(),
                SoundEvent.ENTITY_BAT_HURT.id(), 1943,
                25.0
        );

        setSplitsInPath(3);
        setKillDistance(0);
        setOccupyTarget(false);
    }
}
