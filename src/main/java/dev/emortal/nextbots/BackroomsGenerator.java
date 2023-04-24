package dev.emortal.nextbots;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import java.util.concurrent.ThreadLocalRandom;

public class BackroomsGenerator {

    public void generateRoom(GenerationUnit unit, int y) {
        var modifier = unit.modifier();
        var start = unit.absoluteStart();
        var yPos = start.blockY() + y;

        for (int x = 0; x < unit.size().blockX(); x++) {
            for (int z = 0; z < unit.size().blockZ(); z++) {
                var xPos = start.blockX() + x;
                var zPos = start.blockZ() + z;

                modifier.setBlock(xPos, yPos - 1, zPos, Block.BEDROCK);
                modifier.setBlock(xPos, yPos + 5, zPos, Block.BEDROCK);

                modifier.setBlock(xPos, yPos, zPos, Block.RED_SANDSTONE);
                modifier.setBlock(xPos, yPos + 4, zPos, Block.RED_SANDSTONE);

                // idk man... dont ask about this
                if (
                        (x - 2) % 4 == 0 && (
                                ((z - 2) % 8 == 0) ||
                                        ((z - 2) % 8 == 1) ||
                                        ((z - 2) % 8 == 3) ||
                                        ((z - 2) % 8 == 4)
                        )
                ) {
                    modifier.setBlock(xPos, yPos + 4, zPos, Block.SEA_LANTERN);
                }
            }
        }

        generateWalls(modifier, start.blockX() + 0, yPos + 1, start.blockZ() + 0);
        generateWalls(modifier, start.blockX() + 8, yPos + 1, start.blockZ() + 0);
        generateWalls(modifier, start.blockX() + 0, yPos + 1, start.blockZ() + 8);
        generateWalls(modifier, start.blockX() + 8, yPos + 1, start.blockZ() + 8);
    }

    private void generateWalls(UnitModifier modifier, int xPos, int yPos, int zPos) {
        generatePillar(modifier, xPos, yPos, zPos, Block.RED_SANDSTONE);

        var rand = ThreadLocalRandom.current();
        var wall1 = rand.nextInt(4) != 0;
        var wall2 = rand.nextInt(4) != 0;
        var door1 = rand.nextInt(2) != 0;
        var door2 = rand.nextInt(2) != 0;

        var door1pos = rand.nextInt(2, 6);
        var door2pos = rand.nextInt(2, 6);

        if (wall1) {
            for (int x = xPos; x < xPos + 8; x++) {
                generatePillar(modifier, x, yPos, zPos, Block.RED_SANDSTONE);
            }
        }

        if (wall2) {
            for (int z = zPos; z < zPos + 8; z++) {
                generatePillar(modifier, xPos, yPos, z, Block.RED_SANDSTONE);
            }
        }

        if (door1 && wall1) {
            generatePillar(modifier, xPos + door1pos, yPos, zPos, Block.AIR);
            generatePillar(modifier, xPos + door1pos + 1, yPos, zPos, Block.AIR);
        }

        if (door2 && wall2) {
            generatePillar(modifier, xPos, yPos, zPos + door2pos, Block.AIR);
            generatePillar(modifier, xPos, yPos, zPos + door2pos + 1, Block.AIR);
        }
    }

    private void generatePillar(UnitModifier modifier, int xPos, int yPos, int zPos, Block material) {
        for (int y = yPos; y < yPos + 3; y++) {
            modifier.setBlock(xPos, y, zPos, material);
        }
    }
}
