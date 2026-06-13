@FieldDefaults(level = AccessLevel.PRIVATE)
public class HitBubbles extends Module {

    private final List<HitBubble> bubbles = new CopyOnWriteArrayList<>();
    final Identifier bubbleTexture = Identifier.of("textures/bubble.png");

    private boolean lastAttackKeyPressed = false;

    public HitBubbles() {
        super("HitBubbles", "Hit Bubbles", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean currentAttack = mc.options.attackKey.isPressed();

        if (currentAttack && !lastAttackKeyPressed && mc.player != null) {
            LivingEntity target = getTarget();

            if (target != null) {
                Vec3d bubblePos = getHitPosition(target);
                bubbles.add(new HitBubble(bubblePos, new Timer()));
            }
        }

        lastAttackKeyPressed = currentAttack;
        bubbles.removeIf(b -> b.timer().passedMs(3000));
    }

    private LivingEntity getTarget() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
            Entity entity = entityHit.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    private Vec3d getHitPosition(LivingEntity target) {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
            return entityHit.getPos();
        }

        return new Vec3d(
                target.getX(),
                target.getY() + target.getHeight() / 2,
                target.getZ()
        );
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        renderBubbles(e.getStack());
    }

    private void renderBubbles(MatrixStack stack) {
        if (bubbles.isEmpty()) return;

        for (HitBubble bubble : bubbles) {
            renderSingleBubble(stack, bubble);
        }
    }

    private void renderSingleBubble(MatrixStack stack, HitBubble bubble) {
        float progress = (float) bubble.timer().getPassedTimeMs() / 3000f;

        if (progress >= 1f) return;

        float scale = progress * 2f;
        float alpha = 1f - progress;
        float rotation = bubble.timer().getPassedTimeMs() / 10f;

        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d vec = bubble.pos().subtract(camera.getPos());

        MatrixStack matrix = new MatrixStack();
        matrix.push();

        matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrix.translate(vec.x, vec.y, vec.z);
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));

        MatrixStack.Entry entry = matrix.peek();

        Vector4i colors = ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(90), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(270)), 1, alpha);

        Render3DUtil.drawTexture(entry, bubbleTexture, -scale / 2, -scale / 2, scale, scale, colors, true);
        matrix.pop();
    }

    public record HitBubble(Vec3d pos, Timer timer) {}
}
