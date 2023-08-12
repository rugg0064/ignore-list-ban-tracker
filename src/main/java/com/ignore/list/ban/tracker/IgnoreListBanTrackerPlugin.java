package com.ignore.list.ban.tracker;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;

@Slf4j
@PluginDescriptor(
	name = "Ignore List Ban Tracker",
	description = "See how many of your reports have resulted in action",
	tags = {"Reports", "Ban", "Ignore", "Mute"}
)
public class IgnoreListBanTrackerPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ConfigManager configManager;
	@Inject private ColorPickerManager colorPickerManager;
	@Inject private Notifier notifier;
	@Inject private ChatMessageManager chatMessageManager;

	private static final String CONFIG_GROUP = "ignoreListBanTracker";
	private static final String CONFIG_KEY = "lastIgnoreListSize";

	private int checkIgnoreListCountdown;
	private boolean hasSeenLoginScreenSinceLastCheck;

	@Override
	protected void startUp() throws Exception
	{
		checkIgnoreListCountdown = -1;
		hasSeenLoginScreenSinceLastCheck = client.getGameState() == GameState.LOGIN_SCREEN;
		System.out.println(client.getGameState());
	}

	// Returns true if and only if the setting is enabled to remove banned players
	private boolean getBannedRemovalSetting() {
		return client.getVarbitValue(8059) == 0;
	}

	// Returns true if and only if the setting is enabled to remove muted players
	private boolean getMutedRemovalSetting() {
		return client.getVarbitValue(8060) == 1;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		int ignoreCount = client.getIgnoreContainer().getCount();
		Integer lastIgnoreCount = configManager.getRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY, Integer.class);
		if(checkIgnoreListCountdown == 0) {
			if(lastIgnoreCount != null) {
				if(ignoreCount < lastIgnoreCount) {
					final int sizeChange = lastIgnoreCount - ignoreCount;
					String actionString;
					boolean ban = getBannedRemovalSetting();
					boolean mute = getMutedRemovalSetting();
					if(ban && mute) {
						actionString = "banned/muted";
					} else if(ban) {
						actionString = "banned";
					} else if(mute) {
						actionString = "muted";
					} else {
						actionString = "removed";
					}
					final String message = String.format("%d %s from your ignore list %s been %s.", sizeChange, sizeChange == 1 ? "user" : "users", sizeChange == 1 ? "has" : "have", actionString);
					sendMessage(message);
				}
			}
		}
		if(checkIgnoreListCountdown >= -1) {
			checkIgnoreListCountdown--;
		}
		if(checkIgnoreListCountdown == -1 && (lastIgnoreCount == null || lastIgnoreCount != ignoreCount)) {
			configManager.setRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY, ignoreCount);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		GameState currentGameState = gameStateChanged.getGameState();
		// When you log in, check the ignore list
		if(
				hasSeenLoginScreenSinceLastCheck &&
				currentGameState == GameState.LOGGED_IN
		) {
			// If you just on the login screen and signed in, start the check timer
			checkIgnoreListCountdown = 3;
			hasSeenLoginScreenSinceLastCheck = false;
		} else if (currentGameState == GameState.LOGIN_SCREEN) {
			// If on login screen, enable checking later
			hasSeenLoginScreenSinceLastCheck = true;
		}
	}

	// Sends a stylized red text message to the game chat tab
	private void sendMessage(String message) {
		final String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(message)
			.build();
		chatMessageManager.queue(
			QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build()
		);
	}
}