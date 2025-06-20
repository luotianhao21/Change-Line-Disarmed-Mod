package com.baqian.changelinedisarmed;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TripWireBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("changelinedisarmed")
public class ChangeLineDisarmed
{
    // Directly reference a log4j logger.
    // private static final Logger LOGGER = LogManager.getLogger();

    public ChangeLineDisarmed() {
        // 注册我们以监听服务器和其他游戏事件
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void sendMessageToSpecificPlayer(ServerPlayerEntity player, String message) {
        player.connection.send(new SChatPacket(new StringTextComponent(message), ChatType.SYSTEM, player.getUUID()));
    }

    @SubscribeEvent
    public int onServerChat(ServerChatEvent event) {
        if (event.getMessage().equalsIgnoreCase("!!changeline")) {
            // 改变线的Disarmed值
            ServerPlayerEntity player = event.getPlayer();

            // 获取玩家面前的第一个非空气方块
            // 获取玩家位置
            Vector3d playerPos = player.getPosition(1.0F); // pPartialTicks代表时间因子，1.0F代表当前时间，0.0F是上个游戏刻
            Vector3d offset = new Vector3d(0.0, 1.55, 0.0); // 眼睛偏移量
            // 获取玩家眼睛的位置
            Vector3d playerEyePos = playerPos.add(offset);
            // 获取玩家视角方向
            Vector3d playerView = player.getViewVector(1.0F);

            Vector3d blockPos = playerEyePos;
            Block block = Blocks.AIR;

            // 遍历接下来玩家视角方向的5个方块（精细度1/100）
            for (int i = 0; i < 500; i++) {
                double j = (double) i / 100;

                // 获取玩家视角方向的方块位置
                blockPos = playerEyePos.add(playerView.x() * j, playerView.y() * j, playerView.z() * j);
                // 获取方块
                block = player.level.getBlockState(new BlockPos(blockPos.x, blockPos.y, blockPos.z)).getBlock();

                if (block != Blocks.AIR) {
                    break;
                }
            }

            if (block == Blocks.TRIPWIRE) {
                BlockPos pos = new BlockPos(blockPos);
                player.level.setBlock(pos, player.level.getBlockState(pos).setValue(TripWireBlock.DISARMED, true), 3);
                sendMessageToSpecificPlayer(player, "<ChangeLineDisarmed> | 已将该绊线的Disarmed设置为true！");
                return 0;
            }
            // 将blockPos的坐标都转换为整数
            Vector3d blockPosInt = new Vector3d(MathHelper.floor(blockPos.x), MathHelper.floor(blockPos.y), MathHelper.floor(blockPos.z));

            sendMessageToSpecificPlayer(player, "<ChangeLineDisarmed> | 您所面对的并不是绊线！\n> " + block.getStateDefinition().toString() + "@" + blockPosInt.toString());
        }

        return 0;
    }
}
