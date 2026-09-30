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
