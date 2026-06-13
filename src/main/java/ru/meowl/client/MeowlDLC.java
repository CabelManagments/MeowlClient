package ru.meowl.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.meowl.client.modules.ModuleManager;

public class MeowlDLC implements ClientModInitializer {
    public static final String NAME = "MeowlDLC";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    
    public static MeowlDLC INSTANCE;
    public ModuleManager moduleManager;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("Инициализация {} v{}...", NAME, VERSION);
        
        // Загружаем менеджер модулей
        moduleManager = new ModuleManager();
        
        // Позже здесь мы инициализируем твой кастомный шрифт и систему ивентов
        LOGGER.info("MeowlDLC успешно загружен!");
    }
}

