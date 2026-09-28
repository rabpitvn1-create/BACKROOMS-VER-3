package com.rabpit.backroom.core.gameplay

import com.rabpit.backroom.core.GameState
import com.rabpit.backroom.core.GameStateCodec
import com.rabpit.backroom.core.ExplorationOutcome
import com.rabpit.backroom.core.ExplorationRuntime
import com.rabpit.backroom.core.ItemCommand
import com.rabpit.backroom.core.CharacterState
import com.rabpit.backroom.core.CommandSource
import com.rabpit.backroom.core.PartyCommand
import com.rabpit.backroom.core.PartyEngine
import com.rabpit.backroom.core.StateReducer
import com.rabpit.backroom.core.StatUpgradeCommand
import com.rabpit.backroom.core.KAI_ID
import org.junit.Assert.*
import org.junit.Test

class GameplayPortTest {
  @Test fun v2GameplayContractsSurviveKotlinPort() {
    assertEquals(1, CharacterProgressionRules.upgradeCost(5))
    assertEquals(2, CharacterProgressionRules.upgradeCost(7))
    assertEquals(150, CharacterProgressionRules.scaledCoreReward(100, 1))
    assertEquals(225, CharacterProgressionRules.scaledCoreReward(100, 2))

    val state = GameState.initial().copy(coreResource = CoreResourceState(quantity = 1))
    val upgraded = CharacterProgressionRules.upgrade(state, KAI_ID, "VIT").state
    assertEquals(6, upgraded.characters.getValue(KAI_ID).progression.stats.vit)
    assertEquals(55, CharacterStatRules.project(upgraded, KAI_ID)!!.maxHp)

    assertEquals(8, GameplayCatalog.chestPool.size)
    assertEquals(35, GameplayCatalog.item("first-aid-kit")!!.effect.hp)
    assertEquals(150, GameplayCatalog.entity("hound")!!.baseMaxHp)
    assertEquals(3, GameplayCatalog.entity("hound")!!.skills.size)
    assertEquals(5, GameplayCatalog.procSkills("cao_minh").size)
    assertEquals(0, GameplayCatalog.procSkills("kai").size)

    assertEquals(PokerDiceRules.Hand.SSF, PokerDiceRules.classify(1,2,3,4,5))
    assertEquals(PokerDiceRules.Hand.FSF, PokerDiceRules.classify(6,6,6,6,6))
    assertEquals(33, PokerDiceRules.basicDamage(30, 6, 100))

    var route = LevelRuntimeState()
    repeat(6) { turn -> route = LevelRouteRules.roll(route, turn + 1, "origin", 1) }
    assertTrue(route.route.exitAvailable)

    val decoded = GameStateCodec.decode(GameStateCodec.encode(upgraded.copy(levelRuntime = route)))
    assertEquals(upgraded.coreResource, decoded.coreResource)
    assertEquals(6, decoded.characters.getValue(KAI_ID).progression.stats.vit)
    assertTrue(decoded.levelRuntime.route.exitAvailable)

    val graph = LevelGraph.fromText("""{"schemaVersion":1,"nodes":[
      {"key":"0","parentLevel":0,"stageIndex":0,"displayName":"L0","defaultLocation":"L0"},
      {"key":"0.1","parentLevel":0,"stageIndex":1,"displayName":"L01","defaultLocation":"L01"}
    ],"edges":[{"from":"0","to":"0.1"}]}""")
    assertTrue(graph.allows("0", "0.1"))
    assertFalse(graph.allows("0.1", "0"))
  @Test fun reducerOwnsUpgradeSharedConsumableAndJoinSurvivalBaseline() {
    val iris = CharacterState("iris", "Iris", progression = CharacterProgressionState(currentHp = 10))
    var state = GameState.initial().copy(
      characters = GameState.initial().characters + ("iris" to iris),
      coreResource = CoreResourceState(quantity = 1)
    )

    val joined = PartyEngine.execute(state, PartyCommand(
      commandId = "join-iris",
      turnId = state.turn.currentTurnId,
      actorId = KAI_ID,
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
      actorId = KAI_ID,
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
      actorId = KAI_ID,
      targetId = "iris",
      source = CommandSource.UI,
      operation = ItemCommand.Operation.USE,
      itemId = "first-aid-kit",
      itemName = "Túi Sơ Cứu"
    ))
    assertTrue(shared.applied)
    assertEquals(45, shared.state.characters.getValue("iris").progression.currentHp)
    assertFalse(shared.state.inventories.getValue(KAI_ID).items.containsKey("first-aid-kit"))

    val upgraded = StateReducer.execute(shared.state, StatUpgradeCommand(
      commandId = "upgrade-vit",
      turnId = shared.state.turn.currentTurnId,
      actorId = KAI_ID,
      source = CommandSource.UI,
      stat = "VIT"
    ))
    assertTrue(upgraded.applied)
    assertEquals(6, upgraded.state.characters.getValue(KAI_ID).progression.stats.vit)
    assertEquals(0, upgraded.state.coreResource.quantity)
  }

  }
  @Test fun equipmentUsesOwnedItemReferencesAndPreservesMissingHp() {
    var state = GameState.initial()
    val armor = ItemCommand(
      commandId = "grant-armor",
      turnId = state.turn.currentTurnId,
      actorId = KAI_ID,
      source = CommandSource.SYSTEM,
      operation = ItemCommand.Operation.PICKUP,
      itemId = "field-armor",
      itemName = "Field Armor",
      metadata = mapOf(
        "equipmentSlot" to "armor",
        "bonus.VIT" to "2",
        "bonus.HP" to "20"
      )
    )
    state = StateReducer.execute(state, armor).state
    val equip = StateReducer.execute(state, ItemCommand(
      commandId = "equip-armor",
      turnId = state.turn.currentTurnId,
      actorId = KAI_ID,
      source = CommandSource.UI,
      operation = ItemCommand.Operation.EQUIP,
      itemId = "field-armor",
      itemName = "Field Armor",
      slot = "armor"
    ))
    assertTrue(equip.applied)
    val equippedStats = CharacterStatRules.project(equip.state, KAI_ID)!!
    assertEquals(80, equippedStats.maxHp)
    assertEquals(80, equippedStats.currentHp)
    assertEquals(2, equippedStats.stats.getValue(CharacterStat.VIT).equipmentBonus)
    assertTrue(equip.state.inventories.getValue(KAI_ID).items.containsKey("field-armor"))

    val dropEquipped = StateReducer.execute(equip.state, ItemCommand(
      commandId = "drop-equipped",
      turnId = equip.state.turn.currentTurnId,
      actorId = KAI_ID,
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
      actorId = KAI_ID,
      source = CommandSource.UI,
      operation = ItemCommand.Operation.UNEQUIP,
      itemId = "field-armor",
      itemName = "Field Armor",
      slot = "armor"
    ))
    assertTrue(unequip.applied)
    val plainStats = CharacterStatRules.project(unequip.state, KAI_ID)!!
    assertEquals(50, plainStats.maxHp)
    assertEquals(50, plainStats.currentHp)
  }
  @Test fun scopedRngAndExplorationAreDeterministicWithoutCanon() {
    val a = GameplayRng("TURN_9", 3)
    val b = GameplayRng("TURN_9", 3)
    assertEquals(a.nextInt(GameplayRng.Scope.ROUTE, 100), b.nextInt(GameplayRng.Scope.ROUTE, 100))
    assertEquals(a.nextInt(GameplayRng.Scope.SITUATION_SELECTION, 10_000), b.nextInt(GameplayRng.Scope.SITUATION_SELECTION, 10_000))

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
    val opened = ExplorationRuntime.openChest(state)
    assertEquals(ExplorationOutcome.CHEST, opened.outcome)
    assertEquals(1, opened.coreReward)
    assertFalse(ExplorationRuntime.chestPresent(opened.state))
    assertEquals(1, opened.state.inventories.getValue(KAI_ID).items.values.sumOf { it.quantity })
    assertEquals(1, opened.state.coreResource.quantity)
  }


}
