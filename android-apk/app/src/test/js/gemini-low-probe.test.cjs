const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const probe = require(path.resolve(__dirname,'../../../../tools/gemini-low-probe.cjs'));

const choices = [{text:'Quan sát vị trí phía trước'},{text:'Tìm đường vòng để tránh tiếng động'}];
test('provider probe checks batch JSON shape without an API call',()=>{
  assert.equal(probe.validateBatch({steps:[{replyA:'Cao Minh '+('a'.repeat(110)),replyB:'Cao Minh '+('b'.repeat(110)),nextChoices:choices}]},1),'');
  assert.match(probe.validateBatch({steps:[{replyA:'a',replyB:'b',nextChoices:choices}]},1),/invalid/);
  assert.match(probe.validateBatch({steps:[]},1),/length/);
});
test('probe rotates unique configured Gemini keys like the APK',()=>{
  assert.deepEqual(probe.configuredKeys({
    GEMINI_API_KEY_1:' key-a ',GEMINI_API_KEY_2:'key-b',
    GEMINI_API_KEY_3:'key-a',GEMINI_API_KEY:'legacy'
  }),['key-a','key-b','legacy']);
  assert.deepEqual(probe.configuredKeys({}),[]);
});
test('key rotation performs a real provider attempt without recursion',async()=>{
  const previousFetch=global.fetch;
  const previous1=process.env.GEMINI_API_KEY_1;
  const previousLegacy=process.env.GEMINI_API_KEY;
  try{
    process.env.GEMINI_API_KEY_1='fake-key';
    delete process.env.GEMINI_API_KEY;
    global.fetch=async()=>({ok:true,json:async()=>({
      candidates:[{content:{parts:[{text:'{"ok":true}'}]}}],
      usageMetadata:{totalTokenCount:12}
    })});
    const results={httpAttempts:0,calls:[]};
    const output=await probe.generateAcrossKeys('fixture','rotation_fixture',results);
    assert.deepEqual(output.parsed,{ok:true});
    assert.equal(results.httpAttempts,1);
    assert.equal(results.calls.length,1);
  }finally{
    global.fetch=previousFetch;
    if(previous1===undefined)delete process.env.GEMINI_API_KEY_1;
    else process.env.GEMINI_API_KEY_1=previous1;
    if(previousLegacy===undefined)delete process.env.GEMINI_API_KEY;
    else process.env.GEMINI_API_KEY=previousLegacy;
  }
});
test('probe style validator rejects real Gemini failure modes',()=>{
  assert.match(probe.validateProse(
    'Bạn hoảng loạn chạy đi và nhịp tim đập dồn dập.','Tôi chạy về phía sau'),/third-person|second-person|emotion/);
  assert.match(probe.validateProse(
    'Cao Minh đứng dậy rồi bước qua góc rẽ.','Tôi ngồi xuống và nhất quyết không bước thêm'),/stationary/);
  assert.match(probe.validateProse(
    'Cao Minh quan sát và thấy khu vực này hoàn toàn an toàn.','Quan sát khu vực'),/safety/);
  assert.match(probe.validateSingle({reply:'Cao Minh '+('a'.repeat(110)),
    choices:[{text:'Đánh dấu đường cũ trước khi đi'},{text:'Quan sát tường'}]},'Quan sát'),/invalid/);
});
test('provider probe classifies failures without persisting secret-bearing error strings',()=>{
  assert.deepEqual(probe.classifyFailure(new Error('Gemini request X failed: HTTP 503')),
    {httpStatus:503,reason:'HTTP 503'});
  assert.deepEqual(probe.classifyFailure(new Error('Gemini request X failed: HTTP 429')),
    {httpStatus:429,reason:'HTTP 429'});
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
  assert.equal(probe.validateSingle({reply:'Cao Minh '+('a'.repeat(110)),choices},'Đi về phía trước'),'');
  assert.match(probe.validateSingle({reply:'a'.repeat(110),choices:[choices[0],choices[0]]}),/invalid/);
  assert.deepEqual(probe.usageOf({usageMetadata:{promptTokenCount:50,totalTokenCount:87}}),
    {inputTokens:50,outputTokens:null,thinkingTokens:null,totalTokens:87});
});
