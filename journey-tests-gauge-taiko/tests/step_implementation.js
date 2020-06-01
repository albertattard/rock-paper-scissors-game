/* globals gauge*/
"use strict";

const {openBrowser, write, closeBrowser, goto, press, screenshot, text, focus, textBox, toRightOf, below, dropDown, button, click} = require('taiko');
const assert = require("assert");
const headless = process.env.headless_chrome.toLowerCase() === 'true';

beforeSuite(async () => {
  await openBrowser({headless: headless})
});

afterSuite(async () => {
  await closeBrowser();
});

gauge.screenshotFn = async function () {
  return await screenshot({encoding: 'base64'});
};

step("Goto homepage", async () => {
  /* TODO: Should come from property or environment variables */
  await goto('http://localhost:8080/');
});

step("Opponent play <hand>", async (hand) => {
  const waitingForOpponentToPlay = 'Waiting for opponent to play ';
  await text(waitingForOpponentToPlay, below('Rock Paper Scissors')).exists();
  const message = await text(waitingForOpponentToPlay, below('Rock Paper Scissors')).text();
  const code = message.substring(waitingForOpponentToPlay.length,
    waitingForOpponentToPlay.length + 8
  );

  assert.ok(await text(code, below('Active Games')).exists());
  await click(button(hand, toRightOf(text(code, below('Active Games')))))

  assert.ok(await text(code, below('Closed Games')).exists());
});

step("Play <hand> against <opponentType>", async (hand, opponentType) => {
  assert.ok(await dropDown(below('Rock Paper Scissors')).exists());
  await dropDown(below('Rock Paper Scissors')).select(opponentType)
  assert.strictEqual(opponentType, await dropDown(below('Rock Paper Scissors')).value())

  assert.ok(await button(hand, below('Rock Paper Scissors')).exists());
  await click(button(hand, below('Rock Paper Scissors')))
});

step("Page contains <content>", async (content) => {
  assert.ok(await text(content).exists());
});
