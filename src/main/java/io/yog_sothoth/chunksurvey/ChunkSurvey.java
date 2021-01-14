package io.yog_sothoth.chunksurvey;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("chunksurvey")
public class ChunkSurvey {
    public ChunkSurvey() {
        MinecraftForge.EVENT_BUS.register(ChunkSurvey.class);
    }
    
    @SubscribeEvent
	static void registerCommands(RegisterCommandsEvent event) {
        SurveyCommand.register(event.getDispatcher());
    }
}
