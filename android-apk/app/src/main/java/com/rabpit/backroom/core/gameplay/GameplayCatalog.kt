package com.rabpit.backroom.core.gameplay

import com.rabpit.backroom.core.ItemStack
import java.util.Locale

data class ItemEffect(val hunger: Int = 0, val thirst: Int = 0, val hp: Int = 0)
data class GameplayItem(val id: String, val name: String, val category: String, val effect: ItemEffect)
data class EntitySkill(val name: String, val damagePercent: Int, val procPercent: Int)
data class EntityDefinition(
  val key: String,
  val name: String,
  val baseMaxHp: Int,
  val baseDamage: Int,
  val autoSpawnRatePercent: Double = 0.0,
  val autoSpawn: Boolean = false,
  val treasure: Boolean = false,
  val levels: Set<Int> = emptySet(),
  val skills: List<EntitySkill> = emptyList()
)
data class ActiveSkill(val name: String, val damagePercent: Int, val effect: String = "", val turns: Int = 0, val value: Int = 0)
data class ProcSkill(val name: String, val procPercent: Int, val bonusDamagePercent: Int, val effect: String = "", val turns: Int = 0, val value: Int = 0)
data class UltimateSkill(val name: String, val hitCount: Int, val bonusPercent: Int)

object GameplayCatalog {
  const val CHEST_SPAWN_RATE_PERCENT = 5
  const val ENTITY_DROP_RATE_PERCENT = 100
  const val CORE_CHEST_DROP_PERCENT = 100

  private val items = listOf(
    GameplayItem("almond-water", "Almond Water", "FOOD_DRINK", ItemEffect(50, 100)),
    GameplayItem("bandage", "Băng Gạc Y Tế", "HEALING", ItemEffect(hp = 15)),
    GameplayItem("first-aid-kit", "Túi Sơ Cứu", "HEALING", ItemEffect(hp = 35)),
    GameplayItem("lavie-water", "Nước Suối Lavie", "DRINK", ItemEffect(thirst = 50)),
    GameplayItem("coconut-water", "Nước Dừa", "FOOD_DRINK", ItemEffect(10, 70)),
    GameplayItem("banh-mi-thit", "Bánh Mì Thịt", "FOOD", ItemEffect(hunger = 45)),
    GameplayItem("hot-soy-milk", "Sữa Đậu Nành Nóng", "FOOD_DRINK", ItemEffect(25, 30)),
    GameplayItem("com-tam-suon-bi-cha", "Cơm Tấm Sườn Bì Chả", "FOOD", ItemEffect(hunger = 80))
  ).associateBy { it.id }

  val chestPool = listOf(
    "almond-water", "bandage", "first-aid-kit", "lavie-water",
    "coconut-water", "banh-mi-thit", "hot-soy-milk", "com-tam-suon-bi-cha"
  ).map(items::getValue)
  val entityDropPool = listOf("almond-water", "bandage").map(items::getValue)

  private fun es(vararg value: Triple<String, Int, Int>) = value.map { EntitySkill(it.first, it.second, it.third) }
  private val entities = listOf(
    EntityDefinition("hound","Hound",150,15,3.5,true,levels=setOf(1,2,4,5),skills=es(Triple("Dead Bite",120,35),Triple("Rending Pounce",115,32),Triple("Pack Maul",110,34))),
    EntityDefinition("clump","Clump",190,17,3.5,true,levels=setOf(1,2),skills=es(Triple("Grasping Crush",120,34),Triple("Limb Barrage",115,35),Triple("Drag Down",110,31))),
    EntityDefinition("duller","Duller",160,14,3.25,true,levels=setOf(1),skills=es(Triple("Blindside Strike",125,33),Triple("Distorted Lunge",115,35),Triple("Column Ambush",120,30))),
    EntityDefinition("deathmoth","Deathmoth",120,13,3.4,true,levels=setOf(1,3,4),skills=es(Triple("Ceiling Dive",110,31),Triple("Wing Strike",115,32),Triple("Dark Recess Ambush",120,23))),
    EntityDefinition("hostile_faceling","Hostile Faceling",130,14,3.2,true,levels=setOf(1),skills=es(Triple("Sudden Grasp",110,35),Triple("Close-Quarters Strike",115,26),Triple("Predatory Lunge",120,27))),
    EntityDefinition("false_puddle","False Puddle",170,16,3.15,true,levels=setOf(1),skills=es(Triple("Toothed Snap",110,31),Triple("Footstep Ambush",115,27),Triple("Puddle Bite",120,25))),
    EntityDefinition("paintings","Paintings",120,12,3.1,true,levels=setOf(1),skills=es(Triple("Canvas Reach",110,33),Triple("Frame Ambush",115,27),Triple("Pulling Grasp",120,21))),
    EntityDefinition("smiler","Smiler",150,18,3.4,true,levels=setOf(2),skills=es(Triple("Darkness Lunge",110,32),Triple("Blindside Strike",115,26),Triple("Shadow Bite",120,23))),
    EntityDefinition("skin-stealer","Skin-Stealer",180,18,3.25,true,levels=setOf(2,3,4,5),skills=es(Triple("Disguised Strike",110,32),Triple("Close-Range Slash",115,29),Triple("Ambush Lunge",120,21))),
    EntityDefinition("predatory_window","Predatory Window",210,17,3.1,true,levels=setOf(2,4,5),skills=es(Triple("Window Reach",110,34),Triple("Threshold Grasp",115,28),Triple("Close-Range Pull",120,25))),
    EntityDefinition("biological_pipeline","Biological Pipeline",220,18,3.0,true,levels=setOf(2),skills=es(Triple("Constriction",110,33),Triple("Pipeline Crush",115,30),Triple("Corrosive Contact",120,20))),
    EntityDefinition("wretch","Wretch",145,16,3.3,true,levels=setOf(3),skills=es(Triple("Ragged Swipe",110,32),Triple("Pain-Fueled Lunge",115,27),Triple("Scent-Tracked Strike",120,22))),
    EntityDefinition("cable_mimic","Cable Mimic",180,17,3.2,true,levels=setOf(3),skills=es(Triple("Cable Lash",110,35),Triple("Bundle Constriction",115,29),Triple("Current-Driven Grip",120,28))),
    EntityDefinition("the_beast_of_level_5","The Beast of Level 5",300,22,3.0,true,levels=setOf(5),skills=es(Triple("Cornered Strike",110,33),Triple("Isolated Prey Ambush",115,32),Triple("Hotel Corridor Lunge",120,21))),
    EntityDefinition("hotel_corpse_lure","Hotel Corpse Lure",190,18,3.0,true,levels=setOf(5),skills=es(Triple("Corpse-Lure Grasp",110,35),Triple("Close-Range Ambush",115,28),Triple("Sudden Strike",120,20))),
    EntityDefinition("jeff_the_killer","Jeff",240,20,3.0,true,levels=(0..6).toSet(),skills=es(Triple("Stalking Slash",110,34),Triple("Close-Range Lunge",115,29),Triple("Tactical Ambush",120,23))),
    EntityDefinition("async_rifleman","ASYNC Rifleman",180,20,3.0,true,levels=(0..6).toSet(),skills=es(Triple("Controlled Burst",110,32),Triple("Cover Fire",115,32),Triple("Crossfire Burst",120,28))),
    EntityDefinition("copx","CopX",260,22,3.0,true,levels=(0..6).toSet(),skills=es(Triple("Static Burst",110,30),Triple("Servo Pivot",115,25),Triple("Last Directive",120,20))),
    EntityDefinition("tam_ma_cao_minh","Tâm Ma Cao Minh",300,30,4.0,true,true,(0..6).toSet(),es(Triple("Tâm Ma Trảm",120,35),Triple("Huyết Ảnh Phản Kích",115,40),Triple("Ma Hổ Phệ",110,45))),
    EntityDefinition("jane_the_killer","Jane",270,20),
    EntityDefinition("slenderman","Slenderman",360,23),
    EntityDefinition("diep_minh","Diệp Minh",1200,42,3.5,true,levels=(0..6).toSet(),skills=es(Triple("Golden Sword Slash",110,32),Triple("Demonic Claw",115,32),Triple("Sword-Claw Assault",120,21)))
  ).associateBy { it.key }

  private val activeSkills = mapOf(
    "cao_minh" to listOf(ActiveSkill("Huyết Ma Tứ Liên",170,"Chảy máu",3,5),ActiveSkill("Ma Tâm Trấn Hồn",130,"Choáng",1),ActiveSkill("Huyết Ảnh Ma Độn",147)),
    "iris" to listOf(ActiveSkill("Twosome Time",155),ActiveSkill("Rain Storm",145),ActiveSkill("Honeycomb Fire",185,"Xuyên giáp",2,20),ActiveSkill("Charged Shot",175)),
    "syvial" to listOf(ActiveSkill("Rift Sever",175),ActiveSkill("Crimson Guillotine",190,"Chảy máu",3,4),ActiveSkill("Lucifer Breaker",155,"Choáng",1),ActiveSkill("Spatial Dominion",210,"Mất phương hướng",2,25)),
    "luc_tram" to listOf(ActiveSkill("Tịch Quang Hợp Kích",150))
  )
  private val procSkills = mapOf(
    "cao_minh" to listOf(ProcSkill("Huyết Sát Kiếm Ấn",50,25,"Chảy máu",2,3),ProcSkill("Phá Giáp Ma Kiếm",48,20,"Xuyên giáp",2,10),ProcSkill("Ma Tâm Chấn",45,15,"Choáng",1),ProcSkill("Huyết Độc Ma Khí",47,20,"Trúng độc",2,3),ProcSkill("Huyết Liệt Ma Ấn",51,20,"Chảy máu",2,4)),
    "luc_tram" to listOf(ProcSkill("Tịch Quang Phản Kiếm",52,25,"Trúng độc",2,3),ProcSkill("Nhất Tuyến Phá Vọng",55,20,"Xuyên giáp",2,10),ProcSkill("Thiên Kiếm Chấn",46,15,"Choáng",1),ProcSkill("Bạch Hồng Quán Nhật",49,20,"Chảy máu",2,3),ProcSkill("Vạn Kiếm Quy Tâm",51,20,"Trúng độc",2,4))
  )
  private val ultimates = mapOf(
    "cao_minh" to UltimateSkill("Huyết Ma Nhị Thập Tứ Trảm",24,15),
    "luc_tram" to UltimateSkill("Thiên Kiếm Định Giới",60,15)
  )

  fun item(id: String?) = items[id?.trim()?.lowercase(Locale.ROOT)]
  fun itemFor(stack: ItemStack): GameplayItem? =
    item(stack.archetypeId) ?: item(stack.itemId.substringBefore(':')) ?:
      items.values.firstOrNull { it.name.equals(stack.name, ignoreCase = true) }

  fun decorate(stack: ItemStack): ItemStack {
    val item = itemFor(stack) ?: return stack
    val effects = buildMap {
      if (item.effect.hunger > 0) put("effect.hunger", item.effect.hunger.toString())
      if (item.effect.thirst > 0) put("effect.thirst", item.effect.thirst.toString())
      if (item.effect.hp > 0) put("effect.hp", item.effect.hp.toString())
    }
    return stack.copy(
      name = item.name,
      archetypeId = item.id,
      metadata = stack.metadata + effects + mapOf("category" to item.category, "consumable" to "true")
    )
  }

  fun entity(key: String?) = entities[key?.trim()?.lowercase(Locale.ROOT)]
  fun allEntities(): Collection<EntityDefinition> = entities.values
  fun activeSkills(characterId: String) = activeSkills[characterId.trim().lowercase()].orEmpty()
  fun procSkills(characterId: String) = procSkills[characterId.trim().lowercase()].orEmpty()
  fun ultimate(characterId: String) = ultimates[characterId.trim().lowercase()]
  fun shouldSpawnChest(roll: Int) = roll in 0 until CHEST_SPAWN_RATE_PERCENT
  fun validAutoSpawnRate(rate: Double) = rate in 3.0..3.5
  fun validTreasureAutoSpawnRate(rate: Double) = rate > 0.0 && rate <= 4.0

  fun entityStats(entity: EntityDefinition, stageIndex: Int): Pair<Int, Int> {
    val percent = 100 + 10 * maxOf(0, stageIndex)
    fun scale(value: Int) = ((maxOf(1, value).toLong() * percent + 50L) / 100L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return scale(entity.baseMaxHp) to scale(entity.baseDamage)
  }
}

object PokerDiceRules {
  const val MAX_REROLLS = 3
  const val DICE_COUNT = 5
  enum class Hand(val v2Name: String, val token: String) {
    NO_HAND("NO HAND", "[NO HAND]"),
    ONE_PAIR("ONE PAIR", "[PAIR]"),
    TWO_PAIR("TWO PAIR", "[TWO PAIR]"),
    THREE("THREE OF A KIND", "[TRIPLE]"),
    STRAIGHT("STRAIGHT", "[STRAIGHT]"),
    FULL_HOUSE("FULL HOUSE", "[FULL HOUSE]"),
    FOUR("FOUR OF A KIND", "[F.O.A.K]"),
    SSF("SSF", "[SSF]"),
    FSF("FSF", "[FSF]")
  }

  fun fromV2Name(raw: String?): Hand =
    Hand.entries.firstOrNull { it.v2Name == raw } ?: Hand.NO_HAND

  fun classify(vararg dice: Int): Hand {
    if (dice.size != DICE_COUNT || dice.any { it !in 1..6 }) return Hand.NO_HAND
    if (dice.distinct().size == 1) return Hand.FSF
    if (dice.contentEquals(intArrayOf(1,2,3,4,5)) || dice.contentEquals(intArrayOf(5,4,3,2,1))) return Hand.SSF
    if (dice.contentEquals(intArrayOf(2,3,4,5,6)) || dice.contentEquals(intArrayOf(6,5,4,3,2))) return Hand.STRAIGHT
    val counts = IntArray(7).also { c -> dice.forEach { c[it]++ } }
    val pairs = counts.count { it == 2 }
    return when {
      counts.any { it == 4 } -> Hand.FOUR
      counts.any { it == 3 } && pairs == 1 -> Hand.FULL_HOUSE
      counts.any { it == 3 } -> Hand.THREE
      pairs >= 2 -> Hand.TWO_PAIR
      pairs == 1 -> Hand.ONE_PAIR
      else -> Hand.NO_HAND
    }
  }

  fun basicDamage(base: Int, str: Int, handPercent: Int) =
    scale(base, CharacterProgressionRules.statPercent(str), handPercent)

  fun skillDamage(base: Int, skillPercent: Int, skl: Int, handPercent: Int): Int {
    val skillBase = ((maxOf(1, base).toLong() * maxOf(0, skillPercent) + 50L) / 100L)
      .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return scale(skillBase, CharacterProgressionRules.statPercent(skl), handPercent)
  }

  fun ultimateDamage(currentDamage: Int, hitCount: Int, bonusPercent: Int, handPercent: Int): Int {
    val perHit = ((maxOf(1, currentDamage).toLong() * (100L + maxOf(0, bonusPercent)) + 50L) / 100L)
    return ((perHit * maxOf(1, hitCount) * maxOf(0, handPercent) + 50L) / 100L)
      .coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
  }

  fun defendedIncomingDamage(rawDamage: Int, def: Int): Int {
    val percent = CharacterProgressionRules.statPercent(def)
    return ((maxOf(1, rawDamage).toLong() * 100L + percent / 2L) / percent)
      .coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
  }

  fun criticalDamage(damage: Int) =
    ((maxOf(1, damage).toLong() * 150L + 50L) / 100L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

  fun effectiveChance(chance: Int, resistance: Int) = (maxOf(0, chance) - maxOf(0, resistance)).coerceIn(0, 100)

  fun basicHandPercent(hand: Hand) = if (hand == Hand.ONE_PAIR) 125 else 100

  fun skillHandPercent(hand: Hand) = when (hand) {
    Hand.STRAIGHT -> 150
    Hand.FULL_HOUSE -> 200
    Hand.FOUR -> 250
    else -> 100
  }

  fun ultimateHandPercent(hand: Hand) = if (hand == Hand.FSF) 200 else 100

  fun deterministicDie(seed: Int, sequence: Int, slot: Int): Int {
    var mixed = (seed.toLong() and 0xffffffffL) * 1_103_515_245L +
      (sequence + 1L) * 12_345L +
      (slot + 1L) * 2_654_435_761L
    mixed = mixed xor (mixed ushr 17)
    mixed = mixed xor (mixed shl 13)
    return 1 + Math.floorMod(mixed, 6L).toInt()
  }

  fun characterProcRoll(seed: Int, sequence: Int, actorId: String, procName: String): Int {
    val mixed = (seed.toLong() and 0xffffffffL) * 31L +
      (sequence + 1L) * 131L +
      (actorId + ":" + procName).hashCode().toLong() * 17L
    return Math.floorMod(mixed, 100L).toInt()
  }

  fun entitySkillProcRoll(seed: Int, round: Int, actorIndex: Int, skillIndex: Int): Int {
    var mixed = (seed.toLong() and 0xffffffffL) * 1_664_525L +
      maxOf(1, round).toLong() * 1_013_904_223L +
      (maxOf(0, actorIndex) + 1L) * 2_654_435_761L +
      (skillIndex + 1L) * 97_531L
    mixed = mixed xor (mixed ushr 16)
    mixed = mixed xor (mixed shl 11)
    return Math.floorMod(mixed, 100L).toInt()
  }

  fun entitySkillDamage(rawDamage: Int, percent: Int): Int =
    ((maxOf(1, rawDamage).toLong() * maxOf(0, percent).toLong() + 50L) / 100L)
      .coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()

  private fun scale(base: Int, statPercent: Int, handPercent: Int) =
    ((maxOf(1, base).toLong() * maxOf(0, statPercent) * maxOf(0, handPercent) + 5_000L) / 10_000L)
      .coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
}
