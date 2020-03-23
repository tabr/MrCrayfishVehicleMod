package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.common.inventory.IAttachableChest;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageAttachChest implements IMessage<MessageAttachChest>
{
    private int entityId;

    public MessageAttachChest() {}

    public MessageAttachChest(int entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public void encode(MessageAttachChest message, PacketBuffer buffer)
    {
        buffer.writeVarInt(message.entityId);
    }

    @Override
    public MessageAttachChest decode(PacketBuffer buffer)
    {
        return new MessageAttachChest(buffer.readVarInt());
    }

    @Override
    public void handle(MessageAttachChest message, Supplier<NetworkEvent.Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayerEntity player = supplier.get().getSender();
            if(player != null)
            {
                World world = player.world;
                Entity targetEntity = world.getEntityByID(message.entityId);
                if(targetEntity instanceof IAttachableChest)
                {
                    float reachDistance = (float) player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
                    if(player.getDistance(targetEntity) < reachDistance)
                    {
                        IAttachableChest attachableChest = (IAttachableChest) targetEntity;
                        if(!attachableChest.hasChest())
                        {
                            ItemStack stack = player.inventory.getCurrentItem();
                            if(!stack.isEmpty() && stack.getItem() == Items.CHEST)
                            {
                                attachableChest.attachChest(stack);
                                world.playSound(null, targetEntity.posX, targetEntity.posY, targetEntity.posZ, SoundType.WOOD.getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                    }
                }
            }
        });
        supplier.get().setPacketHandled(true);
    }
}
