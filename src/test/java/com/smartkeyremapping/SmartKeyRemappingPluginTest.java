package com.smartkeyremapping;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SmartKeyRemappingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SmartKeyRemappingPlugin.class);
		RuneLite.main(args);
	}
}