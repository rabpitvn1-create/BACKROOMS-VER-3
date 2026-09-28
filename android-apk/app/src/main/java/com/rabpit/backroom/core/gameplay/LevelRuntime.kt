package com.rabpit.backroom.core.gameplay

import android.content.Context
import org.json.JSONObject

data class LevelNode(val key: String, val parentLevel: Int, val stageIndex: Int)
enum class RouteResult { NONE, SUCCESS, EXIT_AVAILABLE, RESET }
data class LevelRouteState(
  val streak: Int = 0,
  val exitAvailable: Boolean = false,
  val lastRollTurn: Int? = null,
  val lastResult: RouteResult = RouteResult.NONE,
  val originLocation: String? = null,
  val returnLocation: String? = null
)
data class LevelRuntimeState(val key: String = "0", val stageIndex: Int = 0, val route: LevelRouteState = LevelRouteState())

class LevelGraph private constructor(
  private val nodes: Map<String, LevelNode>,
  private val edges: Map<String, List<String>>,
  private val snapshots: Map<Int, List<String>> = emptyMap()
) {
  fun node(key: String) = nodes[key]
  fun allows(from: String, to: String) = from == to && nodes.containsKey(from) || to in edges[from].orEmpty()
  fun next(from: String) = edges[from].orEmpty().singleOrNull()
  fun snapshotPath(key: String, turn: Int): String? {
    val level = nodes[key]?.parentLevel ?: return null
    val assets = snapshots[level].orEmpty()
    if (assets.isEmpty()) return null
    return "file:///android_asset/level_snapshots/drive/" + assets[Math.floorMod(turn.coerceAtLeast(1) - 1, assets.size)]
  }

  companion object {
    const val ASSET = "level_graph.json"
    const val SNAPSHOT_ASSET = "level_snapshots/drive/manifest.json"
    fun load(context: Context): LevelGraph {
      val graph = context.assets.open(ASSET).bufferedReader().use { it.readText() }
      val manifest = context.assets.open(SNAPSHOT_ASSET).bufferedReader().use { it.readText() }
      return fromText(graph, manifest)
    }

    fun fromText(raw: String, snapshotManifest: String? = null): LevelGraph {
      val root = JSONObject(raw)
      require(root.optInt("schemaVersion") == 1) { "Unsupported level graph schema" }
      val nodesJson = root.getJSONArray("nodes")
      val edgesJson = root.getJSONArray("edges")
      val nodes = linkedMapOf<String, LevelNode>()
      val stages = mutableSetOf<Int>()
      repeat(nodesJson.length()) { index ->
        val json = nodesJson.getJSONObject(index)
        val node = LevelNode(
          json.getString("key").trim(),
          json.getInt("parentLevel"),
          json.getInt("stageIndex")
        )
        require(node.key.isNotEmpty() && node.parentLevel >= 0 && node.stageIndex >= 0 && node.key !in nodes && stages.add(node.stageIndex))
        nodes[node.key] = node
      }
      val edges = nodes.keys.associateWith { mutableListOf<String>() }.toMutableMap()
      repeat(edgesJson.length()) { index ->
        val json = edgesJson.getJSONObject(index)
        val from = json.getString("from").trim()
        val to = json.getString("to").trim()
        require(from in nodes && to in nodes && from != to)
        if (to !in edges.getValue(from)) edges.getValue(from).add(to)
      }
      val snapshots = snapshotManifest?.let { text ->
        val manifest = JSONObject(text)
        require(manifest.getString("root") == "level_snapshots/drive")
        val levels = manifest.getJSONObject("levels")
        (0..6).associateWith { level ->
          val assets = levels.getJSONObject(level.toString()).getJSONArray("assets")
          List(assets.length()) { assets.getString(it) }.also { paths ->
            require(paths.isNotEmpty() && paths.all { it.matches(Regex("level_$level/[0-9]{2}\\.webp")) })
          }
        }
      }.orEmpty()
      return LevelGraph(nodes, edges.mapValues { it.value.toList() }, snapshots)
    }
  }
}

object LevelRouteRules {
  const val SUCCESS_PERCENT = 50
  const val TRIPLE_SUCCESS_PERCENT = 1
  const val REQUIRED_STREAK = 6

  fun roll(runtime: LevelRuntimeState, turn: Int, location: String, roll: Int): LevelRuntimeState {
    require(roll in 0 until 100)
    if (runtime.route.exitAvailable || runtime.route.lastRollTurn == turn) return runtime
    var route = runtime.route
    if (route.streak == 0 && route.originLocation.isNullOrBlank()) route = route.copy(originLocation = location)
    if (roll >= SUCCESS_PERCENT) return runtime.copy(route = route.copy(
      streak = 0, exitAvailable = false, lastRollTurn = turn, lastResult = RouteResult.RESET,
      returnLocation = route.originLocation ?: location, originLocation = null
    ))
    val streak = minOf(REQUIRED_STREAK, route.streak + if (roll < TRIPLE_SUCCESS_PERCENT) 3 else 1)
    return runtime.copy(route = route.copy(
      streak = streak, exitAvailable = streak >= REQUIRED_STREAK, lastRollTurn = turn,
      lastResult = if (streak >= REQUIRED_STREAK) RouteResult.EXIT_AVAILABLE else RouteResult.SUCCESS,
      returnLocation = null
    ))
  }

  fun transition(runtime: LevelRuntimeState, graph: LevelGraph, target: String): LevelRuntimeState? {
    if (!runtime.route.exitAvailable || !graph.allows(runtime.key, target)) return null
    val node = graph.node(target) ?: return null
    return LevelRuntimeState(node.key, node.stageIndex)
  }
}
