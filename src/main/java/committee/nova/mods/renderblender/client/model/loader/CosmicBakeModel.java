package committee.nova.mods.renderblender.client.model.loader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.renderblender.api.client.render.CosmicRenderCall;
import committee.nova.mods.renderblender.api.client.render.CosmicRenderQueue;
import committee.nova.mods.renderblender.api.iface.transform.CosmicRenderable;
import committee.nova.mods.renderblender.client.compat.IrisCompat;
import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;
import committee.nova.mods.renderblender.api.iface.transform.IBowTransform;
import committee.nova.mods.renderblender.api.iface.transform.IToolTransform;
import committee.nova.mods.renderblender.client.shader.RBRenderTypes;
import committee.nova.mods.renderblender.client.shader.RBShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.renderblender.client.shader.RBShaders.COSMIC_UVS;

public class CosmicBakeModel extends WrappedItemModel implements CosmicRenderable {
    private final List<ResourceLocation> maskSprite;

    public CosmicBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
        this.cosmic = true;
    }

    @Override
    public void renderItem(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int packedLight,
            int packedOverlay
    ) {
        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() instanceof IBowTransform) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }

        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);

        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        if (committee.nova.mods.renderblender.client.compat.IrisCompat.shouldSkipCustomShaderRendering()) {
            return;
        }

        if (IrisCompat.shouldDefer(transformType)) {
            CosmicRenderQueue.enqueue(
                    new CosmicRenderCall(
                            this, stack, transformType, pStack,
                            packedLight, packedOverlay,
                            RenderSystem.getProjectionMatrix(),
                            RenderSystem.getModelViewMatrix()
                    )
            );
            return;
        }

        renderCosmicLayer(stack, transformType, pStack, source, packedLight, packedOverlay);
    }

    @Override
    public void renderCosmicLayer(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int packedLight,
            int packedOverlay
    ) {
        if (RBShaders.COSMIC_SHADER == null) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();

        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;

        if (RBShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else if (mc.player != null) {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        RBShaders.cosmicTime.set((System.currentTimeMillis() - RBShaders.renderTime) / 2000.0F);
        RBShaders.cosmicYaw.set(yaw);
        RBShaders.cosmicPitch.set(pitch);
        RBShaders.cosmicExternalScale.set(scale);
        RBShaders.cosmicOpacity.set(1.0F);

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(RBShaders.rl("misc/cosmic_" + i));
            COSMIC_UVS[i * 4]     = sprite.getU0();
            COSMIC_UVS[i * 4 + 1] = sprite.getV0();
            COSMIC_UVS[i * 4 + 2] = sprite.getU1();
            COSMIC_UVS[i * 4 + 3] = sprite.getV1();
        }

        if (RBShaders.cosmicUVs != null) {
            RBShaders.cosmicUVs.set(COSMIC_UVS);
        }

        final VertexConsumer cons = source.getBuffer(RBRenderTypes.COSMIC);

        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            atlasSprite.add(mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }

        mc.getItemRenderer().renderQuadList(
                pStack, cons, bakeItemFromLocations(maskSprite),
                stack, packedLight, packedOverlay
        );

        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(RBRenderTypes.COSMIC);
        }

        if (committee.nova.mods.renderblender.client.compat.IrisCompat.shouldSkipCustomShaderRendering()) {
            return;
        }
    }
}
