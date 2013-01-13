package org.minr.Zaraza107.MinrCheckpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	
	public void onEnable() {
    	this.server = this.getServer();
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] (version " + pdfFile.getVersion() + ") is enabled!");
        
        /* Nickman's new code */
        new BlockListener(this);
        new PlayerListener(this);
        
        //Do we need to actually do this???
        mBPermissionsApi = getServer().getPluginManager().getPlugin("bPermissions"); //Check to see if the plugin is available
        /* END */
        
        
        //this.server.getPluginManager().registerEvent(org.bukkit.event.block.SignChangeEvent.class, blockListener, org.bukkit.event.EventPriority.MONITOR, this);
        //this.server.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Monitor, this);
        //this.server.getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Monitor, this);
        
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
	
	public void onDisable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] (version " + pdfFile.getVersion() + ") disabled. :(");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if(sender.isOp() && sender instanceof Player) {
				Player player = (Player)sender;
				if(args[0].equalsIgnoreCase("create"))
					return create(player, args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
				else if(args[0].equalsIgnoreCase("delete"))
					return delete(player, args[1]);
				else if(args[0].equalsIgnoreCase("remove"))
					return remove(player, args[1]);
				else if(args[0].equalsIgnoreCase("points"))
					return points(player, args);
				else if(args[0].equalsIgnoreCase("ffa"))
					return ffa(player, args);				
				else if(args[0].equalsIgnoreCase("give"))
					return give(player, args[1], args[2]);
				else
					return false;
			} else
				return false;
		} catch(Exception e) {
			if(e instanceof NumberFormatException) {
				sender.sendMessage(ChatColor.RED + "Coordinates are in wrong format!");
			}
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean create(Player player, String name, double x, double y, double z) {
		if(this.signMap.containsKey(name)) {
			this.signMap.remove(name);
			this.signDB.removeKey(name);
		}
			
		this.signMap.put(name, player.getWorld().getName() + "," + x + "," + y + "," + z);
		this.signDB.setString(name, player.getWorld().getName() + "," + x + "," + y + "," + z);
		
		System.out.println(name + "," + player.getWorld().getName() + "," + x + "," + y + "," + z);
			
		player.sendMessage(ChatColor.DARK_GREEN + "Checkpoint saved!");
		
		return true;
	}
	
	public boolean delete(Player player, String name) {
		if(this.signMap.containsKey(name)) {
			this.signMap.remove(name);
			this.signDB.removeKey(name);
			player.sendMessage(ChatColor.DARK_AQUA + "Checkpoint removed from database :)");
		} else {
			player.sendMessage(ChatColor.DARK_AQUA + "No such checkpoint in the database.");
		}
		
		return true;
	}
	
	public boolean remove(Player player, String name) {
		if(this.playerMap.containsKey(name)) {
			this.playerMap.remove(name);
			this.playerDB.removeKey(name);
			this.pointDB.removeKey(name);
			this.pointMap.remove(name);
			this.ffaDB.removeKey(name);
			this.ffaMap.remove(name);
			player.sendMessage(ChatColor.DARK_AQUA + "Player removed from database :)");
		} else {
			player.sendMessage(ChatColor.DARK_AQUA + "No such player in the database.");
		}
		
		return true;
	}
	
	public boolean points(Player player, String[] msg) {
		if(player.isOp() && msg.length > 1) {
			//Nickman Get all possible matches, show when there are multiple options
			ArrayList<String> matchedNames = matchPlayerNames(msg[1], this.pointMap.keySet());
			
			if (matchedNames.size() == 1) {
				String currName = matchedNames.get(0); //Retrieve the actual name
				if(this.pointMap.containsKey(currName))
					player.sendMessage(ChatColor.DARK_AQUA + "Player " + currName + " has " + this.pointMap.get(currName) + " Maze Points");
				else
					player.sendMessage(ChatColor.DARK_AQUA + "Player " + currName + " has no Maze Point");
			} else if (matchedNames.size() == 0) {
				player.sendMessage(ChatColor.DARK_AQUA + "No players found that matched " + msg[1] + ".");
			} else {
				player.sendMessage(ChatColor.DARK_AQUA + "Following players matched your search, please retry:");
				for (int i = 0; i < matchedNames.size(); ++i) {
					player.sendMessage(ChatColor.GRAY + "- " + matchedNames.get(i));
				}
			}
			//Nickman end
		}
		if(!player.isOp() && this.playerMap.containsKey(player.getName())) {
			if(this.pointMap.containsKey(player.getName()))
				player.sendMessage(ChatColor.DARK_AQUA + "You have " + this.pointMap.get(player.getName()) + " Maze Points");
			else
				player.sendMessage(ChatColor.DARK_AQUA + "You have no Maze Points");
		}
		return true;
	}

	/* v4 - c_dric new list ffa levels completed  */	
	public boolean ffa(Player player, String[] msg) {
		if(player.isOp() && msg.length > 1) {
			//Nickman Get all possible matches, show when there are multiple options
			ArrayList<String> matchedNames = matchPlayerNames(msg[1], this.ffaMap.keySet());
			
			if (matchedNames.size() == 1) {
				String currName = matchedNames.get(0); //Retrieve the actual name
				if(this.ffaMap.containsKey(currName))
					player.sendMessage(ChatColor.DARK_AQUA + "Player " + currName + " completed : " + this.ffaMap.get(currName));
				else
					player.sendMessage(ChatColor.DARK_AQUA + "Player " + currName + " did not complete a FFA level yet.");
			} else if (matchedNames.size() == 0) {
				player.sendMessage(ChatColor.DARK_AQUA + "No players found that matched " + msg[1] + ".");
			} else {
				player.sendMessage(ChatColor.DARK_AQUA + "Following players matched your search, please retry:");
				for (int i = 0; i < matchedNames.size(); ++i) {
					player.sendMessage(ChatColor.GRAY + "- " + matchedNames.get(i));
				}
			}
			//Nickman end
		}
		return true;
	}	
	
	public boolean give(Player player, String name, String value) {
		this.pointDB.setString(name, value);
		this.pointMap.put(name, value);
		return true;
	}
	
	/* Nickman's new code */
	private static ArrayList<String> matchPlayerNames(String input, Set<String> names) { //Nickman get a list of (partially) matching users
		ArrayList<String> matches = new ArrayList<String>();
		
		if (names.contains(input)) {
			matches.add(input); //We have an exact match, use this one!
		} else {
			//Check for all possible matches to see if we can identify a single one
			String filter = "(?i).*?"+input+".*"; //Create a filter where there can be 0-unlimited characters in front of the input, and 0 to unlimited characters behind the input
			for (Iterator<String> i = names.iterator(); i.hasNext();) {
				String currName = i.next();
				if (currName.matches(filter)) matches.add(currName);
			}
		}
		
		return matches;
	}
}