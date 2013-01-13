package org.minr.Zaraza107.MinrCheckpoint;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockListener implements Listener  { //Nickman changed the base type

	// v4 New code c_dric
	public Checkpoint plugin;

    public BlockListener(Checkpoint plugin) {
        this.plugin = plugin;
        this.registerEvents();
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		
		Player player = event.getPlayer();
		String p = player.getPlayerListName();
		String warp = event.getLine(2);
		Block b = event.getBlock();
		Location loc = b.getLocation();
		World world = loc.getWorld();
		String w = world.getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
		String where = w + "," + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);   
				
		if (event.getLine(1).toLowerCase().contains("[checkpoint")) {
			
			if (player.isOp()) {
				
				if ((event.getLine(1).toLowerCase().matches("\\[checkpoint[pefw]{0,1}[1-5]{0,1}\\]")) && warp.length() > 1) {
					
					player.sendMessage(ChatColor.DARK_GREEN + event.getLine(1) + " / " + warp + " sign created successfully!");
					System.out.println("[MinrCheckpoint] " + p + " created " + event.getLine(1) + " / " + warp + " sign at " + where);
					
				} else {
					
					if (!event.getLine(1).toLowerCase().matches("\\[checkpoint[pefw]{0,1}\\d{0,1}\\]")) {
						player.sendMessage(ChatColor.RED + event.getLine(1) + " is not a valid checkpoint sign!");
					}

					if (warp.length() < 1) {
						player.sendMessage(ChatColor.RED + "Third line cannot be empty!");
					}
					
					System.out.println("[MinrCheckpoint] " + p + " failed creating " + event.getLine(1) + " / " + warp + " sign at " + where);
					event.getBlock().breakNaturally();
					event.setCancelled(true);
					return;

				}
				
			} else {
				
				player.sendMessage(ChatColor.RED + "You cannot make checkpoint signs!");
				System.out.println("[MinrCheckpoint] " + p + " failed creating " + event.getLine(1) + " / " + warp + " sign at " + where);
				event.getBlock().breakNaturally();
				event.setCancelled(true);
				return;
				
			}

		}
	}
}