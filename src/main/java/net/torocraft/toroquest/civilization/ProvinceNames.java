package net.torocraft.toroquest.civilization;

import java.util.Random;

public class ProvinceNames
{

	// private static final String[] PARTS1 = { "a", "e", "i", "o", "u", "", "", "",
	// "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" };
	// private static final String[] PARTS2 = { "b", "c", "d", "f", "g", "h", "j",
	// "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z", "br",
	// "cr", "dr", "fr", "gr", "kr", "pr", "qr", "sr", "tr", "vr", "wr", "yr", "zr",
	// "str",
	// "bl", "cl", "fl", "gl", "kl", "pl", "sl", "vl", "yl", "zl", "ch", "kh", "ph",
	// "sh", "yh", "zh" };
	// private static final String[] PARTS3 = { "a", "e", "i", "o", "u", "ae", "ai",
	// "ao", "au", "aa", "ee", "ea", "ei", "eo", "eu", "ia", "ie", "io", "iu", "oa",
	// "oe", "oi", "oo", "ou", "ua", "ue", "ui", "uo", "uu", "a", "e", "i", "o",
	// "u",
	// "a", "e", "i", "o", "u", "a", "e", "i", "o", "u", "a", "e", "i", "o", "u",
	// "a", "e", "i", "o", "u" };
	// private static final String[] PARTS4 = { "b", "c", "d", "f", "g", "h", "j",
	// "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z", "br",
	// "cr", "dr", "fr", "gr", "kr", "pr", "tr", "vr", "wr", "zr", "st", "bl", "cl",
	// "fl",
	// "gl", "kl", "pl", "sl", "vl", "zl", "ch", "kh", "ph", "sh", "zh" };
	// private static final String[] PARTS5 = { "c", "d", "f", "h", "k", "l", "m",
	// "n", "p", "r", "s", "t", "x", "y", "", "", "", "", "", "", "", "", "", "",
	// "", "", "", "", "" };
	//
	// ANY_START
	// ANY_CENTER
	// "hyth", "", "vor", "th", "az", "ond", "ar", "an", "ay", "hem", "ak", "av",
	// "ev"
	// ANY_END

	// private static final String[] PARTS6 = { "aco", "ada", "adena", "ago",
	// "agos", "aka", "ale", "alo", "am", "anbu", "ance", "and", "ando", "ane",
	// "ans", "anta", "arc", "ard", "ares", "ario", "ark", "aso", "athe", "eah",
	// "edo", "ego",
	// "eigh", "eim", "eka", "eles", "eley", "ence", "ens", "ento", "erton", "ery",
	// "esa", "ester", "ey", "ia", "ico", "ido", "ila", "ille", "in", "inas", "ine",
	// "ing", "irie", "ison", "ita", "ock", "odon", "oit", "ok", "olis", "olk",
	// "oln", "ona", "oni", "onio", "ont", "ora", "ord", "ore", "oria", "ork",
	// "osa", "ose", "ouis", "ouver", "ul", "urg", "urgh", "ury" };

	private static final String[] START_X =
	{
		"foreck", "lok", "vvarden", "rotn", "eagle", "wolf", "wolfe", "drift", "razer", "razor", "widow", "vvarz", "vvel", "sunken", "ender", "wither", "neth", "skel", "stev", "king", "duke", "baron", "aro", "arow", "fort", "angel", "demon", "daemon", "rother", "davon", "narn", "blen", "dews", "raven", "ravens", "ror", "nor", "falk", "wind", "mor", "mark", "wyver", "boar", "dragon", "alc", "farn", "fourne", "strath", "jed", "bar", "dale", "cael", "hals", "calen", "ha", "dagger", "swift", "far", "gloom", "hero", "might", "mid", "modan", "loch", "mal", "vy", "vor", "vvar", "vir", "vyn", "mor", "went", "bear", "hartl", "ter", "terg", "swan", "doon", "mas", "high", "fae", "new", "first", "hinter", "north", "south", "east", "west", "way", "liver", "sky", "brer", "ez", "skol", "breeze", "wind", "val", "fjor", "forn", "wither", "stoh", "grog", "elder", "dunn", "sin", "rod", "soar", "wilt", "wult", "ad", "small", "sult", "sword", "ox", "mount", "old", "new", "blood", "hinter", "north", "south", "east", "west", "way", "kul", "lys", "mar", "rael"
	};

	private static final String[] END_X =
	{
		"mend", "lot", "lock", "herst", "vvast", "vast", "gar", "ggar", "garr", "hook", "ille", "ath", "kath", "thal", "end", "tome", "crown", "mare", "song", "foss", "ghost", "gost", "clod", "cloud", "azan", "wall", "aron", "aslahti", "pridd", "iston", "kirkey", "might", "age", "spirit", "ison", "odon", "ury", "brine", "ax", "axe", "barrow", "bell", "bend", "bert", "borne", "brand", "brawn", "break", "bridge", "burg", "burgh", "bury", "bus", "by", "caste", "castle", "cast", "caster", "cester", "chester", "glen", "eglos", "cost", "crest", "cry", "dale", "deep", "deft", "hunt", "ingham", "keld", "del", "bell", "dell", "delve", "den", "dyn", "dence", "denfel", "dew", "diff", "ding", "don", "down", "worthy", "worth", "werth", "bydder", "ledo", "never", "meld", "hyth", "fall", "falls", "fare", "fast", "fel", "feld", "fell", "field", "fields", "ford", "forge", "fray", "ruther", "ouver", "gan", "gard", "garde", "gas", "gate", "gend", "glade", "glen", "gow", "grasp", "heim", "gaard", "spear", "syde", "kyln", "kyrk", "lynd", "dren", "drova", "guard", "gulch", "hal", "ham", "hamm", "hammer", "haven", "head", "heart", "hearth", "heath", "helm", "sted", "steed", "steen", "lend", "dust", "edge", "hill", "hold", "hollow", "breth", "morag", "yeld", "yeild", "fen", "nite", "night", "holm", "horn", "hal", "heel", "hall", "haal", "holme", "hull", "kin", "spear", "knox", "kiln", "keg", "post", "holde", "kirk", "kled", "kneel", "heart", "ley", "gatnon", "rin", "glen", "dydd", "land", "lands", "keep", "deep", "ling", "hallow", "lull", "mar", "gale", "march", "mark", "mead", "lry", "meet", "mer", "mere", "mert", "mery", "end", "erilon", "scar", "ward", "mond", "mont", "moon", "moar", "more", "moth", "mourn", "mouth", "myr", "nard", "ox", "ston", "rother", "hol", "path", "stow", "vein", "breach", "pass", "phia", "phis", "pike", "pole", "polis", "pool", "port", "post", "quarin", "veldt", "ver", "vik", "borne", "las", "den", "quay", "rage", "ran", "rest", "rester", "ridge", "burgh", "rift", "sby", "rith", "road", "roads", "sten", "stin", "stine", "stoe", "erston", "ron", "rora", "ross", "rough", "run", "ern", "wit", "sa", "sall", "sas", "atel", "scape", "set", "sey", "side", "son", "wik", "will", "fair", "combe", "peak", "lance", "point", "spell", "sham", "shaw", "shawl", "shade", "brow", "shayd", "bellows", "sheen", "shelter", "shield", "shire", "erton", "vault", "valt", "wich", "stone", "storm", "sunder", "ta", "taed", "ten", "ther", "thorn", "thorps", "thral", "tin", "tine", "fair", "fare", "hand", "tol", "tomb", "ton", "vail", "vale",
		"valley", "saw", "var", "veld", "sper", "skull", "stall", "star", "stead", "yta", "zyrn", "eve", "mantle", "host", "ville", "cliff", "rock", "lyrock", "ving", "vist", "vsor", "vyr", "war", "watch", "way", "well", "wen", "threl", "wick", "clae", "rell", "rel", "riel", "spur", "cath", "walk", "wych", "runn", "mora", "morag", "wytch", "widge", "wright", "wulf", "wyck", "ysaf", "lens", "kil"
	};

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= LEAF
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private static final String[] START_EARTH =
	{
		"green", "wild", "maple", "pine", "oak", "oaken", "willow", "tangle", "primal", "brush", "lumber", "thrag", "strong", "elder", "verdure", "virid", "emerald", "moss", "ash", "emerald", "wilder", "timber", "tangle", "summer", "vvild", "copse", "elm", "willow", "birch", "ashen", "lumber", "mid", "elf", "elven", "brent", "bright"
	};

	private static final String[] END_EARTH =
	{
		"weald", "glade", "lush", "copse", "thicket", "clave", "garden", "fern", "wood", "woods", "bark", "forest", "leaf", "peak", "vale", "glade", "lot", "land", "grove", "stand", "wilds", "hill"
	};

	private static final String[] EARTH =
	{
		"sycamore", "elfsong", "daggerford", "lumber", "notch", "leilon", "orchard", "lumber", "timber", "arbor", "arbour", "alcove", "arborvitae", "bower", "labyrinth", "lombardy", "viburnum", "arrowwood", "sycamore", "laurel", "sequoia", "maidenhair", "maple", "adansonia", "cordata", "caprea", "tilia", "quercus", "hemlock", "camphor", "yggdrassil"
	};

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= MOUNTAIN
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private static final String[] START_WIND =
	{
		"hammer", "bryn", "oxen", "steel", "sunder", "smolder", "thunder", "sky", "boulder", "grey", "gray", "brow", "stone", "iron", "tin", "copper", "silver", "slate", "high", "bronze", "clay", "shale", "granite", "coal", "slate", "flint", "silt", "high", "sulfur", "grey", "ash", "sky", "high"
	};

	private static final String[] END_WIND =
	{
		"steed", "keep", "born", "smith", "smoke", "brow", "crown", "ridge", "heights", "pinnacle", "rise", "fist", "bluff", "summit", "slopes", "land", "planes", "smith", "fall", "forge", "reach", "rise", "brick", "burg", "clif", "cliff", "peak", "cliffe", "rock", "brick", "bleak", "break", "hill", "hills", "mont", "rize", "gate"
	};

	private static final String[] WIND =
	{
		"whiterock", "blackrock", "blackcliff", "anvil", "adamantine", "agate", "coade", "boulder", "alps", "zenith", "summit", "shale", "chisel", "quarry", "grotto", "forge", "apex", "bronn", "sunder", "skyreach", "thunder", "flint", "hammer", "haven", "forge", "ironhill"
	};
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= ROSE
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private static final String[] START_FIRE =
	{
		"spring", "tangle", "vine", "vyne", "red", "spring", "clover", "thorn", "petal", "flora", "floral", "bloom", "rose", "lila", "aloe", "aster", "calla", "crimson", "anther", "prim", "flax", "sweet", "fox", "far", "vast"
	};

	private static final String[] END_FIRE =
	{
		"thorn", "glade", "garden", "grove", "cone", "seed", "stem", "bloom", "meadow", "meadows", "fern", "field", "fields", "glen", "dale", "vale", "plains", "crown", "reath", "briar", "land", "brier", "valley", "acer", "shrub", "acre", "bloom", "blossom", "haven", "heath", "hearth", "garden", "flats"
	};

	private static final String[] FIRE =
	{
		"edelweiss", "leilon", "amaranth", "hydrangea", "daffodil", "azalea", "virrose", "floret", "anther", "allium", "azela", "perennial", "avens", "yucca", "hawthorn", "crimson", "scarlet", "eglantine", "vera", "acer", "aloe", "hibiscus", "calluna", "heather", "lonicera", "malenocarpa", "weigela", "lantana", "daphne", "peony", "bloom", "talon", "sage", "yarrow", "aster", "iris", "lavender", "prim", "calla", "lily", "laurel", "silverleaf", "anemone", "ivy", "amaryllis", "camellia", "maidenhair", "alstroemeria", "bleedingheart", "purslane", "coreopsis", "blossom", "clematis", "vernal", "vinca", "weigela", "phlox", "alyssum", "dahlias", "forsythia", "foxglove", "spirea", "meadow", "lombardy", "thistle", "marigold", "lilac", "coppice", "bosquet", "garnet", "thornhill", "harvest"
	};
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= GLACIER
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private static final String[] START_WATER =
	{
		"bryn", "water", "ivar", "sno", "snow", "drift", "flur", "bliz", "sleet", "storm", "bleak", "alf", "sleet", "white", "pale", "winter", "freeze", "frost", "cold", "winter", "river", "shiver", "kirk", "surj", "nov", "thor", "dew", "sunke", "ice", "glace", "glacier", "bleak", "numb", "glacial", "wolf", "polar", "novi", "hers", "ulf"
	};

	private static final String[] END_WATER =
	{
		"shander", "crest", "stead", "sted", "burg", "jarl", "berg", "heim", "rime", "kir", "dner", "dnar", "grasp", "gate", "watch", "bite", "fjord", "gard", "guard", "vik", "vin", "kild", "igrad", "graft", "hild", "hold", "hearth", "ford", "weld", "whelm", "kyr", "thaw", "post", "fort", "watch", "well", "rift", "run", "scape", "weald", "snap", "fall", "igrad", "ridge"
	};

	private static final String[] WATER =
	{
		"luskan", "targos", "caer-konig", "caer-dineval", "bremen", "brynshander", "hindarsfjall", "bremervoord", "velen", "ulfhednar", "odin", "thor", "loki", "hersir", "alfheim", "berserkir", "ice", "rime", "solitude", "coldsnap", "shatter", "frigid", "solitude", "wintergrasp", "vigrar", "sapphire", "haithabu", "nidaros", "bjorgvin", "dyflin", "vadrejford", "kirkjuvagr", "fjord", "vellir", "burrrg", "berrrg", "shiver", "weald", "bite", "fjord", "thaw", "frost", "blizzard", "berksard", "birka", "roskilde", "reykjavik", "jorvik", "wulfir", "brattahild", "leirvik", "vallenkyr", "fyresdal", "undvik", "skillige", "aard", "yrden", "verglas", "myrkr", "forndom", "vaknan", "ivar", "herja", "domadagr", "skapanir", "munarvagr", "novigrad", "slumber", "wolfir"
	};
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= SUN
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private static final String[] SUN =
	{
		"llux", "ssol", "igni", "bllaze", "buurn", "szaand", "rajtle", "aarid", "ssun", "smuulder", "sstar", "sspaer", "sznaayke", "fyyre", "qysous", "dhune", "dune", "menso", "dehru", "qiszu", "acnudet", "eksoudos", "kuzutmaty", "medje", "apkhelzum", "bastet", "amon", "nefaru", "retzu", "djoser", "khufu", "ikhnaton", "vizier", "dehno", "farsathis", "ahkn", "ashruzum", "gessyty", "clyssena", "sakrubenu", "shekha", "kizutjer", "besuthis", "hatshepsut", "thutmose", "amenhotep", "nefertiti", "neknenutjer", "kerbezum", "nekhsaihdet", "sakdjuta", "hebsousut", "kusdjeris", "bbehdfu", "akso", "medtutaten", "akhenaten", "tutankhamun", "naphurureya", "djedsa", "shetneisma", "acdjuhdet", "mmddjumunein", "kutepis", "shasous", "cusdjuyut", "sshasasiris", "nabefu", "ramses", "xerxes", "cleopatra", "amenophis", "qisbusir", "behretaten", "sett", "ra", "osiris", "thoth", "ptah", "hathor", "anubis"
	};
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= SWAMP
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	private static final String[] START_MOON =
	{
		"rot", "wailing", "fallen", "soul", "ash", "salt", "blight", "swamp", "peat", "peats", "corpse", "rot", "weap", "stilt", "pitch", "copse", "shamble", "ink", "soot", "wilt", "dark", "bog", "mud", "musk", "shadow", "ahsen", "bitter", "blight", "fog", "weeping", "black", "gilt", "black", "scar", "night", "luster", "ink", "mirk", "murk", "moor", "mourn", "quag", "barren", "ghast", "bleak", "dead", "sump", "marsh", "sump", "glade", "slough", "musk", "gloom", "moon", "dusk", "shade", "willow", "luster", "lily", "ghost", "death"
	};

	private static final String[] END_MOON =
	{
		"gunk", "cove", "mist", "deep", "smog", "marsh", "burg", "fen", "fens", "basin", "borg", "cove", "quag", "basin", "pond", "keg", "well", "dam", "morg", "warg", "wail", "muck", "clog"
	};

	private static final String[] MOON =
	{
		"barren", "decay", "silt", "scythe", "end", "quiteus", "gloom", "hadean", "soot", "ruin", "woe", "weap", "ink", "ash", "stygian", "lament", "luster", "abyss", "wetland", "fens", "fen", "ash", "willows", "scar", "sorrow", "ghostmarsh", "wilt", "shade", "blight", "hallow", "shamble", "will-o", "ghast", "dread", "lanquish", "phlegm", "hollow", "dusk", "shade", "bittermarsh", "ghast"
	};
	/*
	 * 
	 * 
	 * 
	 * "burn
	 * "dark oaken green hero brine
	 * hammer
	 * coal celt igar caste rly by water bul wer buck dyre hollow bright break brax
	 * bridge branch rellbolttyde grave thonfort banerose yrion
	 * bark mor
	 * polder marsh swale
	 * blush
	 * seed
	 * wild verdant
	 * "blak" "isle" "gray", "grey", ","murk","muck","moore","mire"
	 * ore fay
	 * acor knox hard
	 * "melt"
	 * decay bay deep flood
	 * shell
	 * acres ash dark putri mush misty smog smogg smoggy sore mud mudd muddy
	 * blanch
	 * brush oaken maple clover
	 * sigil
	 * fyre byrch barkam
	 * tyde
	 * gyll gill gil
	 * 
	 * 
	 * skul vvar rich lucia
	 * vvarden modan lock lok hill herst dam town keep
	 * 
	 * cold wolf gilmmer rage cliff lime chill kneel ruin sage mouth new ton ash
	 * guard breeze home
	 * lin gold port maur it ania salus malazan iverin rich miry alven hime reven
	 * riven dell
	 * nill hime ren felen stone eo ander van innis huis kraken gaard
	 * lumen saunt Saunt anslem wren fallow knight agilhon son fobble goz reh
	 * sky west wick 's Outpost asradala Iron silver moon Tel'Doras light
	 * grave Deeps DDominion loch dun morogh phen casterly drog skul acron bog sworm
	 * koor swym argor hun see grim cliff rock
	 * bul phan dalin winter thunder tree llast Port lusk kan
	 * horn ham wharf tide front wind bourne ham run watch wallow
	 * thorn, weed, flower
	 * star tead ridge bury borne rift ford burg well burgh burg mont
	 * ridge vale stead bridge mond tead ten
	 * 
	 * HOUSE MYTHRIL - brown - mountain
	 * HOUSE WILD - green - leaf
	 * HOUSE DAWN - yellow - sun
	 * HOUSE MIRE - black - swamp
	 * HOUSE GLACIER - teal - snow
	 * "glacier", "ports", "frost", "winter", "teal", "blue", "
	 * 
	 * "chill", "well", "port", "wave", "snow",
	 * 
	 * moss tangle leaf primal green timber sage virid verdure moss green maple oak
	 * oaken ash willow elder emerald wilder elder
	 * 
	 * thicket wood woods wilds wild shelter weald forest bark vine grove
	 * 
	 * 
	 * 
	 * "sun",
	 * bleak
	 * 
	 * acre
	 * sea
	 * shell
	 * brook
	 * fjord
	 * marsh
	 * port
	 * pool
	 * salt
	 * pool
	 * sea
	 * hill
	 * shore
	 * 
	 * winter
	 * wend
	 * 
	 * 
	 */

	public static String random( Random rand, CivilizationType civ )
	{
		StringBuilder buf = new StringBuilder();

		if ( civ == null )
		{
			buf.append(choose(rand, START_X));
			buf.append(choose(rand, END_X));
		}
		else
			switch( civ )
			{
			case EARTH:
			{
				if ( rand.nextInt(4) == 0 )
				{
					if ( rand.nextBoolean() )
					{
						buf.append(choose(rand, START_EARTH));
					}
					else
					{
						buf.append(choose(rand, START_X));
					}

					if ( rand.nextBoolean() )
					{
						buf.append(choose(rand, END_EARTH));
					}
					else
					{
						buf.append(choose(rand, END_X));
					}
				}
				else
				{
					buf.append(choose(rand, EARTH));
				}
				break;
			}
			case WIND:
			{
				if ( rand.nextInt(16) == 0 )
				{
					buf.append(choose(rand, WIND));
				}
				else
				{
					if ( rand.nextBoolean() )
					{
						buf.append(choose(rand, START_WIND));
					}
					else
					{
						buf.append(choose(rand, START_X));
					}

					if ( rand.nextBoolean() )
					{
						buf.append(choose(rand, END_WIND));
					}
					else
					{
						buf.append(choose(rand, END_X));
					}
				}
				break;
			}
			case FIRE:
			{
				if ( rand.nextInt(4) == 0 )
				{
					buf.append(choose(rand, START_FIRE));

					if ( rand.nextBoolean() )
					{
						buf.append(choose(rand, END_FIRE));
					}
					else
					{
						buf.append(choose(rand, END_X));
					}
				}
				else
				{
					buf.append(choose(rand, FIRE));
				}
				break;
			}
			case WATER:
			{
				if ( rand.nextInt(6) == 0 )
				{
					buf.append(choose(rand, WATER));
				}
				else
				{
					if ( rand.nextInt(3) == 0 )
					{
						buf.append(choose(rand, START_X));
					}
					else
					{
						buf.append(choose(rand, START_WATER));
					}
					if ( rand.nextInt(6) == 0 )
					{
						buf.append(choose(rand, END_WATER));
					}
					else
					{
						buf.append(choose(rand, END_X));
					}
				}
				break;
			}
			case SUN:
			{
				// if ( rand.nextInt(5) == 0 )
				// {
				// if ( rand.nextInt(3) == 0 )
				// {
				// buf.append(choose(rand, START_SUN));
				// }
				// else
				// {
				// buf.append(choose(rand, START_X));
				// }
				// if ( rand.nextInt(3) == 0 )
				// {
				// buf.append(choose(rand, END_SUN));
				// }
				// else
				// {
				// buf.append(choose(rand, END_X));
				// }
				// }
				// else
				{
					buf.append(choose(rand, SUN));
				}
				break;
			}
			case MOON:
			{
				if ( rand.nextBoolean() )
				{
					if ( rand.nextBoolean() )
					{
						buf.append(choose(rand, START_MOON));
					}
					else
					{
						buf.append(choose(rand, START_X));
					}
					if ( rand.nextInt(3) == 0 )
					{
						buf.append(choose(rand, END_MOON));
					}
					else
					{
						buf.append(choose(rand, END_X));
					}
				}
				else
				{
					buf.append(choose(rand, MOON));
				}
				break;
			}
			default:
			{
				buf.append(choose(rand, START_X));
				buf.append(choose(rand, END_X));
				break;
			}
			}

		// if (i < 3) {
		// buf.append(choose(rand, PARTS1));
		// buf.append(choose(rand, PARTS2));
		// buf.append(choose(rand, PARTS3));
		// buf.append(choose(rand, PARTS5));
		// buf.append(choose(rand, PARTS7));
		// } else if (i < 5) {
		// buf.append(choose(rand, PARTS3));
		// buf.append(choose(rand, PARTS4));
		// buf.append(choose(rand, PARTS3));
		// buf.append(choose(rand, PARTS5));
		// buf.append(choose(rand, PARTS7));
		// } else if (i < 8) {
		// buf.append(choose(rand, PARTS1));
		// buf.append(choose(rand, PARTS2));
		// buf.append(choose(rand, PARTS6));
		// } else {
		// buf.append(choose(rand, PARTS1));
		// buf.append(choose(rand, PARTS2));
		// buf.append(choose(rand, PARTS3));
		// buf.append(choose(rand, PARTS4));
		// buf.append(choose(rand, PARTS6));
		// }

		String name = buf.toString();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private static String choose( Random rand, String[] parts )
	{
		return parts[rand.nextInt(parts.length)];
	}

}