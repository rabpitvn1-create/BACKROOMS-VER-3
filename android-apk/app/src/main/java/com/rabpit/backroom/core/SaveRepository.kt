package com.rabpit.backroom.core

import android.content.Context

interface SaveRepository {
  fun save(state: GameState)
  fun load(): GameState
  fun exists(): Boolean
  fun clear()
  fun saveCheckpoint(clientStateJson: String) { throw UnsupportedOperationException("checkpoint_not_supported") }
  fun loadCheckpoint(): Pair<GameState, String> { throw UnsupportedOperationException("checkpoint_not_supported") }
  fun hasCheckpoint(): Boolean = false
  fun clearCheckpoint() = clear()
}

class SharedPreferencesSaveRepository(context: Context) : SaveRepository {
  private val preferences = context.getSharedPreferences("backroom_game_state_core", Context.MODE_PRIVATE)
  @Volatile private var liveState: GameState? = null

  @Synchronized override fun save(state: GameState) { liveState = state }

  @Synchronized override fun load(): GameState = liveState ?: GameState.initial()

  override fun exists(): Boolean = liveState != null

  @Synchronized override fun clear() {
    liveState = null
    clearCheckpoint()
  }

  @Synchronized override fun saveCheckpoint(clientStateJson: String) {
    val state = liveState ?: throw IllegalStateException("Chưa có game để lưu.")
    val committed = preferences.edit()
      .putString(KEY_MANUAL_CORE, GameStateCodec.encode(state))
      .putString(KEY_MANUAL_CLIENT, clientStateJson)
      .remove(KEY_LEGACY_AUTO_STATE)
      .commit()
    if (!committed) throw IllegalStateException("Không thể lưu game.")
  }

  @Synchronized override fun loadCheckpoint(): Pair<GameState, String> {
    val rawCore = preferences.getString(KEY_MANUAL_CORE, null)
      ?: throw IllegalStateException("Chưa có bản lưu thủ công.")
    val state = GameStateCodec.decode(rawCore)
    val client = preferences.getString(KEY_MANUAL_CLIENT, null)
      ?: throw IllegalStateException("Bản lưu thủ công thiếu UI state.")
    liveState = state
    return state to client
  }

  override fun hasCheckpoint(): Boolean =
    preferences.contains(KEY_MANUAL_CORE) && preferences.contains(KEY_MANUAL_CLIENT)

  @Synchronized override fun clearCheckpoint() {
    preferences.edit()
      .remove(KEY_MANUAL_CORE)
      .remove(KEY_MANUAL_CLIENT)
      .remove(KEY_LEGACY_AUTO_STATE)
      .commit()
  }

  companion object {
    private const val KEY_MANUAL_CORE = "manual_game_state"
    private const val KEY_MANUAL_CLIENT = "manual_client_state"
    private const val KEY_LEGACY_AUTO_STATE = "game_state"
  }
}
