package dev.tr7zw.waveycapes.renderlayers;

import dev.tr7zw.waveycapes.CapeHolder;
import dev.tr7zw.waveycapes.CapeMovement;
import dev.tr7zw.waveycapes.WaveyCapesBase;
import dev.tr7zw.waveycapes.WindMode;
import dev.tr7zw.waveycapes.sim.StickSimulation;
import dev.tr7zw.waveycapes.util.Matrix4f;
import dev.tr7zw.waveycapes.util.Mth;
import dev.tr7zw.waveycapes.util.PoseStack;
import dev.tr7zw.waveycapes.util.Vector3f;
import dev.tr7zw.waveycapes.util.Vector4f;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;

public class CustomCapeRenderLayer implements LayerRenderer<AbstractClientPlayer> {
    
    private static int partCount;
    private ModelRenderer[] customCape = new ModelRenderer[partCount];
    private final RenderPlayer playerRenderer;
    
    public CustomCapeRenderLayer(RenderPlayer playerRenderer, ModelBase model) {
        partCount = 16;
        this.playerRenderer = playerRenderer;
        buildMesh(model);
    }
    
    private void buildMesh(ModelBase model) {
        customCape = new ModelRenderer[partCount];
        for (int i = 0; i < partCount; i++) {
            ModelRenderer base = new ModelRenderer(model, 0, i);
            base.setTextureSize(64, 32);
            this.customCape[i] = base.addBox(-5.0F, (float)i, -1.0F, 10, 1, 1);
        }
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer abstractClientPlayer, float paramFloat1, float paramFloat2, float deltaTick,
            float animationTick, float paramFloat5, float paramFloat6, float paramFloat7) {
    //public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float delta, float j, float k, float l) {
        if(abstractClientPlayer.isInvisible())return;
//        CapeRenderer renderer = getCapeRenderer(abstractClientPlayer);
//        if(renderer == null) return;
        
        if (!abstractClientPlayer.hasPlayerInfo() || abstractClientPlayer.isInvisible()
                || !abstractClientPlayer.isWearing(EnumPlayerModelParts.CAPE)
                || abstractClientPlayer.getLocationCape() == null) {
            return;
        }
        
        if(WaveyCapesBase.config.capeMovement == CapeMovement.BASIC_SIMULATION) {
            CapeHolder holder = (CapeHolder) abstractClientPlayer;
            holder.updateSimulation(abstractClientPlayer, partCount);
        }
        this.playerRenderer.bindTexture(abstractClientPlayer.getLocationCape());
        renderSmoothCape(abstractClientPlayer, deltaTick);
        if(true)return;
//        if (WaveyCapesBase.config.capeStyle == CapeStyle.SMOOTH && renderer.vanillaUvValues()) {
//            renderSmoothCape(poseStack, multiBufferSource, renderer, abstractClientPlayer, delta, i);
//        } else {
        ModelRenderer[] parts = customCape;
        for (int part = 0; part < partCount; part++) {
            ModelRenderer model = parts[part];
            GlStateManager.pushMatrix();
            modifyPoseStack(abstractClientPlayer, deltaTick, part);
            model.render(0.0625F);
            //renderer.render(abstractClientPlayer, part, model, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
            GlStateManager.popMatrix();
        }
//        }
    }
    
    private void renderSmoothCape(AbstractClientPlayer abstractClientPlayer, float delta) {
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        PoseStack poseStack = new PoseStack();
        GlStateManager.getFloat(partCount, null);
        
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();

        Matrix4f oldPositionMatrix = null;
        for (int part = 0; part < partCount; part++) {
            modifyPoseStack(poseStack, abstractClientPlayer, delta, part);

            if (oldPositionMatrix == null) {
                oldPositionMatrix = poseStack.last().pose();
            }

            if (part == 0) {
                addTopVertex(worldrenderer, poseStack.last().pose(), oldPositionMatrix,
                        0.3F,
                        0,
                        0F,
                        -0.3F,
                        0,
                        -0.06F, part);
            }

//            if (part == partCount - 1) {
//                addBottomVertex(bufferBuilder, poseStack.last().pose(), poseStack.last().pose(),
//                        0.3F,
//                        (part + 1) * (0.96F / partCount),
//                        0F,
//                        -0.3F,
//                        (part + 1) * (0.96F / partCount),
//                        -0.06F, part, light);
//            }
//
//            addLeftVertex(bufferBuilder, poseStack.last().pose(), oldPositionMatrix,
//                    -0.3F,
//                    (part + 1) * (0.96F / partCount),
//                    0F,
//                    -0.3F,
//                    part * (0.96F / partCount),
//                    -0.06F, part, light);
//
            addRightVertex(worldrenderer, poseStack.last().pose(), oldPositionMatrix,
                    0.3F,
                    (part + 1) * (0.96F / partCount),
                    0F,
                    0.3F,
                    part * (0.96F / partCount),
                    -0.06F, part);
//
//            addBackVertex(bufferBuilder, poseStack.last().pose(), oldPositionMatrix,
//                    0.3F,
//                    (part + 1) * (0.96F / partCount),
//                    -0.06F,
//                    -0.3F,
//                    part * (0.96F / partCount),
//                    -0.06F, part, light);
//
//            addFrontVertex(bufferBuilder, oldPositionMatrix, poseStack.last().pose(),
//                    0.3F,
//                    (part + 1) * (0.96F / partCount),
//                    0F,
//                    -0.3F,
//                    part * (0.96F / partCount),
//                    0F, part, light);

            oldPositionMatrix = poseStack.last().pose();
            poseStack.popPose();
        }

    }

    private void modifyPoseStack(PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float h, int part) {
        if(WaveyCapesBase.config.capeMovement == CapeMovement.BASIC_SIMULATION) {
            modifyPoseStackSimulation(poseStack, abstractClientPlayer, h, part);
            return;
        }
        modifyPoseStackVanilla(abstractClientPlayer, h, part);
    }
    
    private void modifyPoseStack(AbstractClientPlayer abstractClientPlayer, float h, int part) {
        if(WaveyCapesBase.config.capeMovement == CapeMovement.BASIC_SIMULATION) {
            modifyPoseStackSimulation(abstractClientPlayer, h, part);
            return;
        }
        modifyPoseStackVanilla(abstractClientPlayer, h, part);
    }
    
    private void modifyPoseStackSimulation(PoseStack poseStack, AbstractClientPlayer abstractClientPlayer, float delta, int part) {
        StickSimulation simulation = ((CapeHolder)abstractClientPlayer).getSimulation();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.125D);
        
        float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
        if(z > 0) {
            z = 0;
        }
        float y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);
        
//        float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0F;
//        sidewaysRotationOffset = Mth.clamp(sidewaysRotationOffset, -20.0F, 20.0F);
        float sidewaysRotationOffset = 0;
        float partRotation = (float) -Math.atan2(y, z);
        partRotation = Math.max(partRotation, 0);
        if(partRotation != 0)
            partRotation = (float) (Math.PI-partRotation);
        partRotation *= 57.2958;
        partRotation *= 2;
        
        float height = 0;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0F;
            poseStack.translate(0, 0.15F, 0);
        }

        float naturalWindSwing = getNatrualWindSwing(part);

        
        // vanilla rotating and wind
        poseStack.mulPose(Vector3f.XP.rotationDegrees(6.0F + height + naturalWindSwing));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(sidewaysRotationOffset / 2.0F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - sidewaysRotationOffset / 2.0F));
        poseStack.translate(0, y/partCount, z/partCount); // movement from the simulation
        //offsetting so the rotation is on the cape part
        //float offset = (float) (part * (16 / partCount))/16; // to fold the entire cape into one position for debugging
        poseStack.translate(0, /*-offset*/ + (0.48/16) , - (0.48/16)); // (0.48/16)
        poseStack.translate(0, part * 1f/partCount, part * (0)/partCount);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-partRotation)); // apply actual rotation
        // undoing the rotation
        poseStack.translate(0, -part * 1f/partCount, -part * (0)/partCount);
        poseStack.translate(0, -(0.48/16), (0.48/16));
        
    }
    
    private void modifyPoseStackSimulation(AbstractClientPlayer abstractClientPlayer, float delta, int part) {
        StickSimulation simulation = ((CapeHolder)abstractClientPlayer).getSimulation();
        GlStateManager.translate(0.0D, 0.0D, 0.125D);
        
        float z = simulation.points.get(part).getLerpX(delta) - simulation.points.get(0).getLerpX(delta);
        if(z > 0) {
            z = 0;
        }
        float y = simulation.points.get(0).getLerpY(delta) - part - simulation.points.get(part).getLerpY(delta);
        
//        float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0F;
//        sidewaysRotationOffset = Mth.clamp(sidewaysRotationOffset, -20.0F, 20.0F);
        float sidewaysRotationOffset = 0;
        float partRotation = (float) -Math.atan2(y, z);
        partRotation = Math.max(partRotation, 0);
        if(partRotation != 0)
            partRotation = (float) (Math.PI-partRotation);
        partRotation *= 57.2958;
        partRotation *= 2;
        
        float height = 0;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0F;
            GlStateManager.translate(0, 0.15F, 0);
        }

        float naturalWindSwing = getNatrualWindSwing(part);

        
        // vanilla rotating and wind
        GlStateManager.rotate(6.0F + height + naturalWindSwing, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(sidewaysRotationOffset / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-sidewaysRotationOffset / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0, y/partCount, z/partCount); // movement from the simulation
        //offsetting so the rotation is on the cape part
        //float offset = (float) (part * (16 / partCount))/16; // to fold the entire cape into one position for debugging
        GlStateManager.translate(0, /*-offset*/ + (0.48/16) , - (0.48/16)); // (0.48/16)
        GlStateManager.translate(0, part * 1f/partCount, part * (0)/partCount);
        //GlStateManager.rotate(-partRotation, 1.0F, 0.0F, 0.0F);
        // undoing the rotation
        GlStateManager.translate(0, -part * 1f/partCount, -part * (0)/partCount);
        GlStateManager.translate(0, -(0.48/16), (0.48/16));
        
    }
    
    private void modifyPoseStackVanilla(AbstractClientPlayer abstractClientPlayer, float h, int part) {
        GlStateManager.translate(0.0D, 0.0D, 0.125D);
        double d = Mth.lerp(h, abstractClientPlayer.prevChasingPosX, abstractClientPlayer.chasingPosX)
                - Mth.lerp(h, abstractClientPlayer.prevPosX, abstractClientPlayer.posX);
        double e = Mth.lerp(h, abstractClientPlayer.prevChasingPosY, abstractClientPlayer.chasingPosY)
                - Mth.lerp(h, abstractClientPlayer.prevPosY, abstractClientPlayer.posY);
        double m = Mth.lerp(h, abstractClientPlayer.prevChasingPosZ, abstractClientPlayer.chasingPosZ)
                - Mth.lerp(h, abstractClientPlayer.prevPosZ, abstractClientPlayer.posZ);
        float n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        double o = Math.sin(n * 0.017453292F);
        double p = -Math.cos(n * 0.017453292F);
        float height = (float) e * 10.0F;
        height = MathHelper.clamp_float(height, -6.0F, 32.0F);
        float swing = (float) (d * o + m * p) * easeOutSine(1.0F/partCount*part)*100;
        swing = MathHelper.clamp_float(swing, 0.0F, 150.0F * easeOutSine(1F/partCount*part));
        float sidewaysRotationOffset = (float) (d * p - m * o) * 100.0F;
        sidewaysRotationOffset = MathHelper.clamp_float(sidewaysRotationOffset, -20.0F, 20.0F);
        float t = Mth.lerp(h, abstractClientPlayer.prevCameraYaw, abstractClientPlayer.cameraYaw);
        height += Math.sin(Mth.lerp(h, abstractClientPlayer.prevDistanceWalkedModified, abstractClientPlayer.distanceWalkedModified) * 6.0F) * 32.0F * t;
        if (abstractClientPlayer.isSneaking()) {
            height += 25.0F;
            GlStateManager.translate(0, 0.15F, 0);
        }

        float naturalWindSwing = getNatrualWindSwing(part);
        
        GlStateManager.rotate(6.0F + swing / 2.0F + height + naturalWindSwing, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(sidewaysRotationOffset / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-sidewaysRotationOffset / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
    }
    
    private float getNatrualWindSwing(int part) {
        long highlightedPart = (System.currentTimeMillis() / 3) % 360;
        float relativePart = (float) (part + 1) / partCount;
        if (WaveyCapesBase.config.windMode == WindMode.WAVES) {
            return (float) (Math.sin(Math.toRadians((relativePart) * 360 - (highlightedPart))) * 3);
        }
//        if (WaveyCapesBase.config.windMode == WindMode.SLIGHT) {
//            return getWind(60);
//        }
        return 0;
    }

//    private static void addBackVertex(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light) {
//        float i;
//        Matrix4f k;
//        if (x1 < x2) {
//            i = x1;
//            x1 = x2;
//            x2 = i;
//        }
//
//        if (y1 < y2) {
//            i = y1;
//            y1 = y2;
//            y2 = i;
//
//            k = matrix;
//            matrix = oldMatrix;
//            oldMatrix = k;
//        }
//
//        float minU = .015625F;
//        float maxU = .171875F;
//
//        float minV = .03125F;
//        float maxV = .53125F;
//
//        float deltaV = maxV - minV;
//        float vPerPart = deltaV / partCount;
//        maxV = minV + (vPerPart * (part + 1));
//        minV = minV + (vPerPart * part);
//
//        bufferBuilder.vertex(oldMatrix, x1, y2, z1).color(1f, 1f, 1f, 1f).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z1).color(1f, 1f, 1f, 1f).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x2, y1, z2).color(1f, 1f, 1f, 1f).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x1, y1, z2).color(1f, 1f, 1f, 1f).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//    }
//
//    private static void addFrontVertex(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light) {
//        float i;
//        Matrix4f k;
//        if (x1 < x2) {
//            i = x1;
//            x1 = x2;
//            x2 = i;
//        }
//
//        if (y1 < y2) {
//            i = y1;
//            y1 = y2;
//            y2 = i;
//
//            k = matrix;
//            matrix = oldMatrix;
//            oldMatrix = k;
//        }
//
//        float minU = .1875F;
//        float maxU = .34375F;
//
//        float minV = .03125F;
//        float maxV = .53125F;
//
//        float deltaV = maxV - minV;
//        float vPerPart = deltaV / partCount;
//        maxV = minV + (vPerPart * (part + 1));
//        minV = minV + (vPerPart * part);
//
//        bufferBuilder.vertex(oldMatrix, x1, y1, z1).color(1f, 1f, 1f, 1f).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y1, z1).color(1f, 1f, 1f, 1f).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x2, y2, z2).color(1f, 1f, 1f, 1f).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x1, y2, z2).color(1f, 1f, 1f, 1f).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//    }
//
//    private static void addLeftVertex(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light) {
//        float i;
//        if (x1 < x2) {
//            i = x1;
//            x1 = x2;
//            x2 = i;
//        }
//
//        if (y1 < y2) {
//            i = y1;
//            y1 = y2;
//            y2 = i;
//        }
//
//        float minU = 0;
//        float maxU = .015625F;
//
//        float minV = .03125F;
//        float maxV = .53125F;
//
//        float deltaV = maxV - minV;
//        float vPerPart = deltaV / partCount;
//        maxV = minV + (vPerPart * (part + 1));
//        minV = minV + (vPerPart * part);
//
//        bufferBuilder.vertex(matrix, x2, y1, z1).color(1f, 1f, 1f, 1f).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x2, y1, z2).color(1f, 1f, 1f, 1f).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z2).color(1f, 1f, 1f, 1f).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z1).color(1f, 1f, 1f, 1f).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//    }
//
    private static void addRightVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .171875F;
        float maxU = .1875F;

        float minV = .03125F;
        float maxV = .53125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        //matrix
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, maxV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, matrix, x2, y1, z1).tex(maxU, maxV).normal(1, 0, 0).endVertex();
        //oldMatrix
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(maxU, minV).normal(1, 0, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z2).tex(minU, minV).normal(1, 0, 0).endVertex();
        
//        bufferBuilder.vertex(matrix, x2, y1, z2).color(1f, 1f, 1f, 1f).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x2, y1, z1).color(1f, 1f, 1f, 1f).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z1).color(1f, 1f, 1f, 1f).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z2).color(1f, 1f, 1f, 1f).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
    }
//
//    private static void addBottomVertex(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part, int light) {
//        float i;
//        if (x1 < x2) {
//            i = x1;
//            x1 = x2;
//            x2 = i;
//        }
//
//        if (y1 < y2) {
//            i = y1;
//            y1 = y2;
//            y2 = i;
//        }
//
//        float minU = .171875F;
//        float maxU = .328125F;
//
//        float minV = 0;
//        float maxV = .03125F;
//
//        float deltaV = maxV - minV;
//        float vPerPart = deltaV / partCount;
//        maxV = minV + (vPerPart * (part + 1));
//        minV = minV + (vPerPart * part);
//
//        bufferBuilder.vertex(oldMatrix, x1, y2, z2).color(1f, 1f, 1f, 1f).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z2).color(1f, 1f, 1f, 1f).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x2, y1, z1).color(1f, 1f, 1f, 1f).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//        bufferBuilder.vertex(matrix, x1, y1, z1).color(1f, 1f, 1f, 1f).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(1, 0, 0).endVertex();
//    }

    private static WorldRenderer vertex(WorldRenderer worldrenderer, Matrix4f matrix4f, float f, float g, float h) {
        Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
        vector4f.transform(matrix4f);
        worldrenderer.pos(vector4f.x(), vector4f.y(), vector4f.z());
        return worldrenderer;
    }
    
    private static void addTopVertex(WorldRenderer worldrenderer, Matrix4f matrix, Matrix4f oldMatrix, float x1, float y1, float z1, float x2, float y2, float z2, int part) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float minU = .015625F;
        float maxU = .171875F;

        float minV = 0;
        float maxV = .03125F;

        float deltaV = maxV - minV;
        float vPerPart = deltaV / partCount;
        maxV = minV + (vPerPart * (part + 1));
        minV = minV + (vPerPart * part);

        //oldMatrix
        vertex(worldrenderer, oldMatrix, x1, y2, z1).tex(maxU, maxV).normal(0, 1, 0).endVertex();
        vertex(worldrenderer, oldMatrix, x2, y2, z1).tex(minU, maxV).normal(0, 1, 0).endVertex();
        //newMatrix
        vertex(worldrenderer, matrix, x2, y1, z2).tex(minU, minV).normal(0, 1, 0).endVertex();
        vertex(worldrenderer, matrix, x1, y1, z2).tex(maxU, minV).normal(0, 1, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x1, y2, z1).color(1f, 1f, 1f, 1f).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
//        bufferBuilder.vertex(oldMatrix, x2, y2, z1).color(1f, 1f, 1f, 1f).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
//        bufferBuilder.vertex(matrix, x2, y1, z2).color(1f, 1f, 1f, 1f).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
//        bufferBuilder.vertex(matrix, x1, y1, z2).color(1f, 1f, 1f, 1f).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 1, 0).endVertex();
    }

//    private static VanillaCapeRenderer vanillaCape = new VanillaCapeRenderer();
//    
//    private CapeRenderer getCapeRenderer(AbstractClientPlayer abstractClientPlayer) {
//        for(ModSupport support : SupportManager.getSupportedMods()) {
//            if(support.shouldBeUsed(abstractClientPlayer)) {
//                return support.getRenderer();
//            }
//        }
//        if (!abstractClientPlayer.hasPlayerInfo() || abstractClientPlayer.isInvisible()
//                || !abstractClientPlayer.isWearing(EnumPlayerModelParts.CAPE)
//                || abstractClientPlayer.getLocationCape() == null) {
//            return null;
//        } else {
//            return vanillaCape;
//        }
//    }
    
    private static int scale = 1000*60*60;
    
    /**
     * Returns between 0 and 2
     * 
     * @param posY
     * @return
     */
    private static float getWind(double posY) {
        float x = (System.currentTimeMillis()%scale)/10000f;
        float mod = MathHelper.clamp_float(1f/200f*(float)posY, 0f, 1f);
        return MathHelper.clamp_float((float) (Math.sin(2 * x) + Math.sin(Math.PI * x)) * mod, 0, 2);
    }
    
    
    /**
     * https://easings.net/#easeOutSine
     * 
     * @param x
     * @return
     */
    private static float easeOutSine(float x) {
        return (float) Math.sin((x * Math.PI) / 2f);

      }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
    
}