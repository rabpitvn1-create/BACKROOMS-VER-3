package com.rabpit.backroom.core.gameplay

import com.rabpit.backroom.core.CharacterState
import com.rabpit.backroom.core.CommandSource
import com.rabpit.backroom.core.ExplorationOutcome
import com.rabpit.backroom.core.ExplorationRuntime
import com.rabpit.backroom.core.GameState
import com.rabpit.backroom.core.GameStateCodec
import com.rabpit.backroom.core.ItemCommand
import com.rabpit.backroom.core.PLAYER_ID
import com.rabpit.backroom.core.PartyCommand
import com.rabpit.backroom.core.PartyEngine
import com.rabpit.backroom.core.StateReducer
import com.rabpit.backroom.core.StatUpgradeCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayPortTest {
  @Test fun v2GameplayContractsSurviveKotlinPort() {
    assertEquals(1, CharacterProgressionRules.upgradeCost(5))
    assertEquals(2, CharacterProgressionRules.upgradeCost(7))
    assertEquals(150, CharacterProgressionRules.scaledCoreReward(100, 1))
    assertEquals(225, CharacterProgressionRules.scaledCoreReward(100, 2))

    val initialCaoMinh = GameState.initial()
    assertEquals(7, initialCaoMinh.saveVersion)
    assertEquals("Cao Minh", initialCaoMinh.characters.getValue(PLAYER_ID).name)
    assertEquals("file:///android_asset/avatars/cao_minh_avatar.jpg", initialCaoMinh.characters.getValue(PLAYER_ID).avatarRef)
    assertEquals(
      setOf("Huyết Ma Kiếm", "Huyết Ma Chiến Khải", "Vạn Tàng Giới"),
      initialCaoMinh.inventories.getValue(PLAYER_ID).items.values.map { it.name }.toSet()
    )
    assertEquals(
      listOf("Huyết Ma Tứ Liên", "Ma Tâm Trấn Hồn", "Huyết Ảnh Ma Độn"),
      GameplayCatalog.activeSkills("cao_minh").map { it.name }
    )
    assertEquals("Huyết Ma Nhị Thập Tứ Trảm", GameplayCatalog.ultimate("cao_minh")!!.name)

    val state = GameState.initial().copy(coreResource = CoreResourceState(quantity = 1))
    val upgraded = CharacterProgressionRules.upgrade(state, PLAYER_ID, "VIT").state
    assertEquals(6, upgraded.characters.getValue(PLAYER_ID).progression.stats.vit)
    assertEquals(550, CharacterStatRules.project(upgraded, PLAYER_ID)!!.maxHp)
    assertEquals(55, CharacterStatRules.project(upgraded, PLAYER_ID)!!.currentHp)

    assertEquals(5, GameplayCatalog.CHEST_SPAWN_RATE_PERCENT)
    assertEquals(100, GameplayCatalog.ENTITY_DROP_RATE_PERCENT)
    assertEquals(8, GameplayCatalog.chestPool.size)
    assertEquals(
      listOf("almond-water", "bandage", "first-aid-kit", "lavie-water", "coconut-water", "banh-mi-thit", "hot-soy-milk", "com-tam-suon-bi-cha"),
      GameplayCatalog.chestPool.map { it.id }
    )
    assertEquals(listOf("almond-water", "bandage"), GameplayCatalog.entityDropPool.map { it.id })
    assertEquals(35, GameplayCatalog.item("first-aid-kit")!!.effect.hp)
    assertEquals(150, GameplayCatalog.entity("hound")!!.baseMaxHp)
    assertEquals(3, GameplayCatalog.entity("hound")!!.skills.size)
    assertEquals(5, GameplayCatalog.procSkills("cao_minh").size)
    assertEquals(0, GameplayCatalog.procSkills("kai").size)

    assertEquals(PokerDiceRules.Hand.SSF, PokerDiceRules.classify(1, 2, 3, 4, 5))
    assertEquals(PokerDiceRules.Hand.FSF, PokerDiceRules.classify(6, 6, 6, 6, 6))
    assertEquals(33, PokerDiceRules.basicDamage(30, 6, 100))

    var route = LevelRuntimeState()
    repeat(6) { turn -> route = LevelRouteRules.roll(route, turn + 1, "origin", 1) }
    assertTrue(route.route.exitAvailable)

    val decoded = GameStateCodec.decode(GameStateCodec.encode(upgraded.copy(levelRuntime = route)))
    assertEquals(upgraded.coreResource, decoded.coreResource)
    assertEquals(6, decoded.characters.getValue(PLAYER_ID).progression.stats.vit)
    assertTrue(decoded.levelRuntime.route.exitAvailable)

    val graph = LevelGraph.fromText("""{"schemaVersion":1,"nodes":[
      {"key":"0","parentLevel":0,"stageIndex":0,"displayName":"L0","defaultLocation":"L0"},
      {"key":"0.1","parentLevel":0,"stageIndex":1,"displayName":"L01","defaultLocation":"L01"}
    ],"edges":[{"from":"0","to":"0.1"}]}""")
    assertTrue(graph.allows("0", "0.1"))
    assertFalse(graph.allows("0.1", "0"))
  }

  @Test fun v2LootAuthorityRejectsAiAndEntityVictoryDropsExactlyOneCatalogItem() {
    val denied = StateReducer.execute(GameState.initial(), ItemCommand(
      commandId = "gemini-loot",
      turnId = "TURN_1",
      actorId = PLAYER_ID,
      source = CommandSource.GEMINI,
      operation = ItemCommand.Operation.PICKUP,
      itemId = "almond-water",
      itemName = "Almond Water"
    ))
    assertFalse(denied.applied)
    assertEquals("player_pickup_unavailable", denied.validation.reason)

    val before = GameState.initial()
    val beforeQuantity = before.inventories.getValue(PLAYER_ID).items.values.sumOf { it.quantity }
    val rewarded = ExplorationRuntime.entityVictoryRewards(before, "hound")
    val afterQuantity = rewarded.inventories.getValue(PLAYER_ID).items.values.sumOf { it.quantity }
    assertEquals(beforeQuantity + 1, afterQuantity)
    assertTrue(rewarded.inventories.getValue(PLAYER_ID).items.keys.any {
      it == "almond-water" || it == "bandage"
    })
    assertEquals(2, rewarded.coreResource.quantity)
  }

  @Test fun reducerOwnsUpgradeSharedConsumableAndJoinSurvivalBaseline() {
    val iris = CharacterState("iris", "Iris", progression = CharacterProgressionState(currentHp = 10))
    var state = GameState.initial().copy(
      characters = GameState.initial().characters + ("iris" to iris),
      coreResource = CoreResourceState(quantity = 1)
    )

    val joined = PartyEngine.execute(state, PartyCommand(
      commandId = "join-iris",
      turnId = state.turn.currentTurnId,
      actorId = PLAYER_ID,
      targetId = "iris",
      source = CommandSource.SYSTEM,
      operation = PartyCommand.Operation.ADD,
      consentConfirmed = true,
      targetPresent = true
    ))
    assertTrue(joined.applied)
    state = joined.state
    assertEquals(0L, state.characters.getValue("iris").physiology.minutesSinceFood)
    assertEquals(0L, state.characters.getValue("iris").physiology.minutesSinceWater)

    val granted = StateReducer.execute(state, ItemCommand(
      commandId = "grant-kit",
      turnId = state.turn.currentTurnId,
      actorId = PLAYER_ID,
      source = CommandSource.SYSTEM,
      operation = ItemCommand.Operation.PICKUP,
      itemId = "first-aid-kit",
      itemName = "Túi Sơ Cứu"
    ))
    assertTrue(granted.applied)
    state = granted.state

    val shared = StateReducer.execute(state, ItemCommand(
      commandId = "share-kit",
      turnId = state.turn.currentTurnId,
      actorId = PLAYER_ID,
      targetId = "iris",
      source = CommandSource.UI,
      operation = ItemCommand.Operation.USE,
      itemId = "first-aid-kit",
      itemName = "Túi Sơ Cứu"
    ))
    assertTrue(shared.applied)
    assertEquals(45, shared.state.characters.getValue("iris").progression.currentHp)
    assertFalse(shared.state.inventories.getValue(PLAYER_ID).items.containsKey("first-aid-kit"))

    val upgraded = StateReducer.execute(shared.state, StatUpgradeCommand(
      commandId = "upgrade-vit",
      turnId = shared.state.turn.currentTurnId,
      actorId = PLAYER_ID,
      source = CommandSource.UI,
      stat = "VIT"
    ))
    assertTrue(upgraded.applied)
    assertEquals(6, upgraded.state.characters.getValue(PLAYER_ID).progression.stats.vit)
    assertEquals(0, upgraded.state.coreResource.quantity)
  }

  @Test fun equipmentUsesOwnedItemReferencesAndPreservesMissingHp() {
    var state = GameState.initial()
    state = StateReducer.execute(state, ItemCommand(
      commandId = "grant-armor",
      turnId = state.turn.currentTurnId,
      actorId = PLAYER_ID,
      source = CommandSource.SYSTEM,
      operation = ItemCommand.Operation.PICKUP,
      itemId = "field-armor",
      itemName = "Field Armor",
      metadata = mapOf(
        "equipmentSlot" to "armor",
        "bonus.VIT" to "2",
        "bonus.HP" to "20"
      )
    )).state

    val equip = StateReducer.execute(state, ItemCommand(
      commandId = "equip-armor",
      turnId = state.turn.currentTurnId,
      actorId = PLAYER_ID,
      source = CommandSource.UI,
      operation = ItemCommand.Operation.EQUIP,
      itemId = "field-armor",
      itemName = "Field Armor",
      slot = "armor"
    ))
    assertTrue(equip.applied)
    val equippedStats = CharacterStatRules.project(equip.state, PLAYER_ID)!!
    assertEquals(575, equippedStats.maxHp)
    assertEquals(80, equippedStats.currentHp)
    assertEquals(2, equippedStats.stats.getValue(CharacterStat.VIT).equipmentBonus)
    assertTrue(equip.state.inventories.getValue(PLAYER_ID).items.containsKey("field-armor"))

    val dropEquipped = StateReducer.execute(equip.state, ItemCommand(
      commandId = "drop-equipped",
      turnId = equip.state.turn.currentTurnId,
      actorId = PLAYER_ID,
      source = CommandSource.UI,
      operation = ItemCommand.Operation.DROP,
      itemId = "field-armor",
      itemName = "Field Armor"
    ))
    assertFalse(dropEquipped.applied)
    assertEquals("item_equipped", dropEquipped.validation.reason)

    val unequip = StateReducer.execute(equip.state, ItemCommand(
      commandId = "unequip-armor",
      turnId = equip.state.turn.currentTurnId,
      actorId = PLAYER_ID,
      source = CommandSource.UI,
      operation = ItemCommand.Operation.UNEQUIP,
      itemId = "field-armor",
      itemName = "Field Armor",
      slot = "armor"
    ))
    assertTrue(unequip.applied)
    val plainStats = CharacterStatRules.project(unequip.state, PLAYER_ID)!!
    assertEquals(545, plainStats.maxHp)
    assertEquals(50, plainStats.currentHp)
  }

  @Test fun scopedRngAndExplorationAreDeterministicWithoutCanon() {
    val a = GameplayRng("TURN_9", 3)
    val b = GameplayRng("TURN_9", 3)
    assertEquals(a.nextInt(GameplayRng.Scope.ROUTE, 100), b.nextInt(GameplayRng.Scope.ROUTE, 100))
    assertEquals(
      a.nextInt(GameplayRng.Scope.SITUATION_SELECTION, 10_000),
      b.nextInt(GameplayRng.Scope.SITUATION_SELECTION, 10_000)
    )

    val graph = LevelGraph.fromText("""{"schemaVersion":1,"nodes":[
      {"key":"0","parentLevel":0,"stageIndex":0},
      {"key":"0.1","parentLevel":0,"stageIndex":1}
    ],"edges":[{"from":"0","to":"0.1"}]}""")
    val base = GameState.initial()
    assertEquals(
      ExplorationRuntime.resolve(base, "đi tiếp", graph),
      ExplorationRuntime.resolve(base, "đi tiếp", graph)
    )

    val unlocked = base.copy(levelRuntime = base.levelRuntime.copy(
      route = base.levelRuntime.route.copy(streak = 6, exitAvailable = true)
    ))
    val moved = ExplorationRuntime.resolve(unlocked, "đi qua", graph)
    assertEquals(ExplorationOutcome.LEVEL_TRANSITION, moved.outcome)
    assertEquals("0.1", moved.state.levelRuntime.key)
    assertEquals(1, moved.state.coreResource.quantity)
  }

  @Test fun chestOpeningGrantsCatalogItemAndCoreThenClearsChest() {
    val state = GameState.initial().copy(metadata = GameState.initial().metadata + ("loot.chestPresent" to "true"))
    val initialItems = state.inventories.getValue(PLAYER_ID).items.values.sumOf { it.quantity }
    val opened = ExplorationRuntime.openChest(state)
    assertEquals(ExplorationOutcome.CHEST, opened.outcome)
    assertEquals(1, opened.coreReward)
    assertFalse(ExplorationRuntime.chestPresent(opened.state))
    assertEquals(initialItems + 1, opened.state.inventories.getValue(PLAYER_ID).items.values.sumOf { it.quantity })
    assertEquals(1, opened.state.coreResource.quantity)
  }
}
