package ru.meowl.client.modules.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import ru.meowl.client.modules.Module;
import ru.meowl.client.modules.ModuleCategory;

public class TargetESP extends Module {

    public TargetESP() {
        super("TargetESP", "Подсвечивает цели сквозь стены", ModuleCategory.RENDER);
    }

    private void drawGhosts2(MatrixStack matrices, Camera camera, float tickDelta) {
        // Твой код логики отрисовки призраков/таргетов
    }
}
