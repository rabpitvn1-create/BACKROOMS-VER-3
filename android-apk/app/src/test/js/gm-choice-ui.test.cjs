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

function choices(state, gmChoices = []) {
  context.state = state;
  return Array.from(context.displayedExplorerChoices({choices: gmChoices}), choice => ({
    id: choice.id, action: choice.action, text: choice.text
  }));
}

const locked = choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: false}});
assert.equal(locked[2].action, 'Tiếp tục khám phá Level 0.1');
assert.equal(locked.some(choice => choice.action === 'Đi qua lối ra'), false);

const open = choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: true}}, [
  {text: 'A', action: 'A'}, {text: 'B', action: 'B'}, {text: 'C', action: 'C'}
]);
assert.equal(open.length, 3);
assert.deepEqual(open.map(choice => choice.id), ['A', 'B', 'C']);
assert.equal(open[2].action, 'Đi qua lối ra');
assert.equal(open.some(choice => choice.action === 'Tiếp tục khám phá Level 0.1'), false);
assert.equal(choices({currentLevelKey: '1.2', levelRoute: {exitAvailable: true}})[2].action,
  'Đi qua lối ra');

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
