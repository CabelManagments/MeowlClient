package ru.meowl.client.modules.misc;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import ru.meowl.client.modules.Module;
import ru.meowl.client.modules.ModuleCategory;
import ru.meowl.client.ui.glint.MeowlClickGuiScreen;

public class ClickGuiModule extends Module {

    public ClickGuiModule() {
        super("ClickGUI", "Открывает меню управления модулями", ModuleCategory.MISC);
        this.setKey(GLFW.GLFW_KEY_RIGHT_SHIFT); // Ставим правый Shift по умолчанию
    }

    @Override
    public void onEnable() {
        // Проверяем, что мы не в меню, и открываем наш GUI
        if (mc.currentScreen == null) {
            mc.setScreen(new MeowlClickGuiScreen(this));
        }
    }
}

