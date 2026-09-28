const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const assets = path.resolve(__dirname, '../../main/assets');
const graph = JSON.parse(fs.readFileSync(path.join(assets, 'level_graph.json'), 'utf8'));
const knowledge = JSON.parse(fs.readFileSync(path.join(assets, 'knowledge/level_knowledge.json'), 'utf8')).levels;
const snapshots = JSON.parse(fs.readFileSync(path.join(assets, 'level_snapshots/drive/manifest.json'), 'utf8'));
const registry = JSON.parse(fs.readFileSync(path.join(assets, 'knowledge/entity_encounters.json'), 'utf8'));
const snapshotUi = fs.readFileSync(path.join(assets, 'snapshot-ui.js'), 'utf8');
const keys = ['0', ...Array.from({length: 16}, (_, i) => `hua_1900_${i}`), '1'];
const byKey = new Map(graph.nodes.map(node => [node.key, node]));
assert.equal(byKey.size, graph.nodes.length);
for (let i = 0; i < keys.length; i++) {
  assert.equal(byKey.get(keys[i]).stageIndex, i);
  if (i) assert.deepEqual(graph.edges.filter(edge => edge.from === keys[i - 1]).map(edge => edge.to), [keys[i]]);
}
for (const old of ['0.1', '0.2', '0.5', '0.7', 'manila_room', 'the_torment', 'red_rooms']) {
  assert.equal(byKey.has(old), false);
  assert.equal(knowledge[old], undefined);
}
for (const key of keys.slice(1, -1)) {
  const floor = knowledge[key];
  assert.ok(floor?.identity?.length && floor?.entrancesExits?.length, key);
  for (const forbidden of ['threats', 'boss', 'entities', 'encounterStaging']) assert.equal(floor[forbidden], undefined, key);
  assert.match(floor.gmConstraints.join(' '), /không tự tạo threats hoặc Boss/);
  const number = Number(key.split('_').at(-1)) + 1;
  assert.equal(floor.name, `Hui's Family Level ${number}`);
  const files = snapshots.sublevels[key].assets;
  assert.deepEqual(files, Array.from({length: 3}, (_, j) => `huis_level_${String(number).padStart(2, '0')}_snapshot_${String(j + 1).padStart(2, '0')}.webp`));
  for (const file of files) {
    const bytes = fs.readFileSync(path.join(assets, 'level_snapshots/huis_family', file));
    assert.ok(bytes.length > 0 && bytes.length <= 320 * 1024, file);
    assert.equal(bytes.toString('ascii', 0, 4), 'RIFF', file);
    assert.equal(bytes.toString('ascii', 8, 12), 'WEBP', file);
  }
}
const bosses = registry.entities.filter(entity => /^huis_boss_\d{2}$/.test(entity.key));
assert.equal(bosses.length, 16);
assert.equal(registry.rules.roamingAllLevels, true);
for (let i = 1; i <= 16; i++) {
  const key = `huis_boss_${String(i).padStart(2, '0')}`;
  const boss = bosses.find(entity => entity.key === key);
  assert.equal(boss?.ratePercent, 3);
  assert.equal(boss?.tier, 'boss');
  assert.equal(boss?.autoProcSkills?.length, 3);
  assert.ok(snapshotUi.includes(`'${key}'`), key);
  const mapping = snapshotUi.match(/var __huisBossOverlayFiles=(\{[^;]+\});/);
  assert.ok(mapping);
  const overlay = JSON.parse(mapping[1])[key];
  assert.match(overlay, new RegExp(`^huis_boss_${String(i).padStart(2, '0')}_.*\\.webp$`));
  const bytes = fs.readFileSync(path.join(assets, 'entity', overlay));
  assert.ok(bytes.length > 0 && bytes.length <= 320 * 1024, overlay);
  assert.equal(bytes.toString('ascii', 0, 4), 'RIFF', overlay);
  assert.equal(bytes.toString('ascii', 8, 12), 'WEBP', overlay);
}
console.log("Hui's Family route, snapshots, roaming Bosses and overlays passed");
