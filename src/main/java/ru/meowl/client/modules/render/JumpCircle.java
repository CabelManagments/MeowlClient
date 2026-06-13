package ru.meowl.client.modules.render;

import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector4i;
import ru.meowl.client.modules.Module;
import ru.meowl.client.modules.ModuleCategory;
// Если твои ивенты, Counter и утилиты лежат в других пакетах, поправь пути импортов ниже:
import ru.meowl.client.events.EventHandler;
import ru.meowl.client.events.TickEvent;
import ru.meowl.client.events.WorldRenderEvent;
import ru.meowl.client.utils.ColorUtil;
import ru.meowl.client.utils.Counter;
import ru.meowl.client.utils.Render3DUtil;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class JumpCircle extends Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    
    final List<Circle> circles = new ArrayList<>();
    final Identifier circleTexture = Identifier.of("meowldlc", "textures/circle.png");
    boolean wasOnGround = true;

    public JumpCircle() {
        super("JumpCircle", "Jump Circle", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onUpdate(TickEvent event) {
        if (mc.player == null) return;

        boolean isOnGround = mc.player.isOnGround();

        if (wasOnGround && !isOnGround) {
            Vec3d pos = new Vec3d(
                    mc.player.getX(),
                    Math.floor(mc.player.getY()) + 0.001,
                    mc.player.getZ()
            );
            circles.add(new Circle(pos, new Counter()));
        }

        wasOnGround = isOnGround;

        circles.removeIf(c -> c.timer().passedMs(3000));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        renderCircles();
    }

    private void renderCircles() {
        if (circles.isEmpty()) return;

        for (Circle circle : circles) {
            renderSingleCircle(circle);
        }
    }

    private void renderSingleCircle(Circle circle) {
        float lifeTime = (float) circle.timer().getPassedTimeMs();
        float maxTime = 3000f;
        float progress = lifeTime / maxTime;

        if (progress >= 1f) return;

        float scale = progress * 2f;
        float alpha = 1f - (progress * progress);

        int baseColor = ColorUtil.fade((int)(progress * 360f));
        int color = ColorUtil.multAlpha(baseColor, alpha);

        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d cameraPos = camera.getPos();
        Vec3d circlePos = circle.pos();

        MatrixStack matrixStack = new MatrixStack();
        
        // В Minecraft 1.21.4 вращения делаются через кватернионы JOML
        matrixStack.multiply(new Quaternionf().rotationX((float) Math.toRadians(camera.getPitch())));
        matrixStack.multiply(new Quaternionf().rotationY((float) Math.toRadians(camera.getYaw() + 180.0F)));

        matrixStack.translate(circlePos.x - cameraPos.x, circlePos.y - cameraPos.y, circlePos.z - cameraPos.z);                                                                       
        matrixStack.multiply(new Quaternionf().rotationY((float) Math.toRadians(-camera.getYaw())));
        matrixStack.multiply(new Quaternionf().rotationX((float) Math.toRadians(90f)));                
        
        MatrixStack.Entry entry = matrixStack.peek();
        Vector4i colors = new Vector4i(color, color, color, color);

        Render3DUtil.drawTexture(entry, circleTexture, -scale/2, -scale/2, scale, scale, colors, true);
    }                                                                                      
    
    public record Circle(Vec3d pos, Counter timer) {}
}
