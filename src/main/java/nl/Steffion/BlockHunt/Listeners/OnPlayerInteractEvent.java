package nl.Steffion.BlockHunt.Listeners;

import nl.Steffion.BlockHunt.*;
import nl.Steffion.BlockHunt.Arena.ArenaState;
import nl.Steffion.BlockHunt.PermissionsC.Permissions;
import nl.Steffion.BlockHunt.MemoryStorage;
import nl.Steffion.BlockHunt.Managers.MessageManager;
import nl.Steffion.BlockHunt.Managers.PermissionsManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OnPlayerInteractEvent implements Listener {

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (PermissionsManager.hasPerm(player, Permissions.create, false)) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.getType() != Material.AIR) {
				if (item.getItemMeta().hasDisplayName()) {
					ItemMeta im = item.getItemMeta();
					if (im.getDisplayName().equals(MessageManager.replaceAll((String) MemoryStorage.config.get(ConfigC.wandName)))) {
						Action action = event.getAction();
						if (event.hasBlock()) {
							Location location = event.getClickedBlock().getLocation();
							if (action.equals(Action.LEFT_CLICK_BLOCK)) {
								event.setCancelled(true);
								if (MemoryStorage.pos1.get(player) == null || !MemoryStorage.pos1.get(player).equals(location)) {
									MessageManager.sendFMessage(player, ConfigC.normal_wandSetPosition, "number-1",
											"pos-%N(%A" + location.getBlockX() + "%N, %A" + location.getBlockY() + "%N, %A" + location.getBlockZ() + "%N)", "x-"
													+ location.getBlockX(), "y-" + location.getBlockY(), "z-" + location.getBlockZ());
									MemoryStorage.pos1.put(player, location);
								}
							} else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
								event.setCancelled(true);
								if (MemoryStorage.pos2.get(player) == null || !MemoryStorage.pos2.get(player).equals(location)) {
									MessageManager.sendFMessage(player, ConfigC.normal_wandSetPosition, "number-2",
											"pos-%N(%A" + location.getBlockX() + "%N, %A" + location.getBlockY() + "%N, %A" + location.getBlockZ() + "%N)", "x-"
													+ location.getBlockX(), "y-" + location.getBlockY(), "z-" + location.getBlockZ());
									MemoryStorage.pos2.put(player, location);
								}
							}
						}
					}
				}
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock() != null) {
				if (event.getClickedBlock().getState() instanceof Sign) {
					if (SignsHandler.isSign(event.getClickedBlock().getLocation())) {
						Sign sign = (Sign) event.getClickedBlock().getState();
						if (sign.getLine(1) != null) {
							if (sign.getLine(1).equals(MessageManager.replaceAll(MemoryStorage.config.getFile().getStringList(ConfigC.sign_LEAVE.location).get(1)))) {
								if (PermissionsManager.hasPerm(player, Permissions.joinsign, true)) {
									ArenaHandler.playerLeaveArena(player, true, true);
								}
							} else if (sign.getLine(1).equals(MessageManager.replaceAll(MemoryStorage.config.getFile().getStringList(ConfigC.sign_SHOP.location).get(1)))) {
								if (PermissionsManager.hasPerm(player, Permissions.shop, true)) {
									InventoryHandler.openShop(player);
								}
							} else {
								for (Arena arena : MemoryStorage.arenaList) {
									if (sign.getLines()[1].contains(arena.arenaName)) {
										if (PermissionsManager.hasPerm(player, Permissions.joinsign, true)) {
											ArenaHandler.playerJoinArena(player, arena.arenaName);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() != Material.AIR) {
				if (event.getClickedBlock().getType().equals(Material.ENCHANTING_TABLE) || event.getClickedBlock().getType().equals(Material.CRAFTING_TABLE)
						|| event.getClickedBlock().getType().equals(Material.FURNACE) || event.getClickedBlock().getType().equals(Material.CHEST)
						|| event.getClickedBlock().getType().equals(Material.ANVIL) || event.getClickedBlock().getType().equals(Material.ENDER_CHEST)
						|| event.getClickedBlock().getType().equals(Material.JUKEBOX) || block.getRelative(event.getBlockFace()).getType().equals(Material.FIRE)) {
					for (Arena arena : MemoryStorage.arenaList) {
						if (arena.playersInArena.contains(player)) {
							event.setCancelled(true);
						}
					}
				}
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			for (Arena arena : MemoryStorage.arenaList) {
				if (arena.seekers.contains(player)) {
					for (Player pl : arena.playersInArena) {
						if (MemoryStorage.hiddenLoc.get(pl) != null) {
							Block pLoc = event.getClickedBlock();
							Block moveLocBlock = MemoryStorage.hiddenLoc.get(pl).getBlock();
							if (moveLocBlock.getX() == pLoc.getX() && moveLocBlock.getY() == pLoc.getY() && moveLocBlock.getZ() == pLoc.getZ()) {
								MemoryStorage.moveLoc.put(pl, new Location(pl.getWorld(), 0, 0, 0));
								pl.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
								SolidBlockHandler.makePlayerUnsolid(pl);
							}
						}
					}
				}
			}
		}

		for (Arena arena : MemoryStorage.arenaList) {
			if (arena.playersInArena.contains(player) && (arena.gameState.equals(ArenaState.WAITING) || arena.gameState.equals(ArenaState.STARTING))) {
				event.setCancelled(true);
				ItemStack item = player.getInventory().getItemInMainHand();
				if (item.getType() != Material.AIR) {
					if (item.getItemMeta().getDisplayName() != null) {
						if (item.getItemMeta().getDisplayName().equals(MessageManager.replaceAll((String) MemoryStorage.config.get(ConfigC.shop_blockChooserv1Name)))) {
							Inventory blockChooser = Bukkit.createInventory(null, 36, MessageManager.replaceAll("\u00A7r" + MemoryStorage.config.get(ConfigC.shop_blockChooserv1Name)));
							if (arena.disguiseBlocks != null) {
								for (int i = arena.disguiseBlocks.size(); i > 0; i = i - 1) {
									blockChooser.setItem(i - 1, arena.disguiseBlocks.get(i - 1));
								}
							}

							player.openInventory(blockChooser);
						}

						if (item.getItemMeta().getDisplayName().equals(MessageManager.replaceAll((String) MemoryStorage.config.get(ConfigC.shop_BlockHuntPassv2Name)))) {
							Inventory BlockHuntPass = Bukkit.createInventory(null, 9, MessageManager.replaceAll("\u00A7r" + MemoryStorage.config.get(ConfigC.shop_BlockHuntPassv2Name)));
							ItemStack BlockHuntPassSEEKER = new ItemStack(Material.BLUE_WOOL, 1);
							ItemMeta BlockHuntPassIM = BlockHuntPassSEEKER.getItemMeta();
							BlockHuntPassIM.setDisplayName(MessageManager.replaceAll("&eSEEKER"));
							BlockHuntPassSEEKER.setItemMeta(BlockHuntPassIM);
							BlockHuntPass.setItem(1, BlockHuntPassSEEKER);

							ItemStack BlockHuntPassHIDER = new ItemStack(Material.RED_WOOL, 1);
							BlockHuntPassIM.setDisplayName(MessageManager.replaceAll("&eHIDER"));
							BlockHuntPassHIDER.setItemMeta(BlockHuntPassIM);
							BlockHuntPass.setItem(7, BlockHuntPassHIDER);

							player.openInventory(BlockHuntPass);
						}
					}
				}
			}
		}

	}
}
