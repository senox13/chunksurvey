package io.yog_sothoth.chunksurvey;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class SurveyCommand {
	private static final int MAX_RADIUS = 9;
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("survey").requires(source->source.hasPermissionLevel(2))
			//survey <block> <radius>
			.then(Commands.argument("block", BlockStateArgument.blockState())
			.then(Commands.argument("radius", IntegerArgumentType.integer(0, MAX_RADIUS))
			.executes(ctx -> {
				return SurveyChunk(ctx.getSource(), new ChunkPos(ctx.getSource().assertIsEntity().getPosition()), BlockStateArgument.getBlockState(ctx, "block"), IntegerArgumentType.getInteger(ctx, "radius"));
			})))
			//survey <target> <block> <radius>
			.then(Commands.argument("target", EntityArgument.entity())
			.then(Commands.argument("block", BlockStateArgument.blockState())
			.then(Commands.argument("radius", IntegerArgumentType.integer(0, MAX_RADIUS))
			.executes(ctx -> {
				return SurveyChunk(ctx.getSource(), EntityArgument.getEntity(ctx, "target"), BlockStateArgument.getBlockState(ctx, "block"), IntegerArgumentType.getInteger(ctx, "radius"));
			}))))
			//survey <chunkX> <chunkY> <block> <radius>
			.then(Commands.argument("chunkX", IntegerArgumentType.integer())
			.then(Commands.argument("chunkZ", IntegerArgumentType.integer())
			.then(Commands.argument("block", BlockStateArgument.blockState())
			.then(Commands.argument("radius", IntegerArgumentType.integer(0, MAX_RADIUS))
			.executes(ctx -> {
				ChunkPos targetChunkPos = new ChunkPos(IntegerArgumentType.getInteger(ctx, "chunkX"), IntegerArgumentType.getInteger(ctx, "chunkZ"));
				return SurveyChunk(ctx.getSource(), targetChunkPos, BlockStateArgument.getBlockState(ctx, "block"), IntegerArgumentType.getInteger(ctx, "radius"));
			})))))
		);
	}
	
	private static int SurveyChunk(CommandSource source, Entity target, BlockStateInput blockInput, int radius) {
		return SurveyChunk(source, new ChunkPos(target.getPosition()), blockInput, radius);
	}
	
	private static int SurveyChunk(CommandSource source, ChunkPos targetChunkPos, BlockStateInput blockInput, int radius) {
		long startTimeMS = Util.milliTime();
		
		ChunkPos startChunkPos = new ChunkPos(targetChunkPos.x - radius, targetChunkPos.z - radius);
		ChunkPos endChunkPos = new ChunkPos(targetChunkPos.x + radius, targetChunkPos.z + radius);
		BlockPos startPos = new BlockPos(startChunkPos.getXStart(), 0, startChunkPos.getZStart());
		BlockPos endPos = new BlockPos(endChunkPos.getXEnd(), 255, endChunkPos.getZEnd());
		BlockState targetState = blockInput.getState();
		int chunkCount = (int)Math.pow(radius * 2 + 1, 2);
		
		ServerWorld world = source.getWorld();
		int count = SurveyRange(world, targetState, startPos, endPos);
		
		long endTimeMS = Util.milliTime();
		source.sendFeedback(new TranslationTextComponent("commands.chunksurvey.results", count, targetState.getBlock().getRegistryName(), chunkCount, endTimeMS - startTimeMS), false);
		return count;
	}
	
	private static int SurveyRange(ServerWorld world, BlockState surveyTarget, BlockPos startPos, BlockPos endPos){
		int count = 0;
		for(int x=startPos.getX(); x<=endPos.getX(); x++) {
			for(int y=startPos.getY(); y<=endPos.getY(); y++) {
				for(int z=startPos.getZ(); z<=endPos.getZ(); z++) {
					BlockState state = world.getBlockState(new BlockPos(x, y, z));
					if(surveyTarget.equals(state)) {
						count++;
					}
				}
			}
		}
		return count;
	}
}
