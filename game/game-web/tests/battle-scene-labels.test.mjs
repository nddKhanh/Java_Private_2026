import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const source = await readFile(new URL('../src/BattleScene.ts', import.meta.url), 'utf8');

test('entity IDs render as contrasting overlays inside every entity symbol', () => {
  assert.match(source, /addIdLabel\(shape, center\.x, center\.y - size \* 0\.32, headquarters\.id/);
  assert.match(source, /addIdLabel\(shape, center\.x, center\.y - size \* 0\.3, building\.id/);
  assert.match(source, /center\.y - cellSize \* unit\.height \* 0\.3,[\s\S]*unit\.id/);
  assert.match(source, /backgroundColor: '#020617'/);
  assert.match(source, /label\.on\('pointerover'/);
});

test('small cells reveal ID labels on hover instead of covering neighbours', () => {
  assert.match(source, /const compact = cellSize < 28;/);
  assert.match(source, /target\.on\('pointerover', \(\) => label\.setVisible\(true\)\)/);
});

test('headquarters hexagon uses non-negative points so it stays centered on its cell', () => {
  const hexagon = source.match(/this\.add\.polygon\(center\.x, center\.y, \[([\s\S]*?)\]/);
  assert.ok(hexagon);
  assert.doesNotMatch(hexagon[1], /-\s*(?:size|halfWidth|halfHeight)/);
});

test('workers and soldiers render as people while SIEGE stays a vehicle', () => {
  assert.match(source, /unit\.type === 'SIEGE'/);
  assert.match(source, /this\.drawPerson\(center\.x, center\.y, cellSize \* 0\.9, color, alpha, unit\.type\)/);
  assert.match(source, /figure\.fillCircle\(0, headY/);
});
