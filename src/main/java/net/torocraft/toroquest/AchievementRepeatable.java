package net.torocraft.toroquest;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class AchievementRepeatable implements ICriterionTrigger<AchievementRepeatable.Instance>
{
	private final ResourceLocation id;
	private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

	public AchievementRepeatable( String id )
	{
		this.id = new ResourceLocation(id);
	}

	public AchievementRepeatable( ResourceLocation id )
	{
		this.id = id;
	}

	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	public void addListener( PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<AchievementRepeatable.Instance> listener )
	{
		AchievementRepeatable.Listeners l = listeners.get(playerAdvancementsIn);

		if ( l == null )
		{
			l = new AchievementRepeatable.Listeners(playerAdvancementsIn);
			listeners.put(playerAdvancementsIn, l);
		}

		l.add(listener);
	}

	@Override
	public void removeListener( PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener )
	{
		AchievementRepeatable.Listeners l = listeners.get(playerAdvancementsIn);

		if ( l != null )
		{
			l.remove(listener);

			if ( l.isEmpty() )
			{
				listeners.remove(playerAdvancementsIn);
			}
		}
	}

	@Override
	public void removeAllListeners( PlayerAdvancements playerAdvancementsIn )
	{
		listeners.remove(playerAdvancementsIn);
	}

	@Override
	public AchievementRepeatable.Instance deserializeInstance( JsonObject json, JsonDeserializationContext context )
	{
		return new AchievementRepeatable.Instance(getId());
	}

	public void trigger( EntityPlayerMP parPlayer )
	{
		AchievementRepeatable.Listeners l = listeners.get(parPlayer.getAdvancements());

		if ( l != null )
		{
			l.trigger(parPlayer);
		}
	}

	public static class Instance extends AbstractCriterionInstance
	{

		public Instance( ResourceLocation parRL )
		{
			super(parRL);
		}

		public boolean test()
		{
			return true;
		}
	}

	static class Listeners
	{
		private final PlayerAdvancements playerAdvancements;
		private final Set<Listener<Instance>> listeners = Sets.newHashSet();

		public Listeners( PlayerAdvancements playerAdvancementsIn )
		{
			playerAdvancements = playerAdvancementsIn;
		}

		public boolean isEmpty()
		{
			return listeners.isEmpty();
		}

		public void add( ICriterionTrigger.Listener<Instance> listener )
		{
			listeners.add(listener);
		}

		public void remove( ICriterionTrigger.Listener<Instance> listener )
		{
			// listeners.remove(listener);
		}

		public void trigger( EntityPlayerMP player )
		{
			ArrayList<Listener<Instance>> list = null;

			Advancement advancement = null;
			String criterionName = null;

			for ( ICriterionTrigger.Listener<Instance> listener : listeners )
			{
				try
				{
					advancement = (Advancement) ObfuscationReflectionHelper.findField(listener.getClass(), "advancement").get(listener);
					criterionName = (String) ObfuscationReflectionHelper.findField(listener.getClass(), "criterionName").get(listener);
					if ( advancement != null && advancement != null )
					{
						playerAdvancements.revokeCriterion(advancement, criterionName);
					}
				}
				catch (Exception e)
				{

				}

				if ( listener.getCriterionInstance().test() )
				{
					if ( list == null )
					{
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if ( list != null )
			{
				for ( ICriterionTrigger.Listener<Instance> listener1 : list )
				{
					listener1.grantCriterion(playerAdvancements);
				}
			}
		}
	}
}