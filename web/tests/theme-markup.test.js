const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..');
const html = fs.readFileSync(path.join(root, 'index.html'), 'utf8');
const css = fs.readFileSync(path.join(root, 'style.css'), 'utf8');
const js = fs.readFileSync(path.join(root, 'app.js'), 'utf8');

test('Neon Club backdrop markup is separated cleanly from the page header', () => {
  assert.match(html, /<div class="club-backdrop" aria-hidden="true">[\s\S]*?<\/div>\s*<header class="topbar">/);
  assert.doesNotMatch(html, /\\\\n<header/);
});

test('background controls and muted inline video hooks exist', () => {
  for (const id of ['club-video', 'club-background-file', 'club-background-reset', 'club-motion']) {
    assert.match(html, new RegExp('id="' + id + '"'));
  }
  assert.match(html, /<video[^>]*autoplay[^>]*muted[^>]*loop[^>]*playsinline/);
  for (const hook of ['club-background-file', 'club-video', 'club-background-reset', 'club-motion']) {
    assert.ok(js.includes("getElementById('" + hook + "')"), 'missing JS hook: ' + hook);
  }
});

test('background never intercepts workspace input and reduced motion disables backdrop animation', () => {
  assert.match(css, /\.club-backdrop\s*\{[^}]*pointer-events\s*:\s*none/s);
  assert.match(css, /@media\s*\(prefers-reduced-motion:\s*reduce\)[\s\S]*?\.club-backdrop video\s*\{\s*display:\s*none\s*\}/);
});
