private void drawGhosts2(MatrixStack matrices, Camera camera, float tickDelta) {
float tProgress = (float) animation.getValue();
if (tProgress <= 0.01f) {
for (LinkedList<Vec3d> trail : ghostTrails) trail.clear();
 return;
    }
 
double targetX = interpolate(lastTarget.getX(), lastTarget.lastRenderX, tickDelta);
double targetY = interpolate(lastTarget.getY(), lastTarget.lastRenderY, tickDelta);
double targetZ = interpolate(lastTarget.getZ(), lastTarget.lastRenderZ, tickDelta);

    Vec3d camPos = camera.getPos();

RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
RenderSystem.enableBlend();
RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
RenderSystem.disableCull();
RenderSystem.disableDepthTest();
RenderSystem.depthMask(false);

int c1 = mixWithHurt(customColor.getValue(), getAuraHurtFactor(lastTarget));
int c2 = twoColorsTheme.getValue() ? mixWithHurt(ColorProvider.getThemeColorTwo(), getAuraHurtFactor(lastTarget)) : c1;

long timeMs = System.currentTimeMillis();

for (int i = 0; i < 5; i++) {
double speed = timeMs * 0.005;
double offset = i * (Math.PI * 2.0 / 5.0);

double radius = lastTarget.getWidth() + 0.15;
double tx = Math.sin(speed + offset) * radius;
double tz = Math.cos(speed + offset) * radius;

double verticalDir = (i % 2 == 0) ? 1.0 : -1.0;
double yOscillation = Math.sin((speed + offset) * 0.8) * verticalDir;
double ty = (yOscillation * (lastTarget.getHeight() * 0.55)) + (lastTarget.getHeight() * 0.5);
 
Vec3d relativePos = new Vec3d(tx, ty, tz);

 ghostTrails[i].addFirst(relativePos);
if (ghostTrails[i].size() > 35) ghostTrails[i].removeLast();

        matrices.push();
int segmentIdx = 0;
for (Vec3d relPos : ghostTrails[i]) {
float fade = 1.0f - (segmentIdx / (float) ghostTrails[i].size());
  
float renderX = (float) (targetX + relPos.x - camPos.x);
float renderY = (float) (targetY + relPos.y - camPos.y);
float renderZ = (float) (targetZ + relPos.z - camPos.z);

 int trailColor = getGradient(c1, c2, fade);
float baseScale = (segmentIdx == 0) ? 0.34f : 0.21f;
 float trailScale = baseScale * fade * tProgress;
int alpha = (int) (255 * Math.pow(fade, 1.8) * tProgress);

Tessellator tess = Tessellator.getInstance();
  
BufferBuilder buffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
drawQuad(buffer, matrices, camera, renderX, renderY, renderZ, trailScale, ColorProvider.setAlpha(trailColor, alpha));
BufferRenderer.drawWithGlobalProgram(buffer.end());
  
if (segmentIdx == 0) {
BufferBuilder coreBuffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
float coreScale = 0.2f * tProgress;
drawQuad(coreBuffer, matrices, camera, renderX, renderY, renderZ, coreScale, ColorProvider.setAlpha(0xFFFFFFFF, (int)(255 * tProgress)));
BufferRenderer.drawWithGlobalProgram(coreBuffer.end());
            }

            segmentIdx++;
        }
        matrices.pop();
    }

RenderSystem.depthMask(true);
RenderSystem.enableDepthTest();
RenderSystem.defaultBlendFunc();
RenderSystem.disableBlend();
RenderSystem.enableCull();
}

