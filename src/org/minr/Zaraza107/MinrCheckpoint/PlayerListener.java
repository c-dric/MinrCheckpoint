package org.minr.Zaraza107.MinrCheckpoint;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

public class PlayerListener implements Listener {

	public Checkpoint plugin;
	public Logger log = Logger.getLogger("Minecraft");

	// What Nickman says

	public PlayerListener(Checkpoint instance) {

		this.plugin = instance;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin); //Nickman clean way of registering ourselves

	}

	// onPlayerInteract stuff.

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {

		// If a block got clicked, which type of block was it?

		Block bl = event.getClickedBlock();

		// Only bother when right clicking a sign

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && (bl.getType() == Material.SIGN || bl.getType() == Material.SIGN_POST )) {

			// Get playername and uuid.

			UUID player_id = event.getPlayer().getUniqueId();
			String player_string = player_id.toString();
			Player player = Bukkit.getPlayer(player_id);
			String name = player.getName();

			// Get location

			Location bloc = bl.getLocation();
			World world = bloc.getWorld();
			String w = world.getName();
			double x = bloc.getX();
			double y = bloc.getY();
			double z = bloc.getZ();
			String where = w + "," + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);

			// What was on the sign the player clicked ?

			Sign sign = (Sign)bl.getState();

			String str1;
			String str2;
			
			try {

				// str1 is the second line of the sign. (first line is str0).
				str1 = sign.getLine(1);

				// str2 is the third line. 
				str2 = sign.getLine(2);

			} catch(Exception e) {

				// I guess something bad happened.
				return;

			}

			// Checkpoint signs 

			if(str1.equalsIgnoreCase("[Checkpoint]")) {

				// Check if the checkpoint on the 3rd line is in the database.

				if(plugin.signMap.containsKey(str2)) {

					// Check if the player has a CP.

					if(plugin.playerMap.containsKey(player_string)) {

						String string = plugin.playerMap.get(player_string);

						// If the player already set this CP:

						if(string.equalsIgnoreCase(str2)) {

							player.sendMessage(ChatColor.DARK_AQUA + "You already set this checkpoint.");
							return;

						}

						// If the player has another CP set.

						plugin.playerMap.remove(player_string);

					}

					// Set a new CP for this player.

					plugin.playerMap.put(player_string, str2);
					plugin.playerDB.setString(player_string, str2);

					// Let the player know.

					player.sendMessage(ChatColor.DARK_AQUA + "Your new checkpoint is set! :)");

					// Let @Barrack know.

					System.out.println("[MinrCheckpoint] " + name + " - " + player_string + " set new checkpoint to " + str2 + " / " + where);

				} else {

				// Someone tried to save a non-existing CP.

					// Inform the player.

					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint doesn't exist. Please inform an admin.");

					// Inform @Barrack.

					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");

				}

			// End checkpoint of Hardcore

			} else if(str1.equalsIgnoreCase("[CheckpointE]")) {

				// Check if the cp on line 2 exists.

				if(plugin.signMap.containsKey(str2)) {

					// Teleport to cp on line 2.

					String[] split = plugin.signMap.get(str2).split(",");
					Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
					player.teleport(loc);

					// Congratulate the player on finishing HC.

					player.sendMessage(ChatColor.DARK_AQUA + "Congratulations! You finished Hardcore!"); // c_dric edited message

					// Remove the player CP.

					plugin.playerDB.removeKey(player_string);
					plugin.playerMap.remove(player_string);

					/* Don't remove FFA points and FFA history

					plugin.pointDB.removeKey(name);
					plugin.pointMap.remove(name);
					plugin.ffaDB.removeKey(name);
					plugin.ffaMap.remove(name);

					*/

					// Inform @Barrack.

					this.log.info("[MinrCheckpoint] " + name + " - " + player_string + " finished hardcore maze set at " + str2 + " / " + where);

				}  else {

					// CP doesn't exist.

					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");

				}

			// v3 - Start checkpoint of HC.

			} else if(str1.equalsIgnoreCase("[CheckpointF]")) {

				// Check if the cp on line 2 exists.

				if(plugin.signMap.containsKey(str2)) {

					// Check if the player exists in the points DB.

					if(plugin.pointMap.containsKey(player_string)) {

						// Check if the player has enough points.

						int prq = Integer.parseInt(plugin.pointMap.get(player_string));

						if(( prq > plugin.pointsReq ) || ( prq == plugin.pointsReq )) {

							// Teleport the player.

							String[] split = plugin.signMap.get(str2).split(",");
							Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
							player.teleport(loc);

							// Welcome the player to HC.

							player.sendMessage(ChatColor.DARK_AQUA + "Congratulations! Welcome to Hardcore!");

							/* Don't remove FFA points and FFA history.

							plugin.playerDB.removeKey(name);
							plugin.playerMap.remove(name);
							plugin.pointDB.removeKey(name);
							plugin.pointMap.remove(name);
							plugin.ffaDB.removeKey(name);
							plugin.ffaMap.remove(name);

							*/

							// Nickman : change the users group.

							if (plugin.mBPermissionsApi != null) { //Can we check in some way if the plugin is actually loaded?

								String groups[] = ApiLayer.getGroups(split[0], CalculableType.USER, player.getName());

								if (groups.length == 1 && groups[0].equalsIgnoreCase("default")) {

									ApiLayer.addGroup(split[0], CalculableType.USER, player.getName(), "fish");

								}

							}

							// Inform @Barrack.

							System.out.println("[MinrCheckpoint] " + name + " - " + player_string + " used checkpointF " + str2 + " / " + where);

						} else {

							// Player doesn't have enough points.

							player.sendMessage(ChatColor.DARK_AQUA + "You need at least " + plugin.pointsReq + " FFA maze points!");

						}

					} else {

						// Player unknown

						player.sendMessage(ChatColor.DARK_AQUA + "You need at least " + plugin.pointsReq + " FFA maze points!");

					}

				} else {

					// CP on line 2 doesn't exist.

					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");

				}

			// v4 - Checkpoint warp signs.

			} else if(str1.equalsIgnoreCase("[CheckpointW]")) {

				// Check if the cp on line 2 exists.

				if(plugin.signMap.containsKey(str2)) {					

					// Get the CP location

					String[] split = plugin.signMap.get(str2).split(",");
					Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));

					// Teleport the player.

					player.teleport(loc);

					// Inform @Barrack.

					System.out.println("[MinrCheckpoint] " + name + " - " + player_string + " used checkpointW at " + str2 + " / " + where);

				} else {

					// Inform the player that the CP doesn't exist.

					player.sendMessage(ChatColor.RED + "ERROR! This CheckpointW was removed. Please inform an admin.");

					// Inform @Barrack.

					System.out.println("[MinrCheckpoint] CheckpointW " + str2 + " / " + where + " doesn't exist!");

				}

			// v4 - Checkpoint point signs.

			} else if(str1.toLowerCase().contains("[checkpointp")) {

				// Check if the cp on line 2 exists.

				if(plugin.signMap.containsKey(str2)) {

					// Get the number of points to give from line 2

					String p = str1.substring(12, 13);

					// If no point given, use 1 as default.

					if (p.equalsIgnoreCase("]")) {
						p = "1" ;
					}

					// Voodoo.

					int mp = Integer.parseInt(p);

					// Get the player's current points

					int i = plugin.pointDB.getInt(player_string, 0);

					// Get the coords of the CP on line 3.

					String[] split = plugin.signMap.get(str2).split(",");
					Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));

					// Does the player exist in the DB?

					if(!plugin.ffaMap.containsKey(player_string)) {

						// No. Create a record and add this level.

						plugin.ffaDB.setString(player_string, str2);
						plugin.ffaMap.put(player_string, str2);

						// Inform the player.

						player.sendMessage(ChatColor.DARK_AQUA + "You gained " + String.valueOf(mp) + " point(s)! Your current score is " + String.valueOf(i + mp) + " point(s)");

					} else {

						// Yes. Player exists on DB. Check if (s)he has beaten this level already.

						String ffa = plugin.ffaMap.get(player_string);
						String[] ffaSplit = ffa.split(",");

						for(int j = 0; j < ffaSplit.length; j++) {

							if(ffaSplit[j].equalsIgnoreCase(str2)) {

								// Level already beaten. No points.

								player.sendMessage(ChatColor.DARK_RED + "You already gained " + String.valueOf(mp) + " point(s) for completing this maze.");

								// Only a teleport.

								player.teleport(loc);

								return;

							}

						}

						// Add the level to the player's FFA DB.

						ffa += "," + str2;
						plugin.ffaDB.setString(player_string, ffa);
						plugin.ffaMap.put(player_string, ffa);

						// Inform the player.

						player.sendMessage(ChatColor.DARK_AQUA + "You gained " + String.valueOf(mp) + " point(s)! Your current score is " + String.valueOf(i + mp) + " point(s)");

					}

					// Change the player's point in the points DB.

					plugin.pointDB.setInt(player_string, i + mp);
					plugin.pointMap.put(player_string, String.valueOf(i + mp));

					// Teleport the player to the CP location.

					player.teleport(loc);

					// Inform @Barrack.

					System.out.println("[MinrCheckpoint] " + name + " gained " + String.valueOf(mp) + " point(s) at " + str2 + " / " + where + " - New total : " + String.valueOf(i + mp) + " point(s)");

				}  else {

					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");

					// Inform @Barrack.

					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");

				}

			// Dividing by Zero for dummies!

			} else if((str1.toLowerCase().contains("[boom]")) && (str2.toLowerCase().contains("me up scotty!"))) {

				// Define Boom

				float power = 4F;
				boolean setFire = false;
				boolean breakBlocks = false;

				// Le Boom

				world.createExplosion(x, y, z, power, setFire, breakBlocks);

				// Keeping @Barrack in the loop.

				System.out.println("[MinrCheckpoint] " + name + " exploded at " + where);

			}

		}

	}

	// Whenever a player respawns, check if (s)he has a cp. If so, respawn there.

	@EventHandler(priority = EventPriority.HIGHEST) // Makes your event Highest priority
	public void onPlayerRespawn(final PlayerRespawnEvent event) {

		// Get nickname and uuid of the player

		UUID player_id = event.getPlayer().getUniqueId();
		String player_string = player_id.toString();
		String name = event.getPlayer().getName();

		// Search for the UUID in the database

		if(plugin.playerMap.containsKey(player_string)) {

			// Get the CP name and coords

			String sign = plugin.playerMap.get(player_string);
			String[] split = plugin.signMap.get(sign).split(",");

			// Teleporting the player to his/her CP.

			Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
			event.setRespawnLocation(loc);

			// Keeping @Barrack in the loop.

			System.out.println("[MinrCheckpoint] " + name + " - " + player_string + " respawned at " + sign + " / " + plugin.signMap.get(sign));

		}

	}

}
