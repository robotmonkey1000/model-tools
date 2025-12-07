package studio.robotmonkey.model_tools.packets;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import studio.robotmonkey.model_tools.ModelTools;

public class Packets {
    public record ConnectClient2S(BlockPos pos) implements CustomPacketPayload {
        public static final ResourceLocation CONNECTING_CLIENT_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(ModelTools.MOD_ID, "connect_client_to_server");
        public static final CustomPacketPayload.Type<ConnectClient2S> ID = new CustomPacketPayload.Type<>(CONNECTING_CLIENT_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ConnectClient2S> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ConnectClient2S::pos, ConnectClient2S::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ConnectServer2C(BlockPos pos) implements CustomPacketPayload {
        public static final ResourceLocation CONNECTING_CLIENT_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(ModelTools.MOD_ID, "connect_server_to_client");
        public static final CustomPacketPayload.Type<ConnectServer2C> ID = new CustomPacketPayload.Type<>(CONNECTING_CLIENT_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ConnectServer2C> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ConnectServer2C::pos, ConnectServer2C::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record ApplyOperation(ItemOperation operation, ResourceLocation model, EquipmentSlot slot) implements CustomPacketPayload {

        public enum ItemOperation { ITEM, ARMOR }
        public static final ResourceLocation APPLY_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(ModelTools.MOD_ID, "apply_operation");
        public static final CustomPacketPayload.Type<ApplyOperation> ID = new CustomPacketPayload.Type<>(APPLY_PAYLOAD_ID);

        public static final StreamCodec<FriendlyByteBuf, ItemOperation> ITEM_OPERATION_STREAM_CODEC =
                StreamCodec.of(
                        (buf, value) -> buf.writeVarInt(value.ordinal()),
                        buf -> ItemOperation.values()[buf.readVarInt()]
                );
        public static final StreamCodec<RegistryFriendlyByteBuf, ApplyOperation> CODEC = StreamCodec.composite(ITEM_OPERATION_STREAM_CODEC, ApplyOperation::operation, ResourceLocation.STREAM_CODEC, ApplyOperation::model, EquipmentSlot.STREAM_CODEC, ApplyOperation::slot, ApplyOperation::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
