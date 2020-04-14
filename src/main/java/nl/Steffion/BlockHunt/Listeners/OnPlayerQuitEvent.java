package nl.Steffion.BlockHunt.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import nl.Steffion.BlockHunt.Arena;
import nl.Steffion.BlockHunt.ArenaHandler;
import nl.Steffion.BlockHunt.MemoryStorage;

public class OnPlayerQuitEvent implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		for (Arena arena : MemoryStorage.arenaList) {
			if (arena.playersInArena.contains(player)) {
				ArenaHandler.playerLeaveArena(player, true, true);
			}
		}
	}

//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void onPlayerJoinEvent(PlayerJoinEvent event) {
//		Player playerJoining = event.getPlayer();
//		playerJoining.teleport(playerJoining.getWorld().getSpawnLocation());
//	}
}
