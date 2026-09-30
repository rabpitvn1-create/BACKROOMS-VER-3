const assert = require('node:assert/strict');
const path = require('node:path');
const test = require('node:test');
const {analyze} = require(path.resolve(__dirname, '../../../../tools/narrative-metrics.cjs'));

test('aggregates experiment events without reading secrets or payloads', () => {
  const report = analyze([
    'D/BackroomMain: HIDDEN_CHAIN_BATCH_API requests=1 beats=3',
    'D/BackroomMain: NARRATIVE_API_USAGE provider=gemini attempts=1 input_tokens=850 output_tokens=300 thinking_tokens=18 total_tokens=1168',
    'D/BackroomMain: HIDDEN_CHAIN_CACHE_HIT choice=A',
    'D/BackroomMain: EMERGENT TURN TELEMETRY: total=115ms turnId=abc situation=NONE',
    'D/BackroomMain: HIDDEN_CHAIN_CACHE_HIT choice=B',
    'D/BackroomMain: EMERGENT TURN TELEMETRY: total=120ms turnId=def situation=NONE',
    'D/BackroomMain: HIDDEN_CHAIN_PLAYER_ACTION_REWRITE',
    'D/BackroomMain: NARRATIVE_API_USAGE provider=haiku attempts=2 input_tokens=100 output_tokens=70',
    'D/BackroomMain: EMERGENT TURN TELEMETRY: total=200ms turnId=ghi situation=NONE'
  ].join('\n'));
  assert.equal(report.chainBatches, 1);
  assert.equal(report.prewrittenBeats, 3);
  assert.equal(report.batchHttpAttempts, 1);
  assert.equal(report.cachedChoiceHits, 2);
  assert.equal(report.freeformRewrites, 1);
  assert.equal(report.canonicalTurns, 3);
  assert.equal(report.recordedInputTokens, 950);
  assert.equal(report.recordedOutputTokens, 370);
  assert.equal(report.recordedThinkingTokens, 18);
  assert.equal(report.recordedTotalTokens, 1168);
  assert.deepEqual(report.providerUsageResponses, {gemini:1, haiku:1});
});

test('handles empty log without inventing savings', () => {
  const report = analyze('');
  assert.equal(report.canonicalTurns, 0);
  assert.equal(report.chainBatches, 0);
  assert.equal(report.recordedTotalTokens, 0);
});
