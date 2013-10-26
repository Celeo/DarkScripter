package com.darktidegames.celeo.scripter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.darktidegames.empyrean.C;

public class DarkScripter extends JavaPlugin implements Listener
{

	public List<Script> scripts = new ArrayList<Script>();
	public String p_use = "scripter.use";
	public Map<Player, String> pending = new HashMap<Player, String>();

	@Override
	public void onLoad()
	{
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		getLogger().info("Pre-enable setup complete");
	}

	@Override
	public void onEnable()
	{
		load();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable()
	{
		save();
		getServer().getScheduler().cancelTasks(this);
		getLogger().info("Disabled");
	}

	private void load()
	{
		scripts.clear();
		pending.clear();
		reloadConfig();
		p_use = getConfig().getString("permissions.use", "scripter.use");
		Script script = null;
		try
		{
			if (getConfig().isSet("scripts"))
			{
				for (String key : getConfig().getConfigurationSection("scripts").getValues(false).keySet())
				{
					script = new Script(this);
					script.setLocation(C.stringToLocation(key));
					script.setLines(getConfig().getStringList("scripts." + key));
					scripts.add(script);
				}
			}
		}
		catch (Exception e)
		{}
		getLogger().info("All data loaded");
	}

	private void save()
	{
		getConfig().set("permissions.use", p_use);
		getConfig().set("scripts", null);
		getConfig().set("scripts", "");
		for (Script script : scripts)
			getConfig().set("scripts." + C.locationToString(script.location), script.lines);
		saveConfig();
		getLogger().info("All data saved");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			if (args == null || args.length == 0)
				return false;
			if (args[0].equalsIgnoreCase("save"))
				save();
			else if (args[0].equalsIgnoreCase("reload"))
				load();
			else
				getLogger().info("/" + label + " [save|reload]");
			return true;
		}
		Player player = (Player) sender;
		if (!player.hasPermission(p_use))
			return false;
		if (args == null || args.length == 0)
			return false;
		String p = args[0].toLowerCase();
		if (p.equals("save"))
		{
			save();
			player.sendMessage("§aData saved");
			return true;
		}
		if (p.equals("reload"))
		{
			load();
			player.sendMessage("§aData reloaded");
			return true;
		}
		if (p.equals("stop"))
		{
			pending.remove(player);
			player.sendMessage("§7Pending action, if applicable, removed");
			return true;
		}
		if (p.equals("remove"))
		{
			pending.put(player, "-remove-");
			player.sendMessage("§7Right click the location");
			return true;
		}
		if (args.length == 1)
			return false;
		String string = "";
		for (int i = 0; i < args.length; i++)
		{
			if (string.equals(""))
				string = args[i];
			else
				string += " " + args[i];
		}
		pending.put(player, string);
		player.sendMessage("§7Right click the location");
		return true;
	}

	public static boolean hasPerm(Player player, String node)
	{
		if (!player.hasPermission(node))
		{
			player.sendMessage("§cYou cannot use that command");
			return false;
		}
		return true;
	}

	public Script getScriptFor(Location location)
	{
		for (Script script : scripts)
			if (script.location.equals(location))
				return script;
		Script script = new Script(this);
		script.setLocation(location);
		scripts.add(script);
		return script;
	}

	public boolean tryExecute(Player player, Location location)
	{
		for (Script script : scripts)
			if (script.tryExecute(player, location))
				return true;
		return false;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		tryExecute(event.getPlayer(), event.getTo());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (pending.containsKey(player))
		{
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				String line = pending.get(player);
				pending.remove(player);
				if (line.equals("-remove-"))
				{
					if (scripts.remove(getScriptFor(event.getClickedBlock().getLocation())))
						player.sendMessage("§aScript at that location removed");
					else
						player.sendMessage("§7There was no script at that location");
				}
				else
				{
					getScriptFor(event.getClickedBlock().getLocation()).addLine(line);
					player.sendMessage("§aData added");
				}
				return;
			}
		}
		if (event.getClickedBlock() == null)
			return;
		tryExecute(player, event.getClickedBlock().getLocation());
	}

}