package ru.meowl.client.modules;

import ru.meowl.client.modules.render.HitBubbles;
import ru.meowl.client.modules.render.JumpCircle;
import ru.meowl.client.modules.render.TargetESP;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Инициализируем твои визуальные модули
        modules.add(new JumpCircle());
        modules.add(new HitBubbles());
        // modules.add(new TargetESP()); // Раскомментируй, когда добавишь его класс

        // Здесь позже добавим модули для ClickGUI и HUD'а
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    public Module getModuleByName(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}

