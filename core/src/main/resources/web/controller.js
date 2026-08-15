/*
 * The Contract — phone controller.
 *
 * An ordinary browser page: no service worker, no install prompt, no manifest, no native shell.
 * It never mutates game state; it renders whatever the TV server sends and posts actions back.
 *
 * Reconnection contract (specification section 8):
 *   - a device id and a resume token live in localStorage
 *   - HELLO carries both, so a refresh, a lock, a dropped Wi-Fi connection or a closed tab all
 *     return this phone to its own player slot
 *   - a full state snapshot arrives after every (re)connection
 *   - every action carries a unique id and the expected state version, so retries are safe
 */
(function () {
  "use strict";

  var KEY_DEVICE = "thecontract.deviceId";
  var KEY_RESUME = "thecontract.resume";

  var S = {
    token: null,
    deviceId: null,
    resume: null,          // { sessionId, slot, token }
    slot: null,
    version: 0,
    view: null,
    ws: null,
    retry: 0,
    retryTimer: null,
    pingTimer: null,
    tickTimer: null,
    timerAnchor: 0,
    profileDraft: null,
    setupDraft: null,
    setupRevision: null,   // the revision the draft was built from; see setupDraft()
    setupPanel: null,      // null | "presets" | "save"
    setupPresetName: "",
    setupConfirm: null,    // { kind: "overwrite" | "delete", id: <preset id> }
    reclaim: null,
    pendingActions: []
  };

  // ------------------------------------------------------------------ helpers

  function $(id) { return document.getElementById(id); }

  function el(tag, cls, text) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (text !== undefined && text !== null) n.textContent = text;
    return n;
  }

  function uuid() {
    if (window.crypto && crypto.randomUUID) return crypto.randomUUID();
    var b = new Uint8Array(16);
    (window.crypto || {}).getRandomValues ? crypto.getRandomValues(b) : b.forEach(function (_, i) { b[i] = Math.random() * 256; });
    return Array.prototype.map.call(b, function (x) { return ("0" + x.toString(16)).slice(-2); }).join("");
  }

  function load(key) { try { return localStorage.getItem(key); } catch (e) { return null; } }
  function save(key, value) { try { localStorage.setItem(key, value); } catch (e) { /* private mode */ } }

  function toast(message) {
    var t = $("toast");
    t.textContent = message;
    t.hidden = false;
    clearTimeout(toast._t);
    toast._t = setTimeout(function () { t.hidden = true; }, 3800);
  }

  function fmtClock(ms) {
    var total = Math.max(0, Math.ceil(ms / 1000));
    var m = Math.floor(total / 60);
    var s = total % 60;
    return m + ":" + (s < 10 ? "0" : "") + s;
  }

  // ------------------------------------------------------------------ transport

  function tokenFromLocation() {
    var m = location.pathname.match(/\/join\/([^\/?#]+)/);
    return m ? decodeURIComponent(m[1]) : null;
  }

  function connect() {
    if (S.ws && (S.ws.readyState === 0 || S.ws.readyState === 1)) return;
    setLink("Connecting", "pill-warn");
    var proto = location.protocol === "https:" ? "wss:" : "ws:";
    var url = proto + "//" + location.host + "/ws?token=" + encodeURIComponent(S.token || "");
    var ws;
    try {
      ws = new WebSocket(url);
    } catch (e) {
      scheduleRetry();
      return;
    }
    S.ws = ws;

    ws.onopen = function () {
      S.retry = 0;
      setLink("Connected", "pill-ok");
      var hello = { type: "HELLO", deviceId: S.deviceId };
      if (S.resume && S.resume.token) hello.resumeToken = S.resume.token;
      send(hello);
      startPing();
      flushPending();
    };

    ws.onmessage = function (event) {
      var msg;
      try { msg = JSON.parse(event.data); } catch (e) { return; }
      handle(msg);
    };

    ws.onclose = function () {
      setLink("Reconnecting", "pill-warn");
      stopPing();
      scheduleRetry();
    };

    ws.onerror = function () { /* onclose follows */ };
  }

  function scheduleRetry() {
    clearTimeout(S.retryTimer);
    var delay = Math.min(8000, 400 * Math.pow(2, S.retry++));
    S.retryTimer = setTimeout(connect, delay);
  }

  function send(obj) {
    if (!S.ws || S.ws.readyState !== 1) return false;
    S.ws.send(JSON.stringify(obj));
    return true;
  }

  /**
   * Resends every action still awaiting a reply. Safe to call on every reconnect: each envelope
   * keeps its original actionId, and the server already deduplicates by actionId and rejects a
   * stale expectedVersion, so a resend can only be a harmless no-op if the original somehow did
   * get through, or applied exactly once if it did not. An envelope is removed from the queue
   * only once its ACTION_ACCEPTED/ACTION_REJECTED reply arrives (see handle()), not merely
   * because send() returned true -- a send can succeed locally and still never reach the server
   * if the connection drops in the window before the reply comes back.
   */
  function flushPending() {
    S.pendingActions.forEach(function (envelope) { send(envelope); });
  }

  function removePending(actionId) {
    S.pendingActions = S.pendingActions.filter(function (envelope) { return envelope.actionId !== actionId; });
  }

  function act(action) {
    var envelope = { type: "PLAYER_ACTION", actionId: uuid(), expectedVersion: S.version, action: action };
    S.pendingActions.push(envelope);
    if (!send(envelope)) {
      // A brief reconnect (a real one happens even on a healthy local network) must not silently
      // drop the tap that triggered it -- it stays queued and flushPending() resends it once
      // reconnected.
      toast("Not connected. Will send again once reconnected.");
      connect();
    }
  }

  /**
   * Application-level heartbeat. The server also pings this socket on its own schedule, and the
   * browser answers those automatically without waking any page script, so this is the second
   * line of defence rather than the only one -- which matters because mobile browsers throttle
   * setInterval hard once the tab is backgrounded or the screen locks. The interval must stay
   * comfortably inside the server's socket read timeout (ContractServer.SOCKET_TIMEOUT_MS).
   */
  function startPing() {
    stopPing();
    S.pingTimer = setInterval(function () { send({ type: "PING", t: Date.now() }); }, 10000);
  }
  function stopPing() { clearInterval(S.pingTimer); }

  function setLink(text, cls) {
    var n = $("link");
    n.textContent = text;
    n.className = "pill " + (cls || "");
  }

  // ------------------------------------------------------------------ inbound

  function handle(msg) {
    switch (msg.type) {
      case "HELLO_OK":
        S.slot = msg.slot;
        S.version = msg.version;
        S.resume = { sessionId: msg.sessionId, slot: msg.slot, token: msg.resumeToken };
        save(KEY_RESUME, JSON.stringify(S.resume));
        S.reclaim = null;
        if (msg.reclaimed) toast("Slot confirmed from the TV.");
        break;

      case "SESSION_FULL":
        S.view = null;
        renderMessage("Session full", msg.message +
          " This game allows exactly two phones. If one of them is yours, reconnect from that phone " +
          "or release the slot with the TV remote.");
        return;

      case "RECLAIM_PENDING":
        S.reclaim = msg;
        renderMessage("Waiting for the TV", msg.message);
        return;

      case "STATE_SNAPSHOT":
      case "STATE_CHANGED":
        S.view = msg.view;
        S.version = msg.view.version;
        S.timerAnchor = Date.now();
        if (S.view.phase !== "PRIVATE_PROFILES" && S.view.phase !== "WAITING_FOR_PROFILES") S.profileDraft = null;
        if (S.view.phase !== "PLAYER_1_SETUP") {
          S.setupDraft = null;
          S.setupPanel = null;
          S.setupConfirm = null;
          S.setupPresetName = "";
        }
        render();
        return;

      case "TIMER_UPDATE":
        if (S.view) {
          S.view.timers = msg.timers;
          S.version = msg.version;
          S.timerAnchor = Date.now();
          paintTimers();
        }
        return;

      case "ACTION_ACCEPTED":
        S.version = msg.version;
        removePending(msg.actionId);
        return;

      case "ACTION_REJECTED":
        S.version = msg.version;
        removePending(msg.actionId);
        if (msg.code === "STALE_VERSION") {
          toast("That screen had moved on. Showing the current one.");
        } else if (msg.code !== "DUPLICATE") {
          toast(msg.message || "That is not available right now.");
        }
        return;

      case "ERROR":
        toast(msg.message || "Something went wrong.");
        return;

      case "PONG":
        return;
    }
  }

  // ------------------------------------------------------------------ rendering

  function renderMessage(heading, body) {
    var main = $("main");
    main.textContent = "";
    var card = el("section", "card");
    var headingEl = el("h1", null, heading);
    headingEl.id = "heading";
    var bodyEl = el("p", "body", body);
    bodyEl.id = "body";
    card.appendChild(headingEl);
    card.appendChild(bodyEl);
    main.appendChild(card);
  }

  // ------------------------------------------------------------------ choice help

  /*
   * Two of the four answers to a proposal are not self-explanatory, and a player who does not
   * understand them simply never uses them. Each gets a "?" beside it that opens a bubble.
   * Plain words only: this is read by someone mid-scene, not studying.
   */
  var HELP = {
    counteroffer:
      "Changes this term instead of killing it. You pick one change to it \u2014 gentler, " +
      "shorter, roles swapped, no toys \u2014 and then you both vote on the new version. " +
      "If you both say yes it goes in the contract. If either of you says no, the term is " +
      "gone for good.",
    bundle:
      "Puts a second term in alongside this one, and you pick that second term yourself from " +
      "a short list \u2014 everywhere else the game chooses for you. Both go in together or " +
      "neither does, it uses two of your term slots, and a single consideration pays for the " +
      "pair instead of two. The other player has to agree to it."
  };

  var openHelp = null;       // id of the choice whose bubble is showing
  var openHelpKey = "";      // what was on screen when it was opened
  var helpJustOpened = false; // scroll it clear of the Pause bar, but only on the opening tap

  /* A new proposal, or any new screen, closes a bubble left open on the last one. */
  function helpKey(v) {
    return (v.phase || "") + "|" + ((v.term && v.term.termId) || "");
  }

  function closeHelp() {
    if (openHelp === null) return false;
    openHelp = null;
    render();
    return true;
  }

  function render() {
    var v = S.view;
    if (!v) return;

    if (helpKey(v) !== openHelpKey) { openHelp = null; openHelpKey = helpKey(v); }

    $("who").textContent = (v.names && v.names[v.slot] ? v.names[v.slot] : v.slot === "PLAYER_1" ? "Player 1" : "Player 2") +
      (v.roles && v.roles[v.slot] ? " · " + (v.roles[v.slot] === "DOMINANT" ? "Dominant" : "submissive") : "");

    var other = v.slot === "PLAYER_1" ? "PLAYER_2" : "PLAYER_1";
    var otherState = v.connections ? v.connections[other] : null;
    var peer = $("peer");
    if (otherState === "CONNECTED") { peer.textContent = "Other phone: connected"; peer.className = "pill pill-ok"; }
    else if (otherState === "DISCONNECTED") { peer.textContent = "Other phone: offline"; peer.className = "pill pill-bad"; }
    else { peer.textContent = "Other phone: not joined"; peer.className = "pill"; }

    $("progress").textContent = v.progress
      ? "Act " + v.progress.act + " · " + v.progress.actTitle + " · " +
        v.progress.signedRegular + "/" + v.progress.requiredRegular + " terms" +
        (v.progress.signedClosing ? " · " + v.progress.signedClosing + "/2 closing" : "")
      : "";

    var main = $("main");
    main.textContent = "";

    var head = el("section", "card");
    var headingEl = el("h1", null, v.heading || "");
    headingEl.id = "heading";
    head.appendChild(headingEl);
    if (v.body) {
      var bodyEl = el("p", "body", v.body);
      bodyEl.id = "body";
      head.appendChild(bodyEl);
    }
    if (v.waiting) {
      var w = el("div", "waiting");
      w.appendChild(el("span", "dot"));
      w.appendChild(el("span", null, "Waiting…"));
      head.appendChild(w);
    }
    main.appendChild(head);

    if (v.blockedNotice) {
      var nb = el("section", "card quiet");
      nb.appendChild(el("div", "notice", v.blockedNotice));
      main.appendChild(nb);
    }

    if (v.setup) main.appendChild(renderSetup(v.setup));
    if (v.profile) main.appendChild(renderProfile(v.profile));
    if (v.term) main.appendChild(renderTerm(v.term, termLabel(v)));
    if (v.bundledTerm) main.appendChild(renderTerm(v.bundledTerm, "Second term in the trade"));
    if (v.termOptions && v.termOptions.length) {
      v.termOptions.forEach(function (t) { main.appendChild(renderTerm(t, "Option")); });
    }
    if (v.consideration) main.appendChild(renderConsideration(v.consideration));
    if (v.considerationOptions && v.considerationOptions.length) {
      v.considerationOptions.forEach(function (c) { main.appendChild(renderConsideration(c, true)); });
    }
    if (v.execution) main.appendChild(renderExecution(v.execution));
    if (v.timers && v.timers.length) main.appendChild(renderTimers(v));
    if (v.draft) main.appendChild(renderDraft(v.draft));

    if (v.choices && v.choices.length) {
      var box = el("section", "card");
      var list = el("div", "choices");
      v.choices.forEach(function (c) {
        if (c.kind === "status") { list.appendChild(el("div", "tag", c.label)); return; }
        var b = el("button", "btn" + (c.danger ? " btn-danger" : c.kind === "nav" ? " btn-nav" : " btn-primary"));
        b.type = "button";
        b.appendChild(document.createTextNode(c.label));
        if (c.detail) b.appendChild(el("small", null, c.detail));
        b.addEventListener("click", function () { dispatchChoice(c.id); });

        if (!HELP[c.id]) { list.appendChild(b); return; }

        var wrap = el("div", "choice-help");
        var row = el("div", "choice-row");
        row.appendChild(b);

        var q = el("button", "help-btn", "?");
        q.type = "button";
        q.setAttribute("aria-label", "What does \u201c" + c.label + "\u201d do?");
        q.setAttribute("aria-expanded", openHelp === c.id ? "true" : "false");
        // Without this the document handler below would close the bubble in the same tap.
        q.addEventListener("click", function (ev) {
          ev.stopPropagation();
          openHelp = openHelp === c.id ? null : c.id;
          helpJustOpened = openHelp !== null;
          render();
        });
        row.appendChild(q);
        wrap.appendChild(row);

        if (openHelp === c.id) {
          var bubble = el("div", "bubble", HELP[c.id]);
          bubble.setAttribute("role", "note");
          bubble.addEventListener("click", function (ev) { ev.stopPropagation(); });
          wrap.appendChild(bubble);
          // The Pause bar is stuck over the bottom of the screen and can cover the last line of
          // a bubble opened near it. scrollIntoView will not help — as far as it is concerned
          // the bubble is already on screen — so measure the overlap and scroll by exactly that.
          if (helpJustOpened) {
            helpJustOpened = false;
            setTimeout(function () {
              var foot = document.getElementById("foot");
              var covered = foot ? foot.getBoundingClientRect().height : 0;
              var over = bubble.getBoundingClientRect().bottom - (window.innerHeight - covered - 8);
              if (over > 0) window.scrollBy(0, over);
            }, 0);
          }
        }
        list.appendChild(wrap);
      });
      box.appendChild(list);
      main.appendChild(box);
    }

    if (v.canGoBack && !(v.choices || []).some(function (c) { return c.id === "back"; })) {
      var backBox = el("section", "card");
      var back = el("button", "btn btn-nav", "Back");
      back.type = "button";
      back.addEventListener("click", function () { act({ type: "back" }); });
      backBox.appendChild(back);
      main.appendChild(backBox);
    }

    if (v.stopWord) {
      var sw = el("p", "meta");
      sw.appendChild(document.createTextNode("Stop word: "));
      sw.appendChild(el("strong", null, v.stopWord));
      main.appendChild(sw);
    }

    paintTimers();
  }

  /*
   * What the term card is called depends on where the game is. It said "Proposed term" on every
   * screen that carried one, including the screen that had just announced it signed and every
   * screen of the final scene, where nothing is being proposed to anybody.
   */
  function termLabel(v) {
    if (v.execution) return "Term in the scene";
    switch (v.phase) {
      case "TERM_SIGNED":
        return "Signed term";
      case "CONSIDERATION_PRIVATE_SELECTION":
      case "CLOSING_TERM_CONSIDERATION":
      case "CONSIDERATION_PUBLIC_EXECUTION":
      case "WAITING_FOR_SIGNATURE_CONFIRMATION":
        return "The term being earned";
      default:
        return "Proposed term";
    }
  }

  /**
   * Labelled suggestion lists for an instruction that tells a man to do something without
   * telling him what — things he could say, positions he could use. Suggestions, not orders;
   * the heading says so, because a list under an instruction otherwise reads as part of it.
   */
  function appendSuggestions(card, lists) {
    (lists || []).forEach(function (group) {
      if (!group || !group.items || !group.items.length) return;
      var box = el("div", "examples");
      box.appendChild(el("div", "examples-head", group.heading));
      var list = el("ul", "examples-list");
      group.items.forEach(function (line) { list.appendChild(el("li", null, line)); });
      box.appendChild(list);
      card.appendChild(box);
    });
  }

  function renderTerm(t, label) {
    var card = el("section", "card");
    card.appendChild(el("h3", null, label + " · " + t.actTitle + (t.climax ? " · closing term" : "")));
    card.appendChild(el("h2", null, t.title));
    if (t.instruction) card.appendChild(el("p", "instruction", t.instruction));

    if (t.equipment && t.equipment.length) {
      var eq = el("div");
      t.equipment.forEach(function (e) { eq.appendChild(el("span", "tag", e)); });
      card.appendChild(eq);
    }
    appendSuggestions(card, t.suggestions);
    (t.conditions || []).forEach(function (c) { card.appendChild(el("div", "notice", "Condition: " + c)); });
    (t.amendments || []).forEach(function (a) { card.appendChild(el("div", "notice", "Amendment: " + a)); });

    if (t.timers && t.timers.length) {
      card.appendChild(el("p", "meta", timingLine(t.timers)));
    }

    if (t.benefitExplanation) {
      card.appendChild(el("p", "meta", t.benefitExplanation));
    }
    return card;
  }

  /** "Timed: Left ear 0:45 · Right ear 0:45 · 1:30 in total" */
  function timingLine(timers) {
    var parts = timers.map(function (t) { return t.label + " " + fmtClock(t.totalSeconds * 1000); });
    var total = timers.reduce(function (a, t) { return a + t.totalSeconds; }, 0);
    var line = "Timed: " + parts.join(" · ");
    if (timers.length > 1) line += " · " + fmtClock(total * 1000) + " in total";
    return line;
  }

  function renderConsideration(c, selectable) {
    var card = el("section", "card quiet");
    card.appendChild(el("h3", null, selectable ? "Consideration option" : "Consideration"));
    card.appendChild(el("h2", null, c.title));
    card.appendChild(el("p", "instruction", c.instruction));
    appendSuggestions(card, c.suggestions);
    if (c.equipment && c.equipment.length) {
      var eq = el("div");
      c.equipment.forEach(function (e) { eq.appendChild(el("span", "tag", e)); });
      card.appendChild(eq);
    }
    // How long it runs, on the selection screen as well as during it: choosing between
    // consideration actions without knowing whether one is forty-five seconds or four minutes
    // is choosing blind.
    if (c.timers && c.timers.length) {
      card.appendChild(el("p", "meta", timingLine(c.timers)));
    }
    var who = c.mutual
      ? "Both of you perform this and both of you confirm."
      : c.performerName + " performs this for " + c.recipientName + ".";
    card.appendChild(el("p", "meta", who));
    if (selectable) {
      var b = el("button", "btn btn-primary", "Choose this");
      b.type = "button";
      b.addEventListener("click", function () { act({ type: "pick_consideration", actionId: c.actionId }); });
      card.appendChild(b);
    }
    return card;
  }

  function renderExecution(x) {
    var card = el("section", "card quiet");
    card.appendChild(el("h3", null, "Final scene"));
    card.appendChild(el("p", "meta", "Term " + x.stepIndex + " of " + x.stepCount +
      (x.started ? " · running" : " · not started")));
    return card;
  }

  function renderTimers(v) {
    var card = el("section", "card");
    card.id = "timers";
    card.appendChild(el("h3", null, "Timers"));
    var control = v.timerControl || { mayControl: true, mayComplete: true, showStopAll: false };

    v.timers.forEach(function (t) {
      var box = el("div", "timer " + t.state.toLowerCase());
      box.dataset.timerId = t.id;

      var head = el("div", "timer-head");
      head.appendChild(el("span", "timer-label", t.label));
      var clock = el("span", "timer-clock", fmtClock(t.remainingMs));
      clock.dataset.role = "clock";
      head.appendChild(clock);
      box.appendChild(head);

      var bar = el("div", "timer-bar");
      var fill = el("span");
      fill.dataset.role = "bar";
      fill.style.width = Math.max(0, Math.min(100, (t.remainingMs / (t.totalSeconds * 1000)) * 100)) + "%";
      bar.appendChild(fill);
      box.appendChild(bar);

      if (control.mayControl) {
        var row = el("div", "btn-row");
        [["start", "Start"], ["pause", "Pause"], ["resume", "Resume"], ["reset", "Reset"]].forEach(function (pair) {
          var b = el("button", "btn", pair[1]);
          b.type = "button";
          b.addEventListener("click", function () {
            act({ type: "timer", timerId: t.id, command: pair[0] });
          });
          row.appendChild(b);
        });
        box.appendChild(row);
      } else {
        box.appendChild(el("p", "meta", (control.controllerName || "The other player") + " controls this timer."));
      }
      card.appendChild(box);
    });

    if (control.showStopAll && control.mayControl) {
      var stop = el("button", "btn btn-danger", "Stop all timers");
      stop.type = "button";
      stop.style.marginTop = "12px";
      stop.addEventListener("click", function () { act({ type: "stop_all_timers" }); });
      card.appendChild(stop);
    }
    return card;
  }

  function paintTimers() {
    var v = S.view;
    if (!v || !v.timers) return;
    var elapsed = Date.now() - S.timerAnchor;
    v.timers.forEach(function (t) {
      var box = document.querySelector('[data-timer-id="' + CSS.escape(t.id) + '"]');
      if (!box) return;
      var remaining = t.state === "RUNNING" ? Math.max(0, t.remainingMs - elapsed) : t.remainingMs;
      var clock = box.querySelector('[data-role="clock"]');
      if (clock) clock.textContent = fmtClock(remaining);
      var bar = box.querySelector('[data-role="bar"]');
      if (bar) bar.style.width = Math.max(0, Math.min(100, (remaining / (t.totalSeconds * 1000)) * 100)) + "%";
      box.className = "timer " + t.state.toLowerCase();
    });
  }

  function renderDraft(d) {
    var card = el("section", "card");
    card.appendChild(el("h2", null, "Draft contract"));
    card.appendChild(el("p", "meta",
      d.signedRegular + " of " + d.requiredRegular + " regular terms · " +
      d.signedClosing + " of 2 closing terms · " + d.receiptsCompleted + " consideration receipts"));
    Object.keys(d.byAct).forEach(function (act) {
      card.appendChild(el("h3", null, act));
      d.byAct[act].forEach(function (t) {
        var box = el("div", "draft-term");
        box.appendChild(el("div", "num", "Term " + t.index + (t.closingForName ? " · closing for " + t.closingForName : "")));
        box.appendChild(el("h2", null, t.title));
        box.appendChild(el("p", "instruction", t.instruction));
        if (t.bundledInstruction) {
          box.appendChild(el("div", "num", "Traded with"));
          box.appendChild(el("p", "instruction", t.bundledInstruction));
        }
        appendSuggestions(box, t.suggestions);
        (t.conditions || []).forEach(function (c) { box.appendChild(el("span", "tag", "Condition: " + c)); });
        (t.amendments || []).forEach(function (a) { box.appendChild(el("span", "tag", "Amendment: " + a)); });
        if (t.considerationTitle) {
          box.appendChild(el("p", "meta", "Consideration performed: " + t.considerationTitle));
        }
        box.appendChild(el("p", "meta", "Signed by " + (t.signedBy || []).join(" and ")));
        card.appendChild(box);
      });
    });
    return card;
  }

  // ------------------------------------------------------------------ profile

  function profileDraft(form) {
    if (!S.profileDraft) {
      S.profileDraft = { answers: {}, conditions: {} };
      form.sections.forEach(function (sec) {
        sec.items.forEach(function (item) {
          S.profileDraft.answers[item.id] = item.answer;
          if (item.condition) S.profileDraft.conditions[item.id] = item.condition;
        });
      });
    }
    return S.profileDraft;
  }

  function renderProfile(form) {
    var draft = profileDraft(form);
    var card = el("section", "card");
    card.appendChild(el("h2", null, "Private profile"));
    card.appendChild(el("p", "meta",
      "Everything starts at Maybe. A Maybe is not a no — it is a yes with the condition you pick."));

    var global = el("div", "btn-row");
    [["YES", "Everything Yes"], ["MAYBE", "Everything Maybe"], ["NO", "Everything No"]].forEach(function (p) {
      var b = el("button", "btn", p[1]);
      b.type = "button";
      b.addEventListener("click", function () { bulk(form, null, p[0]); });
      global.appendChild(b);
    });
    card.appendChild(global);

    form.sections.forEach(function (sec) {
      var det = el("details", "section");
      var sum = el("summary");
      sum.appendChild(document.createTextNode(sec.title));
      var count = el("span", "tag", countFor(sec, draft));
      count.dataset.role = "count";
      count.dataset.section = sec.id;
      sum.appendChild(count);
      det.appendChild(sum);

      var body = el("div", "section-body");
      var row = el("div", "btn-row");
      [["YES", "All Yes"], ["MAYBE", "All Maybe"], ["NO", "All No"]].forEach(function (p) {
        var b = el("button", "btn", p[1]);
        b.type = "button";
        b.addEventListener("click", function (e) { e.preventDefault(); bulk(form, sec.id, p[0]); });
        row.appendChild(b);
      });
      body.appendChild(row);

      sec.items.forEach(function (item) {
        body.appendChild(renderPref(form, item, draft));
      });
      det.appendChild(body);
      card.appendChild(det);
    });

    var save = el("button", "btn btn-nav", "Save my answers");
    save.type = "button";
    save.addEventListener("click", function () { submitProfile(false); });
    card.appendChild(save);

    var done = el("button", "btn btn-primary", "Save and finish");
    done.type = "button";
    done.style.marginTop = "10px";
    done.addEventListener("click", function () { submitProfile(true); });
    card.appendChild(done);
    return card;
  }

  function countFor(sec, draft) {
    var yes = 0, no = 0;
    sec.items.forEach(function (i) {
      if (draft.answers[i.id] === "YES") yes++;
      if (draft.answers[i.id] === "NO") no++;
    });
    return yes + " yes · " + no + " no";
  }

  function renderPref(form, item, draft) {
    var box = el("div", "pref");
    box.appendChild(el("div", "pref-label", item.label));
    var seg = el("div", "seg");
    [["YES", "Yes", "yes"], ["MAYBE", "Maybe", "maybe"], ["NO", "No", "no"]].forEach(function (p) {
      var b = el("button", p[2], p[1]);
      b.type = "button";
      b.setAttribute("aria-pressed", String(draft.answers[item.id] === p[0]));
      b.addEventListener("click", function (e) {
        e.preventDefault();
        draft.answers[item.id] = p[0];
        Array.prototype.forEach.call(seg.children, function (c) {
          c.setAttribute("aria-pressed", String(c === b));
        });
        cond.hidden = draft.answers[item.id] !== "MAYBE";
        refreshCounts(form, draft);
      });
      seg.appendChild(b);
    });
    box.appendChild(seg);

    var cond = el("div", "cond");
    cond.hidden = draft.answers[item.id] !== "MAYBE";
    var sel = document.createElement("select");
    var def = document.createElement("option");
    def.value = "";
    def.textContent = "Use the session default condition";
    sel.appendChild(def);
    form.conditionOptions.forEach(function (o) {
      var opt = document.createElement("option");
      opt.value = o.id;
      opt.textContent = o.label;
      sel.appendChild(opt);
    });
    sel.value = draft.conditions[item.id] || "";
    sel.addEventListener("change", function () {
      if (sel.value) draft.conditions[item.id] = sel.value;
      else delete draft.conditions[item.id];
    });
    cond.appendChild(sel);
    box.appendChild(cond);
    return box;
  }

  function bulk(form, sectionId, answer) {
    var draft = profileDraft(form);
    form.sections.forEach(function (sec) {
      if (sectionId && sec.id !== sectionId) return;
      sec.items.forEach(function (item) { draft.answers[item.id] = answer; });
    });
    // Collapsed sections are updated too; re-render so every control reflects the change.
    render();
  }

  function refreshCounts(form, draft) {
    form.sections.forEach(function (sec) {
      var n = document.querySelector('[data-role="count"][data-section="' + CSS.escape(sec.id) + '"]');
      if (n) n.textContent = countFor(sec, draft);
    });
  }

  function submitProfile(complete) {
    var draft = S.profileDraft;
    if (!draft) return;
    act({ type: "save_profile", answers: draft.answers, conditions: draft.conditions, complete: !!complete });
    toast(complete ? "Profile saved." : "Answers saved.");
  }

  // ------------------------------------------------------------------ setup

  /**
   * The setup form is edited locally and only sent when it is saved or submitted, so the draft
   * survives every re-render. It is rebuilt when the server says the setup underneath has been
   * replaced -- which is what loading a saved setting does. Without that the fields would keep
   * showing the old values and the load would look like it had done nothing.
   */
  function setupDraft(form) {
    var revision = form.revision || 0;
    if (S.setupDraft && S.setupRevision !== revision) S.setupDraft = null;
    S.setupRevision = revision;
    if (!S.setupDraft) {
      S.setupDraft = {
        player1Name: form.player1Name,
        player2Name: form.player2Name,
        dominantSlot: form.dominantSlot,
        analRoles: Object.assign({}, form.analRoles),
        erectionDifficulty: Object.assign({}, form.erectionDifficulty),
        narrationEnabled: form.narrationEnabled !== false,
        sessionLength: form.sessionLength,
        explicitness: form.explicitness,
        finaleFormat: form.finaleFormat,
        stopWord: form.stopWord,
        defaultMaybeCondition: form.defaultMaybeCondition,
        boundaries: (form.boundaries || []).slice(),
        equipment: (form.equipment || []).slice()
      };
    }
    return S.setupDraft;
  }

  function field(labelText, control) {
    var f = el("div", "field");
    var l = el("label", null, labelText);
    f.appendChild(l);
    f.appendChild(control);
    return f;
  }

  function selectFor(options, value, onChange) {
    var sel = document.createElement("select");
    options.forEach(function (o) {
      var opt = document.createElement("option");
      opt.value = o.id;
      opt.textContent = o.detail ? o.label + " — " + o.detail : o.label;
      sel.appendChild(opt);
    });
    sel.value = value;
    sel.addEventListener("change", function () { onChange(sel.value); });
    return sel;
  }

  function textFor(value, onChange) {
    var i = document.createElement("input");
    i.type = "text";
    i.value = value || "";
    i.autocomplete = "off";
    i.addEventListener("input", function () { onChange(i.value); });
    return i;
  }

  function checkList(options, selected, onToggle) {
    var wrap = el("div", "checks");
    options.forEach(function (o) {
      var lab = el("label", "check");
      var cb = document.createElement("input");
      cb.type = "checkbox";
      cb.checked = selected.indexOf(o.id) >= 0;
      cb.addEventListener("change", function () { onToggle(o.id, cb.checked); });
      lab.appendChild(cb);
      lab.appendChild(el("span", null, o.label));
      wrap.appendChild(lab);
    });
    return wrap;
  }

  // Mirrors SetupPreset.idFor on the server, so the phone can tell a new name from one already
  // in the list and ask before writing over it.
  function presetId(name) {
    var id = (name || "").trim().toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "");
    return id || "preset";
  }

  function fmtDate(ms) {
    try {
      return new Date(ms).toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" });
    } catch (e) {
      return "";
    }
  }

  function setupPanelButton(label, panel, count) {
    var b = el("button", "btn" + (S.setupPanel === panel ? " btn-primary" : ""),
      count === undefined ? label : label + " (" + count + ")");
    b.type = "button";
    b.setAttribute("aria-expanded", S.setupPanel === panel ? "true" : "false");
    b.addEventListener("click", function () {
      S.setupPanel = S.setupPanel === panel ? null : panel;
      S.setupConfirm = null;
      render();
    });
    return b;
  }

  /** The list of saved settings: use one, or delete one. Both need a second tap to take effect. */
  function renderPresetList(form) {
    var wrap = el("div", "presets");
    if (!form.presets || !form.presets.length) {
      wrap.appendChild(el("p", "meta",
        "Nothing saved yet. Set the game up the way you want it, then use Save these settings."));
      return wrap;
    }
    wrap.appendChild(el("p", "meta",
      "Loading one replaces everything on this screen — names, roles, length, wording, stop word, " +
      "boundaries and equipment. You still read it through and submit it yourself."));
    form.presets.forEach(function (p) {
      var row = el("div", "preset");
      var head = el("div", "preset-head");
      head.appendChild(el("span", "preset-name", p.name));
      head.appendChild(el("span", "meta", "Saved " + fmtDate(p.savedAtMs)));
      row.appendChild(head);

      var buttons = el("div", "btn-row");
      var use = el("button", "btn", "Use these settings");
      use.type = "button";
      use.addEventListener("click", function () {
        S.setupPanel = null;
        S.setupConfirm = null;
        act({ type: "load_setup_preset", id: p.id });
        toast("Loaded " + p.name + ".");
      });
      buttons.appendChild(use);

      var confirming = S.setupConfirm && S.setupConfirm.kind === "delete" && S.setupConfirm.id === p.id;
      var del = el("button", "btn btn-danger", confirming ? "Tap again to delete" : "Delete");
      del.type = "button";
      del.addEventListener("click", function () {
        if (confirming) {
          S.setupConfirm = null;
          act({ type: "delete_setup_preset", id: p.id });
          toast("Deleted " + p.name + ".");
        } else {
          S.setupConfirm = { kind: "delete", id: p.id };
          render();
        }
      });
      buttons.appendChild(del);
      row.appendChild(buttons);
      wrap.appendChild(row);
    });
    return wrap;
  }

  /** Naming and saving what is currently on the screen. */
  function renderPresetSave(form, d) {
    var wrap = el("div", "presets");
    var existing = (form.presets || []).filter(function (p) {
      return p.id === presetId(S.setupPresetName);
    })[0];
    var full = (form.presets || []).length >= (form.presetLimit || 0) && !existing;

    wrap.appendChild(field("Name these settings", textFor(S.setupPresetName, function (v) {
      var wasOverwrite = !!existing;
      S.setupPresetName = v;
      S.setupConfirm = null;
      // Re-render only when the button's meaning changes, so the keyboard is not disturbed on
      // every keystroke.
      var isOverwrite = (form.presets || []).some(function (p) { return p.id === presetId(v); });
      if (wasOverwrite !== isOverwrite) render();
    })));

    if (full) {
      wrap.appendChild(el("p", "meta",
        "There is room for " + form.presetLimit + " saved settings. Delete one before saving another."));
      return wrap;
    }

    var confirming = !!(existing && S.setupConfirm && S.setupConfirm.kind === "overwrite" &&
      S.setupConfirm.id === existing.id);
    var label = confirming ? "Tap again to overwrite " + existing.name
      : existing ? "Overwrite " + existing.name
        : "Save these settings";
    var save = el("button", "btn btn-primary", label);
    save.type = "button";
    // Read the name and the confirmation live rather than from the render that built this
    // button: the field can have moved on since without the button needing to be redrawn.
    save.addEventListener("click", function () {
      var name = (S.setupPresetName || "").trim();
      if (!name) { toast("Give these settings a name first."); return; }
      var match = (form.presets || []).filter(function (p) { return p.id === presetId(name); })[0];
      var confirmed = !!(match && S.setupConfirm && S.setupConfirm.kind === "overwrite" &&
        S.setupConfirm.id === match.id);
      if (match && !confirmed) {
        S.setupConfirm = { kind: "overwrite", id: match.id };
        render();
        return;
      }
      S.setupConfirm = null;
      S.setupPanel = null;
      S.setupPresetName = "";
      act({ type: "save_setup_preset", name: name, setup: buildSetup(d) });
      toast("Saved as " + name + ".");
    });
    wrap.appendChild(save);
    wrap.appendChild(el("p", "meta",
      "Everything on this screen is saved, including the hard boundaries and the equipment list."));
    return wrap;
  }

  function renderSetup(form) {
    var d = setupDraft(form);
    var card = el("section", "card");
    card.appendChild(el("h2", null, "Shared setup"));
    card.appendChild(el("p", "meta", "Only you can change these. The other phone waits until you finish."));

    var tools = el("div", "btn-row");
    tools.style.marginTop = "12px";
    tools.appendChild(setupPanelButton("Saved settings", "presets", (form.presets || []).length));
    tools.appendChild(setupPanelButton("Save these settings", "save"));
    card.appendChild(tools);
    if (S.setupPanel === "presets") card.appendChild(renderPresetList(form));
    if (S.setupPanel === "save") card.appendChild(renderPresetSave(form, d));

    card.appendChild(field("Player 1 name (you)", textFor(d.player1Name, function (v) { d.player1Name = v; })));
    card.appendChild(field("Player 2 name", textFor(d.player2Name, function (v) { d.player2Name = v; })));

    card.appendChild(field("Who is Dominant?", selectFor(
      [{ id: "PLAYER_1", label: "Player 1" }, { id: "PLAYER_2", label: "Player 2" }],
      d.dominantSlot, function (v) { d.dominantSlot = v; })));

    card.appendChild(field("Session length", selectFor(form.options.sessionLength, d.sessionLength,
      function (v) { d.sessionLength = v; })));

    card.appendChild(el("h3", null, "Anal roles"));
    ["PLAYER_1", "PLAYER_2"].forEach(function (slot) {
      card.appendChild(field(slot === "PLAYER_1" ? "Player 1" : "Player 2",
        selectFor(form.options.analRole, d.analRoles[slot], function (v) { d.analRoles[slot] = v; })));
      var lab = el("label", "check");
      var cb = document.createElement("input");
      cb.type = "checkbox";
      cb.checked = !!d.erectionDifficulty[slot];
      cb.addEventListener("change", function () { d.erectionDifficulty[slot] = cb.checked; });
      lab.appendChild(cb);
      lab.appendChild(el("span", null, "Has trouble getting or staying hard"));
      card.appendChild(lab);
    });

    card.appendChild(el("h3", null, "Style"));
    card.appendChild(field("Explicitness", selectFor(form.options.explicitness, d.explicitness,
      function (v) { d.explicitness = v; })));
    card.appendChild(field("Finale format", selectFor(form.options.finaleFormat, d.finaleFormat,
      function (v) { d.finaleFormat = v; })));
    card.appendChild(field("Stop word", textFor(d.stopWord, function (v) { d.stopWord = v; })));
    card.appendChild(field("Default condition for a Maybe", selectFor(form.options.maybeCondition,
      d.defaultMaybeCondition, function (v) { d.defaultMaybeCondition = v; })));

    var narr = el("label", "check");
    var ncb = document.createElement("input");
    ncb.type = "checkbox";
    ncb.checked = !!d.narrationEnabled;
    ncb.addEventListener("change", function () { d.narrationEnabled = ncb.checked; });
    narr.appendChild(ncb);
    narr.appendChild(el("span", null, "TV reads instructions out loud"));
    card.appendChild(narr);

    card.appendChild(el("h3", null, "Shared hard boundaries"));
    card.appendChild(checkList(form.options.boundaries, d.boundaries, function (id, on) {
      toggle(d.boundaries, id, on);
    }));

    card.appendChild(el("h3", null, "Equipment you actually have"));
    card.appendChild(checkList(form.options.equipment, d.equipment, function (id, on) {
      toggle(d.equipment, id, on);
    }));

    var submit = el("button", "btn btn-primary", "Save setup and open the profiles");
    submit.type = "button";
    submit.style.marginTop = "14px";
    submit.addEventListener("click", function () { act({ type: "submit_setup", setup: buildSetup(d) }); });
    card.appendChild(submit);
    return card;
  }

  function toggle(list, id, on) {
    var i = list.indexOf(id);
    if (on && i < 0) list.push(id);
    if (!on && i >= 0) list.splice(i, 1);
  }

  function buildSetup(d) {
    return {
      player1: {
        name: d.player1Name,
        role: d.dominantSlot === "PLAYER_1" ? "DOMINANT" : "SUBMISSIVE",
        analRole: d.analRoles.PLAYER_1,
        erectionDifficulty: !!d.erectionDifficulty.PLAYER_1
      },
      player2: {
        name: d.player2Name,
        role: d.dominantSlot === "PLAYER_2" ? "DOMINANT" : "SUBMISSIVE",
        analRole: d.analRoles.PLAYER_2,
        erectionDifficulty: !!d.erectionDifficulty.PLAYER_2
      },
      narrationEnabled: !!d.narrationEnabled,
      sessionLength: d.sessionLength,
      explicitness: d.explicitness,
      finaleFormat: d.finaleFormat,
      stopWord: d.stopWord,
      defaultMaybeCondition: d.defaultMaybeCondition,
      boundaries: d.boundaries,
      equipment: d.equipment
    };
  }

  // ------------------------------------------------------------------ choices

  function dispatchChoice(id) {
    var i = id.indexOf(":");
    var head = i < 0 ? id : id.slice(0, i);
    var arg = i < 0 ? null : id.slice(i + 1);

    switch (head) {
      case "read_again": return act({ type: "read_again" });
      case "sign": return act({ type: "proposal_response", response: "SIGN" });
      case "counteroffer": return act({ type: "proposal_response", response: "COUNTEROFFER" });
      case "bundle": return act({ type: "proposal_response", response: "BUNDLE" });
      case "reject": return act({ type: "proposal_response", response: "REJECT" });
      case "pick_amendment": return act({ type: "pick_amendment", amendment: arg });
      case "vote_amendment": return act({ type: "vote_amendment", amendment: arg });
      case "approve_amended": return act({ type: "approve_amended", approve: arg === "true" });
      case "pick_bundle": return act({ type: "pick_bundle", termId: arg });
      case "vote_bundle": return act({ type: "vote_bundle", approve: arg === "true" });
      case "pick_consideration": return act({ type: "pick_consideration", actionId: arg });
      case "consideration_performed": return act({ type: "consideration_performed" });
      case "confirm_signature": return act({ type: "confirm_signature", earned: arg === "true" });
      case "continue": return act({ type: "continue" });
      case "pick_closing": return act({ type: "pick_closing", termId: arg });
      case "vote_closing": return act({ type: "vote_closing", approve: arg === "true" });
      case "pick_finale_order": return act({ type: "pick_finale_order", order: arg });
      case "exec": return act({ type: "exec", command: arg });
      case "global_pause": return act({ type: "global_pause" });
      case "resume": return act({ type: "resume_vote", confirm: true });
      case "end": return act({ type: "end_session" });
      case "back": return act({ type: "back" });
      case "open_draft": return act({ type: "open_draft" });
      case "close_draft": return act({ type: "close_draft" });
      case "save_contract": return act({ type: "save_contract" });
      case "reopen_profile":
        S.profileDraft = null;
        if (S.view && S.view.profile) S.view.profile.complete = false;
        return render();
      default:
        return;
    }
  }

  // ------------------------------------------------------------------ boot

  function boot() {
    S.token = tokenFromLocation();
    S.deviceId = load(KEY_DEVICE);
    if (!S.deviceId) { S.deviceId = uuid(); save(KEY_DEVICE, S.deviceId); }
    try { S.resume = JSON.parse(load(KEY_RESUME) || "null"); } catch (e) { S.resume = null; }

    $("pauseBtn").addEventListener("click", function () { act({ type: "global_pause" }); });

    // Tapping the screen away from the bubble puts it away. The bubble and its "?" stop the
    // event before it gets here, so only a tap somewhere else counts.
    document.addEventListener("click", function () { closeHelp(); });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" || e.key === "Esc") closeHelp();
    });

    // A phone that comes back from lock or from another app reconnects immediately.
    document.addEventListener("visibilitychange", function () {
      if (!document.hidden) connect();
    });
    window.addEventListener("online", connect);
    window.addEventListener("pageshow", connect);

    S.tickTimer = setInterval(paintTimers, 250);
    connect();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
})();
