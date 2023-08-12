package com.ignore.list.ban.tracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class IgnoreListBanTrackerTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(IgnoreListBanTrackerPlugin.class);
		RuneLite.main(args);
	}
}