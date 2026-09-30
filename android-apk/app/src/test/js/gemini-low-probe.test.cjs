const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const probe = require(path.resolve(__dirname,'../../../../tools/gemini-low-probe.cjs'));

const choices = [{text:'Quan sát vị trí phía trước'},{text:'Tìm đường vòng để tránh tiếng động'}];
test('provider probe checks batch JSON shape without an API call',()=>{
  assert.equal(probe.validateBatch({steps:[{replyA:'a'.repeat(110),replyB:'b'.repeat(110),nextChoices:choices}]},1),'');
  assert.match(probe.validateBatch({steps:[{replyA:'a',replyB:'b',nextChoices:choices}]},1),/invalid/);
  assert.match(probe.validateBatch({steps:[]},1),/length/);
});
test('provider probe rejects duplicate choices and incomplete single reply',()=>{
  assert.equal(probe.validateSingle({reply:'a'.repeat(110),choices}),'');
  assert.match(probe.validateSingle({reply:'a'.repeat(110),choices:[choices[0],choices[0]]}),/invalid/);
  assert.deepEqual(probe.usageOf({usageMetadata:{promptTokenCount:50,totalTokenCount:87}}),
    {inputTokens:50,outputTokens:null,thinkingTokens:null,totalTokens:87});
});
