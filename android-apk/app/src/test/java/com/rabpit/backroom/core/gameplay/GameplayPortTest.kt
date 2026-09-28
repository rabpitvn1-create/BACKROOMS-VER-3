package com.rabpit.backroom.core.gameplay

import com.rabpit.backroom.core.GameState
import com.rabpit.backroom.core.GameStateCodec
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
  }
}
