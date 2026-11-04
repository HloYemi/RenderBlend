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

import static committee.nova.mods.renderblender.client.shader.RBShaders.HELL_UVS;

/**
 * Halo + Hell 组合 BakeModel:光晕层 + 地狱特效层叠加。
 * <p>
 * 与 {@link HaloCosmicBakedModel} 结构完全对称,只是 cosmic 层替换为 hell 层
 * (使用 HELL_SHADER / hellTime / HELL_UVS)。
 */
public class HaloHellBakedModel extends WrappedItemModel {
    private final Random random;
    private final BakedQuad haloQuad;
    private final boolean pulse;
    private final List<ResourceLocation> maskSprite;

    public HaloHellBakedModel(BakedModel wrapped, TextureAtlasSprite sprite, int color, int size, boolean pulse, List<ResourceLocation> maskSprite) {
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
        } else if (stack.getItem() instanceof net.minecraft.world.item.SwordItem
                || stack.getItem() instanceof net.minecraft.world.item.DiggerItem
                || stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }

        if (transformType == ItemDisplayContext.GUI) {
            Minecraft.getInstance().getItemRenderer()
                    .renderQuadList(pStack, source.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)),
                            List.of(this.haloQuad), stack, light, overlay);
            if (this.pulse) {
                pStack.pushPose();
                double scale = random.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pStack.translate(trans, trans, 0.0D);
                pStack.scale((float) scale, (float) scale, 1.0001F);
                this.renderWrapped(stack, pStack, source, light, overlay, true,
                        (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pStack.popPose();
            }
        }

        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        if (committee.nova.mods.renderblender.client.compat.IrisCompat.shouldSkipCustomShaderRendering()) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;
        if (RBShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        RBShaders.hellTime.set((System.currentTimeMillis() - RBShaders.renderTime) / 2000.0F);
        RBShaders.hellYaw.set(yaw);
        RBShaders.hellPitch.set(pitch);
        RBShaders.hellExternalScale.set(scale);
        RBShaders.hellOpacity.set(1.0F);

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(RenderBlenderLib.rl("misc/cosmic_" + i));
            HELL_UVS[i * 4] = sprite.getU0();
            HELL_UVS[i * 4 + 1] = sprite.getV0();
            HELL_UVS[i * 4 + 2] = sprite.getU1();
            HELL_UVS[i * 4 + 3] = sprite.getV1();
        }
        if (RBShaders.hellUVs != null) {
            RBShaders.hellUVs.set(HELL_UVS);
        }

        final VertexConsumer cons = source.getBuffer(RBRenderTypes.HELL);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons,
                committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel.bakeItemFromLocations(maskSprite),
                stack, light, overlay);
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