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

	// Set standard tag.

	String stag = ChatColor.GRAY + "[" + ChatColor.WHITE + "MCP" + ChatColor.GRAY + "] ";

	// Set warning tag.

	String wtag = ChatColor.GRAY + "[" + ChatColor.YELLOW + "MCP" + ChatColor.GRAY + "] ";

	// Set error tag.

	String etag = ChatColor.GRAY + "[" + ChatColor.RED + "MCP" + ChatColor.GRAY + "] ";

	// Set HC completion tag

	String gtag = ChatColor.GRAY + "[" + ChatColor.GREEN + "MCP" + ChatColor.GRAY + "] ";

	// Set blue tag

	String btag = ChatColor.GRAY + "[" + ChatColor.AQUA + "MCP" + ChatColor.GRAY + "] ";
	
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

			else if(args[0].equalsIgnoreCase("convert"))
				return convert(player, args);

			else if((args[0].equalsIgnoreCase("convert")) && (sender.isOp()) && (args.length == 2))
				return create(player, args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));

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

			else if((args[0].equalsIgnoreCase("set")) && (sender.isOp()) && (args.length > 2))
				return set(player, args[1], args[2]);

			else if (args[0].length() < 1)
				return help(player);

			else
				return help(player);

		} catch(Exception e) {

			if(e instanceof NumberFormatException) {
				sender.sendMessage(etag + ChatColor.GRAY + "Coordinates are in the wrong format!");

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

		player.sendMessage(stag + ChatColor.GRAY + "Checkpoint " + name + " saved!");

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

			player.sendMessage(stag + ChatColor.GRAY + "Checkpoint " + name + " removed from database.");

			// Let @Barrack know.

			String p = player.getPlayerListName();
			System.out.println("[MinrCheckpoint] " + p + " deleted warp " + name);

		} else {

			// CP not found.

			player.sendMessage(ChatColor.RED + "No such checkpoint in the database.");

		}

		return true;
	}

	// Remove a player's CP by name.

	public boolean remove(Player player, String name) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get a UUID for the player to remove. From cache or mojang.com.

		UUID player_uuid = UUIDManager.getUUIDFromPlayer(name);
		if (player_uuid == null) {

			// Unknown player.

			player.sendMessage(ChatColor.RED + "Unknown Mojang player.");

		} else {

			// Convert UUID to string format.

			String player_id = player_uuid.toString();

			// Does the player have a CP?

			if(this.playerMap.containsKey(player_id)) {

				// If so, remove it.

				this.playerMap.remove(player_id);
				this.playerDB.removeKey(player_id);

				// Inform the player.

				player.sendMessage(stag + ChatColor.GRAY + name + " / " + player_id + " removed from the CP database.");

				// Let @Barrack know.

				System.out.println("[MinrCheckpoint] " + p + " removed the CP of player " + name + " / " + player_id);

			} else {

				// Nothing found in this DB.

				player.sendMessage(etag + ChatColor.GRAY + "No such player in the CP database.");

			}

			// Does the player have points?

			if(this.pointMap.containsKey(player_id)) {

				// If so, remove it.

				this.pointDB.removeKey(player_id);
				this.pointMap.remove(player_id);

				// Inform the player.

				player.sendMessage(stag + ChatColor.GRAY + name + " / " + player_id + " removed from the Points database.");

				// Let @Barrack know.

				System.out.println("[MinrCheckpoint] " + p + " removed the points of player " + name + " / " + player_id);

			} else {

				// Nothing found in this DB.

				player.sendMessage(etag + ChatColor.GRAY + "No such player in the Points database.");

			}

			// Does the player have a FFA history?

			if(this.ffaMap.containsKey(player_id)) {

				// If so, remove it.

				this.ffaDB.removeKey(player_id);
				this.ffaMap.remove(player_id);

				// Inform the player.

				player.sendMessage(stag + ChatColor.GRAY + name + " / " + player_id + " removed from the FFA database.");

				// Let @Barrack know.

				System.out.println("[MinrCheckpoint] " + p + " removed the FFA history of player " + name + " / " + player_id);

			} else {

				// Nothing found in this DB.

				player.sendMessage(etag + ChatColor.GRAY + "No such player in the FFA database.");

			}

		}

		return true;

	}

	// Remove a player's CP by UUID.

	public boolean removeid(Player player, String player_id) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get the player name for the UUID to remove. From cache or mojang.com.

		if (!isValid(player_id)) {

			// Does it look like a valid UUID? If not, abort.

			player.sendMessage(etag + ChatColor.GRAY + "Bad UUID format.");

			return true;

		}

		UUID player_uuid = UUID.fromString(player_id);
		String player_name = UUIDManager.getPlayerFromUUID(player_uuid);

		if (player_name == null) {

			// Unknown player.

			player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang UUID.");

		} else {

			// Does the player have a CP?
	
			if(this.playerMap.containsKey(player_id)) {
	
				// If so, remove it.
	
				this.playerMap.remove(player_id);
				this.playerDB.removeKey(player_id);
	
				// Inform the player.
	
				player.sendMessage(stag + ChatColor.GRAY + player_name + " / " + player_id + " removed from the CP database.");
	
				// Let @Barrack know.
	
				System.out.println("[MinrCheckpoint] " + p + " removed the CP of player " + player_name + " / " + player_id);
	
			} else {
	
				// Nothing found in this DB.
	
				player.sendMessage(etag + ChatColor.GRAY + "No such player in the CP database.");
	
			}
	
			// Does the player have points?
	
			if(this.pointMap.containsKey(player_id)) {
	
				// If so, remove it.
	
				this.pointDB.removeKey(player_id);
				this.pointMap.remove(player_id);
	
				// Inform the player.
	
				player.sendMessage(stag + ChatColor.GRAY + player_name + " / " + player_id + " removed from the Points database.");
	
				// Let @Barrack know.
	
				System.out.println("[MinrCheckpoint] " + p + " removed the points of player " + player_name + " / " + player_id);
	
			} else {
	
				// Nothing found in this DB.
	
				player.sendMessage(etag + ChatColor.GRAY + "No such player in the Points database.");
	
			}
	
			// Does the player have a FFA history?
	
			if(this.ffaMap.containsKey(player_id)) {
	
				// If so, remove it.
	
				this.ffaDB.removeKey(player_id);
				this.ffaMap.remove(player_id);
	
				// Inform the player.
	
				player.sendMessage(stag + ChatColor.GRAY + player_name + " / " + player_id + " removed from the FFA database.");
	
				// Let @Barrack know.
	
				System.out.println("[MinrCheckpoint] " + p + " removed the FFA history of player " + player_name + " / " + player_id);
	
			} else {
	
				// Nothing found in this DB.
	
				player.sendMessage(etag + ChatColor.GRAY + "No such player in the FFA database.");
	
			}

		}

		return true;

	}	

	// Check the CP of a player.

	public boolean cp(Player player, String[] msg) {

		if(msg.length == 2) {

			// There is a second argument. Get the UUID for that name.

			UUID player_uuid = UUIDManager.getUUIDFromPlayer(msg[1]);

			if (player_uuid == null) {

				// Unknown player.

				player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang player.");

			} else {

				// We have an ID. Looking it up in our files.

				String player_id = player_uuid.toString();

				if(this.playerMap.containsKey(player_id))
					player.sendMessage(stag + ChatColor.GRAY + msg[1] + " has a CP at " + this.playerMap.get(player_id));
				else
					player.sendMessage(stag + ChatColor.GRAY + msg[1] + " has no CP.");

			}

		} else {

			// No second argument. Get the UUID of the command sender.

			String player_id = UUIDManager.getUUIDFromPlayer(player.getName()).toString();

			if(this.playerMap.containsKey(player_id))
				player.sendMessage(stag + ChatColor.GRAY + "You have a CP at " + this.playerMap.get(player_id));
			else
				player.sendMessage(stag + ChatColor.GRAY + "You have no CP.");

		}

		return true;

	}

	// Check the points of a player.

	public boolean points(Player player, String[] msg) {

		if(msg.length == 2) {

			// There is a second argument. Get the UUID for that name.

			UUID player_uuid = UUIDManager.getUUIDFromPlayer(msg[1]);

			if (player_uuid == null) {

				// Unknown player.

				player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang player.");

			} else {

				// We have an ID. Looking it up in our files.

				String player_id = player_uuid.toString();

				if(this.pointMap.containsKey(player_id))
					player.sendMessage(stag + ChatColor.GRAY + msg[1] + " has " + this.pointMap.get(player_id) + " points");
				else
					player.sendMessage(stag + ChatColor.GRAY + msg[1] + " has no point");

			}

		} else {

			// No second argument. Get the UUID of the command sender.

			String player_id = UUIDManager.getUUIDFromPlayer(player.getName()).toString();

			if(this.pointMap.containsKey(player_id))
				player.sendMessage(stag + ChatColor.GRAY + "You have " + this.pointMap.get(player_id) + " points");
			else
				player.sendMessage(stag + ChatColor.GRAY + "You have no points");

		}

		return true;

	}

	// v4 - List ffa levels completed.	

	public boolean ffa(Player player, String[] msg) {

		if(msg.length == 2) {

			// There is a second argument. Get the UUID for that name.

			UUID player_uuid = UUIDManager.getUUIDFromPlayer(msg[1]);

			if (player_uuid == null) {

				// Unknown player.

				player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang player.");

			} else {

				// We have an ID. Looking it up in our files.

				String player_id = player_uuid.toString();

				if(this.ffaMap.containsKey(player_id))
					player.sendMessage(stag + ChatColor.GRAY + msg[1] + " completed : " + this.ffaMap.get(player_id));
				else
					player.sendMessage(stag + ChatColor.GRAY + msg[1] + " did not complete a FFA level yet.");

			}

		} else {

			// No second argument. Get the info of the command sender.

			String player_id = UUIDManager.getUUIDFromPlayer(player.getName()).toString();

			if(this.ffaMap.containsKey(player_id))
				player.sendMessage(stag + ChatColor.GRAY + "You have completed : " + this.ffaMap.get(player_id));
			else
				player.sendMessage(stag + ChatColor.GRAY + "You did not complete a FFA level yet.");			

		}

		return true;

	}	

	// Change a player's points.

	public boolean give(Player player, String name, String value) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get a UUID for the player. From cache or mojang.com.

		UUID player_uuid = UUIDManager.getUUIDFromPlayer(name);

		if (player_uuid == null) {

			// Unknown player.

			player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang player.");

		} else {

			// Change the player's points in the DB.

			String player_id = player_uuid.toString();

			this.pointDB.setString(player_id, value);
			this.pointMap.put(player_id, value);

			// Inform the player.

			player.sendMessage(stag + ChatColor.GRAY + name + " / " + player_id + " has now " + value + " point(s).");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " gave " + name + " / " + player_id + " : " + value + " point(s)");

		}

		return true;

	}

	// Change a UUID's points.

	public boolean giveid(Player player, String player_id, String value) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get the player name for the UUID. From cache or mojang.com.

		if (!isValid(player_id)) {

			// Does it look like a valid UUID? If not, abort.

			player.sendMessage(etag + ChatColor.GRAY + "Bad UUID format.");

			return true;

		}

		UUID player_uuid = UUID.fromString(player_id);
		String player_name = UUIDManager.getPlayerFromUUID(player_uuid);

		if (player_name == null) {

			// Unknown player.

			player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang UUID.");

		} else {

			// Change the player's points in the DB.

			this.pointDB.setString(player_id, value);
			this.pointMap.put(player_id, value);

			// Inform the player.

			player.sendMessage(stag + ChatColor.GRAY + player_name + " / " + player_id + " has now " + value + " point(s).");

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + p + " gave " + player_name + " / " + player_id + " " + value + " point(s)");

		}

		return true;

	}

	// Change a player's CP.

	public boolean set(Player player, String name, String value) {

		// Get the player name of the command sender.

		String p = player.getPlayerListName();

		// Get a UUID for the player. From cache or mojang.com.

		UUID player_uuid = UUIDManager.getUUIDFromPlayer(name);

		if (player_uuid == null) {

			// Unknown player.

			player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang player.");

		} else {

			String player_id = player_uuid.toString();

			// Check if the checkpoint on the 3rd line is in the database

			if(this.signMap.containsKey(value)) {

				// Check if the player has a CP.

				if(this.playerMap.containsKey(player_id)) {

					String saved_cp = this.playerMap.get(player_id);

					if(saved_cp.equalsIgnoreCase(value)) {

						// If the player already set this CP:

						player.sendMessage(wtag + ChatColor.GRAY + "Player already has this checkpoint.");

					} else {

						// If the player has another CP, remove it.

						this.playerMap.remove(saved_cp);

					}

				}

				// Set a new CP for this player.

				this.playerMap.put(player_id, value);
				this.playerDB.setString(player_id, value);

				// Inform the player.

				player.sendMessage(stag + ChatColor.GRAY + name + " / " + player_id + " has a new CP at " + value);

				// Let @Barrack know.

				System.out.println("[MinrCheckpoint] " + p + " set the CP of " + name + " / " + player_id + " at : " + value);

			} else {

				// Checkpoint not found in files.

				player.sendMessage(etag + ChatColor.GRAY + "Unknown checkpoint.");

			}

		}

		return true;

	}

	// Wrong command? Halp.

	public boolean help(Player player) {

		if (player.isOp()) {

			// Show the commands available to Ops.

			player.sendMessage(wtag + ChatColor.GRAY + "MinrCheckpoint - Ops commands :");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint create " + ChatColor.GRAY + "<warp-name> <x> <y> <z>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint delete " + ChatColor.GRAY + "<warp-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint cp " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint ffa " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint points " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint give " + ChatColor.GRAY + "<player-name> <amount>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint giveid " + ChatColor.GRAY + "<UUID> <amount>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint set " + ChatColor.GRAY + "<player-name> <checkpoint>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint remove " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint removeid " + ChatColor.GRAY + "<UUID>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint convert " + ChatColor.GRAY + "<playername>");

		} else {

			// Show the commands available to non-Ops.

			player.sendMessage(wtag + ChatColor.GRAY + "MinrCheckpoint - Commands :");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint cp " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint ffa " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(stag + ChatColor.WHITE + "/checkpoint points " + ChatColor.GRAY + "<player-name>");
			player.sendMessage(wtag + ChatColor.WHITE + "In case you lost your points or ffa history :");			
			player.sendMessage(wtag + ChatColor.WHITE + "/checkpoint convert");

		}

		return true;

	}

	// Is it a valid UUID ?

	public boolean isValid(String uuid){
		return uuid.matches("[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");
	}

	// Convert old players data.

	public boolean convert(Player player, String[] msg){

		String name = null;
		String player_string = null;

		if(msg.length == 1) {

			// No player specified. Looking up the command sender.
			
			UUID player_id = player.getUniqueId();
			player_string = player_id.toString();
			name = player.getName();

		} else if( (msg.length  == 2) && (player.isOp()) ) {

			// Command sender is Op and there is a player specified.
			
			name = msg[1];

			UUID player_uuid = UUIDManager.getUUIDFromPlayer(name);

			if (player_uuid == null) {

				// Unknown player.

				player.sendMessage(etag + ChatColor.GRAY + "Unknown Mojang player.");

				// Abort.

				return true;

			} else {

				// Getting the UUID in string format.

				player_string = player_uuid.toString();

			}

		} else {

			// ^.^

			help(player);

			// Abort.

			return true;

		}

		// Starting conversion.

		player.sendMessage(stag + ChatColor.GRAY + "Starting Converter for :");
		player.sendMessage(stag + ChatColor.GRAY + name + " - " + player_string);

		// Checking the CP DB.

		if(this.playerMap.containsKey(name)) {

			// Convert.

			String obj = this.playerMap.get(name);

			this.playerMap.put(player_string, obj);
			this.playerDB.setString(player_string, obj);
			this.playerMap.remove(name);
			this.playerDB.removeKey(name);

			// Inform the player.

			if(msg.length == 1) {
				player.sendMessage(stag + ChatColor.GRAY + "Your CP has been updated to the new format.");
			} else {
				player.sendMessage(stag + ChatColor.GRAY + "Player updated to the new CP format:");
				player.sendMessage(stag + ChatColor.GRAY + name + " - " + player_string);
			}

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + player.getName() + " updated the CP of " + name + " / " + player_string);

		}

		if(this.pointMap.containsKey(name)) {

			// Convert.

			String obj = this.pointMap.get(name);

			this.pointMap.put(player_string, obj);
			this.pointDB.setString(player_string, obj);
			this.pointMap.remove(name);
			this.pointDB.removeKey(name);

			// Inform the player.

			if(msg.length == 1) {
				player.sendMessage(stag + ChatColor.GRAY + "Your points have been updated to the new format.");
			} else {
				player.sendMessage(stag + ChatColor.GRAY + "Player updated to the new points format:");
				player.sendMessage(stag + ChatColor.GRAY + name + " - " + player_string);
			}

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + player.getName() + " updated the Points of " + name + " / " + player_string);

		}

		if(this.ffaMap.containsKey(name)) {

			// Convert.

			String obj = this.ffaMap.get(name);

			this.ffaMap.put(player_string, obj);
			this.ffaDB.setString(player_string, obj);
			this.ffaMap.remove(name);
			this.ffaDB.removeKey(name);

			// Inform the player.

			if(msg.length == 1) {
				player.sendMessage(stag + ChatColor.GRAY + "Your FFA history has been updated to the new format.");
			} else {
				player.sendMessage(stag + ChatColor.GRAY + "Player updated to the new FFA history format:");
				player.sendMessage(stag + ChatColor.GRAY + name + " - " + player_string);
			}

			// Let @Barrack know.

			System.out.println("[MinrCheckpoint] " + player.getName() + " updated the FFA history of " + name + " / " + player_string);

		}

		player.sendMessage(stag + ChatColor.GRAY + "All done. Ready to go.");

		return true;

	}

}