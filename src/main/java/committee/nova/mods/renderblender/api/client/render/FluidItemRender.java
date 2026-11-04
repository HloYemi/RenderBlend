package committee.nova.mods.renderblender.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

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

/**
 * 流体物品渲染工具(1.21.1 版)。
 * <p>
 * 把指定流体贴图渲染到 PoseStack 指定的屏幕/世界位置,用于自定义流体桶、
 * 流体容器 GUI、流体方块图标等场景。原实现基于 1.20.1 的
 * {@code Tesselator.getBuilder().begin(...)} + {@code vertex(...).uv(...).color(...).endVertex()},
 * 1.21.1 的 API 改为 {@code Tesselator.begin(mode, format)} +
 * {@code addVertex(matrix, x, y, z).setUv(u, v).setColor(r, g, b, a)} +
 * {@code BufferUploader.drawWithShader(meshData)}。
 * <p>
 * 使用示例:
 * <pre>{@code
 * FluidItemRender.renderFluid(fluidStack, poseStack, x, y, z);
 * }</pre>
 * <p>
 * 或链式构建:
 * <pre>{@code
 * FluidItemRender.sprite(sprite)
 *     .colorRgb(tintColor)
 *     .blending(false)
 *     .dest(x, y, 16, 16)
 *     .blit(poseStack, z);
 * }</pre>
 */
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

    /**
     * 从图集精灵构造(以 sprite 的 UV 范围作为源矩形)。
     * <p>
     * refSize 用大整数避免浮点精度问题,把 0..1 UV 缩放为整数像素坐标后立即
     * 在 blit 时又除回来,数学上等价于直接存浮点 UV。
     *
     * @param sprite 图集精灵(流体 still/flowing 贴图)
     * @return FluidItemRender 实例,可继续链式配置
     */
    public static FluidItemRender sprite(TextureAtlasSprite sprite) {
        final int refSize = Integer.MAX_VALUE / 2;
        return new FluidItemRender(sprite.atlasLocation(), refSize, refSize).src(
                (int) (sprite.getU0() * refSize), (int) (sprite.getV0() * refSize),
                (int) ((sprite.getU1() - sprite.getU0()) * refSize),
                (int) ((sprite.getV1() - sprite.getV0()) * refSize)
        );
    }

    /** 设置源矩形(纹理像素坐标)。 */
    public FluidItemRender src(int x, int y, int w, int h) {
        this.srcRect = new Rect2i(x, y, w, h);
        return this;
    }

    /** 设置目标矩形(屏幕/GUI 像素坐标)。 */
    public FluidItemRender dest(int x, int y, int w, int h) {
        this.destRect = new Rect2i(x, y, w, h);
        return this;
    }

    /** 设置 RGB 颜色(0..1)。 */
    public FluidItemRender color(float r, float g, float b) {
        this.r = (int) (Mth.clamp(r, 0, 1) * 255);
        this.g = (int) (Mth.clamp(g, 0, 1) * 255);
        this.b = (int) (Mth.clamp(b, 0, 1) * 255);
        return this;
    }

    /** 设置 alpha(0..1)。 */
    public FluidItemRender opacity(float a) {
        this.a = (int) (Mth.clamp(a, 0, 1) * 255);
        return this;
    }

    /** 是否启用 alpha 混合(默认 true)。 */
    public FluidItemRender blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    /** 从打包 RGB 整数设置颜色(0xRRGGBB)。 */
    public FluidItemRender colorRgb(int packedRgb) {
        return color((packedRgb >> 16 & 255) / 255.0F,
                (packedRgb >> 8 & 255) / 255.0F,
                (packedRgb & 255) / 255.0F);
    }

    /**
     * 执行绘制(在当前 PoseStack 栈顶位置)。
     * <p>
     * 使用 vanilla 的 {@code PositionTexColor} shader,把流体贴图以 QUADS 模式
     * 绘制到 {@code destRect} 指定的矩形区域。1.21.1 的 {@link BufferBuilder}
     * 不再有 {@code begin/end} 方法,而是由 {@link Tesselator#begin} 直接返回
     * 已初始化的 builder;{@code build()} 产出的 {@link MeshData} 由
     * {@link BufferUploader#drawWithShader} 提交到 GPU。
     *
     * @param poseStack PoseStack(取栈顶矩阵做顶点变换)
     * @param zIndex    Z 深度(GUI 中通常用 0,越大的越靠前)
     */
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
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, x1, y2, zIndex).setUv(minU, maxV).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, zIndex).setUv(maxU, maxV).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y1, zIndex).setUv(maxU, minV).setColor(r, g, b, a);
        buffer.addVertex(matrix, x1, y1, zIndex).setUv(minU, minV).setColor(r, g, b, a);

        if (blending) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }

        MeshData meshData = buffer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        if (blending) {
            RenderSystem.disableBlend();
        }
    }

    /**
     * 一次性渲染流体到指定位置(快捷方法)。
     * <p>
     * 自动从 {@link IClientFluidTypeExtensions} 获取流体 still 贴图与 tint 色,
     * 绘制 1616 的流体方块图标。适用于 GUI 中的流体显示槽。
     *
     * @param fluidStack 流体栈(提供流体类型与 NBT 数据)
     * @param poseStack  PoseStack
     * @param x          GUI 中左上角 X 坐标
     * @param y          GUI 中左上角 Y 坐标
     * @param z          Z 深度
     */
    public static void renderFluid(FluidStack fluidStack, PoseStack poseStack, int x, int y, int z) {
        IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(attributes.getStillTexture(fluidStack));
        sprite(sprite)
                .colorRgb(attributes.getTintColor(fluidStack))
                .blending(false)
                .dest(x, y, 16, 16)
                .blit(poseStack, z);
    }
}