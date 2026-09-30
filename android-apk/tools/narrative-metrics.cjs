#!/usr/bin/env node
'use strict';

// Analyze locally captured DEBUG BackroomMain logcat output. No network or API keys.
const fs = require('node:fs');

function analyze(log) {
  const result = {
    canonicalTurns: 0,
    chainBatches: 0,
    prewrittenBeats: 0,
    batchHttpAttempts: 0,
    cachedChoiceHits: 0,
    freeformRewrites: 0,
    uncachedNarrations: 0,
    providerUsageResponses: {gemini: 0, haiku: 0},
    recordedInputTokens: 0,
    recordedOutputTokens: 0,
    recordedThinkingTokens: 0,
    recordedTotalTokens: 0
  };
  for (const line of String(log || '').split(/\r?\n/)) {
    if (line.includes('EMERGENT TURN TELEMETRY:')) result.canonicalTurns++;
    if (line.includes('HIDDEN_CHAIN_CACHE_HIT choice=')) result.cachedChoiceHits++;
    if (line.includes('HIDDEN_CHAIN_PLAYER_ACTION_REWRITE')) result.freeformRewrites++;
    if (line.includes('HIDDEN_CHAIN_UNCACHED_NARRATION')) result.uncachedNarrations++;
    if (line.includes('HIDDEN_CHAIN_BATCH_API')) {
      const count = line.match(/\brequests=(\d+)\s+beats=(\d+)/);
      if (count) {
        result.chainBatches++;
        result.batchHttpAttempts += Number(count[1]);
        result.prewrittenBeats += Number(count[2]);
      }
    }
    if (line.includes('NARRATIVE_API_USAGE')) {
      const provider = line.match(/\bprovider=(gemini|haiku)\b/);
      if (!provider) continue;
      result.providerUsageResponses[provider[1]]++;
      const fields = [
        ['input_tokens', 'recordedInputTokens'],
        ['output_tokens', 'recordedOutputTokens'],
        ['thinking_tokens', 'recordedThinkingTokens'],
        ['total_tokens', 'recordedTotalTokens']
      ];
      for (const [name, dest] of fields) {
        const found = line.match(new RegExp('\\b' + name + '=(-?\\d+)'));
        if (found && Number(found[1]) >= 0) result[dest] += Number(found[1]);
      }
    }
  }
  result.notes = [
    'Token totals cover only successful provider responses that returned usage metadata.',
    'Haiku and Gemini response usage formats may differ; total tokens may be incomplete.',
    'Provider retries, dropped log lines and fallback narration can change API cost.',
    'Text quality, perceived freedom and detected convergence require human playtesting.'
  ];
  return result;
}

if (require.main === module) {
  if (process.argv.length !== 3) {
    process.stderr.write('Usage: node android-apk/tools/narrative-metrics.cjs <debug-logcat-file>\n');
    process.exitCode = 2;
  } else {
    const log = fs.readFileSync(process.argv[2], 'utf8');
    process.stdout.write(JSON.stringify(analyze(log), null, 2) + '\n');
  }
}

module.exports = {analyze};
