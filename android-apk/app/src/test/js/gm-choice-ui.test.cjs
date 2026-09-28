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
assert.deepEqual(opening.map(choice => choice.action), [
  'Quan sát dãy tường vàng', 'Lắng nghe tiếng đèn trên trần', 'Kiểm tra lối đi gần nhất'
]);
assert.equal(choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: false}}).length, 0);
assert.equal(choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: false}}, [
  {text: 'Kiểm tra cửa', action: 'Kiểm tra cửa'}
]).length, 1);

const open = choices({currentLevelKey: '0.1', levelRoute: {exitAvailable: true}}, [
  {text: 'A', action: 'A'}, {text: 'B', action: 'B'}, {text: 'C', action: 'C'}
]);
assert.equal(open.length, 3);
assert.deepEqual(open.map(choice => choice.id), ['A', 'B', 'C']);
assert.equal(open[2].action, 'Đi qua lối ra');
assert.equal(open.some(choice => choice.action === 'Tiếp tục khám phá Level 0.1'), false);
assert.deepEqual(choices({currentLevelKey: '1.2', levelRoute: {exitAvailable: true}})
  .map(choice => choice.action), ['Đi qua lối ra']);

console.log('Explorer fallback choices passed');
