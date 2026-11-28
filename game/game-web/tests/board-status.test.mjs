import assert from 'node:assert/strict';
import test from 'node:test';
import { botLight, coordinateStep, hpColor, HpScale, REJECT_FLASH_MS } from '../src/boardStatus.ts';

test('bot light is orange right after a rejection, green with queued commands, dark when idle', () => {
  assert.equal(botLight(0, undefined, 1000), 'idle');
  assert.equal(botLight(3, undefined, 1000), 'busy');
  assert.equal(botLight(3, 1000, 1000 + REJECT_FLASH_MS - 1), 'rejected');
  assert.equal(botLight(3, 1000, 1000 + REJECT_FLASH_MS), 'busy');
});

test('health bar turns amber then red as HP drops', () => {
  assert.equal(hpColor(1), '#4ade80');
  assert.equal(hpColor(0.5), '#facc15');
  assert.equal(hpColor(0.2), '#f87171');
});

test('HP scale uses the highest HP seen for the same kind', () => {
  const scale = new HpScale();
  scale.observe('unit:WORKER', 3);
  scale.observe('unit:WORKER', 1);
  assert.equal(scale.max('unit:WORKER', 1), 3);
  assert.equal(scale.ratio('unit:WORKER', 1), 1 / 3);
  assert.equal(scale.ratio('unit:MELEE', 2), 1);
  assert.equal(scale.ratio('hq', 0), 0);
});

test('coordinates label every cell on small maps and every fifth cell on large ones', () => {
  assert.equal(coordinateStep(70), 1);
  assert.equal(coordinateStep(17), 5);
});
