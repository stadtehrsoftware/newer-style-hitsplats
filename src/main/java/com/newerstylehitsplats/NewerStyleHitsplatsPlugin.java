package com.newerstylehitsplats;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;

@PluginDescriptor(
	name = "Newer Style HitSplats",
	description = "Displays compact 2010-era rectangular hitsplats with combat skill icons",
	tags = {"hitsplat", "newer", "2010", "combat", "cosmetic"}
)
public class NewerStyleHitsplatsPlugin extends Plugin
{
	private static final int MANUAL_SPELL_TIMEOUT_TICKS = 8;

	private static final String[] CONFIG_KEYS =
	{
		"showMyHits",
		"showIncomingHits",
		"showOtherHits",
		"scale",
		"fadeOut",
		"opacity",
		"multiplyByTen"
	};

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NewerStyleHitsplatsOverlay overlay;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private NewerStyleHitsplatsConfig config;

	@Inject
	private ConfigManager configManager;

	private final HitsplatTracker tracker = new HitsplatTracker();
	private final Map<Actor, Integer> manualSpellTargets = new IdentityHashMap<>();

	private final Map<CombatIcon, BufferedImage> icons = new ConcurrentHashMap<>();

	private volatile boolean running;

	@Provides
	NewerStyleHitsplatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NewerStyleHitsplatsConfig.class);
	}

	@Override
	protected void startUp()
	{
		migrateLegacyConfig();
		running = true;
		overlayManager.add(overlay);
		loadIcons();
	}

	private void migrateLegacyConfig()
	{
		for (String key : CONFIG_KEYS)
		{
			if (configManager.getConfiguration(NewerStyleHitsplatsConfig.GROUP, key) != null)
			{
				continue;
			}

			String legacyValue = configManager.getConfiguration(NewerStyleHitsplatsConfig.LEGACY_GROUP, key);
			if (legacyValue != null)
			{
				configManager.setConfiguration(NewerStyleHitsplatsConfig.GROUP, key, legacyValue);
			}
		}
	}

	@Override
	protected void shutDown()
	{
		running = false;
		overlayManager.remove(overlay);
		tracker.clear();
		manualSpellTargets.clear();
		icons.clear();
		overlay.clearImageCache();
	}

	HitsplatTracker getTracker()
	{
		return tracker;
	}

	Map<CombatIcon, BufferedImage> getIcons()
	{
		return icons;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Actor actor = event.getActor();
		Hitsplat hitsplat = event.getHitsplat();
		if (actor == null || hitsplat == null || !shouldTrack(actor, hitsplat))
		{
			return;
		}

		List<CombatIcon> icons = iconsFor(actor, hitsplat);
		tracker.add(
			actor,
			hitsplat.getAmount(),
			hitsplat.getHitsplatType(),
			hitsplat.getDisappearsOnGameCycle(),
			client.getGameCycle(),
			icons);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		Widget selectedWidget = client.getSelectedWidget();
		int selectedInterface = selectedWidget == null
			? -1
			: WidgetUtil.componentToInterface(selectedWidget.getId());
		if (!isManualSpellTargetAction(event.getMenuAction(), selectedInterface))
		{
			return;
		}

		Actor target = event.getMenuEntry().getActor();
		if (target != null)
		{
			removeExpiredManualSpellTargets();
			manualSpellTargets.put(target, client.getTickCount() + MANUAL_SPELL_TIMEOUT_TICKS);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			tracker.clear();
			manualSpellTargets.clear();
		}
		if (event.getGameState().ordinal() >= GameState.LOGIN_SCREEN.ordinal())
		{
			loadIcons();
		}
	}

	private boolean shouldTrack(Actor actor, Hitsplat hitsplat)
	{
		Player localPlayer = client.getLocalPlayer();
		if (hitsplat.isMine())
		{
			return config.showMyHits();
		}
		if (actor == localPlayer)
		{
			return config.showIncomingHits();
		}
		return config.showOtherHits();
	}

	private List<CombatIcon> iconsFor(Actor actor, Hitsplat hitsplat)
	{
		int type = hitsplat.getHitsplatType();
		List<CombatIcon> specialIcons = specialIconsFor(type);
		if (!specialIcons.isEmpty())
		{
			return specialIcons;
		}
		if (hitsplat.isMine())
		{
			if (consumeManualSpellTarget(actor))
			{
				return List.of(CombatIcon.MAGIC);
			}
			return CombatStyleIconResolver.resolve(client);
		}
		if (actor == client.getLocalPlayer())
		{
			return List.of(CombatIcon.DEFENCE);
		}
		return List.of(CombatIcon.ATTACK);
	}

	static boolean isManualSpellTargetAction(MenuAction action, int selectedInterface)
	{
		return selectedInterface == InterfaceID.MAGIC_SPELLBOOK
			&& (action == MenuAction.WIDGET_TARGET_ON_NPC
				|| action == MenuAction.WIDGET_TARGET_ON_PLAYER);
	}

	private boolean consumeManualSpellTarget(Actor actor)
	{
		removeExpiredManualSpellTargets();
		return manualSpellTargets.remove(actor) != null;
	}

	private void removeExpiredManualSpellTargets()
	{
		int currentTick = client.getTickCount();
		manualSpellTargets.entrySet().removeIf(entry -> entry.getValue() < currentTick);
	}

	static List<CombatIcon> specialIconsFor(int hitsplatType)
	{
		switch (hitsplatType)
		{
			case HitsplatID.POISON:
				return List.of(CombatIcon.ANTIPOISON);
			case HitsplatID.VENOM:
				return List.of(CombatIcon.ANTIVENOM);
			case HitsplatID.PRAYER_DRAIN:
			case HitsplatID.CORRUPTION:
				return List.of(CombatIcon.PRAYER);
			case HitsplatID.BURN:
				return List.of(CombatIcon.MAGIC);
			case HitsplatID.BLEED:
				return List.of(CombatIcon.ATTACK);
			case HitsplatID.HEAL:
			case HitsplatID.DISEASE:
			case HitsplatID.DISEASE_BLOCKED:
			case HitsplatID.CYAN_UP:
			case HitsplatID.CYAN_DOWN:
			case HitsplatID.SANITY_DRAIN:
			case HitsplatID.SANITY_RESTORE:
			case HitsplatID.DOOM:
				return List.of(CombatIcon.HITPOINTS);
			default:
				return List.of();
		}
	}

	private void loadIcons()
	{
		for (CombatIcon icon : CombatIcon.values())
		{
			if (icons.containsKey(icon))
			{
				continue;
			}
			if (icon.getSource() == CombatIcon.Source.ITEM)
			{
				AsyncBufferedImage image = itemManager.getImage(icon.getImageId());
				icons.put(icon, image);
				image.onLoaded(() ->
				{
					if (running)
					{
						overlay.clearImageCache();
					}
				});
				continue;
			}

			spriteManager.getSpriteAsync(icon.getImageId(), 0, image ->
			{
				if (running && image != null)
				{
					icons.put(icon, image);
					overlay.clearImageCache();
				}
			});
		}
	}
}
