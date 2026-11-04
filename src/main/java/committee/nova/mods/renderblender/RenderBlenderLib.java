package committee.nova.mods.renderblender;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(RenderBlenderLib.MOD_ID)
public class RenderBlenderLib {
    public static final String MOD_ID = "renderblender";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public RenderBlenderLib() {
        LOGGER.info("[RenderBlender] Initialized (NeoForge 1.21.1)");
    }
}
