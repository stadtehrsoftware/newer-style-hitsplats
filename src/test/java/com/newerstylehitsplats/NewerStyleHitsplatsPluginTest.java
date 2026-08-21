package com.newerstylehitsplats;

import static org.junit.Assert.assertEquals;

import java.util.List;
import net.runelite.api.HitsplatID;
import net.runelite.api.MenuAction;
import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

public class NewerStyleHitsplatsPluginTest
{
	@Test
	public void resolvesCombatStyleIcons()
	{
		assertEquals(List.of(CombatIcon.ATTACK),
			CombatStyleIconResolver.iconsForStyleName("Accurate"));
		assertEquals(List.of(CombatIcon.STRENGTH),
			CombatStyleIconResolver.iconsForStyleName("Aggressive"));
		assertEquals(List.of(CombatIcon.DEFENCE),
			CombatStyleIconResolver.iconsForStyleName("Defensive"));
		assertEquals(List.of(CombatIcon.RANGED),
			CombatStyleIconResolver.iconsForStyleName("Ranging"));
		assertEquals(List.of(CombatIcon.MAGIC),
			CombatStyleIconResolver.iconsForStyleName("Casting"));
		assertEquals(List.of(CombatIcon.ATTACK, CombatIcon.STRENGTH, CombatIcon.DEFENCE),
			CombatStyleIconResolver.iconsForStyleName("Controlled"));
		assertEquals(List.of(CombatIcon.RANGED, CombatIcon.DEFENCE),
			CombatStyleIconResolver.iconsForStyleName("Longrange"));
		assertEquals(List.of(CombatIcon.MAGIC, CombatIcon.DEFENCE),
			CombatStyleIconResolver.iconsForStyleName("Defensive Casting"));
		assertEquals(List.of(CombatIcon.MAGIC, CombatIcon.DEFENCE),
			CombatStyleIconResolver.iconsForStyleName("Defensive", 5));
	}

	@Test
	public void resolvesSpecialWeaponFallbackStyles()
	{
		assertEquals(List.of(CombatIcon.ATTACK),
			CombatStyleIconResolver.fallbackForSpecialWeapon(22, 0));
		assertEquals(List.of(CombatIcon.STRENGTH),
			CombatStyleIconResolver.fallbackForSpecialWeapon(22, 1));
		assertEquals(List.of(CombatIcon.DEFENCE),
			CombatStyleIconResolver.fallbackForSpecialWeapon(22, 3));
		assertEquals(List.of(CombatIcon.MAGIC),
			CombatStyleIconResolver.fallbackForSpecialWeapon(22, 4));
		assertEquals(List.of(CombatIcon.MAGIC, CombatIcon.DEFENCE),
			CombatStyleIconResolver.fallbackForSpecialWeapon(22, 5));
		assertEquals(List.of(CombatIcon.STRENGTH),
			CombatStyleIconResolver.fallbackForSpecialWeapon(30, 2));
	}

	@Test
	public void recognizesDirectSpellbookTargets()
	{
		assertEquals(true, NewerStyleHitsplatsPlugin.isManualSpellTargetAction(
			MenuAction.WIDGET_TARGET_ON_NPC, InterfaceID.MAGIC_SPELLBOOK));
		assertEquals(true, NewerStyleHitsplatsPlugin.isManualSpellTargetAction(
			MenuAction.WIDGET_TARGET_ON_PLAYER, InterfaceID.MAGIC_SPELLBOOK));
		assertEquals(false, NewerStyleHitsplatsPlugin.isManualSpellTargetAction(
			MenuAction.NPC_SECOND_OPTION, InterfaceID.MAGIC_SPELLBOOK));
		assertEquals(false, NewerStyleHitsplatsPlugin.isManualSpellTargetAction(
			MenuAction.WIDGET_TARGET_ON_NPC, InterfaceID.INVENTORY));
	}

	@Test
	public void resolvesSpecialHitsplatIcons()
	{
		assertEquals(List.of(CombatIcon.ANTIPOISON),
			NewerStyleHitsplatsPlugin.specialIconsFor(HitsplatID.POISON));
		assertEquals(List.of(CombatIcon.ANTIVENOM),
			NewerStyleHitsplatsPlugin.specialIconsFor(HitsplatID.VENOM));
		assertEquals(List.of(CombatIcon.PRAYER),
			NewerStyleHitsplatsPlugin.specialIconsFor(HitsplatID.PRAYER_DRAIN));
		assertEquals(List.of(CombatIcon.MAGIC),
			NewerStyleHitsplatsPlugin.specialIconsFor(HitsplatID.BURN));
	}

	@Test
	public void preservesSpecialHitsplatColors()
	{
		assertEquals(HitsplatPalette.MISS, HitsplatPalette.forType(HitsplatID.BLOCK_ME));
		assertEquals(HitsplatPalette.POISON, HitsplatPalette.forType(HitsplatID.POISON));
		assertEquals(HitsplatPalette.VENOM, HitsplatPalette.forType(HitsplatID.VENOM));
		assertEquals(HitsplatPalette.HEAL, HitsplatPalette.forType(HitsplatID.HEAL));
		assertEquals(HitsplatPalette.DAMAGE, HitsplatPalette.forType(HitsplatID.DAMAGE_ME));
	}

	@Test
	public void smoothlyFadesNearTheEndOfTheHitsplatLifetime()
	{
		assertEquals(0.95f,
			NewerStyleHitsplatsOverlay.calculateOpacity(100, 200, 130, 95, true), 0.001f);
		float fadingOpacity = NewerStyleHitsplatsOverlay.calculateOpacity(100, 200, 180, 95, true);
		assertEquals(true, fadingOpacity > 0f && fadingOpacity < 0.95f);
		assertEquals(0f,
			NewerStyleHitsplatsOverlay.calculateOpacity(100, 200, 200, 95, true), 0.001f);
	}
}
