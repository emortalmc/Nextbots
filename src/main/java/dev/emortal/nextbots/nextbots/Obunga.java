package dev.emortal.nextbots.nextbots;

import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;

public class Obunga extends Nextbot {
    public Obunga() {
        super(
                Component.text("Obunga"), "\uE001",
                Component.text("You were beatboxed to death"), Component.text("was beatboxed to death"),
                SoundEvent.ENTITY_BAT_AMBIENT.id(), 191,
                13.0
        );
    }
}
