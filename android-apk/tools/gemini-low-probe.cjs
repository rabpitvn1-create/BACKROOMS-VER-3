#!/usr/bin/env node
'use strict';

/**
 * Synthetic live-provider probe for Gemini Low, NOT an Android gameplay test.
 * Uses only fixed fictional test inputs; API keys and prompts are never logged.
 * Triggered only by the dedicated experimental workflow, never as a PR gate.
 */
const fs = require('node:fs');
const path = require('node:path');

const MODEL = process.env.GEMINI_MODEL || 'gemini-3.8-flash';
const OUTPUT = process.env.PROBE_OUTPUT || path.resolve('android-apk/probe-results/gemini-low-probe.json');
const EXPLORER_FACTS = 'Bối cảnh: Cao Minh ở Level 0 của Backrooms. Không có Entity, nhân vật phụ hay vật phẩm mới. Core đã quyết định kết quả các lượt trước khi GM kể chuyện. Không thay đổi Core. Kể ngôi thứ ba hạn định quanh Cao Minh, không gọi nhân vật là "bạn". Không tự bịa sợ hãi, hoang mang, hy vọng hay quyết định nội tâm. Không tuyên bố khu vực an toàn hoặc không có mối đe dọa chỉ vì lượt này không có biến cố. Không tự tạo vết đánh dấu hay thay đổi thế giới tồn tại lâu dài. Nếu Player Action đứng yên, ngồi lại hoặc chỉ lắng nghe thì không tự bắt Cao Minh đứng dậy hay di chuyển.';
const BEATS = [
  {destination:'một đoạn hành lang vàng có ánh đèn huỳnh quang',result:'khám phá tiến triển; không có biến cố mới',a:'Thận trọng tiến vào khoảng tối',b:'Lần theo tiếng ù của bóng đèn'},
  {destination:'một vùng tường vàng có vết ẩm loang',result:'quan sát thành công; không có Entity',a:'Quan sát kỹ các vệt ẩm',b:'Rời khỏi chỗ sáng để tìm lối khác'},
  {destination:'một khúc ngoặt tiếp tục thuộc Level 0',result:'khám phá tiến triển; không có chuyển Level',a:'Tiến qua khúc ngoặt',b:'Đánh dấu đường cũ trước khi bước tiếp'}
];
const UNEXPECTED = [
  'Tôi ngồi xuống, nhắm mắt và nhất quyết không bước thêm.',
  'Tôi quay đầu bỏ chạy thật nhanh về phía sau.',
  'Tôi áp tai vào bức tường và chỉ tập trung lắng nghe.',
  'Tôi cúi xuống, bò sát mặt sàn và tránh mọi lối đi dễ thấy.',
  'Tôi cố tìm cách quay về vị trí ban đầu, từ chối đi theo đường trước mặt.'
];

function configuredKeys(env=process.env) {
  return [...new Set([
    env.GEMINI_API_KEY_1, env.GEMINI_API_KEY_2, env.GEMINI_API_KEY_3,
    env.GEMINI_API_KEY_4, env.GEMINI_API_KEY_5, env.GEMINI_API_KEY
  ].filter(value => typeof value === 'string' && value.trim())
    .map(value => value.trim()))];
}

function validChoices(choices) {
  return Array.isArray(choices) && choices.length === 2
      && choices.every(c => c && typeof c.text === 'string' && c.text.trim().length >= 8
        && !/^(?:đánh dấu|khắc|cào|vẽ|viết ký hiệu|đặt dấu mốc)\b/iu.test(c.text.trim()))
      && choices[0].text.trim() !== choices[1].text.trim();
}
function stationaryAction(action='') {
  const stay=/(?:nhất\s+quyết\s+)?không\s+(?:bước|đi|di\s+chuyển|rời)|\b(?:ngồi\s+(?:yên|xuống)|đứng\s+yên|áp\s+tai|chỉ\s+(?:tập trung\s+)?lắng nghe)\b/iu;
  const move=/(?:^|\s)(?:đi|chạy|bò|bước|tiến|rẽ|leo|di\s+chuyển|rời|quay\s+(?:đầu|gót)|men\s+theo|đi\s+theo)\b/iu;
  return stay.test(action) || (!move.test(action) && /\b(?:lắng nghe|quan sát|chờ|đợi|dừng|nghỉ)\b/iu.test(action));
}
function validateProse(reply, action='') {
  const text=String(reply||'');
  if (!/\bCao Minh\b/u.test(text)) return 'reply must use third-person Cao Minh narration';
  if (/\bbạn\b/iu.test(text)) return 'reply uses second-person narration';
  if (/(?:sự|nỗi)\s+(?:căng thẳng|hoang mang|sợ hãi)|\btâm\s*(?:lý|trí).{0,30}?(?:bình ổn|hoang mang|căng thẳng|sợ hãi)|\bnhịp\s+tim.{0,30}?(?:dồn dập|đập dồn)|\b(?:hy vọng|hoảng loạn|hoang mang|sợ hãi)\b/iu.test(text))
    return 'reply invents inner emotion';
  if (/(?:khu vực(?: này)?|nơi(?: đây| này)?|lối đi(?: này)?)\s+(?:hiện\s+)?(?:hoàn\s+toàn|tuyệt\s+đối)\s+an\s+toàn/iu.test(text)
      || /(?:không|chẳng)\s+(?:hề\s+)?(?:có|xuất hiện).{0,40}?(?:mối\s+(?:nguy hiểm|đe dọa)|dấu hiệu\s+nguy hiểm|bóng dáng\s+thực thể)/iu.test(text))
    return 'reply overclaims safety or threat absence';
  if (/(?:cao minh|hắn)\s+(?:cào|khắc|vẽ|đánh dấu|viết).{0,60}?(?:vết\s+(?:xước|khắc)|ký hiệu|dấu mốc|lên\s+(?:tường|sàn))/iu.test(text))
    return 'reply invents a persistent world edit';
  if (stationaryAction(action)
      && /(?:cao minh|hắn|anh)\s+(?:tự\s+)?(?:đứng\s+dậy(?:.{0,24})?|bước|đi|chạy|bò|tiến|rẽ|leo|di\s+chuyển|rời|quay\s+gót|men\s+theo)\b/iu.test(text))
    return 'reply moves Cao Minh after a stationary action';
  return '';
}
function validateBatch(parsed, count) {
  if (!parsed || !Array.isArray(parsed.steps) || parsed.steps.length !== count) {
    return 'batch length or JSON shape incorrect';
  }
  for (const [i, step] of parsed.steps.entries()) {
    if (!step || typeof step.replyA !== 'string' || typeof step.replyB !== 'string'
        || step.replyA.trim().length < 100 || step.replyB.trim().length < 100
        || !validChoices(step.nextChoices)) return 'invalid replies/choices at step ' + i;
    const proseA=validateProse(step.replyA,(BEATS[i]||{}).a||'');
    const proseB=validateProse(step.replyB,(BEATS[i]||{}).b||'');
    if (proseA || proseB) return 'style violation at step '+i+': '+(proseA||proseB);
  }
  return '';
}
function validateSingle(parsed, action='') {
  if (!parsed || typeof parsed.reply !== 'string' || parsed.reply.trim().length < 100
      || !validChoices(parsed.choices)) return 'invalid reply or choices';
  return validateProse(parsed.reply,action);
}
function usageOf(data) {
  const u = data && data.usageMetadata || {};
  return {
    inputTokens: Number.isFinite(u.promptTokenCount) ? u.promptTokenCount : null,
    outputTokens: Number.isFinite(u.candidatesTokenCount) ? u.candidatesTokenCount : null,
    thinkingTokens: Number.isFinite(u.thoughtsTokenCount) ? u.thoughtsTokenCount : null,
    totalTokens: Number.isFinite(u.totalTokenCount) ? u.totalTokenCount : null
  };
}
async function generate(prompt, name, results) {
  const key = process.env.GEMINI_API_KEY;
  if (!key) throw new Error('Gemini API secret not configured');
  const started = Date.now();
  let response;
  // Only transient provider overload/rate-limit errors are retried.
  // Do not rotate to Haiku: this probe must measure Gemini Low itself.
  for (let attempt = 0; attempt < 3; attempt++) {
    results.httpAttempts++;
    response = await fetch('https://generativelanguage.googleapis.com/v1beta/models/'
        + encodeURIComponent(MODEL) + ':generateContent', {
      method: 'POST',
      headers: {'x-goog-api-key': key, 'content-type': 'application/json'},
      body: JSON.stringify({
        contents: [{role:'user',parts:[{text:prompt}]}],
        generationConfig: {
          responseMimeType:'application/json',
          thinkingConfig:{thinkingLevel:'low'},
          temperature:0.8,
          maxOutputTokens:4096
        }
      }),
      signal: AbortSignal.timeout(60000)
    });
    if (response.ok) break;
    // A 429 indicates exhausted/rate-limited quota. Retrying every fixture
    // hides the failure and burns more API attempts without useful evidence.
    if (response.status === 429) {
      throw new Error('Gemini request ' + name + ' failed: HTTP 429');
    }
    const retryable = [500,502,503,504].includes(response.status);
    if (!retryable || attempt === 2) {
      throw new Error('Gemini request ' + name + ' failed: HTTP ' + response.status);
    }
    const after = Number(response.headers.get('retry-after'));
    const delayMs = Number.isFinite(after) && after > 0
        ? Math.min(15000, after * 1000) : 2500 * (attempt + 1);
    await new Promise(resolve => setTimeout(resolve, delayMs));
  }
  const data = await response.json();
  const text = (data.candidates || []).flatMap(c => ((c.content || {}).parts || []))
    .map(p => p.text || '').join('').trim();
  let parsed;
  try { parsed = JSON.parse(text); } catch (_) { parsed = null; }
  const entry = {name,latencyMs:Date.now()-started,usage:usageOf(data),parseable:parsed!==null};
  results.calls.push(entry);
  return {parsed,entry};
}
/** Continue collecting independent samples even when the provider rejects one. */
function classifyFailure(error) {
  const match = String(error && error.message || '').match(/\bHTTP\s+(\d{3})\b/);
  return {httpStatus: match ? Number(match[1]) : null,
    reason: match ? 'HTTP '+match[1] : 'request failed'};
}
async function generateAcrossKeys(prompt, name, results) {
  const keys = configuredKeys();
  if (!keys.length) throw new Error('Gemini API secret not configured');
  const original = process.env.GEMINI_API_KEY;
  let lastError = null;
  try {
    for (const key of keys) {
      process.env.GEMINI_API_KEY = key;
      try { return await generate(prompt,name,results); }
      catch (error) {
        lastError = error;
        const status = classifyFailure(error).httpStatus;
        if (![429,500,502,503,504].includes(status || 0)) throw error;
      }
    }
  } finally {
    if (original === undefined) delete process.env.GEMINI_API_KEY;
    else process.env.GEMINI_API_KEY = original;
  }
  throw lastError || new Error('Gemini request failed');
}

async function safelyGenerate(prompt, name, results) {
  const started = Date.now();
  try { return await generateAcrossKeys(prompt,name,results); }
  catch (error) {
    const failure = classifyFailure(error);
    if (failure.httpStatus === 429) results.quotaLimited = true;
    results.calls.push({name,latencyMs:Date.now()-started,
      parseable:false,failed:true,httpStatus:failure.httpStatus});
    return {parsed:null,failed:failure.reason};
  }
}
/** Missing usage on a failed HTTP attempt must never crash probe reporting. */
function measuredTokensOf(calls) {
  return calls.reduce((sum,entry) => sum
    + (entry.usage && Number.isFinite(entry.usage.totalTokens) ? entry.usage.totalTokens : 0), 0);
}
function flush(results) {
  fs.mkdirSync(path.dirname(OUTPUT), {recursive:true});
  fs.writeFileSync(OUTPUT, JSON.stringify(results,null,2)+'\n',{mode:0o600});
}
async function main() {
  const results = {
    kind:'synthetic_gemini_low_probe_not_actual_gameplay',
    model:MODEL,
    thinking:'low',
    commit:process.env.GITHUB_SHA || null,
    timestamp:new Date().toISOString(),
    httpAttempts:0,
    calls:[],
    batched:{},
    singleTurnBaseline:[],
    freeformRewrites:[],
    limitations:[
      'Synthetic Core outcomes; production Android prompts and encounter behavior not covered.',
      'Human blind reading is needed to judge perceived freedom and prose quality.',
      'Missing/failed API responses may have unrecorded billed token usage.',
      'Comparing 1 batch against 3 single calls is indicative only: different output lengths.'
    ]
  };
  try {
    if (!configuredKeys().length) throw new Error('Gemini API secret not configured');
    const chainPrompt = EXPLORER_FACTS + '\nBạn phải chuẩn bị trước đúng 3 lượt trong một lời gọi. '
      + 'Mỗi lượt có hai cách tiếp cận khác nhau nhưng cùng kết quả Core. '
      + 'Lời kể không được tiết lộ các lựa chọn hội tụ; thay đổi nhịp điệu, giác quan và quan hệ nhân quả, không chỉ đổi từ đồng nghĩa. '
      + 'Không tạo Entity, chuyển Level hoặc vật phẩm mới. '
      + 'Trả JSON DUY NHẤT: {"steps":[{"replyA":"...","replyB":"...","nextChoices":[{"text":"..."},{"text":"..."}]}]}. '
      + 'Mỗi reply dài 120–220 từ tiếng Việt, chứa hành động và đích đến tương ứng. '
      + 'Bước 0 dùng hai lựa chọn được cung cấp, mỗi bước kế tiếp dùng nextChoices đã tạo từ bước trước.\n'
      + JSON.stringify(BEATS);
    const batch = await safelyGenerate(chainPrompt,'chain_3_beats_2_variants',results);
    const problem = batch.failed || validateBatch(batch.parsed,BEATS.length);
    results.batched = {valid:!problem,problem,steps:batch.parsed&&batch.parsed.steps||null};

    // Baseline: one normal narration request per turn (only one reply and two choices).
    for (let i=0;i<BEATS.length && !results.quotaLimited;i++) {
      const beat=BEATS[i];
      const prompt=EXPLORER_FACTS+'\nChỉ kể một lượt. Người chơi: '+beat.a
        +'. Kết quả Core: '+beat.result+'. Điểm đến: '+beat.destination
        +'. Viết diễn biến tiếng Việt 120–220 từ, tôn trọng hành động, không báo cáo state. '
        + 'Trả JSON duy nhất {"reply":"...","choices":[{"text":"..."},{"text":"..."}]}.';
      const item=await safelyGenerate(prompt,'baseline_turn_'+(i+1),results);
      const error=item.failed || validateSingle(item.parsed,beat.a);
      results.singleTurnBaseline.push({index:i+1,valid:!error,problem:error,output:item.parsed});
    }
    // The same fixed beat must support surprising free-form actions.
    const beat=BEATS[0];
    for (const [i,action] of UNEXPECTED.entries()) {
      if (results.quotaLimited) break;
      const prompt=EXPLORER_FACTS+'\nPLAYER ACTION: '+action
        +'\nKết quả Core không đổi: '+beat.result+'. Đích đến bắt buộc: '+beat.destination
        +'. Hãy kể có quan hệ nhân quả tự nhiên từ hành động THỰC TẾ đến kết quả này. '
        + 'Không giả vờ nhân vật đã tự nguyện chọn một hành động khác; không kể chuyện theo kiểu thông báo hệ thống, '
        + 'không lạm dụng bất tỉnh, dịch chuyển hoặc hành lang đột ngột biến dạng. '
        + 'Viết 120–220 từ. Trả JSON duy nhất {"reply":"...","choices":[{"text":"..."},{"text":"..."}]}.';
      const item=await safelyGenerate(prompt,'freeform_'+(i+1),results);
      const error=item.failed || validateSingle(item.parsed,action);
      results.freeformRewrites.push({index:i+1,action,valid:!error,problem:error,output:item.parsed});
    }
    if (results.batched.valid !== true && /^HTTP 5\\d\\d$/.test(String(results.batched.problem||''))
        && !results.quotaLimited) {
      const retry=await safelyGenerate(chainPrompt,'chain_3_beats_2_variants_retry',results);
      const retryProblem=retry.failed || validateBatch(retry.parsed,BEATS.length);
      results.batched={valid:!retryProblem,problem:retryProblem,
        steps:retry.parsed&&retry.parsed.steps||null,retried:true};
    }
    // A partial run remains a failed gate, but its evidence is still collected.
    if (results.batched.valid !== true || results.quotaLimited
        || results.singleTurnBaseline.length !== BEATS.length
        || results.freeformRewrites.length !== UNEXPECTED.length
        || results.singleTurnBaseline.some(item=>!item.valid)
        || results.freeformRewrites.some(item=>!item.valid)) {
      results.incomplete = true;
      process.exitCode = 1;
    }
  } catch (error) {
    // Do not persist raw errors or provider payloads (avoid accidental secret leakage).
    results.stopped={reason:String(error.message||'unknown').replace(/key=[^\s&]+/gi,'key=[REDACTED]').slice(0,200)};
    process.exitCode=1;
  } finally {
    flush(results);
    const all=results.calls;
    const measuredTokens=measuredTokensOf(all);
    process.stdout.write(JSON.stringify({
      model:results.model,attempts:results.httpAttempts,
      successfulResponses:all.filter(item=>!item.failed).length,
      failedRequests:all.filter(item=>item.failed).length,
      batchValid:results.batched.valid===true,
      freeformValid:results.freeformRewrites.filter(x=>x.valid).length,
      baselineValid:results.singleTurnBaseline.filter(x=>x.valid).length,
      measuredTokens,stopped:!!results.stopped,
      quotaLimited:!!results.quotaLimited,incomplete:!!results.incomplete
    })+'\n');
  }
}
if (require.main===module) main();
module.exports={validChoices,validateBatch,validateSingle,validateProse,stationaryAction,usageOf,classifyFailure,measuredTokensOf,configuredKeys,generateAcrossKeys};
