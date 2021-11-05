package dev.tr7zw.waveycapes.mixin;

import java.util.NoSuchElementException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.tr7zw.waveycapes.accessor.PlayerEntityModelAccessor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> implements PlayerEntityModelAccessor {


    public PlayerModelMixin(float f) {
        super(f);
    }

    private ModelPart[] customCape = new ModelPart[16];

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onCreate(CallbackInfo info) {
        try {
            for (int i = 0; i < 16; i++) {
                ModelPart base = new ModelPart(64, 32, 0, i);
                this.customCape[i] = base.addBox(-5.0F, (float)i, -1.0F, 10.0F, 1.0F, 1F);
            }
        }catch(NoSuchElementException ex) {
            // Do nothing. The "MinecraftCapes Mod" somehow causes Piglins to call this code?!?
        }
    }

    @Inject(method = "renderCloak", at = @At("HEAD"), cancellable = true)
    public void renderCloak(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, CallbackInfo info) {
        info.cancel();
    }

    @Override
    public ModelPart[] getCustomCapeParts() {
        return customCape;
    }

}
