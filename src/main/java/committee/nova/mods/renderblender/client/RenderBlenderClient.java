package committee.nova.mods.renderblender.client;

import committee.nova.mods.renderblender.RenderBlenderLib;
import committee.nova.mods.renderblender.client.model.loader.CosmicModelLoader;
import committee.nova.mods.renderblender.client.model.loader.EternalModelLoader;
import committee.nova.mods.renderblender.client.model.loader.HellModelLoader;
import committee.nova.mods.renderblender.client.model.loader.UnstableModelLoader;
import committee.nova.mods.renderblender.client.model.loader.HaloModelLoader;
import committee.nova.mods.renderblender.client.model.loader.HaloCosmicModelLoader;
import committee.nova.mods.renderblender.client.model.loader.HaloEternalModelLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = RenderBlenderLib.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class RenderBlenderClient {

    @SubscribeEvent
    public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(RenderBlenderLib.rl("cosmic"), CosmicModelLoader.INSTANCE);
        event.register(RenderBlenderLib.rl("eternal"), EternalModelLoader.INSTANCE);
        event.register(RenderBlenderLib.rl("hell"), HellModelLoader.INSTANCE);
        event.register(RenderBlenderLib.rl("unstable"), UnstableModelLoader.INSTANCE);
        event.register(RenderBlenderLib.rl("halo"), HaloModelLoader.INSTANCE);
        event.register(RenderBlenderLib.rl("halo_cosmic"), HaloCosmicModelLoader.INSTANCE);
        event.register(RenderBlenderLib.rl("halo_eternal"), HaloEternalModelLoader.INSTANCE);
    }
}