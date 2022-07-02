package net.torocraft.toroquest.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.gui.VillageLordGuiContainer;
import net.torocraft.toroquest.inventory.IVillageLordInventory;
import net.torocraft.toroquest.item.ItemTrophy;
import net.torocraft.toroquest.network.message.MessageQuestUpdate.DonationReward;

public class MessageSetItemReputationAmount implements IMessage
{

	public static enum MessageCode
	{
		EMPTY, NOTE, STOLEN_ITEM, DONATION, TROPHY
	};

	public int reputation = 0;
	public MessageCode messageCode = MessageCode.EMPTY;

	public MessageSetItemReputationAmount()
	{

	}

	public MessageSetItemReputationAmount( IVillageLordInventory inventory )
	{
		ItemStack item = inventory.getDonationItem();

		if ( item.isEmpty() )
		{
			reputation = 0;
			messageCode = MessageCode.EMPTY;
			return;
		}

		if ( isNoteForLord(inventory.getProvince(), item) )
		{
			reputation = 0;
			messageCode = MessageCode.NOTE;
			return;
		}

		if ( isStolenItemForProvince(inventory.getProvince(), item) )
		{
			reputation = ToroQuestConfiguration.donateArtifactRepGain;
			messageCode = MessageCode.STOLEN_ITEM;
			return;
		}

		if ( isTrophy(inventory.getProvince(), item) )
		{
			reputation = ToroQuestConfiguration.donateTrophyRepGain;
			messageCode = MessageCode.TROPHY;
			return;
		}

		DonationReward reward = MessageQuestUpdate.getRepForDonation(item);

		if ( reward != null )
		{
			reputation = reward.rep;
			messageCode = MessageCode.DONATION;
			return;
		}

		reputation = 0;
		messageCode = MessageCode.EMPTY;
	}

	public static boolean isStolenItemForProvince( Province inProvince, ItemStack stack )
	{
		try
		{
			if ( !stack.hasTagCompound() )
			{
				return false;
			}

			if ( inProvince == null )
			{
				return false;
			}

			String civName = stack.getTagCompound().getString("civilizationName");
			Boolean isStolen = stack.getTagCompound().getBoolean("isStolen");

			if ( isEmpty(civName) )
			{
				return false;
			}

			if ( !isStolen )
			{
				return false;
			}

			if ( inProvince.civilization.name().toString().equals(civName) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static boolean isTrophy( Province inProvince, ItemStack stack )
	{
		if ( inProvince == null )
		{
			return false;
		}

		if ( stack.getItem() instanceof ItemTrophy || stack.getItem() == Item.getByNameOrId("toroquest:legendary_bandit_helmet") || stack.getItem() == Item.getByNameOrId("toroquest:royal_helmet") )
		{
			return true;
		}

		return false;
	}

	public static boolean isNoteForLord( Province inProvince, ItemStack stack )
	{
		try
		{
			if ( stack.getItem() != Item.getByNameOrId("toroquest:lord_note") || !stack.hasTagCompound() )
			{
				return false;
			}

			if ( inProvince == null )
			{
				return false;
			}

			String toProvinceID = stack.getTagCompound().getString("toProvinceID");
			String questId = stack.getTagCompound().getString("questId");

			if ( isEmpty(toProvinceID) || isEmpty(questId) )
			{
				return false;
			}

			return inProvince.id.toString().equals(toProvinceID);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		reputation = buf.readInt();
		messageCode = e(buf.readInt());
	}

	private MessageCode e( int i )
	{
		try
		{
			return MessageCode.values()[i];
		}
		catch (Exception e)
		{
			return MessageCode.EMPTY;
		}
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt(reputation);
		buf.writeInt(messageCode.ordinal());
	}

	public static class Worker
	{
		public void work( MessageSetItemReputationAmount message )
		{
			Minecraft minecraft = Minecraft.getMinecraft();
			final EntityPlayer player = minecraft.player;

			if ( player == null )
			{
				return;
			}

			VillageLordGuiContainer.setDonateInfo(message);
		}
	}

	public static class Handler implements IMessageHandler<MessageSetItemReputationAmount, IMessage>
	{

		@Override
		public IMessage onMessage( final MessageSetItemReputationAmount message, MessageContext ctx )
		{
			if ( ctx.side != Side.CLIENT )
			{
				return null;
			}

			Minecraft.getMinecraft().addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					new Worker().work(message);
				}
			});

			return null;
		}
	}

	public static boolean isSet( String s )
	{
		return s != null && s.trim().length() > 0;
	}

	public static boolean isEmpty( String s )
	{
		return !isSet(s);
	}

	// public static boolean isReply(String s)
	// {
	// return s.equals("true");
	// }
}
