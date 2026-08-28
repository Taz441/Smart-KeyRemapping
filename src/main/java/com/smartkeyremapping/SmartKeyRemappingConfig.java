package com.smartkeyremapping;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;
import java.awt.Color;
import java.awt.event.KeyEvent;

@ConfigGroup("smartkeyremapping")
public interface SmartKeyRemappingConfig extends Config
{
    @ConfigSection(
            name = "Chat Behavior",
            description = "Configure chat focus and unfocused status",
            position = 0
    )
    String chatSection = "chatBehavior";

    @ConfigItem(
            keyName = "preserveChatInput",
            name = "Keep Typed Message on Click",
            description = "Keep the currently typed message when clicking outside the chatbox",
            section = chatSection,
            position = 0
    )
    default boolean preserveChatInput()
    {
        return true;
    }

    @ConfigItem(
            keyName = "clickToFocus",
            name = "Click to Type",
            description = "Enter typing mode by clicking the chat input",
            section = chatSection,
            position = 1
    )
    default boolean clickToFocus()
    {
        return true;
    }

    @ConfigItem(
            keyName = "stayFocusedAfterSend",
            name = "Stay Typing After Sending",
            description = "Remain in typing mode after sending a chat message",
            section = chatSection,
            position = 2
    )
    default boolean stayFocusedAfterSend()
    {
        return false;
    }

    @ConfigItem(
            keyName = "showNotTypingStatus",
            name = "Show Status When Not Typing",
            description = "Replace the typed message with status text while you're not typing",
            section = chatSection,
            position = 3
    )
    default boolean showNotTypingStatus()
    {
        return true;
    }

    @ConfigItem(
            keyName = "draftPeekKey",
            name = "Typed Message Peek Key",
            description = "Hold this key while unfocused to reveal the preserved typed message",
            section = chatSection,
            position = 4
    )
    default Keybind draftPeekKey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigSection(
            name = "Appearance",
            description = "Configure unfocused status and hover appearance",
            position = 1
    )
    String visualSection = "visuals";

    @ConfigItem(
            keyName = "notTypingText",
            name = "Not Typing Status Text",
            description = "Text shown in the chat input while typing is unfocused",
            section = visualSection,
            position = 0
    )
    default String notTypingText()
    {
        return "Currently not typing...";
    }

    @ConfigItem(
            keyName = "notTypingColor",
            name = "Status Color",
            description = "Color of the status text",
            section = visualSection,
            position = 1
    )
    default Color notTypingColor()
    {
        return new Color(0xE7, 0x85, 0x87);
    }

    @ConfigItem(
            keyName = "showHoverHighlight",
            name = "Enable Input Box Hover Overlay",
            description = "Show overlay when hovering over the input box while unfocused",
            section = visualSection,
            position = 2
    )
    default boolean showHoverHighlight()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
            keyName = "hoverFillColor",
            name = "Hover Fill Color",
            description = "Background fill color when hovering over the input line",
            section = visualSection,
            position = 3
    )
    default Color hoverFillColor()
    {
        return new Color(255, 200, 0, 30);
    }

    @Alpha
    @ConfigItem(
            keyName = "hoverOutlineColor",
            name = "Hover Outline Color",
            description = "Border outline color when hovering over the input line",
            section = visualSection,
            position = 4
    )
    default Color hoverOutlineColor()
    {
        return new Color(255, 200, 0, 180);
    }

    @ConfigSection(
            name = "Camera Remapping",
            description = "Rebind keys to move camera while chat is unfocused",
            position = 2
    )
    String cameraSection = "cameraSection";

    @ConfigItem(
            keyName = "remapCamera",
            name = "Remap Camera",
            description = "Enable remapping camera movement keys",
            section = cameraSection,
            position = 0
    )
    default boolean remapCamera()
    {
        return true;
    }

    @ConfigItem(
            keyName = "up",
            name = "Camera Up key",
            description = "Key to move camera up",
            section = cameraSection,
            position = 1
    )
    default ModifierlessKeybind up()
    {
        return new ModifierlessKeybind(KeyEvent.VK_W, 0);
    }

    @ConfigItem(
            keyName = "down",
            name = "Camera Down key",
            description = "Key to move camera down",
            section = cameraSection,
            position = 2
    )
    default ModifierlessKeybind down()
    {
        return new ModifierlessKeybind(KeyEvent.VK_S, 0);
    }

    @ConfigItem(
            keyName = "left",
            name = "Camera Left key",
            description = "Key to move camera left",
            section = cameraSection,
            position = 3
    )
    default ModifierlessKeybind left()
    {
        return new ModifierlessKeybind(KeyEvent.VK_A, 0);
    }

    @ConfigItem(
            keyName = "right",
            name = "Camera Right key",
            description = "Key to move camera right",
            section = cameraSection,
            position = 4
    )
    default ModifierlessKeybind right()
    {
        return new ModifierlessKeybind(KeyEvent.VK_D, 0);
    }

    @ConfigSection(
            name = "F Key Remapping",
            description = "Rebind keys to target tabs",
            position = 3
    )
    String fKeySection = "fKeySection";

    @ConfigItem(
            keyName = "remapFKeys",
            name = "Remap F Keys",
            description = "Enable remapping F keys",
            section = fKeySection,
            position = 0
    )
    default boolean remapFKeys()
    {
        return true;
    }

    @ConfigItem(
            keyName = "f1",
            name = "F1",
            description = "Key remapped to F1",
            section = fKeySection,
            position = 1
    )
    default ModifierlessKeybind f1()
    {
        return new ModifierlessKeybind(KeyEvent.VK_1, 0);
    }

    @ConfigItem(
            keyName = "f2",
            name = "F2",
            description = "Key remapped to F2",
            section = fKeySection,
            position = 2
    )
    default ModifierlessKeybind f2()
    {
        return new ModifierlessKeybind(KeyEvent.VK_2, 0);
    }

    @ConfigItem(
            keyName = "f3",
            name = "F3",
            description = "Key remapped to F3",
            section = fKeySection,
            position = 3
    )
    default ModifierlessKeybind f3()
    {
        return new ModifierlessKeybind(KeyEvent.VK_3, 0);
    }

    @ConfigItem(
            keyName = "f4",
            name = "F4",
            description = "Key remapped to F4",
            section = fKeySection,
            position = 4
    )
    default ModifierlessKeybind f4()
    {
        return new ModifierlessKeybind(KeyEvent.VK_4, 0);
    }

    @ConfigItem(
            keyName = "f5",
            name = "F5",
            description = "Key remapped to F5",
            section = fKeySection,
            position = 5
    )
    default ModifierlessKeybind f5()
    {
        return new ModifierlessKeybind(KeyEvent.VK_5, 0);
    }

    @ConfigItem(
            keyName = "f6",
            name = "F6",
            description = "Key remapped to F6",
            section = fKeySection,
            position = 6
    )
    default ModifierlessKeybind f6()
    {
        return new ModifierlessKeybind(KeyEvent.VK_6, 0);
    }

    @ConfigItem(
            keyName = "f7",
            name = "F7",
            description = "Key remapped to F7",
            section = fKeySection,
            position = 7
    )
    default ModifierlessKeybind f7()
    {
        return new ModifierlessKeybind(KeyEvent.VK_7, 0);
    }

    @ConfigItem(
            keyName = "f8",
            name = "F8",
            description = "Key remapped to F8",
            section = fKeySection,
            position = 8
    )
    default ModifierlessKeybind f8()
    {
        return new ModifierlessKeybind(KeyEvent.VK_8, 0);
    }

    @ConfigItem(
            keyName = "f9",
            name = "F9",
            description = "Key remapped to F9",
            section = fKeySection,
            position = 9
    )
    default ModifierlessKeybind f9()
    {
        return new ModifierlessKeybind(KeyEvent.VK_9, 0);
    }

    @ConfigItem(
            keyName = "f10",
            name = "F10",
            description = "Key remapped to F10",
            section = fKeySection,
            position = 10
    )
    default ModifierlessKeybind f10()
    {
        return new ModifierlessKeybind(KeyEvent.VK_0, 0);
    }

    @ConfigItem(
            keyName = "f11",
            name = "F11",
            description = "Key remapped to F11",
            section = fKeySection,
            position = 11
    )
    default ModifierlessKeybind f11()
    {
        return new ModifierlessKeybind(KeyEvent.VK_MINUS, 0);
    }

    @ConfigItem(
            keyName = "f12",
            name = "F12",
            description = "Key remapped to F12",
            section = fKeySection,
            position = 12
    )
    default ModifierlessKeybind f12()
    {
        return new ModifierlessKeybind(KeyEvent.VK_EQUALS, 0);
    }

    @ConfigItem(
            keyName = "esc",
            name = "ESC",
            description = "Key remapped to ESC",
            section = fKeySection,
            position = 13
    )
    default ModifierlessKeybind esc()
    {
        return new ModifierlessKeybind(KeyEvent.VK_ESCAPE, 0);
    }

    @ConfigItem(
            keyName = "space",
            name = "Space",
            description = "Key remapped to Space",
            section = fKeySection,
            position = 14
    )
    default ModifierlessKeybind space()
    {
        return new ModifierlessKeybind(KeyEvent.VK_SPACE, 0);
    }

    @ConfigItem(
            keyName = "control",
            name = "Control",
            description = "Key remapped to Control",
            section = fKeySection,
            position = 15
    )
    default ModifierlessKeybind control()
    {
        return new ModifierlessKeybind(KeyEvent.VK_CONTROL, 0);
    }

    @ConfigItem(
            keyName = "shift",
            name = "Shift",
            description = "Key remapped to Shift",
            section = fKeySection,
            position = 16
    )
    default ModifierlessKeybind shift()
    {
        return new ModifierlessKeybind(KeyEvent.VK_SHIFT, 0);
    }
}