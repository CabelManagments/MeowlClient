package ru.meowl.client.modules;

import ru.meowl.client.modules.render.HitBubbles;
import ru.meowl.client.modules.render.JumpCircle;
import ru.meowl.client.modules.render.HudModule;
import ru.meowl.client.modules.render.TargetESP;
import ru.meowl.client.modules.misc.ClickGuiModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Рендер-модули
        modules.add(new JumpCircle());
        modules.add(new HitBubbles());
        modules.add(new HudModule());
        modules.add(new TargetESP());

        // Остальные модули
        modules.add(new ClickGuiModule());
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> categoryModules = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) {
                categoryModules.add(m);
            }
        }
        return categoryModules;
    }

    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
}
