package net.torocraft.toroquest.generation.village;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.torocraft.toroquest.generation.village.VillageHandlerShop.VillagePieceShop;
import net.torocraft.toroquest.generation.village.util.VillagePieceBlockMap;

public class VillageHandlerWall implements VillagerRegistry.IVillageCreationHandler
{
	
	protected static final String NAME = "tqr_wall";

	public static void init()
	{
		MapGenStructureIO.registerStructureComponent(VillagePieceWall.class, NAME);
		MapGenStructureIO.registerStructureComponent(VillagePieceWall.class, NAME + "_destroyed");
		VillagerRegistry.instance().registerVillageCreationHandler(new VillageHandlerWall());
	}

	@Override
	public PieceWeight getVillagePieceWeight( Random random, int i )
	{
		return new PieceWeight(VillagePieceWall.class, 30, 2);
	}

	@Override
	public Class<?> getComponentClass()
	{
		return VillagePieceWall.class;
	}

	@Override
	public Village buildComponent( PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, EnumFacing facing, int p5 )
	{
		return VillagePieceWall.createPiece(startPiece, pieces, random, p1, p2, p3, facing, p5);
	}
	
	public static void registerComponent( final Class<? extends StructureVillagePieces.Village> clazz, final int weight, final int min, final int max )
	{
		VillagerRegistry.instance().registerVillageCreationHandler(new VillageHandlerWall());
	}
	
	public static class VillagePieceWall extends VillagePieceBlockMap
	{
		private StructureVillagePieces.Start start;
		private List pieces;
		private boolean hasMadeWallBlock;

		public VillagePieceWall()
		{
		}

		public static VillagePieceShop createPiece( StructureVillagePieces.Start start, List<StructureComponent> structures, Random rand, int x, int y, int z, EnumFacing facing, int type )
		{
			BlockPos size = new BlockPos(0,1,0);
			StructureBoundingBox bounds = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, size.getX(), size.getY(), size.getZ(), facing);
			return canVillageGoDeeper(bounds) && StructureComponent.findIntersecting(structures, bounds) == null ? new VillagePieceShop(NAME, start, type, rand, bounds, facing) : null;
		}

		public VillagePieceWall( String name, Start start, int type, Random rand, StructureBoundingBox bounds, EnumFacing facing )
		{
			super(name, start, type, rand, bounds, facing);
		}

		public void buildComponent( final StructureComponent component, final List<StructureComponent> pieces, final Random rand )
		{
			super.buildComponent(component, pieces, rand);
			this.pieces = pieces;
		}

		public boolean addComponentParts( final World world, final Random rand, final StructureBoundingBox bounds )
		{
			if ( this.averageGroundLvl < 0 )
			{
				this.averageGroundLvl = this.getAverageGroundLevel(world, bounds);
				if ( this.averageGroundLvl < 0 )
				{
					return true;
				}
				this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 7 - 1, 0);
			}
			final int x = 1;
			final int z = 1;
			final int xCoord = this.getXWithOffset(x, z);
			final int yCoord = this.getYWithOffset(1);
			final int zCoord = this.getZWithOffset(x, z);
			if ( this.pieces != null && bounds.isVecInside(new Vec3i(xCoord, yCoord, zCoord)) )
			{
				BlockVillageWallGen.TileEntityVillageWallGen tile = new BlockVillageWallGen.TileEntityVillageWallGen();
				tile.setPos(new BlockPos(xCoord,yCoord,zCoord));
				tile.setStructure(this.pieces, this.start);
			}
			return true;
		}

		public static void placeWalls( final World world, final List<StructureBounds> bb, final int xCoord, final int yCoord, final int zCoord, final Biome biome, final boolean desert )
		{
			int minX = Integer.MAX_VALUE;
			int minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxZ = Integer.MIN_VALUE;
			
			System.out.println("Generating town walls at " + xCoord + " " + yCoord + " " + zCoord);
			
			for ( int i = 0; i < bb.size(); ++i )
			{
				minX = Math.min(bb.get(i).minX, minX);
				minZ = Math.min(bb.get(i).minZ, minZ);
				maxX = Math.max(bb.get(i).maxX, maxX);
				maxZ = Math.max(bb.get(i).maxZ, maxZ);
			}
			if ( maxX != Integer.MIN_VALUE && minX != Integer.MAX_VALUE && maxZ != Integer.MIN_VALUE && minZ != Integer.MAX_VALUE )
			{
				final byte[][] a = new byte[maxX - minX + 3][maxZ - minZ + 3];
				final short[][] b = new short[maxX - minX + 3][maxZ - minZ + 3];
				for ( int j = 0; j < bb.size(); ++j )
				{
					final int w = bb.get(j).maxX - bb.get(j).minX + 1;
					final int wMid = w / 2 + bb.get(j).minX - 1;
					final int h = bb.get(j).maxZ - bb.get(j).minZ + 1;
					final int hMid = h / 2 + bb.get(j).minZ - 1;
					for ( int x = bb.get(j).minX; x <= bb.get(j).maxX; ++x )
					{
						for ( int z = bb.get(j).minZ; z <= bb.get(j).maxZ; ++z )
						{
							final int mx = x - minX + 1;
							final int mz = z - minZ + 1;
							if ( !bb.get(j).ew && (z == bb.get(j).minZ || z == bb.get(j).maxZ) && x >= wMid - 1 && x <= wMid + 1 )
							{
								a[mx][mz] = 3;
							}
							else if ( bb.get(j).ew && (x == bb.get(j).minX || x == bb.get(j).maxX) && z >= hMid - 1 && z <= hMid + 1 )
							{
								a[mx][mz] = 3;
							}
							else
							{
								a[mx][mz] = 2;
							}
						}
					}
				}
				for ( int range = 7, x2 = 1; x2 < a.length - range; ++x2 )
				{
					for ( int z2 = 1; z2 < a[x2].length - range; ++z2 )
					{
						if ( a[x2][z2] == 2 )
						{
							for ( int p = 1; p < range; ++p )
							{
								if ( a[x2 + p][z2] == 2 && a[x2 + p - 1][z2] == 0 )
								{
									for ( int p2 = p; p2 > 0; --p2 )
									{
										a[x2 + p2][z2] = 2;
									}
								}
								if ( a[x2][z2 + p] == 2 && a[x2][z2 + p - 1] == 0 )
								{
									for ( int p2 = p; p2 > 0; --p2 )
									{
										a[x2][z2 + p2] = 2;
									}
								}
							}
						}
					}
				}
				for ( int x2 = 1; x2 < a.length - 1; ++x2 )
				{
					for ( int z2 = 1; z2 < a[x2].length - 1; ++z2 )
					{
						final boolean n = a[x2][z2 - 1] == 0;
						final boolean s = a[x2][z2 + 1] == 0;
						final boolean e = a[x2 + 1][z2] == 0;
						final boolean w2 = a[x2 - 1][z2] == 0;
						final boolean ne = a[x2 + 1][z2 - 1] == 0;
						final boolean sw = a[x2 - 1][z2 + 1] == 0;
						final boolean se = a[x2 + 1][z2 + 1] == 0;
						final boolean nw = a[x2 - 1][z2 - 1] == 0;
						if ( !n && !s && !e && !w2 && !ne && !se && !nw )
						{
							if ( !sw )
							{
								a[x2][z2] = 1;
							}
						}
					}
				}
				IBlockState blockBase = Blocks.STONEBRICK.getDefaultState();
				IBlockState blockFence = Blocks.COBBLESTONE_WALL.getDefaultState();
				IBlockState stairsBlock = Blocks.STONE_BRICK_STAIRS.getDefaultState();
				int blockBaseMeta = 0;

				// BiomeEvent.GetVillageBlockID event = new BiomeEvent.GetVillageBlockID(biome, blockBase, blockBaseMeta);

				int guardDist = 0;
				for ( int x3 = 1; x3 < a.length - 1; ++x3 )
				{
					for ( int z3 = 1; z3 < a[x3].length - 1; ++z3 )
					{
						final boolean n2 = a[x3][z3 - 1] >= 2;
						final boolean s2 = a[x3][z3 + 1] >= 2;
						final boolean e2 = a[x3 + 1][z3] >= 2;
						final boolean w3 = a[x3 - 1][z3] >= 2;
						final boolean ne2 = a[x3 + 1][z3 - 1] >= 2;
						final boolean sw2 = a[x3 - 1][z3 + 1] >= 2;
						final boolean se2 = a[x3 + 1][z3 + 1] >= 2;
						final boolean nw2 = a[x3 - 1][z3 - 1] >= 2;
						if ( a[x3][z3] >= 2 )
						{
							final int dx = minX + x3;
							final int dz = minZ + z3;
							int solidCount = 0;
							int dy = 0;
							// for ( solidCount = 0, dy = yCoord; dy > 1 && solidCount < 9; --dy )
							// {
							// solidCount = 0;
							// for ( int ddx = dx - 1; ddx <= dx + 1; ++ddx )
							// {
							// for ( int ddz = dz - 1; ddz <= dz + 1; ++ddz )
							// {
							// BlockPos pos = new BlockPos(ddx, dy, ddz);
							// if ( world.getBlockState(pos).getBlock() instanceof BlockAir )
							// {
							//
							// }
							// else
							// {
							// ++solidCount;
							// }
							// }
							// }
							// }
							final int minHeight = 9;
							int startY = dy + 9;
							final int near = Math.max(Math.max(Math.max(b[x3 - 1][z3], b[x3 + 1][z3]), b[x3][z3 + 1]), b[x3][z3 - 1]);
							if ( near > 0 )
							{
								if ( near > startY )
								{
									startY = near - 1;
								}
								else if ( near < startY )
								{
									startY = near + 1;
								}
							}
							final int lowestY = dy;
							if ( startY - lowestY > 0 )
							{
								b[x3][z3] = (short) Math.min(Math.max(startY, 0), 32767);
							}
							for ( dy = startY; dy > lowestY; --dy )
							{
								if ( dy == startY )
								{
									if ( !ne2 && !n2 && !e2 )
									{
										setBlock(world, dx + 2, dy, dz - 2, blockBase, blockBaseMeta);
										setBlock(world, dx + 2, dy, dz - 1, blockBase, blockBaseMeta);
										setBlock(world, dx + 1, dy, dz - 2, blockBase, blockBaseMeta);
										setBlock(world, dx + 2, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx + 2, dy + 1, dz - 1, blockBase, blockBaseMeta, false);
										setBlock(world, dx + 1, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
									}
									if ( !nw2 && !n2 && !w3 )
									{
										setBlock(world, dx - 2, dy, dz - 2, blockBase, blockBaseMeta);
										setBlock(world, dx - 1, dy, dz - 2, blockBase, blockBaseMeta);
										setBlock(world, dx - 2, dy, dz - 1, blockBase, blockBaseMeta);
										setBlock(world, dx - 2, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx - 1, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx - 2, dy + 1, dz - 1, blockBase, blockBaseMeta, false);
									}
									if ( !se2 && !s2 && !e2 )
									{
										setBlock(world, dx + 2, dy, dz + 2, blockBase, blockBaseMeta);
										setBlock(world, dx + 1, dy, dz + 2, blockBase, blockBaseMeta);
										setBlock(world, dx + 2, dy, dz + 1, blockBase, blockBaseMeta);
										setBlock(world, dx + 2, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx + 1, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx + 2, dy + 1, dz + 1, blockBase, blockBaseMeta, false);
									}
									if ( !sw2 && !s2 && !w3 )
									{
										setBlock(world, dx - 2, dy, dz + 2, blockBase, blockBaseMeta);
										setBlock(world, dx - 1, dy, dz + 2, blockBase, blockBaseMeta);
										setBlock(world, dx - 2, dy, dz + 1, blockBase, blockBaseMeta);
										setBlock(world, dx - 2, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx - 1, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
										setBlock(world, dx - 2, dy + 1, dz + 1, blockBase, blockBaseMeta, false);
									}
									if ( !n2 && !ne2 && !nw2 )
									{
										setBlock(world, dx, dy, dz - 2, blockBase, blockBaseMeta);
										setBlock(world, dx, dy + 1, dz - 2, stairsBlock, 0, false);
									}
									if ( !e2 && !se2 && !ne2 )
									{
										setBlock(world, dx + 2, dy, dz, blockBase, blockBaseMeta);
										setBlock(world, dx + 2, dy + 1, dz, stairsBlock, 2, false);
									}
									if ( !s2 && !se2 && !sw2 )
									{
										setBlock(world, dx, dy, dz + 2, blockBase, blockBaseMeta);
										setBlock(world, dx, dy + 1, dz + 2, stairsBlock, 0, false);
									}
									if ( !w3 && !nw2 && !sw2 )
									{
										setBlock(world, dx - 2, dy, dz, blockBase, blockBaseMeta);
										setBlock(world, dx - 2, dy + 1, dz, stairsBlock, 2, false);
									}
									if ( ++guardDist > 200 )
									{
										// spawnGuard(world, dx, dy, dz);
										guardDist = 0;
									}
								}
								else
								{
									final int distCheck = 4;
									final boolean gate = a[x3][z3] == 3 && ((x3 > distCheck && x3 < a.length - distCheck && a[x3 - distCheck][z3] == 2 && a[x3 + distCheck][z3] == 2) || (z3 > distCheck && z3 < a[x3].length - distCheck && a[x3][z3 - distCheck] == 2 && a[x3][z3 + distCheck] == 2));
									if ( gate && dy == startY - 3 )
									{
										world.setBlockState(new BlockPos(dx, dy, dz), blockFence);
										if ( a[x3 + 1][z3] != 3 || a[x3 - 1][z3] != 3 )
										{
											if ( a[x3 + 1][z3] == 3 )
											{
												// 5
												world.setBlockState(new BlockPos(dx, dy, dz - 1), stairsBlock);
												world.setBlockState(new BlockPos(dx, dy, dz + 1), stairsBlock);
											}
											else if ( a[x3 - 1][z3] == 3 )
											{
												// 4
												world.setBlockState(new BlockPos(dx, dy, dz - 1), stairsBlock);
												world.setBlockState(new BlockPos(dx, dy, dz + 1), stairsBlock);
											}
											else if ( a[x3][z3 + 1] != 3 || a[x3][z3 - 1] != 3 )
											{
												if ( a[x3][z3 - 1] == 3 )
												{
													// 6
													world.setBlockState(new BlockPos(dx, dy, dz - 1), stairsBlock);
													world.setBlockState(new BlockPos(dx, dy, dz + 1), stairsBlock);
												}
												else if ( a[x3][z3 + 1] == 3 )
												{
													// 7
													world.setBlockState(new BlockPos(dx, dy, dz - 1), stairsBlock);
													world.setBlockState(new BlockPos(dx, dy, dz + 1), stairsBlock);
												}
											}
										}
									}
									if ( !gate || dy > startY - 3 )
									{
										world.setBlockState(new BlockPos(dx, dy, dz), blockBase);
										final boolean ng = a[x3][z3 - 1] == 3;
										final boolean sg = a[x3][z3 + 1] == 3;
										final boolean eg = a[x3 + 1][z3] == 3;
										final boolean wg = a[x3 - 1][z3] == 3;
										if ( !ng )
										{
											world.setBlockState(new BlockPos(dx, dy, dz - 1), blockBase);
										}
										if ( !ng && !eg )
										{
											world.setBlockState(new BlockPos(dx + 1, dy, dz - 1), blockBase);
										}
										if ( !ng && !wg )
										{
											world.setBlockState(new BlockPos(dx - 1, dy, dz - 1), blockBase);
										}
										if ( !eg )
										{
											world.setBlockState(new BlockPos(dx + 1, dy, dz), blockBase);
										}
										if ( !sg )
										{
											world.setBlockState(new BlockPos(dx, dy, dz + 1), blockBase);
										}
										if ( !sg && !eg )
										{
											world.setBlockState(new BlockPos(dx + 1, dy, dz + 1), blockBase);
										}
										if ( !sg && !wg )
										{
											world.setBlockState(new BlockPos(dx - 1, dy, dz + 1), blockBase);
										}
										if ( !wg )
										{
											world.setBlockState(new BlockPos(dx - 1, dy, dz), blockBase);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		public static class StructureBounds extends StructureBoundingBox
		{
			public final boolean ew;

			public StructureBounds( final StructureVillagePieces.Path path, final int expansionX, final int expansionZ )
			{
				this(path.getBoundingBox(), expansionX, expansionZ);
			}

			public StructureBounds( final StructureBoundingBox bb, final int expansionX, final int expansionZ )
			{
				this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, expansionX, expansionZ);
			}

			public StructureBounds( final int x, final int y, final int z, final int x2, final int y2, final int z2, final int expansionX, final int expansionZ )
			{
				this.ew = (x2 - x > z2 - z);
				if ( this.ew )
				{
					this.minX = x - expansionZ;
					this.maxX = x2 + expansionZ;
					this.minZ = z - expansionX;
					this.maxZ = z2 + expansionX;
				}
				else
				{
					this.minX = x - expansionX;
					this.maxX = x2 + expansionX;
					this.minZ = z - expansionZ;
					this.maxZ = z2 + expansionZ;
				}
				this.minY = y;
				this.maxY = y2;
			}
		}

		public static class BlockVillageWallGen extends BlockContainer
		{
			public BlockVillageWallGen()
			{
				super(Material.AIR);
			}

			public static class TileEntityVillageWallGen extends TileEntity implements ITickable
			{
				private List<StructureBounds> bb;
				private Biome biome;
				private boolean desert;

				@Override
				public void update()
				{
					if ( !this.getWorld().isRemote && this.bb != null )
					{
						placeWalls(this.getWorld(), this.bb, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.biome, this.desert);
						this.bb = null;
						this.getWorld().setBlockToAir(this.getPos());
						this.getWorld().removeTileEntity(this.getPos());
					}
					else if ( !this.getWorld().isRemote )
					{
						this.bb = null;
						this.getWorld().setBlockToAir(this.getPos());
						this.getWorld().removeTileEntity(this.getPos());
					}
				}

				public void setStructure( final List<StructureComponent> pieces, final StructureVillagePieces.Start start )
				{
					this.biome = start.biome;
					this.desert = this.biome.topBlock instanceof BlockSand;
					this.bb = new ArrayList<StructureBounds>();
					for ( final StructureComponent obj : pieces )
					{
						if ( obj instanceof StructureVillagePieces.Path )
						{
							this.bb.add(new StructureBounds((StructureVillagePieces.Path) obj, 20, 7));
						}
					}
				}
			}

			@Override
			public TileEntity createNewTileEntity( World worldIn, int meta )
			{
				return new TileEntityVillageWallGen();
			}
		}

		@Override
		protected void alterPalette( Map<String, IBlockState> palette )
		{
			// TODO Auto-generated method stub
		}
	}

	public static void setBlock( World world, int i, int dy, int j, IBlockState blockBase, int blockBaseMeta )
	{
		world.setBlockState(new BlockPos(i,dy,j), blockBase);
		
	}

	public static void setBlock( World world, int i, int j, int k, IBlockState blockBase, int blockBaseMeta, boolean b )
	{
		world.setBlockState(new BlockPos(i,j,k), blockBase);		
	}
}
