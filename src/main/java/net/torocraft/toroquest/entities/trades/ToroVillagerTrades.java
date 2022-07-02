package net.torocraft.toroquest.entities.trades;

import java.util.Locale;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.torocraft.toroquest.civilization.CivilizationType;
import net.torocraft.toroquest.civilization.CivilizationUtil;
import net.torocraft.toroquest.civilization.Province;
import net.torocraft.toroquest.config.ToroQuestConfiguration;
import net.torocraft.toroquest.config.ToroQuestConfiguration.GatedTrade;
import net.torocraft.toroquest.config.ToroQuestConfiguration.Trade;
import net.torocraft.toroquest.entities.EntityToroVillager;

public class ToroVillagerTrades
{
	@SuppressWarnings( "deprecation" )
	public static MerchantRecipeList trades( EntityToroVillager villager, EntityPlayer player, int rep, CivilizationType civ, String jobName, String varient )
	{
		MerchantRecipeList recipeList = new MerchantRecipeList();
		
		for ( Trade trade : ToroQuestConfiguration.trades )
		{
			try
			{
				if ( (trade.varient.equals(varient) || trade.varient.equals("x")) && (jobName.equals(trade.job) || trade.job.equals("x")) && (trade.province.equals("x") || trade.province.equals(CivilizationType.tradeName(civ))) && (trade.minimunRepRequired.equals("x") || rep >= Integer.parseInt(trade.minimunRepRequired)) )
				{
					int sell = trade.sellAmount;
					int buy = trade.buyAmount;

					ItemStack sellStack = ItemStack.EMPTY;
					ItemStack optional = ItemStack.EMPTY;
					ItemStack buyStack = ItemStack.EMPTY;

					Integer sellDamage = null;
					Integer buyDamage = null;

					String buyName = "" + trade.buyName;
					String sellName = "" + trade.sellName;

					if ( count(trade.sellName, ':') > 1 )
					{
						int index = trade.sellName.lastIndexOf(':');
						sellDamage = Integer.parseInt(trade.sellName.substring(index + 1));
						sellName = sellName.substring(0, index);
					}

					if ( count(trade.buyName, ':') > 1 )
					{
						int index = trade.buyName.lastIndexOf(':');
						buyDamage = Integer.parseInt(trade.buyName.substring(index + 1));
						buyName = buyName.substring(0, index);
					}

					if ( !trade.sellOptional.equals("x") )
					{
						optional = new ItemStack(Item.getByNameOrId(trade.sellOptional), 1);
					}

					if ( sell < buy )
					{
						Item item = Item.getByNameOrId(sellName);
						int maxStackSize = item.getItemStackLimit();
						if ( sell > maxStackSize )
						{
							if ( !trade.sellOptional.equals("x") || sell > maxStackSize * 2 )
							{
								continue;
							}
							else
							{
								optional = new ItemStack(item, sell - maxStackSize);
							}
							sell = maxStackSize;
						}
						sellStack = new ItemStack(item, sell);

						item = Item.getByNameOrId(buyName);
						maxStackSize = item.getItemStackLimit();
						buy = getBuyPrice(buy, rep);
						if ( buy > maxStackSize )
						{
							continue;
						}
						buyStack = new ItemStack(item, buy);
					}
					else
					{
						sell = getSellPrice(sell, rep);
						Item item = Item.getByNameOrId(sellName);
						int maxStackSize = item.getItemStackLimit();

						if ( sell > maxStackSize )
						{
							if ( !trade.sellOptional.equals("x") || sell > maxStackSize * 2 )
							{
								continue;
							}
							else
							{
								optional = new ItemStack(item, sell - maxStackSize);
							}
							sell = maxStackSize;
						}

						sellStack = new ItemStack(item, sell);

						item = Item.getByNameOrId(buyName);

						if ( buy > item.getItemStackLimit() )
						{
							buy = item.getItemStackLimit();
						}

						buyStack = new ItemStack(item, buy);
					}

					if ( trade.enchantment != null )
					{
						// 0 , 1 , 2 , 3 , 4 , 5 , 6
						// item name, enchantment name, enchantment power, enchantment name, enchantment
						// power, ..., ...
						String[] metaArray = trade.enchantment.split("~");

						if ( buyName.equals("minecraft:enchanted_book") )
						{
							for ( int i = 0; i < metaArray.length; )
							{
								ItemEnchantedBook.addEnchantment(buyStack, new EnchantmentData(Enchantment.getEnchantmentByLocation(metaArray[i++]), Integer.parseInt(metaArray[i++])));
							}
						}
						else if ( buyName.equals("minecraft:potion") )
						{
							try
							{
								buyStack = PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId(metaArray[1]), buy), PotionType.getPotionTypeForName(metaArray[0]));
							}
							catch (Exception e)
							{
								player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.sellName + " & " + trade.buyName + ". Check error logs for more details"));

								System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.sellName + "   buyName: " + trade.buyName + "   sellAmount: " + trade.sellAmount + "   buyAmount: " + trade.buyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.sellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.enchantment));
								continue;
							}
						}
						else if ( buyName.equals("minecraft:tipped_arrow") )
						{
							try
							{
								buyStack = PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW, buy), PotionType.getPotionTypeForName(metaArray[0]));
							}
							catch (Exception e)
							{
								player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.sellName + " & " + trade.buyName + ". Check error logs for more details"));

								System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.sellName + "   buyName: " + trade.buyName + "   sellAmount: " + trade.sellAmount + "   buyAmount: " + trade.buyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.sellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.enchantment));
								continue;
							}
						}
						else
						{
							buyStack.setStackDisplayName(metaArray[0]);
							for ( int i = 0; i < metaArray.length - 1; )
							{
								buyStack.addEnchantment(Enchantment.getEnchantmentByLocation(metaArray[++i]), Integer.parseInt(metaArray[++i]));
							}
						}
					}

					if ( sellDamage != null )
					{
						sellStack.setItemDamage(sellDamage);
					}

					if ( buyDamage != null )
					{
						buyStack.setItemDamage(buyDamage);
					}

					int tradeAmount = 1;

					if ( sellStack.getItem() == Items.EMERALD || sellStack.getItem() == Item.getByNameOrId(ToroQuestConfiguration.scrollTradeItem) )
					{
						tradeAmount = MathHelper.clamp(ToroQuestConfiguration.tradeInventoryAmount / sellStack.getCount(), 1, ToroQuestConfiguration.maxTradeAmount);
					}
					else if ( buyStack.getItem() == Items.EMERALD || buyStack.getItem() == Item.getByNameOrId(ToroQuestConfiguration.scrollTradeItem) )
					{
						tradeAmount = MathHelper.clamp(ToroQuestConfiguration.tradeInventoryAmount / buyStack.getCount(), 1, ToroQuestConfiguration.maxTradeAmount);
					}

					recipeList.add(new MerchantRecipe(sellStack, optional, buyStack, 0, tradeAmount));
				}
			}
			catch (Exception e)
			{
				player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.sellName + " & " + trade.buyName + ". Check error logs for more details"));

				System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.sellName + "   buyName: " + trade.buyName + "   sellAmount: " + trade.sellAmount + "   buyAmount: " + trade.buyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.sellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.enchantment));
				continue;
			}
		}
		
		
		
		
		
		// GATED TRADES
		
		
		
		
		
		for ( GatedTrade trade : ToroQuestConfiguration.gatedTrades )
		{
			try
			{
				if ( (trade.varient.equals(varient) || trade.varient.equals("x")) && (jobName.equals(trade.job) || trade.job.equals("x")) && (trade.province.equals("x") || trade.province.equals(CivilizationType.tradeName(civ))) && (trade.minimunRepRequired.equals("x") || rep >= Integer.parseInt(trade.minimunRepRequired)) )
				{
					int sell = trade.sellAmount;
					int buy = trade.buyAmount;

					ItemStack sellStack = ItemStack.EMPTY;
					ItemStack optional = ItemStack.EMPTY;
					ItemStack buyStack = ItemStack.EMPTY;

					Integer sellDamage = null;
					Integer buyDamage = null;

					String buyName = "" + trade.buyName;
					String sellName = "" + trade.sellName;

					if ( count(trade.sellName, ':') > 1 )
					{
						int index = trade.sellName.lastIndexOf(':');
						sellDamage = Integer.parseInt(trade.sellName.substring(index + 1));
						sellName = sellName.substring(0, index);
					}

					if ( count(trade.buyName, ':') > 1 )
					{
						int index = trade.buyName.lastIndexOf(':');
						buyDamage = Integer.parseInt(trade.buyName.substring(index + 1));
						buyName = buyName.substring(0, index);
					}

					if ( !trade.sellOptional.equals("x") )
					{
						optional = new ItemStack(Item.getByNameOrId(trade.sellOptional), 1);
					}

					if ( sell < buy )
					{
						Item item = Item.getByNameOrId(sellName);
						int maxStackSize = item.getItemStackLimit();
						
						if ( sell > maxStackSize )
						{
							if ( !trade.sellOptional.equals("x") || sell > maxStackSize * 2 )
							{
								continue;
							}
							else
							{
								optional = new ItemStack(item, sell - maxStackSize);
							}
							sell = maxStackSize;
						}
						
						sellStack = new ItemStack(item, sell);

						item = Item.getByNameOrId(buyName);
						maxStackSize = item.getItemStackLimit();
						buy = getBuyPrice(buy, rep);
						if ( buy > maxStackSize )
						{
							continue;
						}
						buyStack = new ItemStack(item, buy);
					}
					else
					{
						sell = getSellPrice(sell, rep);
						Item item = Item.getByNameOrId(sellName);
						int maxStackSize = item.getItemStackLimit();

						if ( sell > maxStackSize )
						{
							if ( !trade.sellOptional.equals("x") || sell > maxStackSize * 2 )
							{
								continue;
							}
							else
							{
								optional = new ItemStack(item, sell - maxStackSize);
							}
							sell = maxStackSize;
						}

						sellStack = new ItemStack(item, sell);

						item = Item.getByNameOrId(buyName);

						if ( buy > item.getItemStackLimit() )
						{
							buy = item.getItemStackLimit();
						}

						buyStack = new ItemStack(item, buy);
					}

					if ( trade.enchantment != null )
					{
						// 0 , 1 , 2 , 3 , 4 , 5 , 6
						// item name, enchantment name, enchantment power, enchantment name, enchantment
						// power, ..., ...
						String[] metaArray = trade.enchantment.split("~");

						if ( buyName.equals("minecraft:enchanted_book") )
						{
							for ( int i = 0; i < metaArray.length; )
							{
								ItemEnchantedBook.addEnchantment(buyStack, new EnchantmentData(Enchantment.getEnchantmentByLocation(metaArray[i++]), Integer.parseInt(metaArray[i++])));
							}
						}
						else if ( buyName.equals("minecraft:potion") )
						{
							try
							{
								buyStack = PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId(metaArray[1]), buy), PotionType.getPotionTypeForName(metaArray[0]));
							}
							catch (Exception e)
							{
								player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.sellName + " & " + trade.buyName + ". Check error logs for more details"));

								System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.sellName + "   buyName: " + trade.buyName + "   sellAmount: " + trade.sellAmount + "   buyAmount: " + trade.buyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.sellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.enchantment));
								continue;
							}
						}
						else if ( buyName.equals("minecraft:tipped_arrow") )
						{
							try
							{
								buyStack = PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW, buy), PotionType.getPotionTypeForName(metaArray[0]));
							}
							catch (Exception e)
							{
								player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.sellName + " & " + trade.buyName + ". Check error logs for more details"));

								System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.sellName + "   buyName: " + trade.buyName + "   sellAmount: " + trade.sellAmount + "   buyAmount: " + trade.buyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.sellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.enchantment));
								continue;
							}
						}
						else
						{
							buyStack.setStackDisplayName(metaArray[0]);
							for ( int i = 0; i < metaArray.length - 1; )
							{
								buyStack.addEnchantment(Enchantment.getEnchantmentByLocation(metaArray[++i]), Integer.parseInt(metaArray[++i]));
							}
						}
					}

					if ( sellDamage != null )
					{
						sellStack.setItemDamage(sellDamage);
					}

					if ( buyDamage != null )
					{
						buyStack.setItemDamage(buyDamage);
					}

					int tradeAmount = 1;

					if ( sellStack.getItem() == Items.EMERALD || sellStack.getItem() == Item.getByNameOrId(ToroQuestConfiguration.scrollTradeItem) )
					{
						tradeAmount = MathHelper.clamp(ToroQuestConfiguration.tradeInventoryAmount / sellStack.getCount(), 1, ToroQuestConfiguration.maxTradeAmount);
					}
					else if ( buyStack.getItem() == Items.EMERALD || buyStack.getItem() == Item.getByNameOrId(ToroQuestConfiguration.scrollTradeItem) )
					{
						tradeAmount = MathHelper.clamp(ToroQuestConfiguration.tradeInventoryAmount / buyStack.getCount(), 1, ToroQuestConfiguration.maxTradeAmount);
					}

					/* Unlock Trade */
					if ( villager.unlockedGatedTrades )
					{
						recipeList.add(new MerchantRecipe(sellStack, optional, buyStack, 0, tradeAmount));
					}
					else
					{
						recipeList.add(new MerchantRecipe(sellStack, optional, buyStack, 50000, 50000+tradeAmount));
					}

					int gsell = trade.gatedSellAmount;
					int gbuy = trade.gatedBuyAmount;

					ItemStack gsellStack = ItemStack.EMPTY;
					ItemStack goptional = ItemStack.EMPTY;
					ItemStack gbuyStack = ItemStack.EMPTY;

					Integer gsellDamage = null;
					Integer gbuyDamage = null;

					String gbuyName = "" + trade.gatedBuyName;
					String gsellName = "" + trade.gatedSellName;

					if ( count(trade.gatedSellName, ':') > 1 )
					{
						int index = trade.gatedSellName.lastIndexOf(':');
						gsellDamage = Integer.parseInt(trade.gatedSellName.substring(index + 1));
						gsellName = gsellName.substring(0, index);
					}

					if ( count(trade.gatedBuyName, ':') > 1 )
					{
						int index = trade.gatedBuyName.lastIndexOf(':');
						gbuyDamage = Integer.parseInt(trade.gatedBuyName.substring(index + 1));
						gbuyName = gbuyName.substring(0, index);
					}

					if ( !trade.gatedSellOptional.equals("x") )
					{
						goptional = new ItemStack(Item.getByNameOrId(trade.gatedSellOptional), 1);
					}

					if ( gsell < gbuy )
					{
						Item item = Item.getByNameOrId(gsellName);
						int maxStackSize = item.getItemStackLimit();
						if ( gsell > maxStackSize )
						{
							if ( !trade.gatedSellOptional.equals("x") || gsell > maxStackSize * 2 )
							{
								continue;
							}
							else
							{
								goptional = new ItemStack(item, gsell - maxStackSize);
							}
							gsell = maxStackSize;
						}
						gsellStack = new ItemStack(item, gsell);

						item = Item.getByNameOrId(gbuyName);
						maxStackSize = item.getItemStackLimit();
						gbuy = getBuyPrice(gbuy, rep);
						if ( gbuy > maxStackSize )
						{
							continue;
						}
						gbuyStack = new ItemStack(item, gbuy);
					}
					else
					{
						gsell = getSellPrice(gsell, rep);
						Item item = Item.getByNameOrId(gsellName);
						int maxStackSize = item.getItemStackLimit();

						if ( gsell > maxStackSize )
						{
							if ( !trade.gatedSellOptional.equals("x") || gsell > maxStackSize * 2 )
							{
								continue;
							}
							else
							{
								goptional = new ItemStack(item, gsell - maxStackSize);
							}
							gsell = maxStackSize;
						}

						gsellStack = new ItemStack(item, gsell);

						item = Item.getByNameOrId(gbuyName);

						if ( gbuy > item.getItemStackLimit() )
						{
							gbuy = item.getItemStackLimit();
						}

						gbuyStack = new ItemStack(item, gbuy);
					}

					if ( trade.gatedEnchantment != null )
					{
						// 0 , 1 , 2 , 3 , 4 , 5 , 6
						// item name, enchantment name, enchantment power, enchantment name, enchantment
						// power, ..., ...
						String[] metaArray = trade.gatedEnchantment.split("~");

						if ( gbuyName.equals("minecraft:enchanted_book") )
						{
							for ( int i = 0; i < metaArray.length; )
							{
								ItemEnchantedBook.addEnchantment(gbuyStack, new EnchantmentData(Enchantment.getEnchantmentByLocation(metaArray[i++]), Integer.parseInt(metaArray[i++])));
							}
						}
						else if ( gbuyName.equals("minecraft:potion") )
						{
							try
							{
								gbuyStack = PotionUtils.addPotionToItemStack(new ItemStack(Item.getByNameOrId(metaArray[1]), gbuy), PotionType.getPotionTypeForName(metaArray[0]));
							}
							catch (Exception e)
							{
								player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.gatedSellName + " & " + trade.gatedBuyName + ". Check error logs for more details"));

								System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.gatedSellName + "   buyName: " + trade.gatedBuyName + "   sellAmount: " + trade.gatedSellAmount + "   buyAmount: " + trade.gatedBuyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.gatedSellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.gatedEnchantment));
								continue;
							}
						}
						else if ( gbuyName.equals("minecraft:tipped_arrow") )
						{
							try
							{
								gbuyStack = PotionUtils.addPotionToItemStack(new ItemStack(Items.TIPPED_ARROW, gbuy), PotionType.getPotionTypeForName(metaArray[0]));
							}
							catch (Exception e)
							{
								player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.gatedSellName + " & " + trade.gatedBuyName + ". Check error logs for more details"));

								System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.gatedSellName + "   buyName: " + trade.gatedBuyName + "   sellAmount: " + trade.gatedSellAmount + "   buyAmount: " + trade.gatedBuyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.gatedSellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.gatedEnchantment));
								continue;
							}
						}
						else
						{
							gbuyStack.setStackDisplayName(metaArray[0]);
							for ( int i = 0; i < metaArray.length - 1; )
							{
								gbuyStack.addEnchantment(Enchantment.getEnchantmentByLocation(metaArray[++i]), Integer.parseInt(metaArray[++i]));
							}
						}
					}

					if ( gsellDamage != null )
					{
						gsellStack.setItemDamage(gsellDamage);
					}

					if ( gbuyDamage != null )
					{
						gbuyStack.setItemDamage(gbuyDamage);
					}

					int gtradeAmount = 1;

					if ( gsellStack.getItem() == Items.EMERALD || gsellStack.getItem() == Item.getByNameOrId(ToroQuestConfiguration.scrollTradeItem) )
					{
						gtradeAmount = MathHelper.clamp(ToroQuestConfiguration.tradeInventoryAmount / gsellStack.getCount(), 1, ToroQuestConfiguration.maxTradeAmount);
					}
					else if ( gbuyStack.getItem() == Items.EMERALD || gbuyStack.getItem() == Item.getByNameOrId(ToroQuestConfiguration.scrollTradeItem) )
					{
						gtradeAmount = MathHelper.clamp(ToroQuestConfiguration.tradeInventoryAmount / gbuyStack.getCount(), 1, ToroQuestConfiguration.maxTradeAmount);
					}
										
					if ( villager.unlockedGatedTrades )
					{
						recipeList.add(new MerchantRecipe(gsellStack, goptional, gbuyStack, 0, gtradeAmount));
					}
					else if ( ToroQuestConfiguration.showGatedTradesAsLocked )
					{
						recipeList.add(new MerchantRecipe(gsellStack, goptional, gbuyStack, 99999, 0));
					}
				}
			}
			catch (Exception e)
			{
				player.sendMessage(new TextComponentString("ERROR GENERATING TRADE: " + trade.gatedSellName + " & " + trade.gatedBuyName + ". Check error logs for more details"));

				System.err.println(new TextComponentString("ERROR GENERATING TRADE:" + "   sellName: " + trade.gatedSellName + "   buyName: " + trade.gatedBuyName + "   sellAmount: " + trade.gatedSellAmount + "   buyAmount: " + trade.gatedBuyAmount + "   minimunRepRequired: " + trade.minimunRepRequired + "   province: " + trade.province + "   job: " + trade.job + "   sellOptional: " + trade.gatedSellOptional + "   varient: " + trade.varient + "   enchantment: " + trade.gatedEnchantment));
				continue;
			}
		}
		
		
		
		// ===
		

		// MAPS FOR EMERALDS
		if ( ToroQuestConfiguration.cartographerMapTrade && jobName.equals("cartographer") )
		{
			if ( varient.equals("0") )
			{
				if ( villager.treasureMap != null )
				{
					recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.COMPASS, 1), villager.treasureMap, 0, 1));
				}
				else
				{
					villager.treasureMap = TreasureMapForEmeralds(villager, player, "Mansion", MapDecoration.Type.MANSION);

					if ( villager.treasureMap != null )
					{
						// villager.treasureMap.setStackDisplayName("Map to Woodland Mansion");

						recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 8), new ItemStack(Items.COMPASS, 1), villager.treasureMap, 0, 1));
					}
				}
			}
			else if ( varient.equals("1") )
			{
				if ( villager.treasureMap != null )
				{
					recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.COMPASS, 1), villager.treasureMap, 0, 1));
				}
				else
				{
					villager.treasureMap = TreasureMapForEmeralds(villager, player, "Monument", MapDecoration.Type.MONUMENT);

					if ( villager.treasureMap != null )
					{
						// villager.treasureMap.setStackDisplayName("Map to Ocean Monument");

						recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.COMPASS, 1), villager.treasureMap, 0, 1));
					}
				}
			}
			else
			{
				if ( villager.treasureMap != null )
				{
					recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.COMPASS, 1), villager.treasureMap, 0, 1));
				}
				else
				{
					villager.treasureMap = civMapForEmeralds(villager, player, "Village", MapDecoration.Type.MANSION);

					if ( villager.treasureMap != null )
					{
						recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 4), new ItemStack(Items.COMPASS, 1), villager.treasureMap, 0, 1));
					}
				}
			}
		}
		return recipeList;
	}

	public static ItemStack TreasureMapForEmeralds( EntityToroVillager villager, EntityPlayer player, String destination, MapDecoration.Type destinationType )
	{
		World world = player.world;
		BlockPos blockpos = world.findNearestStructure(destination, player.getPosition(), true);

		if ( blockpos != null )
		{
			ItemStack itemstack = ItemMap.setupNewMap(world, (double) blockpos.getX(), (double) blockpos.getZ(), (byte) 4, true, true);
			ItemMap.renderBiomePreviewMap(world, itemstack);
			MapData.addTargetDecoration(itemstack, blockpos, "+", destinationType);
			itemstack.setTranslatableName("filled_map." + destination.toLowerCase(Locale.ROOT));
			return itemstack;
		}
		return null;
	}

	public static ItemStack civMapForEmeralds( EntityToroVillager villager, EntityPlayer player, String destination, MapDecoration.Type destinationType )
	{
		World world = villager.world;

		BlockPos pos = villager.getPosition();

		Village village = null;

		if ( pos == null )
		{
			return null;
		}

		pos = pos.add((world.rand.nextBoolean() ? 1 : -1) * world.rand.nextInt(32) * 64, 0, (world.rand.nextBoolean() ? 1 : -1) * world.rand.nextInt(32) * 64);

		if ( pos == null )
		{
			pos = villager.getPosition();
		}
		else
		{
			village = world.villageCollection.getNearestVillage(pos, 704);
		}

		if ( village == null )
		{
			village = world.villageCollection.getNearestVillage(villager.getPosition(), 704);
		}

		if ( village == null )
		{
			return null;
		}

		ItemStack itemstack = ItemMap.setupNewMap(world, (double) village.getCenter().getX(), (double) village.getCenter().getZ(), (byte) 4, true, true);

		ItemMap.renderBiomePreviewMap(world, itemstack);

		MapData.addTargetDecoration(itemstack, village.getCenter(), "+", destinationType);

		Province province = CivilizationUtil.getProvinceAt(villager.world, villager.chunkCoordX, villager.chunkCoordZ);

		if ( province == null || province.getCiv() == null )
		{
			itemstack.setStackDisplayName("Map to Village");
		}
		else
		{
			itemstack.setStackDisplayName("Map to " + province.getCiv().getDisplayName(player));
		}

		villager.treasureMap = itemstack;
		return itemstack;
	}

	// LEFT >
	public static int getSellPrice( int price, int rep ) // sell price is reduced the higher reputation
	{
		if ( rep < 0 )
		{
			return (int) (Math.round(MathHelper.clamp(((double) price * MathHelper.clamp((2.0D + rep / 150.0D), 2.0D, 4.0D)), 1, 128)));
		}
		else
		{
			return (int) (Math.round(MathHelper.clamp(((double) price * MathHelper.clamp((2.0D - rep / 3000.0D), 1.0D, 2.0D)), 1, 128)));
		}
	}

	// < RIGHT
	public static int getBuyPrice( int price, double rep ) // buy price is increased the higher reputation
	{
		if ( rep < 0 )
		{
			return (int) (Math.round(MathHelper.clamp(((double) price * MathHelper.clamp((0.5D - rep / 1200.0D), 0.25D, 0.5D)), 1, 128)));
		}
		else
		{
			return (int) (Math.round(MathHelper.clamp(((double) price * MathHelper.clamp((0.5D + rep / 6000.0D), 0.5D, 1.0D)), 1, 128)));
		}
	}

	public static int count( String str, char c )
	{
		int count = 0;

		for ( int i = 0; i < str.length(); i++ )
		{
			if ( str.charAt(i) == c )
				count++;
		}

		return count;
	}
}