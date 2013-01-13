package org.minr.Zaraza107.MinrCheckpoint;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

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
		
		System.out.println("[MinrCheckpoint] [DEBUG] Sign created. ");
		
		if (event.getLine(1).toLowerCase().contains("checkpoint")) {
			
			System.out.println("[MinrCheckpoint] [DEBUG] Checkpoint sign created. ");
			
			if (player.isOp()) {
				player.sendMessage(ChatColor.DARK_GREEN + "Checkpoint created successfully!");
			} else {
				player.sendMessage(ChatColor.RED + "You cannot make checkpoints!");
				event.setCancelled(true);
				return;
			}

		}
	}
}