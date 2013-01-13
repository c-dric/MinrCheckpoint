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
		Block b = event.getBlock();
		Location loc = b.getLocation();
		World world = loc.getWorld();
		String w = world.getName();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
		String where = "World : " + w + "," + String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);   
				
		if (event.getLine(1).toLowerCase().contains("checkpoint")) {
			
			if (player.isOp()) {
				if (event.getLine(1).toLowerCase().matches("\\^[checkpoint\\.?\\d?]\\%")) {
					player.sendMessage(ChatColor.DARK_GREEN + event.getLine(1) + " created successfully!");
					System.out.println("[MinrCheckpoint] " + player + " created " + event.getLine(1) + " sign at " + where);
				} else {
					player.sendMessage(ChatColor.RED + event.getLine(1) + " is not a valid checkpoint!");
					System.out.println("[MinrCheckpoint] " + player + " failed creating " + event.getLine(1) + " sign at " + where);
				}
			} else {
				player.sendMessage(ChatColor.RED + "You cannot make checkpoints!");
				event.setCancelled(true);
				return;
			}

		}
	}
}