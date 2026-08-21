package com.newerstylehitsplats;

import java.util.List;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.ParamID;
import net.runelite.api.StructComposition;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

final class CombatStyleIconResolver
{
	private CombatStyleIconResolver()
	{
	}

	static List<CombatIcon> resolve(Client client)
	{
		int weaponType = client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY);
		int styleIndex = client.getVarpValue(VarPlayerID.COM_MODE);
		if (styleIndex == 4)
		{
			styleIndex += client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE);
		}

		EnumComposition weaponStyles = client.getEnum(EnumID.WEAPON_STYLES);
		int styleEnumId = weaponStyles.getIntValue(weaponType);
		if (styleEnumId == -1)
		{
			return fallbackForSpecialWeapon(weaponType, styleIndex);
		}

		int[] styleStructs = client.getEnum(styleEnumId).getIntVals();
		if (styleIndex < 0 || styleIndex >= styleStructs.length)
		{
			return List.of(CombatIcon.ATTACK);
		}

		StructComposition style = client.getStructComposition(styleStructs[styleIndex]);
		return iconsForStyleName(style.getStringValue(ParamID.ATTACK_STYLE_NAME), styleIndex);
	}

	static List<CombatIcon> iconsForStyleName(String styleName)
	{
		return iconsForStyleName(styleName, -1);
	}

	static List<CombatIcon> iconsForStyleName(String styleName, int styleIndex)
	{
		if (styleName == null)
		{
			return List.of(CombatIcon.ATTACK);
		}

		switch (styleName.toUpperCase(Locale.ROOT))
		{
			case "AGGRESSIVE":
				return List.of(CombatIcon.STRENGTH);
			case "DEFENSIVE":
				// RuneLite's weapon-style enum labels staff index 5 as Defensive,
				// even though it is the Magic + Defence autocast mode.
				if (styleIndex == 5)
				{
					return List.of(CombatIcon.MAGIC, CombatIcon.DEFENCE);
				}
				return List.of(CombatIcon.DEFENCE);
			case "RANGING":
			case "RAPID":
				return List.of(CombatIcon.RANGED);
			case "LONGRANGE":
			case "LONG RANGE":
				return List.of(CombatIcon.RANGED, CombatIcon.DEFENCE);
			case "CASTING":
			case "AUTOCAST":
				return List.of(CombatIcon.MAGIC);
			case "DEFENSIVE CASTING":
			case "DEFENSIVE AUTOCAST":
				return List.of(CombatIcon.MAGIC, CombatIcon.DEFENCE);
			case "CONTROLLED":
				return List.of(CombatIcon.ATTACK, CombatIcon.STRENGTH, CombatIcon.DEFENCE);
			case "ACCURATE":
			default:
				return List.of(CombatIcon.ATTACK);
		}
	}

	static List<CombatIcon> fallbackForSpecialWeapon(int weaponType, int styleIndex)
	{
		if ((weaponType == 22 && styleIndex == 1)
			|| (weaponType == 30 && (styleIndex == 1 || styleIndex == 2)))
		{
			return List.of(CombatIcon.STRENGTH);
		}
		if ((weaponType == 22 || weaponType == 30) && styleIndex == 3)
		{
			return List.of(CombatIcon.DEFENCE);
		}
		if (weaponType == 22 && styleIndex == 4)
		{
			return List.of(CombatIcon.MAGIC);
		}
		if (weaponType == 22 && styleIndex == 5)
		{
			return List.of(CombatIcon.MAGIC, CombatIcon.DEFENCE);
		}
		return List.of(CombatIcon.ATTACK);
	}
}
