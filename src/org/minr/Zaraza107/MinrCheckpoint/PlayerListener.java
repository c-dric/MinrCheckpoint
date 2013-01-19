package org.minr.Zaraza107.MinrCheckpoint;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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
	
	public PlayerListener(Checkpoint instance) {
		this.plugin = instance;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin); //Nickman clean way of registering ourselves
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block bl = event.getClickedBlock();

		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && (bl.getTypeId() == 63 || bl.getTypeId() == 68)) {

			Player player = event.getPlayer();
			String name = player.getName();
			Location bloc = bl.getLocation();
			World world = bloc.getWorld();
			String w = world.getName();
			double x = bloc.getX();
			double y = bloc.getY();
			double z = bloc.getZ();
			String where = w + "," + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);
			Sign sign = (Sign)bl.getState();
			String str1;
			String str2;
			
			try {
				str1 = sign.getLine(1);
				str2 = sign.getLine(2);
			} catch(Exception e) {
				return;
			}
			
			if(str1.equalsIgnoreCase("[Checkpoint]")) {
				if(plugin.signMap.containsKey(str2)) {
					if(plugin.playerMap.containsKey(name)) {
						String string = plugin.playerMap.get(name);
						if(string.equalsIgnoreCase(str2)) {
							player.sendMessage(ChatColor.DARK_AQUA + "You already set this checkpoint.");
							return;
						}
						plugin.playerMap.remove(name);
					}
					plugin.playerMap.put(name, str2);
					plugin.playerDB.setString(name, str2);
					player.sendMessage(ChatColor.DARK_AQUA + "Your new checkpoint is set! :)");
					
					System.out.println("[MinrCheckpoint] " + name + " set new checkpoint to " + str2 + " / " + where);
				} else {
					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");
				}
			} else if(str1.equalsIgnoreCase("[CheckpointE]")) {
				if(plugin.signMap.containsKey(str2)) {					
					String[] split = plugin.signMap.get(str2).split(",");
					Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
					player.teleport(loc);
					
					player.sendMessage(ChatColor.DARK_AQUA + "Congratulations! You finished Hardcore!"); // c_dric edited message
					
					plugin.playerDB.removeKey(name);
					plugin.playerMap.remove(name);
					
					/* c_dric - Don't remove FFA points and FFA history */
					//plugin.pointDB.removeKey(name);
					//plugin.pointMap.remove(name);
					//plugin.ffaDB.removeKey(name);
					//plugin.ffaMap.remove(name);
					
					this.log.info("[MinrCheckpoint] " + name + " finished hardcore maze set at " + str2 + " / " + where);
				}  else {
					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");
				}
			} else if(str1.equalsIgnoreCase("[CheckpointF]")) {
				if(plugin.signMap.containsKey(str2)) {					
					if(plugin.pointMap.containsKey(name)) {
						int prq = Integer.parseInt(plugin.pointMap.get(name));
						if(( prq > plugin.pointsReq ) || ( prq == plugin.pointsReq )) {
							String[] split = plugin.signMap.get(str2).split(",");
							Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
							player.teleport(loc);
								
							/* c_dric - Edited message */
							player.sendMessage(ChatColor.DARK_AQUA + "Congratulations! Welcome to Hardcore!");
								
							/* c_dric - Don't remove FFA points and FFA history */
							//plugin.playerDB.removeKey(name);
							//plugin.playerMap.remove(name);
							//plugin.pointDB.removeKey(name);
							//plugin.pointMap.remove(name);
							//plugin.ffaDB.removeKey(name);
							//plugin.ffaMap.remove(name);
							
							//Nickman change the users group, UNTESTED!!!
							if (plugin.mBPermissionsApi != null) { //Can we check in some way if the plugin is actually loaded?
								String groups[] = ApiLayer.getGroups(split[0], CalculableType.USER, player.getName());
								
								if (groups.length == 1 && groups[0].equalsIgnoreCase("default")) {
									ApiLayer.addGroup(split[0], CalculableType.USER, player.getName(), "fish");
								}
							}
							
							/* c_dric - Edited message */
							System.out.println("[MinrCheckpoint] " + name + " used checkpointF " + str2 + " / " + where);
						} else {
							player.sendMessage(ChatColor.DARK_AQUA + "You need at least " + plugin.pointsReq + " FFA maze points!");
						}
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "You need at least " + plugin.pointsReq + " FFA maze points!");
					}
				} else {
					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");
				}
				
			/* c_dric's new code */
			} else if(str1.equalsIgnoreCase("[CheckpointW]")) {
				if(plugin.signMap.containsKey(str2)) {					
					String[] split = plugin.signMap.get(str2).split(",");
					Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
					player.teleport(loc);
					System.out.println("[MinrCheckpoint] " + name + " used checkpointW at " + str2 + " / " + where);
				} else {
					player.sendMessage(ChatColor.RED + "ERROR! This CheckpointW was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] CheckpointW " + str2 + " / " + where + " doesn't exist!");
				}
			/* END */
				
			} else if(str1.toLowerCase().contains("[checkpointp")) {
				if(plugin.signMap.containsKey(str2)) {
					
					String p = str1.substring(12, 13);

					if (p.equalsIgnoreCase("]")) {
						p = "1" ;
					}
			
					int mp = Integer.parseInt(p);
					int i = plugin.pointDB.getInt(name, 0);

					String[] split = plugin.signMap.get(str2).split(",");
					Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
						
					if(!plugin.ffaMap.containsKey(name)) {
						player.sendMessage(ChatColor.DARK_AQUA + "You gained " + String.valueOf(mp) + " point(s)! Your current score is " + String.valueOf(i + mp) + " point(s)");
						plugin.ffaDB.setString(name, str2);
						plugin.ffaMap.put(name, str2);
					} else {
						String ffa = plugin.ffaMap.get(name);
						String[] ffaSplit = ffa.split(",");
						for(int j = 0; j < ffaSplit.length; j++) {
							if(ffaSplit[j].equalsIgnoreCase(str2)) {
								player.sendMessage(ChatColor.DARK_RED + "You already gained " + String.valueOf(mp) + " point(s) for completing this maze.");
								player.teleport(loc);
								return;
							}
						}
						
						ffa += "," + str2;
						player.sendMessage(ChatColor.DARK_AQUA + "You gained " + String.valueOf(mp) + " point(s)! Your current score is " + String.valueOf(i + mp) + " point(s)");
						plugin.ffaDB.setString(name, ffa);
						plugin.ffaMap.put(name, ffa);
					}
					
					plugin.pointDB.setInt(name, i + mp);
					plugin.pointMap.put(name, String.valueOf(i + mp));
						
					player.teleport(loc);
					
					System.out.println("[MinrCheckpoint] " + name + " gained " + String.valueOf(mp) + " point(s) at " + str2 + " / " + where + " - New total : " + String.valueOf(i + mp) + " point(s)");
				}  else {
					player.sendMessage(ChatColor.RED + "ERROR! This checkpoint was removed. Please inform an admin.");
					System.out.println("[MinrCheckpoint] Checkpoint " + str2 + " / " + where + " doesn't exist!");
				}

			} else if((str1.toLowerCase().contains("[boom]")) && (str2.toLowerCase().contains("me up scotty!"))) {
				System.out.println("[MinrCheckpoint] " + name + " exploded at " + where);
				float power = 4F;
				boolean setFire = false;
				boolean breakBlocks = false;
				world.createExplosion(x, y, z, power, setFire, breakBlocks);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST) // Makes your event Highest priority
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		String name = event.getPlayer().getName();
		
		if(plugin.playerMap.containsKey(name)) {
			String sign = plugin.playerMap.get(name);
			String[] split = plugin.signMap.get(sign).split(",");
			System.out.println("[MinrCheckpoint] " + name + " respawned at " + sign + " / " + plugin.signMap.get(sign));
			
			Location loc = new Location(plugin.server.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]) + 1.0D, Double.parseDouble(split[3]));
			event.setRespawnLocation(loc);
		}
	}
}
