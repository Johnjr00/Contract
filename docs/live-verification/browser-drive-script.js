// Drives the REAL, unmodified controller.html/.css/.js served by the REAL ContractServer,
// using two real headless Chromium tabs as the two phones. No test harness, no simulation —
// this is the exact client code that ships in the APK, talking to the exact server code that
// ships in the APK, over a real WebSocket.
const { chromium } = require('playwright');

const JOIN_URL = process.argv[2];
if (!JOIN_URL) { console.error('usage: node drive.js <joinUrl>'); process.exit(1); }

function log(who, msg) { console.log(`[${who}] ${msg}`); }

async function waitForHeading(page, timeout = 15000) {
  await page.waitForFunction(() => {
    const h = document.getElementById('heading');
    return h && h.textContent && h.textContent.trim().length > 0;
  }, { timeout });
  return page.$eval('#heading', el => el.textContent);
}

async function clickChoice(page, idPrefix) {
  const btn = await page.$(`.choices .btn`);
  const buttons = await page.$$('.choices .btn');
  for (const b of buttons) {
    const text = (await b.textContent()) || '';
  }
}

(async () => {
  const browser = await chromium.launch({
    headless: true,
    executablePath: '/opt/pw-browsers/chromium',
    args: ['--no-sandbox', '--disable-dev-shm-usage']
  });

  const ctx1 = await browser.newContext();
  const ctx2 = await browser.newContext();
  const ctx3 = await browser.newContext();
  const p1 = await ctx1.newPage();
  const p2 = await ctx2.newPage();
  const p3 = await ctx3.newPage();

  p1.on('console', m => { if (m.type() === 'error') log('P1-console-error', m.text()); });
  p2.on('console', m => { if (m.type() === 'error') log('P2-console-error', m.text()); });
  p1.on('pageerror', e => log('P1-PAGE-ERROR', e.message));
  p2.on('pageerror', e => log('P2-PAGE-ERROR', e.message));
  p1.on('requestfailed', r => log('P1-req-failed', `${r.url()} ${r.failure()?.errorText}`));
  p1.on('response', r => { if (r.status() >= 400) log('P1-bad-response', `${r.status()} ${r.url()}`); });

  log('P1', 'opening join URL...');
  await p1.goto(JOIN_URL, { waitUntil: 'domcontentloaded' });
  let h1 = await waitForHeading(p1);
  log('P1', `heading: "${h1}"`);
  await p1.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/01-p1-setup.png' });

  log('P2', 'opening join URL (should become Player 2 and wait)...');
  await p2.goto(JOIN_URL, { waitUntil: 'domcontentloaded' });
  let h2 = await waitForHeading(p2);
  log('P2', `heading: "${h2}"`);
  await p2.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/02-p2-waiting.png' });

  log('P3', 'opening join URL as a THIRD device (must be refused)...');
  await p3.goto(JOIN_URL, { waitUntil: 'domcontentloaded' });
  await p3.waitForTimeout(2000);
  let h3 = await p3.$eval('#heading', el => el.textContent).catch(() => '(no heading)');
  let b3 = await p3.$eval('#body', el => el.textContent).catch(() => '(no body)');
  log('P3', `heading: "${h3}" body: "${b3}"`);
  await p3.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/03-p3-refused.png' });

  // ---- Player 1: fill in the setup form and submit ----
  log('P1', 'checking for the setup form...');
  await p1.waitForSelector('.field input[type="text"]', { timeout: 10000 });
  const nameInputs = await p1.$$('.field input[type="text"]');
  log('P1', `found ${nameInputs.length} text inputs in the setup form`);
  await nameInputs[0].fill('Marcus');
  await nameInputs[1].fill('Dan');
  // stop word is the 3rd text input per the DSL order
  if (nameInputs[2]) await nameInputs[2].fill('pineapple');

  // Check some equipment boxes so later steps have something to offer.
  const checks = await p1.$$('.checks .check input[type="checkbox"]');
  log('P1', `found ${checks.length} equipment/boundary checkboxes`);
  for (let i = 0; i < Math.min(10, checks.length); i++) {
    await checks[i].check();
  }

  await p1.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/04-p1-setup-filled.png', fullPage: true });

  const submitBtn = await p1.$('button:has-text("Save setup and open the profiles")');
  if (!submitBtn) throw new Error('submit setup button not found');
  await submitBtn.click();
  log('P1', 'submitted setup');

  await p1.waitForTimeout(1500);
  h1 = await p1.$eval('#heading', el => el.textContent);
  log('P1', `after submit, heading: "${h1}"`);
  h2 = await p2.$eval('#heading', el => el.textContent);
  log('P2', `after P1 submit, heading: "${h2}"`);

  await p1.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/05-p1-profile.png', fullPage: true });
  await p2.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/06-p2-profile.png', fullPage: true });

  // ---- Both players: bulk-answer their private profile to Yes, then save+finish ----
  // Uses Playwright locators (re-query at click time) rather than cached ElementHandles,
  // because a server broadcast can trigger a full re-render between query and click.
  for (const [tag, page] of [['P1', p1], ['P2', p2]]) {
    log(tag, 'bulk-setting profile to Everything Yes...');
    await page.locator('button:has-text("Everything Yes")').click({ timeout: 8000 });
    await page.waitForTimeout(300);
    await page.locator('button:has-text("Save and finish")').click({ timeout: 8000 });
    log(tag, 'saved and finished profile');
  }

  await p1.waitForTimeout(2000);
  h1 = await p1.$eval('#heading', el => el.textContent);
  h2 = await p2.$eval('#heading', el => el.textContent);
  log('P1', `after profiles, heading: "${h1}"`);
  log('P2', `after profiles, heading: "${h2}"`);

  await p1.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/07-p1-proposal.png', fullPage: true });
  await p2.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/08-p2-proposal.png', fullPage: true });

  const p1Instruction = await p1.$eval('.instruction', el => el.textContent).catch(() => '(none)');
  log('P1', `proposed term instruction: "${p1Instruction}"`);

  // ---- Both sign the first proposal ----
  for (const [tag, page] of [['P1', p1], ['P2', p2]]) {
    await page.locator('button:has-text("Sign it")').click({ timeout: 8000 });
    log(tag, 'signed the proposal');
  }

  await p1.waitForTimeout(2000);
  h1 = await p1.$eval('#heading', el => el.textContent);
  h2 = await p2.$eval('#heading', el => el.textContent);
  log('P1', `after both sign, heading: "${h1}"`);
  log('P2', `after both sign, heading: "${h2}"`);
  await p1.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/09-p1-consideration.png', fullPage: true });
  await p2.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/10-p2-consideration.png', fullPage: true });

  // ---- Reload P2 mid-game to prove reconnection works in a REAL browser (localStorage) ----
  log('P2', 'reloading the page (simulates phone lock / browser close+reopen)...');
  await p2.reload({ waitUntil: 'domcontentloaded' });
  await p2.waitForTimeout(2000);
  h2 = await p2.$eval('#heading', el => el.textContent).catch(() => '(none)');
  log('P2', `after reload, heading: "${h2}"`);
  await p2.screenshot({ path: '/tmp/claude-0/-home-user-Contract/85540b27-2b04-5ea3-8bfe-239c8f6db8a3/scratchpad/pw/11-p2-after-reload.png', fullPage: true });

  await browser.close();
  console.log('DONE');
})().catch(e => { console.error('SCRIPT_ERROR', e); process.exit(1); });
