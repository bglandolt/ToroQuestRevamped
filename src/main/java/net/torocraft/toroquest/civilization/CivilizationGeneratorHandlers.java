package net.torocraft.toroquest.civilization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.torocraft.toroquest.EventHandlers;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.generation.village.util.VillagePieceBlockMap;

public class CivilizationGeneratorHandlers
{

	// https://skmedix.github.io/ForgeJavaDocs/javadoc/forge/1.9.4-12.17.0.2051/net/minecraftforge/fml/common/eventhandler/Event.html

	@SubscribeEvent( priority = EventPriority.HIGH )
	public void aregisterNewCiviliationBorder( PopulateChunkEvent.Pre event )
	{
		if ( event.getWorld().isRemote )
		{
			return;
		}

		if ( event.getWorld().provider != null && event.getWorld().provider.getDimension() != 0 )
		{
			return;
		}

		if ( !event.isHasVillageGenerated() )
		{
			return;
		}

		CivilizationUtil.registerNewCivilization(event.getWorld(), event.getChunkX(), event.getChunkZ());
	}

	@SubscribeEvent( priority = EventPriority.LOWEST )
	public void zregisterNewCiviliationBorderPost( PopulateChunkEvent.Post event )
	{
		if ( event.getWorld().isRemote )
		{
			return;
		}

		if ( event.getWorld().provider != null && event.getWorld().provider.getDimension() != 0 )
		{
			return;
		}

		if ( !event.isHasVillageGenerated() )
		{
			return;
		}

		// CivilizationsWorldSaveData.resgister
		CivilizationUtil.registerNewCivilization(event.getWorld(), event.getChunkX(), event.getChunkZ());

		int x = (event.getChunkX()) * 16; // int x = (event.getChunkX())*16-1;
		int y = EventHandlers.MIN_SPAWN_HEIGHT; // BASE
		int z = (event.getChunkZ()) * 16; // int z = (event.getChunkZ())*16-1;

		boolean destroyedVillage = (ToroQuestConfiguration.destroyedVillagesNearSpawnDistance > 0 && Math.abs(x) < ToroQuestConfiguration.destroyedVillagesNearSpawnDistance && Math.abs(z) < ToroQuestConfiguration.destroyedVillagesNearSpawnDistance);

		boolean hasVillagerChunk = false;

		if ( destroyedVillage )
		{
			List<EntityVillager> villagers = event.getWorld().getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(x - 16, y, z - 16, x + 16, EventHandlers.MAX_SPAWN_HEIGHT, z + 16));

			for ( EntityVillager villager : villagers )
			{
				villager.setHealth(0.0F);
				villager.setDead();
				hasVillagerChunk = true;
			}
		}

		BlockPos pos;
		Block b;

		for ( int xx = 0; xx <= 16; xx++ )
		{
			for ( int zz = 0; zz <= 16; zz++ )
			{
				for ( int yy = EventHandlers.MAX_SPAWN_HEIGHT; yy >= y; yy-- )
				{
					short maxDestroyedBlocks = 2;
					pos = new BlockPos(new BlockPos(x + xx, yy, z + zz));
					b = event.getWorld().getBlockState(pos).getBlock();

					if ( destroyedVillage )
					{
						if ( b instanceof BlockAir )
						{

						}
						else if ( b instanceof BlockGrass || b instanceof BlockSand || b instanceof BlockStone )
						{
							if ( hasVillagerChunk )
							{
								if ( event.getRand().nextInt(80) == 0 )
								{
									if ( event.getRand().nextInt(6) == 0 )
									{
										VillagePieceBlockMap.setBanditBanner(event.getWorld(), pos.up());
									}
									else
									{
										if ( event.getWorld().getBlockState(pos.add(1, 1, 0)).getBlock() instanceof BlockAir && event.getWorld().getBlockState(pos.add(0, 1, 1)).getBlock() instanceof BlockAir && event.getWorld().getBlockState(pos.add(-1, 1, 0)).getBlock() instanceof BlockAir && event.getWorld().getBlockState(pos.add(0, 1, -1)).getBlock() instanceof BlockAir )
										{
											Rotation rotation = Rotation.NONE;

											switch( event.getRand().nextInt(4) )
											{
											case 0:
											{
												rotation = Rotation.NONE;
												break;
											}
											case 1:
											{
												rotation = Rotation.CLOCKWISE_90;
												break;
											}
											case 2:
											{
												rotation = Rotation.COUNTERCLOCKWISE_90;
												break;
											}
											case 3:
											{
												rotation = Rotation.CLOCKWISE_180;
												break;
											}
											}

											event.getWorld().setBlockState(pos.up(2), Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP).withRotation(rotation));

											if ( ToroQuestConfiguration.useIronBarsForHeadSpike )
												event.getWorld().setBlockState(pos.up(1), Blocks.IRON_BARS.getDefaultState());
											else
												event.getWorld().setBlockState(pos.up(1), Blocks.OAK_FENCE.getDefaultState());
											event.getWorld().setBlockState(pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.COARSE_DIRT));
										}
									}
								}
							}
						}
						else if ( b instanceof BlockChest )
						{
							TileEntity tileentity = event.getWorld().getTileEntity(pos);
							if ( tileentity instanceof TileEntityChest )
							{
								((TileEntityChest) tileentity).clear();
								// ((TileEntityChest) tileentity).markDirty();
							}
						}
						else if ( b.getDefaultState() == Blocks.WOOL.getDefaultState() )
						{
							if ( event.getWorld().getBlockState(pos.add(0, -1, 0)).getBlock() instanceof BlockFence && (event.getWorld().getBlockState(pos.add(1, 0, 0)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState() || event.getWorld().getBlockState(pos.add(0, 0, 1)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState() || event.getWorld().getBlockState(pos.add(-1, 0, 0)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState() || event.getWorld().getBlockState(pos.add(0, 0, -1)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState()) )
							{
								event.getWorld().setBlockState(pos.add(1, 0, 0), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos.add(0, 0, 1), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos.add(-1, 0, 0), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos.add(0, 0, -1), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());

								this.placeLantern(event.getWorld(), pos);

								// --
								// | |
								// |
								// |
								// |
								// |

								int i = 1;

								while (i <= 6)
								{
									if ( event.getWorld().getBlockState(pos.add(0, -i, 0)).getBlock() instanceof BlockFence )
									{
										i++;
									}
									else
									{
										if ( i > 1 )
										{
											i--;
											event.getWorld().setBlockState(pos.add(0, -i, 0), Blocks.COBBLESTONE_WALL.getDefaultState());
										}
										break;
									}
								}
							}
							else
							{
								event.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
							}
						}
						else if ( b instanceof BlockBed || b instanceof BlockDoor || b instanceof BlockFurnace || b instanceof BlockAnvil || b instanceof BlockBrewingStand )
						{
							event.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
						}
						else if ( b instanceof BlockPane )
						{
							if ( event.getWorld().rand.nextBoolean() )
							{
								event.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
							}
						}
						else if ( maxDestroyedBlocks > 0 && event.getWorld().rand.nextInt(8) == 0 )
						{
							if ( b instanceof BlockGrassPath )
							{
								event.getWorld().setBlockState(pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.COARSE_DIRT));
							}
							else if ( b.getDefaultState().getMaterial() == Material.GROUND || b.getDefaultState().getMaterial() == Material.ROCK || b.getDefaultState().getMaterial() == Material.SAND )
							{
								maxDestroyedBlocks--;
							}
							else
							{
								event.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
								maxDestroyedBlocks--;
							}
						}
					}
					else // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
					{
						// if ( pos.getY() == 70 )
						// {
						// if ( b instanceof BlockGlass || b.getDefaultState() ==
						// Blocks.GLASS.getDefaultState() )
						// {
						// event.getWorld().setBlockState(pos, Blocks.GOLD_BLOCK.getDefaultState());
						// }
						// event.getWorld().setBlockState(pos, Blocks.GLASS.getDefaultState());
						// }

						if ( b instanceof BlockChest && ToroQuestConfiguration.replaceVillageChestsWithTrappedChests )
						{
							try
							{
								BlockChest chest = (BlockChest) b;
								if ( chest.chestType != BlockChest.Type.TRAP )
								{
									TileEntity tile = event.getWorld().getTileEntity(pos);
									ResourceLocation lootTable = null;
									ArrayList<ItemStack> lootItems = new ArrayList<ItemStack>();

									if ( tile instanceof TileEntityChest )
									{
										lootTable = ((TileEntityChest) tile).getLootTable();

										if ( lootTable == null )
										{
											try
											{
												for ( int i = 0; i < ((TileEntityChest) tile).getSizeInventory(); ++i )
												{
													ItemStack itemstack = ((TileEntityChest) tile).removeStackFromSlot(i);

													if ( itemstack != null )
													{
														lootItems.add(itemstack);
													}
												}
											}
											catch (Exception e)
											{
											}
										}

										((TileEntityChest) tile).clear();
										// ((TileEntityChest) tile).markDirty();
									}

									IBlockState ib = event.getWorld().getBlockState(pos);
									IProperty<EnumFacing> FACING = BlockHorizontal.FACING;
									EnumFacing enumfacing = (EnumFacing) ib.getValue(FACING);
									// EnumTrapped a = (EnumTrapped)ib.getValue(TRAPPED);
									event.getWorld().setBlockState(pos, Blocks.TRAPPED_CHEST.getDefaultState().withProperty(BlockChest.FACING, enumfacing));
									tile = event.getWorld().getTileEntity(pos);
									if ( tile instanceof TileEntityChest )
									{
										if ( lootTable != null )
										{
											((TileEntityChest) tile).setLootTable(lootTable, new Random().nextLong());
											// ((TileEntityChest) tile).markDirty();
										}
										else if ( lootItems != null && !lootItems.isEmpty() )
										{
											try
											{
												int i = 0;
												for ( ItemStack itemstack : lootItems )
												{
													((TileEntityChest) tile).setInventorySlotContents(i, itemstack);
													i++;
												}
											}
											catch (Exception e)
											{
											}
											// ((TileEntityChest) tile).markDirty();
										}
									}
								}
							}
							catch (Exception e)
							{

							}
							continue;
						}
						// ================================ FLOWER POT ================================
						else if ( b instanceof BlockFlowerPot )
						{
							try
							{
								CivilizationType civ = CivilizationUtil.getProvinceAt(event.getWorld(), x / 16, z / 16).civilization;
								TileEntityFlowerPot tileentityflowerpot = this.getFlowerPot(event.getWorld(), pos);
								if ( civ != null && tileentityflowerpot != null )
								{
									switch( civ )
									{
									case FIRE:
									{
										Item item = Item.getItemFromBlock(Blocks.RED_FLOWER);
										ItemStack istack = new ItemStack(item);
										// istack.setItemDamage(0);
										tileentityflowerpot.setItemStack(istack);
										break;
									}
									case EARTH:
									{
										Item item = Item.getItemFromBlock(Blocks.TALLGRASS);
										ItemStack istack = new ItemStack(item);
										istack.setItemDamage(2);
										tileentityflowerpot.setItemStack(istack);
										break;
									}
									case WATER:
									{
										Item item = Item.getItemFromBlock(Blocks.RED_FLOWER);
										ItemStack istack = new ItemStack(item);
										istack.setItemDamage(1);
										tileentityflowerpot.setItemStack(istack);
										break;
									}
									case MOON:
									{
										Item item = Item.getItemFromBlock(Blocks.BROWN_MUSHROOM);
										ItemStack istack = new ItemStack(item);
										// istack.setItemDamage(0);
										tileentityflowerpot.setItemStack(istack);
										break;
									}
									case WIND:
									{
										Item item = Item.getItemFromBlock(Blocks.DEADBUSH);
										ItemStack istack = new ItemStack(item);
										// istack.setItemDamage(0);
										tileentityflowerpot.setItemStack(istack);
										break;
									}
									case SUN:
									{
										Item item = Item.getItemFromBlock(Blocks.CACTUS);
										ItemStack istack = new ItemStack(item);
										// istack.setItemDamage(0);
										tileentityflowerpot.setItemStack(istack);
										break;
									}
									}
									// tileentityflowerpot.markDirty();
									event.getWorld().notifyBlockUpdate(pos, b.getDefaultState(), b.getDefaultState(), 3);
								}
							}
							catch (Exception e)
							{

							}
							continue;
						}
						// ================================ BED ================================
						else if ( b instanceof BlockBed )
						{
							try
							{
								CivilizationType civ = CivilizationUtil.getProvinceAt(event.getWorld(), x / 16, z / 16).civilization;
								TileEntityBed bed = this.getBed(event.getWorld(), pos);
								if ( civ != null && bed != null )
								{
									switch( civ )
									{
									case FIRE:
									{
										bed.setColor(EnumDyeColor.RED);
										break;
									}
									case EARTH:
									{
										bed.setColor(EnumDyeColor.GREEN);
										break;
									}
									case WATER:
									{
										bed.setColor(EnumDyeColor.CYAN);
										break;
									}
									case MOON:
									{
										bed.setColor(EnumDyeColor.BLACK);
										break;
									}
									case WIND:
									{
										bed.setColor(EnumDyeColor.BROWN);
										break;
									}
									case SUN:
									{
										bed.setColor(EnumDyeColor.YELLOW);
										break;
									}
									}
									// bed.markDirty();
									event.getWorld().notifyBlockUpdate(pos, b.getDefaultState(), b.getDefaultState(), 3);
								}
							}
							catch (Exception e)
							{

							}
							continue;
						}
						else if ( b instanceof BlockBanner )
						{
							try
							{
								CivilizationType civ = CivilizationUtil.getProvinceAt(event.getWorld(), x / 16, z / 16).civilization;
								TileEntityBanner banner = this.getBanner(event.getWorld(), pos);

								if ( civ != null && banner != null )
								{
									switch( civ )
									{
									case FIRE:
									{
										banner.setItemValues(VillagePieceBlockMap.getRedBanner(), true);
										break;
									}
									case EARTH:
									{
										banner.setItemValues(VillagePieceBlockMap.getGreenBanner(), true);
										break;
									}
									case WATER:
									{
										banner.setItemValues(VillagePieceBlockMap.getBlueBanner(), true);
										break;
									}
									case MOON:
									{
										banner.setItemValues(VillagePieceBlockMap.getBlackBanner(), true);
										break;
									}
									case WIND:
									{
										banner.setItemValues(VillagePieceBlockMap.getBrownBanner(), true);
										break;
									}
									case SUN:
									{
										banner.setItemValues(VillagePieceBlockMap.getYellowBanner(), true);
										break;
									}
									}
									// banner.markDirty();
									event.getWorld().notifyBlockUpdate(pos, b.getDefaultState(), b.getDefaultState(), 3);
								}
							}
							catch (Exception e)
							{

							}
							continue;
						}
						else if ( b.getDefaultState() == Blocks.WOOL.getDefaultState() ) // LANTERN !!!
						{
							if ( event.getWorld().getBlockState(pos.add(0, -1, 0)).getBlock() instanceof BlockFence && (event.getWorld().getBlockState(pos.add(1, 0, 0)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState() || event.getWorld().getBlockState(pos.add(0, 0, 1)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState() || event.getWorld().getBlockState(pos.add(-1, 0, 0)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState() || event.getWorld().getBlockState(pos.add(0, 0, -1)).getBlock().getDefaultState() == Blocks.TORCH.getDefaultState()) )
							{
								event.getWorld().setBlockState(pos.add(1, 0, 0), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos.add(0, 0, 1), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos.add(-1, 0, 0), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos.add(0, 0, -1), Blocks.AIR.getDefaultState());
								event.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());

								this.placeLantern(event.getWorld(), pos);

								// --
								// | |
								// |
								// |
								// |
								// |

								int i = 1;

								while (i <= 6)
								{
									if ( event.getWorld().getBlockState(pos.add(0, -i, 0)).getBlock() instanceof BlockFence )
									{
										i++;
									}
									else
									{
										if ( i > 1 )
										{
											i--;
											event.getWorld().setBlockState(pos.add(0, -i, 0), Blocks.COBBLESTONE_WALL.getDefaultState());
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private IBlockState getLantern()
	{
		IBlockState LA = Blocks.GLOWSTONE.getDefaultState();

		try
		{
			LA = Block.getBlockFromName(ToroQuestConfiguration.lanternResourceName).getDefaultState().withProperty(PropertyDirection.create("facing"), EnumFacing.DOWN);
		}
		catch (Exception e0)
		{
			try
			{
				LA = Block.getBlockFromName(ToroQuestConfiguration.lanternResourceName).getDefaultState();
			}
			catch (Exception e1)
			{
				try
				{
					LA = Block.getBlockFromName("futuremc:lantern").getDefaultState().withProperty(PropertyDirection.create("facing"), EnumFacing.DOWN);
				}
				catch (Exception e2)
				{
					try
					{
						LA = Block.getBlockFromName("futuremc:lantern").getDefaultState();
					}
					catch (Exception e3)
					{
					}
				}
			}
		}

		return LA;
	}

	private void placeLantern( World world, BlockPos pos )
	{
		IBlockState fence = world.getBlockState(pos.add(0, -1, 0)).getBlock().getDefaultState();
		for ( int i = 0; i < 8; i++ )
		{
			IBlockState gp = world.getBlockState(pos.add(1, -i, 0)).getBlock().getDefaultState();
			if ( gp == Blocks.AIR.getDefaultState() )
			{
				continue;
			}
			else if ( gp == Blocks.GRASS_PATH.getDefaultState() )
			{
				world.setBlockState(pos.add(0, 0, 0), fence);
				world.setBlockState(pos.add(0, 1, 0), fence);
				world.setBlockState(pos.add(1, 1, 0), fence);
				world.setBlockState(pos.add(2, 1, 0), fence);
				// Block chain =
				// Block.getBlockFromName(ToroQuestConfiguration.chainResourceName);
				// world.setBlockState(pos.add(2, 0, 0),
				// (chain==null?Blocks.IRON_BARS.getDefaultState():chain.getDefaultState()) );
				// world.setBlockState(pos.add(2, -1, 0), getLantern() );
				world.setBlockState(pos.add(2, 0, 0), getLantern());
				return;
			}
			break;
		}

		for ( int i = 0; i < 8; i++ )
		{
			IBlockState gp = world.getBlockState(pos.add(0, -i, 1)).getBlock().getDefaultState();
			if ( gp == Blocks.AIR.getDefaultState() )
			{
				continue;
			}
			else if ( gp == Blocks.GRASS_PATH.getDefaultState() )
			{
				world.setBlockState(pos.add(0, 0, 0), fence);
				world.setBlockState(pos.add(0, 1, 0), fence);
				world.setBlockState(pos.add(0, 1, 1), fence);
				world.setBlockState(pos.add(0, 1, 2), fence);
				// Block chain =
				// Block.getBlockFromName(ToroQuestConfiguration.chainResourceName);
				// world.setBlockState(pos.add(0, 0, 2),
				// (chain==null?Blocks.IRON_BARS.getDefaultState():chain.getDefaultState()) );
				// world.setBlockState(pos.add(0, -1, 2), getLantern() );
				world.setBlockState(pos.add(0, 0, 2), getLantern());
				return;
			}
			break;
		}

		for ( int i = 0; i < 8; i++ )
		{
			IBlockState gp = world.getBlockState(pos.add(-1, -i, 0)).getBlock().getDefaultState();
			if ( gp == Blocks.AIR.getDefaultState() )
			{
				continue;
			}
			else if ( gp == Blocks.GRASS_PATH.getDefaultState() )
			{
				world.setBlockState(pos.add(0, 0, 0), fence);
				world.setBlockState(pos.add(0, 1, 0), fence);
				world.setBlockState(pos.add(-1, 1, 0), fence);
				world.setBlockState(pos.add(-2, 1, 0), fence);
				// Block chain =
				// Block.getBlockFromName(ToroQuestConfiguration.chainResourceName);
				// world.setBlockState(pos.add(-2, 0, 0),
				// (chain==null?Blocks.IRON_BARS.getDefaultState():chain.getDefaultState()) );
				// world.setBlockState(pos.add(-2, -1, 0), getLantern() );
				world.setBlockState(pos.add(-2, 0, 0), getLantern());
				return;
			}
			break;
		}

		for ( int i = 0; i < 8; i++ )
		{
			IBlockState gp = world.getBlockState(pos.add(0, -i, -1)).getBlock().getDefaultState();
			if ( gp == Blocks.AIR.getDefaultState() )
			{
				continue;
			}
			else if ( gp == Blocks.GRASS_PATH.getDefaultState() )
			{
				world.setBlockState(pos.add(0, 0, 0), fence);
				world.setBlockState(pos.add(0, 1, 0), fence);
				world.setBlockState(pos.add(0, 1, -1), fence);
				world.setBlockState(pos.add(0, 1, -2), fence);
				// Block chain =
				// Block.getBlockFromName(ToroQuestConfiguration.chainResourceName);
				// world.setBlockState(pos.add(0, 0, -2),
				// (chain==null?Blocks.IRON_BARS.getDefaultState():chain.getDefaultState()) );
				// world.setBlockState(pos.add(0, -1, -2), getLantern() );
				world.setBlockState(pos.add(0, 0, -2), getLantern());
				return;
			}
			break;
		}

		world.setBlockState(pos.add(0, 0, 0), fence);
		world.setBlockState(pos.add(0, 1, 0), fence);
		world.setBlockState(pos.add(1, 1, 0), fence);
		world.setBlockState(pos.add(2, 1, 0), fence);
		// Block chain =
		// Block.getBlockFromName(ToroQuestConfiguration.chainResourceName);
		// world.setBlockState(pos.add(2, 0, 0),
		// (chain==null?Blocks.IRON_BARS.getDefaultState():chain.getDefaultState()) );
		// world.setBlockState(pos.add(2, -1, 0), getLantern() );
		world.setBlockState(pos.add(2, 0, 0), getLantern());
	}

	private TileEntityBanner getBanner( World worldIn, BlockPos pos )
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntityBanner ? (TileEntityBanner) tileentity : null;
	}

	private TileEntityFlowerPot getFlowerPot( World worldIn, BlockPos pos )
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntityFlowerPot ? (TileEntityFlowerPot) tileentity : null;
	}

	private TileEntityBed getBed( World worldIn, BlockPos pos )
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntityBed ? (TileEntityBed) tileentity : null;
	}

}
