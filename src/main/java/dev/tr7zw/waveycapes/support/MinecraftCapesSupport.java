package dev.tr7zw.waveycapes.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.tr7zw.waveycapes.CapeRenderInfo;
import dev.tr7zw.waveycapes.CapeRenderer;
import dev.tr7zw.waveycapes.NMSUtil;
import dev.tr7zw.waveycapes.versionless.ModBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftcapes.config.MinecraftCapesConfig;
import net.minecraftcapes.player.PlayerHandler;

public class MinecraftCapesSupport implements ModSupport {

    private MinecraftCapesRenderer render = new MinecraftCapesRenderer();
    private Function<CapeRenderInfo, PlayerHandler> getCape = null;

    private void init(CapeRenderInfo test) {
        try {
            PlayerHandler.get(test.getCapeHolder().getWCUUID()).getCapeLocation();
            getCape = player -> PlayerHandler.get(player.getCapeHolder().getWCUUID());
            ModBase.LOGGER.info("Using 'get(UUID)' method for MinecraftCapes.");
            return;
        } catch (Throwable ex) {
            // ignore
        }

        //spotless:off
        //#if MC < 12102
        //$$for (Method m : PlayerHandler.class.getMethods()) {
        //$$    try {
        //$$        if (m.getReturnType() != PlayerHandler.class && m.getParameterCount() == 1
        //$$                && m.getParameterTypes()[0] != UUID.class) {
        //$$            continue;
        //$$        }
        //$$        m.invoke(null, test);
        //$$        getCape = player -> {
        //$$            try {
        //$$                return (PlayerHandler) m.invoke(null, player);
        //$$            } catch (IllegalAccessException | InvocationTargetException e) {
        //$$                return null;
        //$$            }
        //$$        };
        //$$        ModBase.LOGGER.info("Using '" + m.getName() + "' method for MinecraftCapes.");
        //$$        return;
        //$$    } catch (Throwable ex) {
        //$$        // ignore, MinecraftCapes wont work
        //$$    }
        //$$}
        //#endif
        //spotless:on
        getCape = player -> null;
        ModBase.LOGGER.info("Unable to find a method for MinecraftCapes.");
    }

    @Override
    public boolean shouldBeUsed(CapeRenderInfo capeRenderInfo) {
        if (!MinecraftCapesConfig.isCapeVisible())
            return false;
        if (getCape == null)
            init(capeRenderInfo);
        PlayerHandler handler = getCape.apply(capeRenderInfo);
        return handler != null && handler.getCapeLocation() != null;
    }

    @Override
    public CapeRenderer getRenderer() {
        return render;
    }

    private class MinecraftCapesRenderer implements CapeRenderer {

        @Override
        public void render(CapeRenderInfo capeRenderInfo, int part, ModelPart model, PoseStack poseStack,
                MultiBufferSource multiBufferSource, int light, int overlay) {
            PlayerHandler playerHandler = getCape.apply(capeRenderInfo);
            VertexConsumer vertexConsumer;
            if (MinecraftCapesConfig.isCapeVisible() && playerHandler.getCapeLocation() != null) {
                // spotless:off
                //#if MC >= 12100
                vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource,
                        RenderType.armorCutoutNoCull(playerHandler.getCapeLocation()),
                        playerHandler.getHasCapeGlint());
              //#else
                //$$ vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource,
                //$$         RenderType.armorCutoutNoCull(playerHandler.getCapeLocation()), false,
                //$$         playerHandler.getHasCapeGlint());
                //#endif
                //spotless:on
            } else {
                // spotless:off
                //#if MC >= 12100
                vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource,
                        RenderType.armorCutoutNoCull(capeRenderInfo.getCapeTexture()), false);
              //#else
              //$$  vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource,
              //$$          RenderType.armorCutoutNoCull(capeRenderInfo.getCapeTexture()), false, false);
              //#endif
                //spotless:on
            }
            model.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        }

        @Override
        public VertexConsumer getVertexConsumer(MultiBufferSource multiBufferSource, CapeRenderInfo capeRenderInfo) {
            PlayerHandler playerHandler = getCape.apply(capeRenderInfo);
            if (MinecraftCapesConfig.isCapeVisible() && playerHandler.getCapeLocation() != null) {
                // spotless:off
                //#if MC >= 12100
                return ItemRenderer.getArmorFoilBuffer(multiBufferSource,
                        RenderType.armorCutoutNoCull(playerHandler.getCapeLocation()),
                        playerHandler.getHasCapeGlint());
              //#else
              //$$  return ItemRenderer.getArmorFoilBuffer(multiBufferSource,
              //$$          RenderType.armorCutoutNoCull(playerHandler.getCapeLocation()), false,
              //$$          playerHandler.getHasCapeGlint());
              //#endif
                //spotless:on
            } else {
                // spotless:off
                //#if MC >= 12100
                return ItemRenderer.getArmorFoilBuffer(multiBufferSource,
                        RenderType.armorCutoutNoCull(capeRenderInfo.getCapeTexture()), false);
              //#else
              //$$  return ItemRenderer.getArmorFoilBuffer(multiBufferSource,
              //$$          RenderType.armorCutoutNoCull(capeRenderInfo.getCapeTexture()), false, false);
              //#endif
                //spotless:on
            }
        }

        @Override
        public boolean vanillaUvValues() {
            return true;
        }

    }

    @Override
    public boolean blockFeatureRenderer(Object feature) {
        return false;
    }

}
