package org.minr.Zaraza107.MinrCheckpoint;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Checkpoint extends JavaPlugin {

	public String path = "plugins" + File.separator + "MinrCheckpoint" + File.separator;
	public Server server;
	public iProperty signDB;
	public iProperty playerDB;
	public iProperty pointDB;
	public iProperty ffaDB;
	public iProperty settings;
	public Map<String,String> signMap;
	public Map<String,String> playerMap;
	public Map<String,String> pointMap;
	public Map<String,String> ffaMap;
	public int pointsReq;
	public Plugin mBPermissionsApi; //Nickman Interface with bPermission

	// What to do on plugin load.

	public void onEnable() {

		this.server = this.getServer();
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] (" + pdfFile.getVersion() + ") is enabled!");
        
        /* Nickman's new code */
        new BlockListener(this);
        new PlayerListener(this);
        
        //Do we need to actually do this???
        mBPermissionsApi = getServer().getPluginManager().getPlugin("bPermissions"); //Check to see if the plugin is available
        /* END */
        
    	new File(this.path).mkdir();
    	
    	this.settings = new iProperty(this.path + "minr.settings");
    	this.pointsReq = settings.getInt("Maze-points-required-to-finish", 10);
    	
        this.signDB = new iProperty(this.path + "sign.db");
        this.playerDB = new iProperty(this.path + "player.db");
        this.pointDB = new iProperty(this.path + "point.db");
        this.ffaDB = new iProperty(this.path + "ffa.db");
        
        try {

        	this.signMap = this.signDB.returnMap();
        	this.playerMap = this.playerDB.returnMap();
        	this.pointMap = this.pointDB.returnMap();
        	this.ffaMap = this.ffaDB.returnMap();

        } catch(Exception e) {

        	e.printStackTrace();
        	this.server.getPluginManager().disablePlugin(this);

        }

	}

	// What to do on plugin unload.

	public void onDisable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] (version " + pdfFile.getVersion() + ") disabled. :(");
	}

	// What to do when a command is run.

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		try {

			Player player = (Player)sender;

			if(args.length < 1)
				return help(player);

			else if(args[0].equalsIgnoreCase("cp"))
				return cp(player, args);

			else if(args[0].equalsIgnoreCase("points"))
				return points(player, args);

			else if(args[0].equalsIgnoreCase("ffa"))
				return ffa(player, args);

			else if(args[0].equalsIgnoreCase("help"))
				return help(player);			

			else if((args[0].equalsIgnoreCase("create")) && (sender.isOp()) && (args.length > 4))
				return create(player, args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));

			else if((args[0].equalsIgnoreCase("delete")) && (sender.isOp()) && (args.length > 1))
				return delete(player, args[1]);

			else if((args[0].equalsIgnoreCase("remove")) && (sender.isOp()) && (args.length > 1))
				return remove(player, args[1]);

			else if((args[0].equalsIgnoreCase("removeid")) && (sender.isOp()) && (args.length > 1))
				return removeid(player, args[1]);

			else if((args[0].equalsIgnoreCase("give")) && (sender.isOp()) && (args.length > 2))
				return give(player, args[1], args[2]);

			else if((args[0].equalsIgnoreCase("giveid")) && (sender.isOp()) && (args.length > 2))
				return giveid(player, args[1], args[2]);

			else if (args[0].length() < 1)
				return help(player);			

			else
				return help(player);

		} catch(Exception e) {

			if(e instanceof NumberFormatException) {
				sender.sendMessage(ChatColor.RED + "Coordinates are in the wrong format!");

			}

			e.printStackTrace();

			return false;

		}

	}

	// Create a checkpoint.

	public boolean create(Player player, String name, double x, double y, double z) {

		// Does the CP already exists?

		if(this.signMap.containsKey(name)) {

			// If so, remove it.

			this.signMap.remove(name);
			this.signDB.removeKey(name);

		}

		// Add the CP to the DB.

		this.signMap.put(name, player.getWorld().getName() + "," + x + "," + y + "," + z);
		this.signDB.setString(name, player.getWorld().getName() + "," + x + "," + y + "," + z);

		// Inform the player.

		player.sendMessage(ChatColor.DARK_GREEN + "Checkpoint " + name + " saved!");

		// Let @Barrack know.

		String p = player.getPlayerListName();
		System.out.println("[MinrCheckpoint] " + p + " created warp " + name + " at " + player.getWorld().getName() + "," + x + "," + y + "," + z);

		return true;

	}

	// Remove a checkpoint.

	public boolean delete(Player player, String name) {

		// Does the CP already exists?

		if(this.signMap.containsKey(name)) {

			// If so, remove it.

			this.signMap.remove(name);
			this.signDB.removeKey(name);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + "Checkpoint " + name + " removed from database :)");

			// Let @Barrack know.

			String p = player.getPlayerListName();
			System.out.println("[MinrCheckpoint] " + p + " deleted warp " + name);

		} else {

			// CP not found.

			player.sendMessage(ChatColor.DARK_AQUA + "No such checkpoint in the database.");

		}

		return true;
	}

	// Remove a player's CP by name.

	public boolean remove(Player player, String name) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get a UUID for the player to remove. From cache or mojang.com.

		String player_id = UUIDManager.getUUIDFromPlayer(name).toString();

		// Does the player have a CP?

		if(this.playerMap.containsKey(player_id)) {

			// If so, remove it.

			this.playerMap.remove(player_id);
			this.playerDB.removeKey(player_id);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + name + " / " + player_id + " removed from the CP database.");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " removed the CP of player " + name + " / " + player_id);

		} else {

			// Nothing found in this DB.

			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the CP database.");

		}

		// Does the player have points?

		if(this.pointMap.containsKey(player_id)) {

			// If so, remove it.

			this.pointDB.removeKey(player_id);
			this.pointMap.remove(player_id);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + name + " / " + player_id + " removed from the Points database.");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " removed the points of player " + name + " / " + player_id);

		} else {

			// Nothing found in this DB.

			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the Points database.");

		}

		// Does the player have a FFA history?

		if(this.ffaMap.containsKey(player_id)) {

			// If so, remove it.

			this.ffaDB.removeKey(player_id);
			this.ffaMap.remove(player_id);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + name + " / " + player_id + " removed from the FFA database.");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " removed the FFA history of player " + name + " / " + player_id);

		} else {

			// Nothing found in this DB.

			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the FFA database.");

		}

		return true;

	}

	// Remove a player's CP by UUID.

	public boolean removeid(Player player, String player_id) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get the player name for the UUID to remove. From cache or mojang.com.

		UUID player_uuid = UUID.fromString(player_id);
		String player_name = UUIDManager.getPlayerFromUUID(player_uuid);

		// Does the player have a CP?

		if(this.playerMap.containsKey(player_id)) {

			// If so, remove it.

			this.playerMap.remove(player_id);
			this.playerDB.removeKey(player_id);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + player_name + " / " + player_id + " removed from the CP database.");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " removed the CP of player " + player_name + " / " + player_id);

		} else {

			// Nothing found in this DB.

			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the CP database.");

		}

		// Does the player have points?

		if(this.pointMap.containsKey(player_id)) {

			// If so, remove it.

			this.pointDB.removeKey(player_id);
			this.pointMap.remove(player_id);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + player_name + " / " + player_id + " removed from the Points database.");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " removed the points of player " + player_name + " / " + player_id);

		} else {

			// Nothing found in this DB.

			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the Points database.");

		}

		// Does the player have a FFA history?

		if(this.ffaMap.containsKey(player_id)) {

			// If so, remove it.

			this.ffaDB.removeKey(player_id);
			this.ffaMap.remove(player_id);

			// Inform the player.

			player.sendMessage(ChatColor.DARK_AQUA + player_name + " / " + player_id + " removed from the FFA database.");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " removed the FFA history of player " + player_name + " / " + player_id);

		} else {

			// Nothing found in this DB.

			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the FFA database.");

		}

		return true;

	}	

	// Check the CP of a player.

	public boolean cp(Player player, String[] msg) {

		if(msg.length == 2) {

			// There is a second argument. Get the UUID for that name.

			String player_id = UUIDManager.getUUIDFromPlayer(msg[1]).toString();

			if(this.playerMap.containsKey(player_id))
				player.sendMessage(ChatColor.DARK_AQUA + msg[1] + " has a CP at " + this.playerMap.get(player_id));
			else
				player.sendMessage(ChatColor.DARK_AQUA + msg[1] + " has no CP.");

		} else {

			// No second argument. Get the UUID of the command sender.

			String player_id = UUIDManager.getUUIDFromPlayer(player.getName()).toString();

			if(this.playerMap.containsKey(player_id))
				player.sendMessage(ChatColor.DARK_AQUA + "You have a CP at " + this.playerMap.get(player_id));
			else
				player.sendMessage(ChatColor.DARK_AQUA + "You have no CP.");

		}

		return true;

	}

	// Check the points of a player.

	public boolean points(Player player, String[] msg) {

		if(msg.length == 2) {

			// There is a second argument. Get the UUID for that name.

			String player_id = UUIDManager.getUUIDFromPlayer(msg[1]).toString();

			if(this.pointMap.containsKey(player_id))
				player.sendMessage(ChatColor.DARK_AQUA + msg[1] + " has " + this.pointMap.get(player_id) + " points");
			else
				player.sendMessage(ChatColor.DARK_AQUA + msg[1] + " has no point");

		} else {

			// No second argument. Get the UUID of the command sender.

			String player_id = UUIDManager.getUUIDFromPlayer(player.getName()).toString();

			if(this.pointMap.containsKey(player_id))
				player.sendMessage(ChatColor.DARK_AQUA + "You have " + this.pointMap.get(player_id) + " points");
			else
				player.sendMessage(ChatColor.DARK_AQUA + "You have no points");

		}

		return true;

	}

	// v4 - List ffa levels completed.	

	public boolean ffa(Player player, String[] msg) {

		if(msg.length == 2) {

			// There is a second argument. Get the UUID for that name.

			String player_id = UUIDManager.getUUIDFromPlayer(msg[1]).toString();

			if(this.ffaMap.containsKey(player_id))
				player.sendMessage(ChatColor.DARK_AQUA + msg[1] + " completed : " + this.ffaMap.get(player_id));
			else
				player.sendMessage(ChatColor.DARK_AQUA + msg[1] + " did not complete a FFA level yet.");

		} else {

			// No second argument. Get the info of the command sender.

			String player_id = UUIDManager.getUUIDFromPlayer(player.getName()).toString();

			if(this.ffaMap.containsKey(player_id))
				player.sendMessage(ChatColor.DARK_AQUA + "You have completed : " + this.ffaMap.get(player_id));
			else
				player.sendMessage(ChatColor.DARK_AQUA + "You did not complete a FFA level yet.");			

		}

		return true;

	}	

	// Change a player's points.

	public boolean give(Player player, String name, String value) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get a UUID for the player. From cache or mojang.com.

		String player_id = UUIDManager.getUUIDFromPlayer(name).toString();

		// Change the player's points in the DB.

		this.pointDB.setString(player_id, value);
		this.pointMap.put(player_id, value);

		// Inform the player.

		player.sendMessage(ChatColor.DARK_AQUA + name + " / " + player_id + " has now " + value + " point(s).");

		// Let @Barrack know.

		System.out.println("[MinrCheckpoint] " + p + " gave " + name + " / " + player_id + " : " + value + " point(s)");

		return true;

	}
	
	// Change a UUID's points.

	public boolean giveid(Player player, String player_id, String value) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get the player name for the UUID. From cache or mojang.com.

		UUID player_uuid = UUID.fromString(player_id);
		String player_name = UUIDManager.getPlayerFromUUID(player_uuid);

		// Change the player's points in the DB.

		this.pointDB.setString(player_id, value);
		this.pointMap.put(player_id, value);

		// Inform the player.

		player.sendMessage(ChatColor.DARK_AQUA + player_name + " / " + player_id + " has now " + value + " point(s).");

		// Let @Barrack know.

		System.out.println("[MinrCheckpoint] " + p + " gave " + player_name + " / " + player_id + " " + value + " point(s)");

		return true;

	}

	// Wrong command? Halp.

	public boolean help(Player player) {

		if (player.isOp()) {

			// Show the commands available to Ops.

			player.sendMessage(ChatColor.GOLD + "MinrCheckpoint - Ops commands :");
			player.sendMessage(ChatColor.GOLD + "/checkpoint create <warp-name> <x> <y> <z>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint delete <warp-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint cp <player-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint ffa <player-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint points <player-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint give <player-name> <amount>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint giveid <UUID> <amount>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint remove <player-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint removeid <UUID>");

		} else {

			// Show the commands available to non-Ops.

			player.sendMessage(ChatColor.GOLD + "MinrCheckpoint - Commands :");
			player.sendMessage(ChatColor.GOLD + "/checkpoint cp <player-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint ffa <player-name>");
			player.sendMessage(ChatColor.GOLD + "/checkpoint points <player-name>");

		}

		return true;

	}

}