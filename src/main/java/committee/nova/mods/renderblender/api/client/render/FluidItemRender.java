package committee.nova.mods.renderblender.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class FluidItemRender {
    private final ResourceLocation texture;
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255, g = 255, b = 255, a = 255;
    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;

    public FluidItemRender(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    public static FluidItemRender sprite(TextureAtlasSprite sprite) {
        final int refSize = Integer.MAX_VALUE / 2;
        return new FluidItemRender(sprite.atlasLocation(), refSize, refSize).src(
                (int) (sprite.getU0() * refSize), (int) (sprite.getV0() * refSize),
                (int) ((sprite.getU1() - sprite.getU0()) * refSize),
                (int) ((sprite.getV1() - sprite.getV0()) * refSize)
        );
    }

    public FluidItemRender src(int x, int y, int w, int h) { this.srcRect = new Rect2i(x, y, w, h); return this; }
    public FluidItemRender dest(int x, int y, int w, int h) { this.destRect = new Rect2i(x, y, w, h); return this; }
    public FluidItemRender color(float r, float g, float b) { this.r = (int) (Mth.clamp(r, 0, 1) * 255); this.g = (int) (Mth.clamp(g, 0, 1) * 255); this.b = (int) (Mth.clamp(b, 0, 1) * 255); return this; }
    public FluidItemRender opacity(float a) { this.a = (int) (Mth.clamp(a, 0, 1) * 255); return this; }
    public FluidItemRender blending(boolean enable) { this.blending = enable; return this; }
    public FluidItemRender colorRgb(int packedRgb) {
        return color((packedRgb >> 16 & 255) / 255.0F, (packedRgb >> 8 & 255) / 255.0F, (packedRgb & 255) / 255.0F);
    }

    public void blit(PoseStack poseStack, int zIndex) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.texture);
        float minU = 0, minV = 0, maxU = 1, maxV = 1;
        if (srcRect != null) {
            minU = srcRect.getX() / (float) referenceWidth;
            minV = srcRect.getY() / (float) referenceHeight;
            maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
            maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
        }
        float x1 = destRect.getX(), y1 = destRect.getY(), x2 = x1, y2 = y1;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            x2 += destRect.getWidth();
            y2 += destRect.getHeight();
        } else if (srcRect != null) {
            x2 += srcRect.getWidth();
            y2 += srcRect.getHeight();
        }
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix, x1, y2, zIndex).setUv(minU, maxV).setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x2, y2, zIndex).setUv(maxU, maxV).setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x2, y1, zIndex).setUv(maxU, minV).setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x1, y1, zIndex).setUv(minU, minV).setColor(r, g, b, a);
        
        // 1.21.1 渲染管道使用 BufferUploader
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void renderFluid(FluidStack fluidStack, PoseStack poseStack, int x, int y, int z) {
        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture(fluidStack));
        sprite(sprite).colorRgb(attributes.getTintColor(fluidStack)).blending(false).dest(x, y, 16, 16).blit(poseStack, z);
    }
}