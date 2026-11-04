package committee.nova.mods.renderblender.client.model.loader;


import committee.nova.mods.renderblender.client.model.loader.base.HaloUtils;import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.renderblender.RenderBlenderLib;
import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.renderblender.api.client.render.buffer.AlphaOverrideVertexConsumer;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;
import committee.nova.mods.renderblender.api.iface.transform.IBowTransform;
import committee.nova.mods.renderblender.api.iface.transform.IToolTransform;
import committee.nova.mods.renderblender.client.shader.RBRenderTypes;
import committee.nova.mods.renderblender.client.shader.RBShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static committee.nova.mods.renderblender.client.shader.RBShaders.COSMIC_UVS;

public class HaloCosmicBakedModel extends WrappedItemModel {
    private final Random random;
    private final BakedQuad haloQuad;
    private final boolean pulse;
    private final List<ResourceLocation> maskSprite;

    public HaloCosmicBakedModel(BakedModel wrapped, TextureAtlasSprite sprite, int color, int size, boolean pulse, List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.random = new Random();
        this.haloQuad = HaloUtils.generateHaloQuad(sprite, size, color);
        this.pulse = pulse;
        this.maskSprite = maskSprite;
        this.cosmic = true;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() instanceof IBowTransform) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }
        if (transformType == ItemDisplayContext.GUI) {
            Minecraft.getInstance().getItemRenderer()
                    .renderQuadList(pStack, source.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)), List.of(this.haloQuad), stack, light, overlay);
            if (this.pulse) {
                pStack.pushPose();
                double scale = random.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pStack.translate(trans, trans, 0.0D);
                pStack.scale((float) scale, (float) scale, 1.0001F);
                this.renderWrapped(stack, pStack, source, light, overlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pStack.popPose();
            }
        }

        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f, pitch = 0.0f, scale = 1f;
        if (RBShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        RBShaders.cosmicTime.set((System.currentTimeMillis() - RBShaders.renderTime) / 2000.0F);
        RBShaders.cosmicYaw.set(yaw);
        RBShaders.cosmicPitch.set(pitch);
        RBShaders.cosmicExternalScale.set(scale);
        RBShaders.cosmicOpacity.set(1.0F);

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(RenderBlenderLib.rl("misc/cosmic_" + i));
            COSMIC_UVS[i * 4] = sprite.getU0();
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
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItem(atlasSprite), stack, light, overlay);
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }

    @Override
    public boolean isCosmic() {
        return true;
    }
}