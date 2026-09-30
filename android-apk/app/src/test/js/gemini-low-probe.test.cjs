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
test('provider probe classifies failures without persisting secret-bearing error strings',()=>{
  assert.deepEqual(probe.classifyFailure(new Error('Gemini request X failed: HTTP 503')),
    {httpStatus:503,reason:'HTTP 503'});
  assert.deepEqual(probe.classifyFailure(new Error('Network error with sensitive context')),
    {httpStatus:null,reason:'request failed'});
});
test('partial provider failures do not crash token accounting',()=>{
  assert.equal(probe.measuredTokensOf([
    {name:'failed',failed:true,httpStatus:503},
    {name:'success',usage:{totalTokens:1748}},
    {name:'no_usage',usage:{totalTokens:null}}
  ]),1748);
});
test('provider probe rejects duplicate choices and incomplete single reply',()=>{
  assert.equal(probe.validateSingle({reply:'a'.repeat(110),choices}),'');
  assert.match(probe.validateSingle({reply:'a'.repeat(110),choices:[choices[0],choices[0]]}),/invalid/);
  assert.deepEqual(probe.usageOf({usageMetadata:{promptTokenCount:50,totalTokenCount:87}}),
    {inputTokens:50,outputTokens:null,thinkingTokens:null,totalTokens:87});
});
