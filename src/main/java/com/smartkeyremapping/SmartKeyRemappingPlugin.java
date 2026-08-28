package com.smartkeyremapping;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@PluginDescriptor(
		name = "Smart key remapping",
		description = "Key remapping with controllable chat focus",
		tags = {"chat", "unfocus", "key remapping", "input"}
)
public class SmartKeyRemappingPlugin extends Plugin implements KeyListener
{
	private static final FontRenderContext DUMMY_FRC = new FontRenderContext(null, true, true);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SmartKeyRemappingOverlay overlay;

	@Inject
	private SmartKeyRemappingConfig config;

	@Inject
	private PluginManager pluginManager;

	private boolean isTyping = false;
	private boolean isPeekingDraft = false;
	private boolean remappingWarningShown = false;
	private String cachedPlayerPrefix = "";
	private final Map<Integer, Integer> remappedKeys = new HashMap<>();
	private final Set<Character> blockedCharacters = new HashSet<>();

	public boolean isTyping()
	{
		return isTyping;
	}

	private final MouseAdapter mouseAdapter = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e) && !client.isMenuOpen() && isChatInputAvailable())
			{
				net.runelite.api.Point canvasPoint = client.getMouseCanvasPosition();
				if (canvasPoint != null)
				{
					Point pt = new Point(canvasPoint.getX(), canvasPoint.getY());
					Rectangle inputArea = getInputAreaBounds();

					if (!isTyping && config.clickToFocus() && inputArea != null && inputArea.contains(pt))
					{
						startTyping();
						e.consume();
						return e;
					}

					if (isTyping)
					{
						checkAndUnfocus(pt);
					}
				}
			}
			return e;
		}
	};

	@Provides
	SmartKeyRemappingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SmartKeyRemappingConfig.class);
	}

	@Override
	protected void startUp()
	{
		mouseManager.registerMouseListener(mouseAdapter);
		keyManager.registerKeyListener(this);
		overlayManager.add(overlay);
		isTyping = false;
		isPeekingDraft = false;
		remappingWarningShown = false;
		remappedKeys.clear();
		blockedCharacters.clear();
		warnAboutEnabledRemappingPlugins();
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				lockChat();
			}
		});
	}

	@Override
	protected void shutDown()
	{
		mouseManager.unregisterMouseListener(mouseAdapter);
		keyManager.unregisterKeyListener(this);
		overlayManager.remove(overlay);
		isPeekingDraft = false;
		remappingWarningShown = false;
		remappedKeys.clear();
		blockedCharacters.clear();
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				unlockChat();
			}
		});
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		if (event.isLoaded())
		{
			warnIfRemappingPlugin(event.getPlugin());
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		switch (event.getEventName())
		{
			case "setChatboxInput":
				if (!isTyping)
				{
					lockChat();
				}
				break;
			case "blockChatInput":
				if (!isTyping)
				{
					int[] intStack = client.getIntStack();
					intStack[client.getIntStackSize() - 1] = 1;
				}
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onPostClientTick(PostClientTick event)
	{
		if (!isTyping
				&& client.getGameState() == GameState.LOGGED_IN
				&& (chatboxFocused() || isBankPinOpen()))
		{
			lockChat();
		}
	}

	private void startTyping()
	{
		isTyping = true;
		isPeekingDraft = false;
		clientThread.invoke(this::unlockChat);
	}

	private void stopTyping(boolean clearText)
	{
		isTyping = false;
		isPeekingDraft = false;
		clientThread.invoke(() ->
		{
			if (clearText)
			{
				client.setVarcStrValue(VarClientID.CHATINPUT, "");
			}
			lockChat();
		});
	}

	private void lockChat()
	{
		Widget inputWidget = client.getWidget(InterfaceID.Chatbox.INPUT);
		if (inputWidget != null)
		{
			String input;
			if (isPeekingDraft)
			{
				input = ColorUtil.wrapWithColorTag(
						client.getVarcStrValue(VarClientID.CHATINPUT),
						getTypedTextColor()
				);
			}
			else if (config.showNotTypingStatus())
			{
				input = ColorUtil.wrapWithColorTag(config.notTypingText(), config.notTypingColor());
			}
			else
			{
				input = ColorUtil.wrapWithColorTag(
						client.getVarcStrValue(VarClientID.CHATINPUT),
						getTypedTextColor()
				);
			}
			setChatboxWidgetInput(inputWidget, input);
		}
	}

	private void unlockChat()
	{
		Widget inputWidget = client.getWidget(InterfaceID.Chatbox.INPUT);
		if (inputWidget == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		String typedText = client.getVarcStrValue(VarClientID.CHATINPUT);
		setChatboxWidgetInput(inputWidget, ColorUtil.wrapWithColorTag(typedText + "*", getTypedTextColor()));
	}

	private Color getTypedTextColor()
	{
		boolean transparent = client.isResized()
				&& client.getVarbitValue(VarbitID.CHATBOX_TRANSPARENCY) == 1;
		return transparent
				? JagexColors.CHAT_TYPED_TEXT_TRANSPARENT_BACKGROUND
				: JagexColors.CHAT_TYPED_TEXT_OPAQUE_BACKGROUND;
	}

	private void setChatboxWidgetInput(Widget inputWidget, String input)
	{
		String text = inputWidget.getText();
		if (text != null && text.contains(":"))
		{
			cachedPlayerPrefix = text.substring(0, text.indexOf(":") + 1);
		}

		if (!cachedPlayerPrefix.isEmpty())
		{
			String newText = cachedPlayerPrefix + " " + input;
			if (!newText.equals(text))
			{
				inputWidget.setText(newText);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		if (isPeekingDraft)
		{
			e.consume();
			return;
		}

		char keyChar = e.getKeyChar();
		if (keyChar != KeyEvent.CHAR_UNDEFINED
				&& blockedCharacters.contains(keyChar)
				&& chatboxFocused())
		{
			e.consume();
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (!chatboxFocused())
		{
			return;
		}

		if (!isTyping && !isDialogOpen() && config.draftPeekKey().matches(e))
		{
			isPeekingDraft = true;
			clientThread.invoke(this::lockChat);
			e.consume();
			return;
		}

		if (isTyping)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				stopTyping(true);
				e.consume();
			}
			else if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if (!config.stayFocusedAfterSend())
				{
					stopTyping(false);
				}
			}
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_ENTER && !isDialogOpen())
		{
			startTyping();
			e.consume();
			return;
		}

		int originalKeyCode = e.getKeyCode();
		char originalKeyChar = e.getKeyChar();
		int remappedKeyCode = getRemappedKeyCode(e, !isDialogOpen());
		if (remappedKeyCode != KeyEvent.VK_UNDEFINED && remappedKeyCode != originalKeyCode)
		{
			remappedKeys.put(originalKeyCode, remappedKeyCode);
			e.setKeyCode(remappedKeyCode);
			e.setKeyChar(KeyEvent.CHAR_UNDEFINED);
			if (originalKeyChar != KeyEvent.CHAR_UNDEFINED)
			{
				blockedCharacters.add(originalKeyChar);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (isPeekingDraft && config.draftPeekKey().matches(e))
		{
			isPeekingDraft = false;
			clientThread.invoke(this::lockChat);
			e.consume();
			return;
		}

		char keyChar = e.getKeyChar();
		if (keyChar != KeyEvent.CHAR_UNDEFINED)
		{
			blockedCharacters.remove(keyChar);
		}

		Integer remappedKeyCode = remappedKeys.remove(e.getKeyCode());
		if (remappedKeyCode != null)
		{
			e.setKeyCode(remappedKeyCode);
			e.setKeyChar(KeyEvent.CHAR_UNDEFINED);
		}
	}

	private void warnAboutEnabledRemappingPlugins()
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (pluginManager.isPluginEnabled(plugin))
			{
				warnIfRemappingPlugin(plugin);
			}
		}
	}

	private void warnIfRemappingPlugin(Plugin plugin)
	{
		if (plugin == this || remappingWarningShown)
		{
			return;
		}

		PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
		if (descriptor == null)
		{
			return;
		}

		String name = descriptor.name().toLowerCase(Locale.ENGLISH);
		String description = descriptor.description().toLowerCase(Locale.ENGLISH);
		String tags = String.join(" ", descriptor.tags()).toLowerCase(Locale.ENGLISH);
		boolean remapsKeys = name.contains("remap")
				|| description.contains("key remap")
				|| description.contains("remap key")
				|| (description.contains("remapping") && description.contains("key"))
				|| tags.contains("key remap")
				|| tags.contains("keyboard remap");
		if (remapsKeys)
		{
			remappingWarningShown = true;
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
					client.getCanvas(),
					"Please disable other key-remapping plugins to avoid any compatibility issues.",
					"Smart key-remapping compatibility warning",
					JOptionPane.WARNING_MESSAGE
			));
		}
	}

	private int getRemappedKeyCode(KeyEvent e, boolean allowFKeyRemaps)
	{
		if (config.remapCamera())
		{
			if (config.up().matches(e)) { return KeyEvent.VK_UP; }
			if (config.down().matches(e)) { return KeyEvent.VK_DOWN; }
			if (config.left().matches(e)) { return KeyEvent.VK_LEFT; }
			if (config.right().matches(e)) { return KeyEvent.VK_RIGHT; }
		}

		if (allowFKeyRemaps && config.remapFKeys())
		{
			if (config.f1().matches(e)) { return KeyEvent.VK_F1; }
			if (config.f2().matches(e)) { return KeyEvent.VK_F2; }
			if (config.f3().matches(e)) { return KeyEvent.VK_F3; }
			if (config.f4().matches(e)) { return KeyEvent.VK_F4; }
			if (config.f5().matches(e)) { return KeyEvent.VK_F5; }
			if (config.f6().matches(e)) { return KeyEvent.VK_F6; }
			if (config.f7().matches(e)) { return KeyEvent.VK_F7; }
			if (config.f8().matches(e)) { return KeyEvent.VK_F8; }
			if (config.f9().matches(e)) { return KeyEvent.VK_F9; }
			if (config.f10().matches(e)) { return KeyEvent.VK_F10; }
			if (config.f11().matches(e)) { return KeyEvent.VK_F11; }
			if (config.f12().matches(e)) { return KeyEvent.VK_F12; }
			if (config.esc().matches(e)) { return KeyEvent.VK_ESCAPE; }
			if (config.space().matches(e)) { return KeyEvent.VK_SPACE; }
			if (config.control().matches(e)) { return KeyEvent.VK_CONTROL; }
			if (config.shift().matches(e)) { return KeyEvent.VK_SHIFT; }
		}

		return KeyEvent.VK_UNDEFINED;
	}

	private boolean isChatboxMinimized()
	{
		Widget chatbox = client.getWidget(InterfaceID.Chatbox.CHATAREA);
		return chatbox == null || chatbox.isSelfHidden();
	}

	boolean isChatInputAvailable()
	{
		return chatboxFocused()
				&& !isDialogOpen()
				&& !isChatboxMinimized();
	}

	private boolean chatboxFocused()
	{
		Widget chatboxParent = client.getWidget(InterfaceID.Chatbox.UNIVERSE);
		if (chatboxParent == null || chatboxParent.getOnKeyListener() == null)
		{
			return false;
		}

		Widget worldMapSearch = client.getWidget(InterfaceID.Worldmap.MAPLIST_DISPLAY);
		if (worldMapSearch != null && client.getVarcIntValue(VarClientID.WORLDMAP_SEARCHING) == 1)
		{
			return false;
		}

		if (client.getWidget(InterfaceID.Reportabuse.UNIVERSE) != null)
		{
			return false;
		}

		return client.getFocusedInputFieldWidget() == null;
	}

	private boolean isDialogOpen()
	{
		return isHidden(InterfaceID.Chatbox.MES_LAYER_HIDE)
				|| isHidden(InterfaceID.Chatbox.CHATDISPLAY)
				|| isBankPinOpen()
				|| client.getWidget(InterfaceID.Chatmenu.OPTIONS) != null;
	}

	private boolean isBankPinOpen()
	{
		return !isHidden(InterfaceID.BankpinKeypad.UNIVERSE);
	}

	private boolean isHidden(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		return widget == null || widget.isSelfHidden();
	}

	public Rectangle getInputAreaBounds()
	{
		Widget inputWidget = client.getWidget(InterfaceID.Chatbox.INPUT);
		if (inputWidget == null || inputWidget.isSelfHidden() || inputWidget.getCanvasLocation() == null)
		{
			return null;
		}

		net.runelite.api.Point location = inputWidget.getCanvasLocation();
		int x = location.getX();
		int y = location.getY();
		int width = inputWidget.getWidth();
		int height = inputWidget.getHeight();

		String text = inputWidget.getText();
		String prefix = cachedPlayerPrefix;

		if ((prefix == null || prefix.isEmpty()) && text != null && text.contains(":"))
		{
			prefix = text.substring(0, text.indexOf(":") + 1);
		}

		if (prefix != null && !prefix.isEmpty() && text != null)
		{
			Font font = FontManager.getRunescapeFont();
			TextLayout prefixLayout = new TextLayout(prefix, font, DUMMY_FRC);
			int prefixWidth = (int) prefixLayout.getAdvance() + 1;

			TextLayout fullLayout = new TextLayout(text.isEmpty() ? " " : text, font, DUMMY_FRC);
			int textWidth = (int) fullLayout.getAdvance();
			int scrollOffset = Math.min(0, width - textWidth);

			int visiblePrefixWidth = Math.max(0, Math.min(width, prefixWidth + scrollOffset));

			x += visiblePrefixWidth;
			width = Math.max(0, width - visiblePrefixWidth);
		}

		return new Rectangle(x, y, width, height);
	}

	private void checkAndUnfocus(Point mousePoint)
	{
		Widget chatMessages = client.getWidget(InterfaceID.Chatbox.CHATDISPLAY);
		Widget chatInput = client.getWidget(InterfaceID.Chatbox.INPUT);
		Rectangle chatBounds = null;

		if (chatMessages != null && !chatMessages.isSelfHidden())
		{
			net.runelite.api.Point location = chatMessages.getCanvasLocation();
			if (location != null)
			{
				chatBounds = new Rectangle(
						location.getX(),
						location.getY(),
						chatMessages.getWidth(),
						chatMessages.getHeight() + 45
				);
			}
		}

		if (chatBounds == null && chatInput != null && chatInput.getCanvasLocation() != null)
		{
			net.runelite.api.Point inputLoc = chatInput.getCanvasLocation();
			chatBounds = new Rectangle(
					inputLoc.getX(),
					inputLoc.getY(),
					chatInput.getWidth(),
					chatInput.getHeight()
			);
		}

		if (chatBounds != null && !chatBounds.contains(mousePoint))
		{
			stopTyping(!config.preserveChatInput());
		}
	}
}