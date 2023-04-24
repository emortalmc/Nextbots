package dev.emortal.nextbots;

import dev.emortal.nextbots.nextbots.Maxwell;
import dev.emortal.nextbots.nextbots.Nextbot;
import dev.emortal.nextbots.nextbots.Obunga;
import dev.emortal.nextbots.nextbots.Quandale;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.packet.server.play.SetPassengersPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        DimensionType fullbright = DimensionType.builder(NamespaceID.from("fullbright"))
//                .ambientLight(0.24f)
                .skylightEnabled(true)
                .logicalHeight(400)
                .height(480)
                .build();
        MinecraftServer.getDimensionTypeManager().addDimension(fullbright);
        InstanceManager im = MinecraftServer.getInstanceManager();
        InstanceContainer instance = im.createInstanceContainer(fullbright);
//        BackroomsGenerator gen = new BackroomsGenerator();

//        instance.setGenerator(mod -> {
//            gen.generateRoom(mod, 50);
//        });
        instance.setChunkLoader(new AnvilLoader("gm_bigcity"));
        instance.setTimeUpdate(null);
        instance.setTimeRate(0);

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        instance.enableAutoChunkLoad(false);
        for (int cx = -10; cx < 10; cx++) {
            for (int cz = -10; cz < 10; cz++) {
                futures.add(instance.loadChunk(cx, cz));
            }
        }

        // Wait until chunks are loaded before spawning bots
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            System.out.println("Obunga is now activeeee");

            Nextbot obunga = new Obunga();
            obunga.spawnEntity(instance, new Pos(40, 65, 20));
//
            Nextbot cat = new Maxwell();
            cat.spawnEntity(instance, new Pos(40, 65, 20));

            Nextbot quandale = new Quandale();
            quandale.spawnEntity(instance, new Pos(40, 65, 50));

//            Nextbot autism = new AutismCreature();
//            autism.spawnEntity(instance, new Pos(10, 65, 10));
        }).exceptionally((a) -> {
            a.printStackTrace();
            return null;
        });


        GlobalEventHandler global = MinecraftServer.getGlobalEventHandler();

        global.addListener(PlayerLoginEvent.class, e -> {
            e.setSpawningInstance(instance);

            Pos spawnPos = new Pos(0, 65, 0);

            e.getPlayer().setRespawnPoint(spawnPos);

            e.getPlayer().setGameMode(GameMode.CREATIVE);

        });

        global.addListener(PlayerSpawnEvent.class, e -> {
            if (e.isFirstSpawn()) {
                e.getPlayer().getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3f);
                e.getPlayer().setFieldViewModifier(0.1f);

                // Re-send passengers packet. Avoids detachment issues!
                for (Nextbot nextbot : Nextbot.ACTIVE_NEXTBOTS) {
                    nextbot.getEntity().sendPacketToViewers(new SetPassengersPacket(nextbot.getEntity().getEntityId(), nextbot.getEntity().getPassengers().stream().map(Entity::getEntityId).toList()));
                }
            }
        });

        global.addListener(PlayerBlockInteractEvent.class, e -> {
            // Implement doors and trapdoors
            String openProperty = e.getBlock().getProperty("open");
            if (openProperty != null) {
                boolean open = openProperty.equals("true");
                String newOpen = Boolean.toString(!open);

                e.getInstance().setBlock(e.getBlockPosition(), e.getBlock().withProperty("open", newOpen));

                String halfProperty = e.getBlock().getProperty("half");
                if (halfProperty != null) {
                    boolean half = halfProperty.equals("upper");

                    e.getInstance().setBlock(e.getBlockPosition().add(0, half ? -1 : 1, 0), e.getBlock().withProperty("open", newOpen).withProperty("half", half ? "lower" : "upper"));
                }
            }
        });

        global.addListener(PlayerChatEvent.class, e -> {
            if (!e.getPlayer().getUsername().equals("emortaldev")) return;
            switch (e.getMessage()) {
                case "respawn" -> {
                    e.setCancelled(true);
                    for (Nextbot nextbot : Nextbot.ACTIVE_NEXTBOTS) {
                        nextbot.getEntity().teleport(new Pos(40, 65, 0));

                    }
                }
                case "creative" -> {
                    e.setCancelled(true);
                    e.getPlayer().setGameMode(GameMode.CREATIVE);
                }
                case "survival" -> {
                    e.setCancelled(true);
                    e.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
            }
        });


        server.start("0.0.0.0", 25565);
    }
}