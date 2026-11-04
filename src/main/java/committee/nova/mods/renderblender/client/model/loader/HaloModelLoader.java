package committee.nova.mods.renderblender.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import committee.nova.mods.renderblender.client.model.loader.base.BaseGeometry;
import committee.nova.mods.renderblender.client.model.loader.base.BaseModelLoader;
import committee.nova.mods.renderblender.client.model.loader.base.HaloSetting;
import committee.nova.mods.renderblender.client.model.loader.base.HaloUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Name: RenderBlenderLib-forge / HaloItemModelLoader
 * Author: cnlimiter
 * CreateTime: 2023/9/18 23:45
 * Description:
 */

public class HaloModelLoader extends BaseModelLoader<HaloModelLoader.HaloItemModelGeometry> {

    public static final HaloModelLoader INSTANCE = new HaloModelLoader();

    @Override
    public @NotNull HaloItemModelGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "halo"), BlockModel.class);
        HaloSetting setting = getHalo(modelContents);
        return new HaloItemModelGeometry(baseModel, setting);
    }


    public static class HaloItemModelGeometry extends BaseGeometry<HaloItemModelGeometry> {
        private final HaloSetting setting;

        public HaloItemModelGeometry(final BlockModel baseModel, final HaloSetting setting) {
            super(baseModel);
            this.setting = setting;
        }

        @Override
        public @NotNull BakedModel bake(final @NotNull IGeometryBakingContext owner, final @NotNull ModelBaker bakery,
                                        final @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                        final @NotNull ModelState modelTransform, final @NotNull ItemOverrides overrides) {
            BakedModel finalModel = this.baseModel.bake(bakery, this.baseModel, spriteGetter, modelTransform, false);
            // --- [RenderBlender] 1.21.1 兼容底模生成 ---
            finalModel = finalModel == null ? null : finalModel; // 确保不是 final
 (finalModel.getQuads(null, null, net.minecraft.util.RandomSource.create()).isEmpty()) {
                net.minecraft.client.resources.model.Material layer0 = this.baseModel.getMaterial("layer0");
                if (layer0 != null) {
                    net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = spriteGetter.apply(layer0);
                    java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> quads = 
                        committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel.bakeItem(java.util.List.of(sprite));
                    finalModel = new net.minecraft.client.resources.model.SimpleBakedModel(
                        quads, new java.util.HashMap<>(), true, false, true, sprite, 
                        this.baseModel.getTransforms(), 
                        net.minecraft.client.renderer.block.model.ItemOverrides.EMPTY
                    );
                }
            }
            // ----------------------------------------
            Material particleLocation = this.baseModel.getMaterial(this.setting.texture());
            TextureAtlasSprite particle = spriteGetter.apply(particleLocation);
            return new HaloBakedModel(HaloUtils.tintLayers(finalModel, this.setting.layerColors()), particle, this.setting.color(), this.setting.size(), this.setting.pulse());
        }
    }
}
