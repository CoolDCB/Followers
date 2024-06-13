package org.lushplugins.followers.entity.tasks;

import me.tofaa.entitylib.wrapper.WrapperLivingEntity;
import org.lushplugins.followers.Followers;
import org.lushplugins.followers.data.FollowerUser;
import org.lushplugins.followers.entity.Follower;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.followers.entity.OwnedFollower;

import java.util.HashMap;
import java.util.UUID;

public class ValidateTask extends FollowerTask {
    private static final HashMap<UUID, Integer> attemptsMap = new HashMap<>();

    public ValidateTask(String id) {
        super(id);
    }

    @Override
    public void tick(Follower follower) {
        WrapperLivingEntity entity = follower.getEntity();
        if (entity == null) {
            cancelFor(follower);
            return;
        }

        if (!(follower instanceof OwnedFollower ownedFollower)) {
            return;
        }

        if (!(ownedFollower.getOwner() instanceof Player player)) {
            return;
        }

        if (!entity.isSpawned()) {
            FollowerUser followerUser = Followers.getInstance().getDataManager().getFollowerUser(player);
            UUID uuid = player.getUniqueId();

            int attempts = attemptsMap.getOrDefault(uuid, 0);
            if (attempts >= Followers.getInstance().getConfigManager().getMaxRespawnAttempts()) {
                attemptsMap.remove(uuid);
                follower.despawn();
            } else {
                if (attempts == 1) {
                    Bukkit.getScheduler().runTaskLater(Followers.getInstance(), () -> attemptsMap.remove(uuid), 600);
                }
                attemptsMap.put(uuid, attempts + 1);
                followerUser.respawnFollower();
            }

            cancelFor(follower);
            return;
        }

        if (!player.isOnline()) {
            Bukkit.getScheduler().runTaskLater(Followers.getInstance(), follower::despawn, 5);
        }
    }

    @Override
    public int getPeriod() {
        return 1;
    }
}
