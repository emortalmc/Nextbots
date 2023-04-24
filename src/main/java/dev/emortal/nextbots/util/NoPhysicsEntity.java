package dev.emortal.nextbots.util;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public class NoPhysicsEntity extends Entity {

    public NoPhysicsEntity(EntityType type) {
        super(type);
        this.hasPhysics = false;
        this.setNoGravity(true);
    }

}
