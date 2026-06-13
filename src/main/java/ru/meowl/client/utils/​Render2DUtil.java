package ru.meowl.client.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class Render2DUtil {

    // Метод отрисовки скругленного прямоугольника
    public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        // В Minecraft 1.21.4 BufferBuilder инициализируется через Tessellator или Allocator
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;

        // Строим углы треугольниками (fan) для создания плавного скругления
        int samples = 18; // Качество скругления
        
        // Центральные точки для 4-х углов
        float[][] quarters = {
            {x + radius, y + radius, 180, 270},
            {x + width - radius, y + radius, 270, 360},
            {x + width - radius, y + height - radius, 0, 90},
            {x + radius, y + height - radius, 90, 180}
        };

        for (float[] quarter : quarters) {
            float cx = quarter[0];
            float cy = quarter[1];
            float startAngle = quarter[2];
            float endAngle = quarter[3];

            for (int i = 0; i <= samples; i++) {
                float angle = startAngle + (endAngle - startAngle) * i / samples;
                float rad = (float) Math.toRadians(angle);
                float sin = (float) Math.sin(rad) * radius;
                float cos = (float) Math.cos(rad) * radius;

                bufferBuilder.vertex(matrix, cx + sin, cy - cos, 0).color(f1, f2, f3, f);
            }
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}

