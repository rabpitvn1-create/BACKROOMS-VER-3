const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.resolve(__dirname, '../../main/assets/gm-choice-ui.js'), 'utf8');
const start = source.indexOf('  function fallbackExplorerChoices() {');
const end = source.indexOf('  function submitChestChoice() {', start);
assert.ok(start !== -1 && end > start, 'Explorer choice functions must remain present');
const context = {state: null};
vm.runInNewContext(source.slice(start, end), context);

function choices(state, gmChoices = [], initial = false) {
  const entry = {choices: gmChoices};
  context.state = {...state, log: initial ? [entry] : [{text: 'opening'}, entry]};
  return Array.from(context.displayedExplorerChoices(entry), choice => ({
    id: choice.id, action: choice.action, text: choice.text
  }));
}

const opening = choices({currentLevelKey: '0', levelRoute: {exitAvailable: false}}, [], true);
assert.deepEqual(opening.map(choice => choice.action), ['Quan sát dãy tường vàng']);
assert.equal(choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: false}}).length, 0);
assert.equal(choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: false}}, [
  {text: 'Kiểm tra cửa', action: 'Kiểm tra cửa'}
]).length, 1);

const open = choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: true}}, [
  {text: 'A', action: 'A'}, {text: 'B', action: 'B'}, {text: 'C', action: 'C'}
]);
assert.equal(open.length, 1);
assert.deepEqual(open.map(choice => choice.id), ['A']);
assert.equal(open[0].action, 'Đi qua lối ra');
assert.equal(open.some(choice => choice.action === 'Tiếp tục khám phá Level 0.1'), false);
assert.deepEqual(choices({currentLevelKey: '1.2', levelRoute: {exitAvailable: true}})
  .map(choice => choice.action), ['Đi qua lối ra']);

assert.deepEqual(choices({currentLevelKey:'0',levelRoute:{exitAvailable:false}},[
  {text:'Quan sát cửa',action:'Quan sát cửa'},
  {text:'Rẽ trái',action:'Rẽ trái'},
  {text:'Rẽ phải',action:'Rẽ phải'}
]).map(choice=>choice.action), ['Quan sát cửa']);
const postFightEntry = {choices:[]};
context.state = {turn:8,log:[{text:'prologue'},postFightEntry],
  combat:{active:false,outcome:'victory',logIndex:1},levelRoute:{exitAvailable:false}};
assert.deepEqual(Array.from(context.displayedExplorerChoices(postFightEntry),x=>x.action),
  ['Quan sát khu vực sau trận chiến']);
const nextGm = {role:'gm', text:'Cao Minh vẫn đứng trong hành lang.', choices:[]};
context.state.log.push({role:'player', text:'Quan sát khu vực sau trận chiến'}, nextGm);
assert.deepEqual(Array.from(context.displayedExplorerChoices(nextGm), x => x.action),
  ['Lắng nghe âm thanh trong khu vực hiện tại'],
  'the next GM entry still offers a suggestion after the post-combat action');
const followingGm = {role:'gm', text:'Chưa nghe thấy tiếng động lạ.', choices:[]};
context.state.log.push({role:'player', text:'Lắng nghe âm thanh trong khu vực hiện tại'}, followingGm);
assert.deepEqual(Array.from(context.displayedExplorerChoices(followingGm), x => x.action),
  ['Quan sát khu vực phía trước'],
  'suggestions continue on subsequent turns without repeating the last action');
const freshChoice = {role:'gm', text:'Cánh cửa hiện ra.', choices:[{text:'Kiểm tra cánh cửa'}]};
context.state.log.push({role:'player', text:'Đứng chờ'}, freshChoice);
assert.deepEqual(Array.from(context.displayedExplorerChoices(freshChoice), x => x.text),
  ['Kiểm tra cánh cửa'], 'real GM suggestions take priority over deterministic fallback');
context.state.flags = {entityEncounterKey:'hound'};
assert.equal(context.displayedExplorerChoices({choices:[]}).length, 0,
  'do not offer explorer suggestions while an entity encounter is pending');
delete context.state.flags;
context.state.combat = {active:true,outcome:'active'};
assert.equal(context.displayedExplorerChoices({choices:[]}).length, 0,
  'do not offer explorer suggestions during combat');
context.state.combat = {active:false,outcome:'defeat'};
assert.equal(context.displayedExplorerChoices({choices:[]}).length, 0,
  'do not offer explorer suggestions after defeat');
assert.equal(source.includes("font-family:'Play'"), false,
  'GAME MASTER text, labels and combat UI must not request the decorative font');

console.log('Explorer fallback choices passed');

const submitStart = source.indexOf('  function lastGmIndex() {');
const submitEnd = source.indexOf('  function chestPresent() {', submitStart);
const entry = {role:'gm', text:'Một cánh cửa hé mở.', choices:[{text:'Kiểm tra cửa'}]};
const calls = [];
const bridge = {
  submitChoice: (...args) => calls.push({kind:'choice', args}),
  submitTurn: (...args) => calls.push({kind:'custom', args}),
  prefetchChoices: (...args) => calls.push({kind:'prefetch', args})
};
const ui = {
  state: {turn:7, log:[entry], flags:{}}, busy:false, submit:{disabled:false},
  Android:bridge, window:{Android:bridge, render(){}}
};
vm.runInNewContext(source.slice(submitStart, submitEnd), ui);
ui.submitExplorerChoice(entry, {id:'A', action:'Kiểm tra cửa'});
ui.submitExplorerChoice(entry, {id:'A', action:'Kiểm tra cửa'});
assert.equal(calls.length, 1, 'double tap dispatches one choice');
assert.equal(calls[0].kind, 'choice');
assert.deepEqual(Array.from(calls[0].args.slice(1)), ['Kiểm tra cửa','A',7,0,entry.text]);
assert.equal(ui.window.__gmEnvironmentLoading,true,'choice submission exposes loading feedback');
const playerSource = fs.readFileSync(path.resolve(__dirname, '../../main/assets/player-action-ui.js'), 'utf8');
const customStart = playerSource.indexOf("  form.addEventListener('submit', function(event){");
const customEnd = playerSource.indexOf('  }, true);', customStart);
assert.ok(customStart >= 0 && customEnd > customStart);
let customSubmit;
const customUi = {
  state:ui.state, modal:{hidden:false}, action:{value:'Kiểm tra cửa'},
  form:{addEventListener(type, callback){customSubmit=callback;}},
  actionLocked:()=>false, closePlayerAction(){}, busy:false, submit:{disabled:false},
  Android:bridge, window:{Android:bridge,render(){}}
};
vm.runInNewContext(playerSource.slice(customStart, customEnd + '  }, true);'.length), customUi);
customSubmit({preventDefault(){},stopImmediatePropagation(){}});
assert.equal(calls[1].kind, 'custom', 'identical free text uses the independent normal bridge');
assert.equal(customUi.window.__gmEnvironmentLoading,true,
  'post-combat free text indicates processing instead of appearing frozen');

const prefetchStart = source.indexOf('  window.backroomPrefetchChoices = function(){');
const prefetchEnd = source.indexOf('\n  window.render();', prefetchStart);
ui.busy = false;
ui.displayedExplorerChoices = () => [
  {id:'A',action:'A'}, {id:'B',action:'B'}, {id:'C',action:'C'}
];
vm.runInNewContext(source.slice(prefetchStart, prefetchEnd), ui);
ui.window.backroomPrefetchChoices();
assert.equal(calls[2].kind, 'prefetch');
assert.equal(JSON.parse(calls[2].args[0]).turn, 7);
assert.deepEqual(JSON.parse(calls[2].args[1]).map(x => x.action), ['A','B','C']);

console.log('Choice origin, double tap, and batch source passed');

const html = fs.readFileSync(path.resolve(__dirname, '../../main/assets/index.html'), 'utf8');
const turnHandler = html.split('\n').find(line => line.startsWith('window.backroomTurn=json=>'));
assert.ok(turnHandler, 'current-turn handler must exist');
const order = [];
const turnUi = {
  state:null, CURRENT_CHARACTER_CANON:{}, actionEl:{value:'before'}, busy:true,
  submitEl:{disabled:true}, window:{backroomPrefetchChoices(){order.push('prefetch');}},
  ensureCurrentLevel:x=>x, render(){order.push('render');}
};
vm.runInNewContext(turnHandler, turnUi);
turnUi.window.backroomTurn(JSON.stringify({turn:8,log:[entry]}));
assert.deepEqual(order, ['render'], 'actual turn renders without automatic speculative A/B/C');
assert.equal(source.includes('  window.backroomPrefetchChoices();'), false,
  'legacy prefetch hook must not run on initial render');

console.log('Current story render order passed');

const finishStart=source.indexOf('  function finishCombatAnimation(token) {');
const finishEnd=source.indexOf('  window.backroomCombatTurn = function',finishStart);
assert.ok(finishStart>=0&&finishEnd>finishStart);
const resumed={state:{combat:{active:false,outcome:'victory'},turn:9},busy:true,
  window:{__combatAnimationToken:7,__combatFeedbackBusy:true,__combatBusy:true,render(){}},
  scrollForCurrentMode(){},status:{textContent:''}};
vm.runInNewContext(source.slice(finishStart,finishEnd),resumed);
resumed.finishCombatAnimation(7);
assert.equal(resumed.busy,false);
assert.equal(resumed.window.__combatBusy,false,
  'combat completion unlocks free text and Core stat upgrades');
console.log('Post-combat unlock passed');
