package dev.emortal.nextbots.nextbots;

import dev.emortal.nextbots.pathfinding.NextbotPathfinding;
import dev.emortal.nextbots.util.NoDragEntity;
import dev.emortal.nextbots.util.NoPhysicsEntity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.EntitySoundEffectPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.emortal.nextbots.pathfinding.NextbotPathfinding.WALKABLE_RESOLUTION;

public abstract class Nextbot {

    public static Tag<Integer> BOT_TARGET_TAG = Tag.Integer("botTarget");
    public static List<Nextbot> ACTIVE_NEXTBOTS = new ArrayList<>();


    private final Component name;
    private String unicodeDisplay;
    private int soundId;
    private int soundTicks;
    private double losSpeed; // (players move at roughly 13 (with movement speed 0.3))
    private int splitsInPath;
    private int pathSkip;
    private boolean allowChangeOfTarget;
    private double killDistance;
    private boolean occupyTarget;

//    private final String deathScreenText;
//    private final String chatText;

    private final NextbotDamage damage;

    private @Nullable Task pathfindingTask;
    private @Nullable Entity entity;
    private @Nullable Entity displayEntity;
    private @Nullable Player playerTarget = null;

    public Nextbot(
            Component name, String unicodeDisplay,
            Component deathScreenText, Component chatText,
            int soundId, int soundTicks,
            double losSpeed
    ) {
        this.name = name;
        this.unicodeDisplay = unicodeDisplay;
        this.losSpeed = losSpeed;
        this.splitsInPath = 1;
        this.pathSkip = 1;
//        this.deathScreenText = deathScreenText;
//        this.chatText = chatText;
        this.soundId = soundId;
        this.soundTicks = soundTicks;

        this.allowChangeOfTarget = true;
        this.occupyTarget = true;

        this.killDistance = 1.5;


        this.damage = new NextbotDamage(this, deathScreenText, chatText);
    }

    public String getUnicodeDisplay() {
        return unicodeDisplay;
    }

    public void setUnicodeDisplay(String unicodeDisplay) {
        this.unicodeDisplay = unicodeDisplay;

        ArmorStandMeta meta = (ArmorStandMeta) displayEntity.getEntityMeta();
        meta.setCustomName(Component.text("\uF80D" + getUnicodeDisplay() + "\uF80D"));
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public int getSoundTicks() {
        return soundTicks;
    }

    public void setSoundTicks(int soundTicks) {
        this.soundTicks = soundTicks;
    }

    public double getLosSpeed() {
        return losSpeed;
    }

    public void setLosSpeed(double losSpeed) {
        this.losSpeed = losSpeed;
    }

    public int getSplitsInPath() {
        return splitsInPath;
    }

    public void setSplitsInPath(int splitsInPath) {
        this.splitsInPath = splitsInPath;
    }

    public int getPathSkip() {
        return pathSkip;
    }

    public void setPathSkip(int pathSkip) {
        this.pathSkip = pathSkip;
    }

    public boolean isAllowChangeOfTarget() {
        return allowChangeOfTarget;
    }

    public void setAllowChangeOfTarget(boolean allowChangeOfTarget) {
        this.allowChangeOfTarget = allowChangeOfTarget;
    }

    public double getKillDistance() {
        return killDistance;
    }

    public void setKillDistance(double killDistance) {
        this.killDistance = killDistance;
    }

    public boolean isOccupyTarget() {
        return occupyTarget;
    }

    public void setOccupyTarget(boolean occupyTarget) {
        this.occupyTarget = occupyTarget;
    }

    public @Nullable Task getPathfindingTask() {
        return pathfindingTask;
    }

    public void setPathfindingTask(@Nullable Task pathfindingTask) {
        this.pathfindingTask = pathfindingTask;
    }

    public @Nullable Entity getEntity() {
        return entity;
    }

    public Component getName() {
        return name;
    }

    public void spawnEntity(Instance instance, Pos pos) {
        Entity vehicle = new NoDragEntity(EntityType.COW, getSoundId());
        vehicle.setInvisible(true);
        vehicle.setBoundingBox(0.5, 0.5, 0.5);

        this.entity = vehicle;

        Entity display = new NoPhysicsEntity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta) display.getEntityMeta();
        meta.setMarker(true);
        meta.setCustomName(Component.text("\uF80D" + getUnicodeDisplay() + "\uF80D"));
        meta.setCustomNameVisible(true);
        display.setInvisible(true);

        this.displayEntity = display;

        vehicle.setInstance(instance, pos).thenRun(() -> {
            display.setInstance(instance, pos).thenRun(() -> {
                vehicle.addPassenger(display);

                ACTIVE_NEXTBOTS.add(this);
            });
        });

        createTasks();

        onSpawn();
    }

    public void onSpawn() {}

    public void kill() {
        if (entity == null) return;
        if (displayEntity == null) return;

        entity.remove();
        displayEntity.remove();
        if (pathfindingTask != null) pathfindingTask.cancel();
    }

    private void createTasks() {
        entity.scheduler().buildTask(() -> {
            if (entity.getAliveTicks() % getSoundTicks() == 0) {
                ServerPacket packet = new EntitySoundEffectPacket(soundId, Sound.Source.MASTER, entity.getEntityId(), 1f, 1f, 1);
                entity.sendPacketToViewers(packet);
            }

            Player target = pickTarget(entity.getInstance().getPlayers());
            if (playerTarget != null && !isAllowChangeOfTarget()) {
                target = playerTarget;
            }

            for (Player player : entity.getInstance().getPlayers()) {
                if (player.isDead()) continue;
                // TODO: Maybe give 3 seconds of immunity
                if (player.getGameMode() == GameMode.CREATIVE) continue;

                double dist = player.getDistanceSquared(entity);

                if (dist < getKillDistance()*getKillDistance()) {
                    player.damage(this.damage, 30f);

                    player.removeTag(BOT_TARGET_TAG);

                    if (player.getGameMode() != GameMode.CREATIVE)
                        entity.getInstance().playSound(Sound.sound(Key.key("entity.nextbot.killplayer"), Sound.Source.MASTER, 15f, 1f), player.getPosition());
                }
            }

            if (target == null) {
                entity.setVelocity(Vec.ZERO);

                return;
            }

            if (isOccupyTarget()) target.setTag(BOT_TARGET_TAG, entity.getEntityId());
            playerTarget = target;


            Vec dirToPlayer = Vec.fromPoint(target.getPosition().sub(entity.getPosition()).withY(0));
            entity.setVelocity(dirToPlayer.normalize().mul(getLosSpeed()).withY(-10));

            if (entity.getAliveTicks() % 7 == 0) { // only attempt pathfind every 10 ticks
                pathfind(target.getPosition());
            }

        }).repeat(TaskSchedule.tick(1)).schedule();
    }


    private void pathfind(Point target) {
        Instance instance = entity.getInstance();
        Point nextbotPos = entity.getPosition();


//        long before = System.nanoTime();
        boolean isWalkable = NextbotPathfinding.isWalkableBetween(instance, nextbotPos, target.add(0, 0.85, 0), WALKABLE_RESOLUTION);

        if (!isWalkable) {
//            List<Point> pathfindResult = NextbotPathfinding.pathfind(instance, nextbotPos, target.add(0, 0.85, 0), (pos) -> NextbotPathfinding.isWalkableBetween(instance, nextbotPos, target.add(0, 0.85, 0), WALKABLE_RESOLUTION));
            List<Point> pathfindResult = NextbotPathfinding.pathfind(instance, nextbotPos, target.add(0, 0.85, 0), (pos) -> false);

            if (!pathfindResult.isEmpty()) {
                if (pathfindingTask != null) pathfindingTask.cancel();

                List<Point> smoothedPath = NextbotPathfinding.smoothPath(instance, pathfindResult);
                if (smoothedPath.size() <= 1) return;

                // split path
                List<Point> splitPath = NextbotPathfinding.splitPath(smoothedPath, getSplitsInPath());

//                instance.sendActionBar(Component.text("Took " + (System.nanoTime() - before) / 1_000_000.0));

                pathfindingTask = entity.scheduler().submitTask(new Supplier<>() {
                    int step = splitPath.size();

                    @Override
                    public TaskSchedule get() {
                        step -= getPathSkip();

                        if (step < 0) {
                            return TaskSchedule.stop();
                        }

                        Pos newPoint = Pos.fromPoint(splitPath.get(step));
                        entity.teleport(newPoint);

                        if (step <= 0) {
                            return TaskSchedule.stop();
                        }

                        return TaskSchedule.tick(1);
                    }
                });

//                for (Point point : splitPath) {
//                    ServerPacket packet = ParticleCreator.createParticlePacket(Particle.FLAME, true, point.x() + 0.5, point.y(), point.z() + 0.5, 0f, 0f, 0f, 0f, 1, null);
//                    instance.sendGroupedPacket(packet);
//                }
            }
        } else {
            if (pathfindingTask != null) pathfindingTask.cancel();
        }
    }

    public @Nullable Player pickTarget(Set<Player> players) {
        if (entity == null) return null;

        double distanceLowScore = Double.MAX_VALUE;
        Player lowestDistancePlayer = null;

        for (Player player : players) {
            if (player.isDead()) continue;
            // TODO: Maybe give 3 seconds of immunity
            if (player.getGameMode() == GameMode.CREATIVE) continue;

            if (isOccupyTarget()) {
                Integer botTarget = player.getTag(BOT_TARGET_TAG);
                if (botTarget != null && botTarget != entity.getEntityId()) continue;
            }

            double dist = player.getDistanceSquared(entity);

            if (dist < distanceLowScore) {
                distanceLowScore = dist;
                lowestDistancePlayer = player;
            }
        }

        return lowestDistancePlayer;
    }
}
