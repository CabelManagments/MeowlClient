package ru.meowl.client.modules.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.meowl.client.modules.Module;
import ru.meowl.client.modules.ModuleCategory;
import ru.meowl.client.utils.Render2DUtil;

public class HudModule extends Module {

    // Идентификатор нашего кастомного шрифта из meowl.json
    private static final Identifier MEOWL_FONT = Identifier.of("meowldlc", "meowl");

    public HudModule() {
        super("HUD", "Отображает красивый внутриигровой интерфейс", ModuleCategory.RENDER);
        setEnabled(true); // Включаем HUD по умолчанию
        
        // Регистрируем отрисовку через Fabric API
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (isEnabled() && mc.player != null && !mc.options.hudHidden) {
                renderHud(drawContext);
            }
        });
    }

    private void renderHud(DrawContext drawContext) {
        MatrixStack matrices = drawContext.getMatrices();

        // 1. Собираем данные для вывода текста
        String title = "MeowlDLC";
        String version = "b1.0";
        String name = mc.getSession().getUsername();
        String fps = String.valueOf(MinecraftClient.getCurrentFps());
        
        // Получаем пинг игрока безопасным способом
        int pingValue = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                pingValue = entry.getLatency();
            }
        }
        String ping = pingValue + "ms";

        // Форматируем всю строку, как на скриншоте
        String fullText = String.format("%s | %s | %s | %s fps | %s ping", title, version, name, fps, ping);

        // 2. Рассчитываем размеры плашки под шрифт
        int paddingX = 8;
        int paddingY = 6;
        int textWidth = mc.textRenderer.getWidth(fullText); // Базовый замер длины текста
        int textHeight = 9; // Высота стандартной строки текста

        float x = 5; // Отступ от левого края экрана
        float y = 5; // Отступ от верхнего края экрана
        float width = textWidth + (paddingX * 2);
        float height = textHeight + (paddingY * 2);
        float radius = 5.0f; // Радиус скругления углов

        // 3. Рендерим скругленный прямоугольник (задний фон плашки)
        // Цвет: 0x90101010 (90 - полупрозрачность альфа-канала, 101010 - очень темный серый)
        Render2DUtil.drawRound(matrices, x, y, width, height, radius, 0x90101010);

        // 4. Отрисовываем текст поверх плашки нашим шрифтом Nexa-Heavy
        // Позиция текста с учетом внутренних отступов (padding)
        float textX = x + paddingX;
        float textY = y + paddingY;

        // В Майнкрафте 1.21.4 DrawContext требует VertexConsumer для кастомных шрифтов,
        // но метод drawText позволяет передавать стили. Сделаем отрисовку через DrawText:
        drawContext.drawText(
                mc.textRenderer, 
                fullText, 
                (int) textX, 
                (int) textY, 
                0xFFFFFFFF, // Белый цвет текста
                false
        );
        
        // Примечание: Чтобы применился именно Nexa-Heavy, Майнкрафту нужно скормить стиль.
        // Если ты хочешь, чтобы ВЕСЬ худ гарантированно был на Nexa-Heavy, мы подменим дефолтный шрифт
        // в файле ресурсов (об этом ниже), тогда метод выше автоматически отрендерит Nexa-Heavy!
    }
}

