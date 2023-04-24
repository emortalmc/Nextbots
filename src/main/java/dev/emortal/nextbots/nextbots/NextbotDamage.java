package dev.emortal.nextbots.nextbots;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NextbotDamage extends DamageType {

    private Nextbot source;
    private Component deathScreenText;
    private Component deathChatText;

    private final Component nameInBrackets;
    private final Component finalDeathScreenText;
    private final Component deathSuffix;
    public NextbotDamage(@NotNull Nextbot source, Component deathScreenText, Component deathChatText) {
        super("attack.nextbot");

        this.source = source;
        this.deathScreenText = deathScreenText;
        this.deathChatText = deathChatText;

        this.nameInBrackets = Component.text()
                .append(Component.text(" ("))
                .append(source.getName())
                .append(Component.text(")"))
                .build();
        this.finalDeathScreenText = Component.text()
                .append(deathScreenText)
                .append(nameInBrackets)
                .build();
        this.deathSuffix = Component.text()
                .append(Component.space())
                .append(deathChatText)
                .append(nameInBrackets)
                .build();
    }


    private static Component DEATH_PREFIX = Component.text()
            .append(Component.text("☠", NamedTextColor.RED))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
            .build();


    @Override
    public @Nullable Component buildDeathMessage(@NotNull Player killed) {
        return Component.text()
                .append(DEATH_PREFIX)
                .append(Component.text(killed.getUsername(), NamedTextColor.YELLOW))
                .append(deathSuffix)
                .build();
    }

    @Override
    public @Nullable Component buildDeathScreenText(@NotNull Player killed) {
        return finalDeathScreenText;
    }
}
