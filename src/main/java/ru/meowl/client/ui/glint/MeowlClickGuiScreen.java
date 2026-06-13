package ru.meowl.client.ui.glint;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import ru.meowl.client.MeowlDLC;
import ru.meowl.client.modules.Module;
import ru.meowl.client.modules.ModuleCategory;
import ru.meowl.client.modules.misc.ClickGuiModule;
import ru.meowl.client.utils.Render2DUtil;

import java.util.List;

public class MeowlClickGuiScreen extends Screen {
    private final ClickGuiModule parentModule;
    private ModuleCategory currentCategory = ModuleCategory.COMBAT;

    // Размеры и координаты главного окна
    private float x, y, width, height;
    private float sidebarWidth;

    public MeowlClickGuiScreen(ClickGuiModule parentModule) {
        super(Text.of("ClickGUI"));
        this.parentModule = parentModule;
    }

    @Override
    protected void init() {
        // Центрируем окно GUI на экране
        this.width = 450;
        this.height = 300;
        this.x = (this.client.getWindow().getScaledWidth() - this.width) / 2f;
        this.y = (this.client.getWindow().getScaledHeight() - this.height) / 2f;
        this.sidebarWidth = 110;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // 1. Затемняем задний фон игры (как на скрине)
        drawContext.fill(0, 0, this.width, this.height, 0x70000000);

        var matrices = drawContext.getMatrices();

        // 2. Рендерим основную подложку всего окна (уголки скруглены)
        Render2DUtil.drawRound(matrices, x, y, width, height, 6.0f, 0xFF18181A);

        // 3. Рендерим левый сайдбар под категории
        Render2DUtil.drawRound(matrices, x, y, sidebarWidth, height, 6.0f, 0xFF111112);
        // Небольшой костыль, чтобы скругление справа у сайдбара не вылезало на панель модулей
        drawContext.fill((int)(x + sidebarWidth - 5), (int)y, (int)(x + sidebarWidth), (int)(y + height), 0xFF111112);

        // Логотип сверху сайдбара
        drawContext.drawText(this.textRenderer, "MeowlDLC", (int)(x + 15), (int)(y + 15), 0xFFFFFFFF, false);

        // 4. Отрисовка кнопок категорий в сайдбаре
        float catY = y + 45;
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean isHovered = mouseX >= x && mouseX <= x + sidebarWidth && mouseY >= catY && mouseY <= catY + 20;
            boolean isSelected = category == currentCategory;

            // Цвет текста: белый если выбрано или наведено, иначе серый
            int textColor = isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFCCCCCC : 0xFF777777);
            
            // Если категория выбрана, рисуем небольшой маркер слева
            if (isSelected) {
                drawContext.fill((int)x + 2, (int)catY + 4, (int)x + 5, (int)catY + 14, 0xFF55FFFF);
            }

            drawContext.drawText(this.textRenderer, category.getName(), (int)(x + 15), (int)catY + 4, textColor, false);
            catY += 22;
        }

        // 5. Отрисовка модулей выбранной категории (Сетка справа)
        if (MeowlDLC.INSTANCE.moduleManager != null) {
            List<Module> modules = MeowlDLC.INSTANCE.moduleManager.getModulesByCategory(currentCategory);
            
            float startX = x + sidebarWidth + 15;
            float startY = y + 20;
            float itemWidth = 145;
            float itemHeight = 35;
            
            int col = 0;
            int row = 0;

            for (Module module : modules) {
                float itemX = startX + (col * (itemWidth + 10));
                float itemY = startY + (row * (itemHeight + 10));

                boolean isOverItem = mouseX >= itemX && mouseX <= itemX + itemWidth && mouseY >= itemY && mouseY <= itemY + itemHeight;
                
                // Цвет плашки модуля (если включен — посветлее, если выключен — темный)
                int cardColor = module.isEnabled() ? 0xFF252528 : (isOverItem ? 0xFF1E1E20 : 0xFF141416);
                
                // Рисуем скругленную карточку модуля
                Render2DUtil.drawRound(matrices, itemX, itemY, itemWidth, itemHeight, 4.0f, cardColor);

                // Название модуля внутри карточки
                int titleColor = module.isEnabled() ? 0xFF55FFFF : 0xFFFFFFFF;
                drawContext.drawText(this.textRenderer, module.getName(), (int)itemX + 8, (int)itemY + 6, titleColor, false);
                
                // Краткое описание под названием
                String desc = module.getDescription();
                if (desc.length() > 24) desc = desc.substring(0, 22) + "..";
                drawContext.drawText(this.textRenderer, desc, (int)itemX + 8, (int)itemY + 18, 0xFF555555, false);

                col++;
                if (col >= 2) { // 2 колонки карточек модулей, как на макете
                    col = 0;
                    row++;
                }
            }
        }

        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Клик по категориям
        float catY = y + 45;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseX >= x && mouseX <= x + sidebarWidth && mouseY >= catY && mouseY <= catY + 20) {
                currentCategory = category;
                this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                        net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            catY += 22;
        }

        // Клик по модулям (включение / выключение)
        if (MeowlDLC.INSTANCE.moduleManager != null) {
            List<Module> modules = MeowlDLC.INSTANCE.moduleManager.getModulesByCategory(currentCategory);
            float startX = x + sidebarWidth + 15;
            float startY = y + 20;
            float itemWidth = 145;
            float itemHeight = 35;
            int col = 0;
            int row = 0;

            for (Module module : modules) {
                float itemX = startX + (col * (itemWidth + 10));
                float itemY = startY + (row * (itemHeight + 10));

                if (mouseX >= itemX && mouseX <= itemX + itemWidth && mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    module.toggle(); // Переключаем состояние модуля со звуком!
                    return true;
                }

                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        // При закрытии GUI выключаем состояние модуля, чтобы его можно было открыть снова
        this.parentModule.setEnabled(false);
        super.close();
    }

    @Override
    public boolean shouldPauseGame() {
        return false; // Игра не встает на паузу при открытии чита в мультиплеере
    }
}

