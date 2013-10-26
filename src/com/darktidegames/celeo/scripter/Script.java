package com.darktidegames.celeo.scripter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.darktidegames.empyrean.C;

public class Script
{

	private final DarkScripter plugin;
	public Location location;
	public List<String> lines;

	public Script(DarkScripter plugin)
	{
		this.plugin = plugin;
		this.location = new Location(plugin.getServer().getWorld("world"), 0, 0, 0);
		this.lines = new ArrayList<String>();
	}

	public String getLine(int lineNumber)
	{
		if (lineNumber > lines.size())
			return null;
		return lines.get(lineNumber);
	}

	public void addLine(String line)
	{
		lines.add(line);
	}

	public boolean tryExecute(Player player, Location location)
	{
		if (this.location == null)
			return false;
		if (!location.equals(this.location))
			return false;
		for (String line : lines)
		{
			if (isValidCommand(line))
				doLine(player, line);
			else
			{
				player.sendMessage("§cIncorrectly configured script at "
						+ C.locationToString(location) + " for line: " + line);
				return true;
			}
		}
		return true;
	}

	public static boolean isValidCommand(String line)
	{
		return line != null && line.contains(" ")
				&& Action.fromString(line.split(" ")[0]) != null
				&& line.contains(">");
	}

	private void doLine(Player player, String line)
	{
		Action command = Action.fromString(line.split(" ")[0]);
		String param = getParamFromLine(line);
		String target = getTargetFromLine(line);
		switch (command)
		{
		case CHANGEBLOCK:
			if (require(param, target))
				// TODO
				player.sendMessage("This is working, but not yet implemented");
			else
				plugin.getLogger().warning("Changeblock: Bad line: " + line);
			break;
		case EFFECT:
			if (require(param))
			{
				for (Effect effect : Effect.values())
					if (effect.name().equalsIgnoreCase(param))
					{
						location.getWorld().playEffect(location, effect, 1);
						return;
					}
				plugin.getLogger().warning("No effect found: " + param);
			}
			else
				plugin.getLogger().warning("Effect: Bad line: " + line);
			break;
		case MESSAGE:
			if (require(param))
				player.sendMessage(line.substring(line.indexOf(">") + 1, line.length()).replace("&", "§"));
			else
				plugin.getLogger().warning("Message: Bad line: " + line);
			break;
		case TP:
			if (require(param))
			{
				Location to = C.stringToLocation(param);
				if (to != null)
					player.teleport(to);
				else
					plugin.getLogger().warning("No location found: " + param);
			}
			else
				plugin.getLogger().warning("TP: Bad line: " + line);
			break;
		}
	}

	private boolean require(String... vars)
	{
		for (String str : vars)
			if (str == null)
				return false;
		return true;
	}

	private String getTargetFromLine(String line)
	{
		for (String str : line.split(" "))
			if (str.startsWith("@"))
				return str.replace("@", "");
		return null;
	}

	private String getParamFromLine(String line)
	{
		for (String str : line.split(" "))
			if (str.startsWith(">"))
				return str.replace(">", "");
		return null;
	}

	private enum Action
	{
		CHANGEBLOCK, TP, MESSAGE, EFFECT;
		public static Action fromString(String string)
		{
			for (Action action : Action.values())
				if (action.name().equalsIgnoreCase(string))
					return action;
			return null;
		}
	}

	public void clear()
	{
		lines.clear();
	}

	public int getLength()
	{
		return lines.size();
	}

	public void setLocation(Location location)
	{
		this.location = location;
	}

	public void setLines(List<String> lines)
	{
		this.lines = lines;
	}

	public DarkScripter getPlugin()
	{
		return plugin;
	}

}