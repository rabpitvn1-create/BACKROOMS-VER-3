package com.rabpit.backroom.core.gameplay

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LevelSnapshotTest {
  @Test fun localSnapshotsRotateByTurnAndFollowCoreLevel() {
    val graph = """{"schemaVersion":1,"nodes":[
      {"key":"0","parentLevel":0,"stageIndex":0},
      {"key":"0.1","parentLevel":0,"stageIndex":1},
      {"key":"1","parentLevel":1,"stageIndex":2}
    ],"edges":[]}"""
    val levels = JSONObject()
    for (level in 0..6) {
      val count = if (level == 0) 8 else 3
      levels.put(level.toString(), JSONObject().put("assets", JSONArray((1..count).map {
        "level_$level/${it.toString().padStart(2, '0')}.webp"
      })))
    }
    val manifest = JSONObject().put("root", "level_snapshots/drive").put("levels", levels)
    val catalog = LevelGraph.fromText(graph, manifest.toString())

    assertEquals("file:///android_asset/level_snapshots/drive/level_0/01.webp", catalog.snapshotPath("0", 1))
    assertEquals("file:///android_asset/level_snapshots/drive/level_0/08.webp", catalog.snapshotPath("0", 8))
    assertEquals("file:///android_asset/level_snapshots/drive/level_0/01.webp", catalog.snapshotPath("0", 9))
    assertEquals("file:///android_asset/level_snapshots/drive/level_0/02.webp", catalog.snapshotPath("0.1", 2))
    assertEquals("file:///android_asset/level_snapshots/drive/level_1/01.webp", catalog.snapshotPath("1", 4))
    assertNull(catalog.snapshotPath("unknown", 1))
  }
}
