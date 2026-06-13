package ru.meowl.client.modules;

import net.minecraft.client.MinecraftClient;
import ru.meowl.client.utils.SoundManager;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    
    private final String name;
    private final String description;
    private final ModuleCategory category;
    
    private boolean enabled;
    private int key; // Кнопка бинда

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = -1; // По умолчанию бинда нет
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
            // Воспроизводим твой Enable Sound (5 (bell).ogg)
            SoundManager.playSound(SoundManager.ENABLE_SOUND); 
        } else {
            onDisable();
            // Воспроизводим твой Disable Sound (3 (bell).ogg)
            SoundManager.playSound(SoundManager.DISABLE_SOUND); 
        }
    }

    public void onEnable() {
        // Здесь можно подписывать модуль на ивенты
    }

    public void onDisable() {
        // Здесь отписываем модуль от ивентов
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ModuleCategory getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
}

