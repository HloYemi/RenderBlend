package committee.nova.mods.renderblender.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.renderblender.RenderBlenderLib;
import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.renderblender.client.shader.RBShaders.UNSTABLE_UVS;

public class UnstableBakeModel extends WrappedItemModel {
    private final List<ResourceLocation> maskSprite;

    public UnstableBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
        super(wrapped);
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

        this.renderWrapped(stack, pStack, source, light, overlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        if (committee.nova.mods.renderblender.client.compat.IrisCompat.shouldSkipCustomShaderRendering()) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0f, pitch = 0.0f, scale = 1f;
        if (RBShaders.inventoryRender || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        RBShaders.unstableTime.set((System.currentTimeMillis() - RBShaders.renderTime) / 2000.0F);
        RBShaders.unstableYaw.set(yaw);
        RBShaders.unstablePitch.set(pitch);
        RBShaders.unstableExternalScale.set(scale);
        RBShaders.unstableOpacity.set(1.5F);

        for (int i = 0; i < 10; ++i) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(RenderBlenderLib.rl("misc/cosmic_" + i));
            UNSTABLE_UVS[i * 4] = sprite.getU0();
            UNSTABLE_UVS[i * 4 + 1] = sprite.getV0();
            UNSTABLE_UVS[i * 4 + 2] = sprite.getU1();
            UNSTABLE_UVS[i * 4 + 3] = sprite.getV1();
        }

        if (RBShaders.unstableUVs != null) {
            RBShaders.unstableUVs.set(UNSTABLE_UVS);
        }

        final VertexConsumer cons = source.getBuffer(RBRenderTypes.UNSTABLE);
        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            atlasSprite.add(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }
        mc.getItemRenderer().renderQuadList(pStack, cons, bakeItemFromLocations(maskSprite), stack, light, overlay);
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