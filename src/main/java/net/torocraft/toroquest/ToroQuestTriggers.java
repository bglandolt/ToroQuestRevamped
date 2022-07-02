package net.torocraft.toroquest;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.advancements.CriteriaTriggers;

/* EventHandlers.repLevelMessage */
// ToroQuestTriggers.FRIENDLY_ACHIEVEMNT.trigger((EntityPlayerMP)player);

public class ToroQuestTriggers
{
	// LEGEND(), HERO(), CHAMPION(), EXALTED(), REVERED(), HONORED(), FRIENDLY(),
	// NEUTRAL(), UNFRIENDLY(), HOSTILE(), HATED(), EXILED();
	// public static final Achievement EXILED_ACHIEVEMNT = new
	// Achievement(ToroQuest.MODID + ":exiled");

	public static final Achievement FRIENDLY_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":friendly");
	public static final Achievement HONORED_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":honored");
	public static final Achievement RENOWNED_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":renowned");
	public static final Achievement EXALTED_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":exalted");
	public static final Achievement CHAMPION_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":champion");
	public static final Achievement HERO_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":hero");
	public static final Achievement LEGEND_ACHIEVEMNT = new Achievement(ToroQuest.MODID + ":legend");

	public static final AchievementRepeatable QUEST_ACHIEVEMENT = new AchievementRepeatable(ToroQuest.MODID + ":quest");
	public static final AchievementRepeatable TRADE_ACHIEVEMENT = new AchievementRepeatable(ToroQuest.MODID + ":trade");

	public static final Achievement MURDERHOBO_ACHIEVEMENT = new Achievement(ToroQuest.MODID + ":murderhobo");

	public static final Achievement KINGSLAYER_ACHIEVEMENT = new Achievement(ToroQuest.MODID + ":kingslayer");
	
	public static final Achievement MURDERHOBO_ACHIEVEMENT_RED = new Achievement(ToroQuest.MODID + ":murderhobo_red");
	public static final Achievement MURDERHOBO_ACHIEVEMENT_GREEN = new Achievement(ToroQuest.MODID + ":murderhobo_green");
	public static final Achievement MURDERHOBO_ACHIEVEMENT_BLUE = new Achievement(ToroQuest.MODID + ":murderhobo_blue");
	public static final Achievement MURDERHOBO_ACHIEVEMENT_BLACK = new Achievement(ToroQuest.MODID + ":murderhobo_black");
	public static final Achievement MURDERHOBO_ACHIEVEMENT_BROWN = new Achievement(ToroQuest.MODID + ":murderhobo_brown");
	public static final Achievement MURDERHOBO_ACHIEVEMENT_YELLOW = new Achievement(ToroQuest.MODID + ":murderhobo_yellow");

	public static void register()
	{
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT_RED);
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT_GREEN);
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT_BLUE);
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT_BLACK);
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT_BROWN);
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT_YELLOW);
		
		CriteriaTriggers.register(MURDERHOBO_ACHIEVEMENT);

		CriteriaTriggers.register(KINGSLAYER_ACHIEVEMENT);

		CriteriaTriggers.register(FRIENDLY_ACHIEVEMNT);
		CriteriaTriggers.register(HONORED_ACHIEVEMNT);
		CriteriaTriggers.register(RENOWNED_ACHIEVEMNT);
		CriteriaTriggers.register(EXALTED_ACHIEVEMNT);
		CriteriaTriggers.register(CHAMPION_ACHIEVEMNT);
		CriteriaTriggers.register(HERO_ACHIEVEMNT);
		CriteriaTriggers.register(LEGEND_ACHIEVEMNT);

		CriteriaTriggers.register(TRADE_ACHIEVEMENT);
		CriteriaTriggers.register(QUEST_ACHIEVEMENT);
	}
}