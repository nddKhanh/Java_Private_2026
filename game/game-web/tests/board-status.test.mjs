import assert from 'node:assert/strict';
import test from 'node:test';
import {
  botLight, canvasSize, clampScroll, clampZoom, coordinateStep, hpColor, hpRatio, maxZoom, REJECT_FLASH_MS, screenAt,
  scrollToKeep, worldAt,
} from '../src/boardStatus.ts';

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

test('health ratio comes from the server max HP and stays within 0..1', () => {
  assert.equal(hpRatio(1, 3), 1 / 3);
  assert.equal(hpRatio(5, 5), 1);
  assert.equal(hpRatio(0, 4), 0);
  assert.equal(hpRatio(2, 0), 0);
});

test('canvas grows with pixel density but stays bounded', () => {
  assert.equal(canvasSize(700, 1), 960);
  assert.equal(canvasSize(740, 2), 1850);
  assert.equal(canvasSize(1600, 3), 2400);
});

test('zoom stays between the whole board and a large cell', () => {
  assert.equal(maxZoom(17.5, 150), 150 / 17.5);
  assert.equal(maxZoom(200, 150), 1);
  assert.equal(clampZoom(0.5, 4), 1);
  assert.equal(clampZoom(9, 4), 4);
});

test('zooming keeps the board point under the mouse in place and never scrolls past the board', () => {
  const size = 960;
  const world = worldAt(300, size, 1, 0);
  const scroll = scrollToKeep(world, 300, size, 2);
  assert.ok(Math.abs(screenAt(world, size, 2, scroll) - 300) < 1e-9);
  assert.equal(clampScroll(10_000, size, 2), 240);
  assert.equal(clampScroll(-10_000, size, 2), -240);
  assert.equal(clampScroll(50, size, 1), 0);
});

test('coordinates label every cell on small maps and every fifth cell on large ones', () => {
  assert.equal(coordinateStep(70), 1);
  assert.equal(coordinateStep(17), 5);
});
