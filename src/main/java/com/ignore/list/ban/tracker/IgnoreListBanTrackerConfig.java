package com.ignore.list.ban.tracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ignoreListBanTracker")
public interface IgnoreListBanTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "showRemovedNames",
		name = "Show removed names",
		description = "Show the names of users removed from your ignore list"
	)
	default boolean showRemovedNames()
	{
		return false;
	}
}
