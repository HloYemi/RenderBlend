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
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class HaloEternalModelLoader extends BaseModelLoader<HaloEternalModelLoader.HaloEternalGeometry> {
    public static final HaloEternalModelLoader INSTANCE = new HaloEternalModelLoader();

    @Override
    public @NotNull HaloEternalGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "halo", "eternal"), BlockModel.class);
        List<ResourceLocation> maskTextures = getMasks(modelContents, "eternal");
        HaloSetting setting = getHalo(modelContents);
        return new HaloEternalGeometry(baseModel, maskTextures, setting);
    }

    public static class HaloEternalGeometry extends BaseGeometry<HaloEternalGeometry> {
        private final List<ResourceLocation> maskTextures;
        private final HaloSetting setting;

        public HaloEternalGeometry(final BlockModel baseModel, final List<ResourceLocation> maskTextures, final HaloSetting setting) {
            super(baseModel);
            this.maskTextures = maskTextures;
            this.setting = setting;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker,
                               @NotNull Function<Material, @NotNull TextureAtlasSprite> spriteGetter,
                               @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
            BakedModel baseBakedModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            if (baseBakedModel.getQuads(null, null, RandomSource.create()).isEmpty()) {
                Material layer0 = this.baseModel.getMaterial("layer0");
                if (layer0 != null) {
                    TextureAtlasSprite sprite = spriteGetter.apply(layer0);
                    List<net.minecraft.client.renderer.block.model.BakedQuad> quads = committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel.bakeItem(List.of(sprite));
                    baseBakedModel = new SimpleBakedModel(quads, new HashMap<>(), true, false, true, sprite, this.baseModel.getTransforms(), ItemOverrides.EMPTY);
                }
            }
            Material particleLocation = this.baseModel.getMaterial(this.setting.texture());
            TextureAtlasSprite particle = spriteGetter.apply(particleLocation != null ? particleLocation : this.baseModel.getMaterial("layer0"));
            
            return new HaloEternalBakedModel(HaloUtils.tintLayers(baseBakedModel, this.setting.layerColors()), particle, this.setting.color(), this.setting.size(), this.setting.pulse(), maskTextures);
        }
    }
}