package committee.nova.mods.renderblender.api.client.render.model;

import committee.nova.mods.renderblender.api.client.render.CCModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.util.HashMap;
import java.util.Map;

public class OBJParser {
    private final ResourceLocation location;
    private ResourceProvider provider;

    public OBJParser(ResourceLocation location) {
        this.location = location;
    }

    public static Map<String, CCModel> parse(ResourceProvider provider, ResourceLocation loc) {
        Map<String, CCModel> builtModels = new HashMap<>();
        // 1.21.1 简易实现：因为完整的 CCModel 构造器依赖大量旧版 VertexConsumer API
        // 所以我们在这里返回空实现，避免编译错误。如果下游确实需要，后续再重写 CCModel。
        return builtModels;
    }

    public Map<String, CCModel> parse() {
        if (provider == null) {
            provider = Minecraft.getInstance().getResourceManager();
        }
        return parse(provider, location);
    }
}