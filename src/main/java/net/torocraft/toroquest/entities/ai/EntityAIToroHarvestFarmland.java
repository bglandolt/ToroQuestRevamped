package net.torocraft.toroquest.entities.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.torocraft.toroquest.entities.EntityToroVillager;

public class EntityAIToroHarvestFarmland extends EntityAIMoveToBlock
{
	/** Villager that is harvesting */
	private final EntityToroVillager villager;
	private boolean hasFarmItem;
	private boolean wantsToReapStuff;
	/** 0 => harvest, 1 => replant, -1 => none */
	private int currentTask;

	public EntityAIToroHarvestFarmland( EntityToroVillager villagerIn, double speedIn )
	{
		super(villagerIn, speedIn, 16);
		this.villager = villagerIn;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute()
	{
		if ( villager.isUnderAttack() || villager.isTrading() || villager.isMating() || villager.chattingWithGuard > 0 )
		{
			return false;
		}

		if ( this.runDelay <= 0 )
		{
			if ( !net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.villager.world, this.villager) )
			{
				return false;
			}

			this.currentTask = -1;
			this.hasFarmItem = this.villager.isFarmItemInInventory();
			this.wantsToReapStuff = this.villager.world.isDaytime();
		}

		return super.shouldExecute();
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting()
	{
		return this.currentTask >= 0 && super.shouldContinueExecuting();
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask()
	{
		super.updateTask();
		this.villager.getLookHelper().setLookPosition((double) this.destinationBlock.getX() + 0.5D, (double) (this.destinationBlock.getY() + 1), (double) this.destinationBlock.getZ() + 0.5D, 10.0F, (float) this.villager.getVerticalFaceSpeed());

		if ( this.getIsAboveDestination() )
		{
			World world = this.villager.world;
			BlockPos blockpos = this.destinationBlock.up();
			IBlockState iblockstate = world.getBlockState(blockpos);
			Block block = iblockstate.getBlock();

			// Harvest
			if ( this.currentTask == 0 && block instanceof BlockCrops && ((BlockCrops) block).isMaxAge(iblockstate) )
			{
				world.destroyBlock(blockpos, true);
				world.setBlockState(blockpos, block.getDefaultState());
			}
			// Plant
			else if ( this.currentTask == 1 && iblockstate.getMaterial() == Material.AIR )
			{
				InventoryBasic inventorybasic = this.villager.getVillagerInventory();

				for ( int i = 0; i < inventorybasic.getSizeInventory(); ++i )
				{
					ItemStack itemstack = inventorybasic.getStackInSlot(i);

					if ( !itemstack.isEmpty() )
					{
						if ( itemstack.getItem() instanceof net.minecraftforge.common.IPlantable )
						{
							if ( ((net.minecraftforge.common.IPlantable) itemstack.getItem()).getPlantType(world, blockpos) == net.minecraftforge.common.EnumPlantType.Crop )
							{
								world.setBlockState(blockpos, ((net.minecraftforge.common.IPlantable) itemstack.getItem()).getPlant(world, blockpos), 3);
							}
						}

						itemstack.shrink(4); // was 1, but now villagers instantly replant

						if ( itemstack.isEmpty() )
						{
							inventorybasic.setInventorySlotContents(i, ItemStack.EMPTY);
						}

						break;
					}
				}
			}

			this.currentTask = -1;
			this.runDelay = 10;
		}
	}

	/**
	 * Return true to set given position as destination
	 */
	protected boolean shouldMoveTo( World worldIn, BlockPos pos )
	{
		Block block = worldIn.getBlockState(pos).getBlock();

		if ( block == Blocks.FARMLAND )
		{
			pos = pos.up();
			IBlockState iblockstate = worldIn.getBlockState(pos);
			block = iblockstate.getBlock();

			if ( block instanceof BlockCrops && ((BlockCrops) block).isMaxAge(iblockstate) && this.wantsToReapStuff && (this.currentTask == 0 || this.currentTask < 0) )
			{
				this.currentTask = 0;
				return true;
			}

			if ( iblockstate.getMaterial() == Material.AIR && this.hasFarmItem && (this.currentTask == 1 || this.currentTask < 0) )
			{
				this.currentTask = 1;
				return true;
			}
		}

		return false;
	}
}