package committee.nova.mods.renderblender.api.client.model.bakedmodels;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mods.renderblender.api.client.model.PerspectiveModel;
import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public abstract class WrappedItemModel implements PerspectiveModel {

    public static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    public static final FaceBakery FACE_BAKERY = new FaceBakery();

    /** bakeItemFromLocations 的 quad 缓存(按 location 列表为 key;WeakHashMap 弱引用,模型被回收即释放)。 */
    private static final Map<List<ResourceLocation>, List<BakedQuad>> BAKE_QUAD_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    protected BakedModel wrapped;
    protected PerspectiveModelState parentState;
    protected boolean cosmic = false;
    @Nullable
    protected LivingEntity entity;
    @Nullable
    protected ClientLevel world;
    protected ItemOverrides overrideList;

    public WrappedItemModel(BakedModel wrapped) {
        this.overrideList = new ItemOverrides() {
            @Override
            public BakedModel resolve(final @NotNull BakedModel originalModel, final @NotNull ItemStack stack, final ClientLevel world, final LivingEntity entity, final int seed) {
                WrappedItemModel.this.entity = entity;
                WrappedItemModel.this.world = ((world == null) ? ((entity == null) ? null : ((ClientLevel) entity.level())) : null);
                if (WrappedItemModel.this.cosmic) {
                    return WrappedItemModel.this.wrapped.getOverrides().resolve(originalModel, stack, world, entity, seed);
                }
                return originalModel;
            }
        };
        this.wrapped = wrapped;
        this.parentState = TransformUtils.DEFAULT_ITEM;
    }

    /**
     * 按 maskSprite location 列表生成/缓存 quad 列表。
     * <p>
     * 与原 bakeItem 的区别:原方法每帧根据 sprite 实例重新生成 quad,但几何是帧无关的
     * (只依赖 sprite 内容与 index),完全可以缓存。此方法按 location 列表做 key
     * (maskSprite 是 final 字段,同一 BakedModel 复用同一列表实例),命中缓存直接返回。
     * <p>
     * WeakHashMap 弱引用:maskSprite 被 GC(BakedModel 被回收)时缓存自动清理。
     *
     * @param locations 贴图 location 列表(maskSprite)
     * @return 生成的 BakedQuad 列表(不可变)
     */
    public static List<BakedQuad> bakeItemFromLocations(final List<ResourceLocation> locations) {
        List<BakedQuad> cached = BAKE_QUAD_CACHE.get(locations);
        if (cached != null) {
            return cached;
        }
        List<BakedQuad> quads = new LinkedList<>();
        // getTextureAtlas(BLOCK_ATLAS) 返回 Function<ResourceLocation, TextureAtlasSprite>
        Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance()
                .getTextureAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
        for (int i = 0; i < locations.size(); i++) {
            TextureAtlasSprite sprite = atlas.apply(locations.get(i));
            if (sprite == null) {
                continue;
            }
            List<BlockElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, sprite.contents());
            for (BlockElement element : unbaked) {
                for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    BakedQuad quad = FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(),
                            sprite, entry.getKey(),
                            new PerspectiveModelState(ImmutableMap.of()),
                            element.rotation, element.shade);
                    if (quad != null) {
                        quads.add(quad);
                    }
                }
            }
        }
        List<BakedQuad> immutable = List.copyOf(quads);
        BAKE_QUAD_CACHE.put(locations, immutable);
        return immutable;
    }

    /** 资源重载 / 世界切换时清空 bake 缓存(由 RenderBlenderClient 或相关 rel 监听调用)。 */
    public static void invalidateBakeCache() {
        BAKE_QUAD_CACHE.clear();
    }

    public static List<BakedQuad> bakeItem(final List<TextureAtlasSprite> sprites) {
        final LinkedList<BakedQuad> quads = new LinkedList<>();
        for (final TextureAtlasSprite sprite : sprites) {
            if (sprite == null) {
                continue;
            }
            final List<BlockElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(
                    sprites.indexOf(sprite), "layer" + sprites.indexOf(sprite), sprite.contents());
            for (final BlockElement element : unbaked) {
                for (final Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    BakedQuad quad = FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(),
                            sprite, entry.getKey(),
                            new PerspectiveModelState(ImmutableMap.of()),
                            element.rotation, element.shade);
                    if (quad != null) {
                        quads.add(quad);
                    }
                }
            }
        }
        return quads;
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return this.parentState;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return this.wrapped.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers,
                                 int packedLight, int packedOverlay, boolean fabulous) {
        renderWrapped(stack, pStack, buffers, packedLight, packedOverlay, fabulous, Function.identity());
    }

    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers,
                                 int packedLight, int packedOverlay, boolean fabulous,
                                 Function<VertexConsumer, VertexConsumer> consOverride) {
        BakedModel model = this.wrapped.getOverrides().resolve(this.wrapped, stack, this.world, this.entity, 0);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (BakedModel bakedModel : model.getRenderPasses(stack, fabulous)) {
            for (RenderType rendertype : bakedModel.getRenderTypes(stack, fabulous)) {
                itemRenderer.renderModelLists(bakedModel, stack, packedLight, packedOverlay, pStack,
                        consOverride.apply(buffers.getBuffer(rendertype)));
            }
        }
    }
}