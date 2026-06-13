package ru.meowl.client.modules;

import ru.meowl.client.modules.render.HitBubbles;
import ru.meowl.client.modules.render.JumpCircle;
import ru.meowl.client.modules.render.HudModule;
import ru.meowl.client.modules.misc.ClickGuiModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    // Список абсолютно всех модулей чита
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // 1. Твои модули (круги и пузыри)
        modules.add(new JumpCircle());
        modules.add(new HitBubbles());
        
        // 2. Наш красивый HUD со скруглениями (Ватермарка)
        modules.add(new HudModule());

        // 3. Твой новый ClickGUI, который открывается на правый Shift
        modules.add(new ClickGuiModule());
    }

    /**
     * Получить полный список всех модулей
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * Получить модули только одной конкретной категории (нужно для сетки в ClickGUI)
     */
    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> categoryModules = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) {
                categoryModules.add(m);
            }
        }
        return categoryModules;
    }

    /**
     * Поиск модуля по его имени
     */
    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
}
