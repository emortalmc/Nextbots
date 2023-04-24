package dev.emortal.nextbots.nextbots;

import net.kyori.adventure.text.Component;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.function.Supplier;

public class Maxwell extends Nextbot {

    private static Character[] CHARACTERS = new Character[] {
            '\uE004',
            '\uE005',
            '\uE006',
            '\uE007',
            '\uE008',
            '\uE009',
            '\uE00A',
            '\uE00B',
            '\uE00C',
            '\uE00D',
            '\uE00E',
            '\uE00F',
            '\uE010',
            '\uE011',
            '\uE012',
            '\uE013',
            '\uE014',
            '\uE015',
            '\uE016',
            '\uE017',
            '\uE018',
            '\uE019',
            '\uE01A',
            '\uE01B'
    };

    public Maxwell() {
        super(
                Component.text("Maxwell"), "\uE004",
                Component.text("You didn't give Maxwell enough attention"), Component.text("didn't give enough attention to Maxwell"),
                SoundEvent.BLOCK_AMETHYST_BLOCK_STEP.id(), Integer.MAX_VALUE,
                2.0
        );

        setSplitsInPath(1);
        setKillDistance(0.51);
        setPathSkip(1);
        setOccupyTarget(false);
    }

    @Override
    public void onSpawn() {
        getEntity().scheduler().submitTask(new Supplier<>() {
            int frame = 0;

            @Override
            public TaskSchedule get() {
                frame++;
                if (frame >= CHARACTERS.length) {
                    frame = 0;
                }

                setUnicodeDisplay(CHARACTERS[frame].toString());

                return TaskSchedule.nextTick();
            }
        });
    }
}
