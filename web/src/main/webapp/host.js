// Browser glue for Time Maze. The game itself is Java compiled to JavaScript
// by TeaVM (js/timemaze.js); this file owns the DOM: the canvas, touch,
// keyboard and gamepad input, Web Audio and localStorage.
(function () {
  "use strict";

  var stage, view, ctx, frameCanvas, frameCtx, image, pixels;
  var fns = null;
  var sharedTouches = null, sharedAudio = null, sourceRate = 22050;
  var deviceW = 0, deviceH = 0;
  var audioCtx = null, audioNode = null;

  // ------------------------------------------------------------ layout

  function measure() {
    var dpr = window.devicePixelRatio || 1;
    var rect = stage.getBoundingClientRect();
    var w = Math.max(1, Math.round(rect.width * dpr));
    var h = Math.max(1, Math.round(rect.height * dpr));
    if (w === deviceW && h === deviceH) return;
    deviceW = w;
    deviceH = h;
    view.width = w;
    view.height = h;
    view.style.width = rect.width + "px";
    view.style.height = rect.height + "px";
    fns.resize(w, h);
  }

  function present(px, w, h, scale) {
    if (!image || image.width !== w || image.height !== h) {
      frameCanvas.width = w;
      frameCanvas.height = h;
      image = frameCtx.createImageData(w, h);
      pixels = new Uint32Array(image.data.buffer);
    }
    // ARGB (Java) -> ABGR (little-endian RGBA bytes)
    var n = w * h;
    for (var i = 0; i < n; i++) {
      var c = px[i];
      pixels[i] = 0xFF000000 | ((c & 0xFF) << 16) | (c & 0xFF00) | ((c >> 16) & 0xFF);
    }
    frameCtx.putImageData(image, 0, 0);
    ctx.imageSmoothingEnabled = false;
    ctx.fillStyle = "#000";
    ctx.fillRect(0, 0, deviceW, deviceH);
    ctx.drawImage(frameCanvas, 0, 0, Math.round(w * scale), Math.round(h * scale));
  }

  // ------------------------------------------------------------ input

  function toDevice(clientX, clientY) {
    var rect = view.getBoundingClientRect();
    return [
      (clientX - rect.left) * (deviceW / rect.width),
      (clientY - rect.top) * (deviceH / rect.height)
    ];
  }

  function syncTouches(list) {
    var n = Math.min(list.length, 10);
    sharedTouches[0] = n;
    for (var i = 0; i < n; i++) {
      var p = toDevice(list[i].clientX, list[i].clientY);
      sharedTouches[1 + i * 2] = p[0];
      sharedTouches[2 + i * 2] = p[1];
    }
  }

  function onTouchStart(e) {
    e.preventDefault();
    unlockAudio();
    for (var i = 0; i < e.changedTouches.length; i++) {
      var p = toDevice(e.changedTouches[i].clientX, e.changedTouches[i].clientY);
      fns.tap(p[0], p[1]);
    }
    syncTouches(e.touches);
  }

  function onTouchMove(e) {
    e.preventDefault();
    syncTouches(e.touches);
  }

  function onTouchEnd(e) {
    e.preventDefault();
    unlockAudio();
    syncTouches(e.touches);
  }

  var mouseDown = false;

  function onMouseDown(e) {
    unlockAudio();
    mouseDown = true;
    var p = toDevice(e.clientX, e.clientY);
    fns.tap(p[0], p[1]);
    syncTouches([e]);
  }

  function onMouseMove(e) {
    if (mouseDown) syncTouches([e]);
  }

  function onMouseUp() {
    mouseDown = false;
    sharedTouches[0] = 0;
  }

  var GAME_KEYS = {
    ArrowLeft: 1, ArrowRight: 1, ArrowUp: 1, ArrowDown: 1, Space: 1, Enter: 1, Escape: 1, Backspace: 1
  };

  function onKey(e, down) {
    if (down) unlockAudio();
    if (e.repeat) {
      if (GAME_KEYS[e.code]) e.preventDefault();
      return;
    }
    if (GAME_KEYS[e.code]) e.preventDefault();
    fns.key(e.code, down);
  }

  // standard gamepad mapping -> synthetic key codes understood by WebMain
  var PAD_BUTTONS = {0: "GamepadA", 1: "GamepadB", 2: "GamepadX", 3: "GamepadY", 5: "GamepadR1",
    9: "GamepadStart", 12: "GamepadUp", 13: "GamepadDown", 14: "GamepadLeft", 15: "GamepadRight"};
  var padState = {};

  function setPad(code, down) {
    if (!!padState[code] === down) return;
    padState[code] = down;
    if (down) unlockAudio();
    fns.key(code, down);
  }

  function pollGamepads() {
    if (!navigator.getGamepads) return;
    var pads = navigator.getGamepads();
    var held = {};
    for (var p = 0; p < pads.length; p++) {
      var pad = pads[p];
      if (!pad) continue;
      for (var b in PAD_BUTTONS) {
        if (pad.buttons[b] && pad.buttons[b].pressed) held[PAD_BUTTONS[b]] = true;
      }
      if (pad.axes.length >= 2) {
        if (pad.axes[0] < -0.5) held.GamepadLeft = true;
        if (pad.axes[0] > 0.5) held.GamepadRight = true;
        if (pad.axes[1] < -0.5) held.GamepadUp = true;
        if (pad.axes[1] > 0.5) held.GamepadDown = true;
      }
    }
    for (var k in PAD_BUTTONS) setPad(PAD_BUTTONS[k], !!held[PAD_BUTTONS[k]]);
  }

  // ------------------------------------------------------------ audio

  var queue = new Float32Array(16384), MASK = 16383, qStart = 0, qEnd = 0, srcPos = 0, ratio = 1;
  var CHUNK = 512;

  function pull() {
    fns.fill(CHUNK);
    for (var k = 0; k < CHUNK; k++) queue[(qEnd + k) & MASK] = sharedAudio[k] / 32768;
    qEnd += CHUNK;
  }

  function onAudio(e) {
    var out = e.outputBuffer.getChannelData(0);
    for (var i = 0; i < out.length; i++) {
      var i0 = Math.floor(srcPos);
      while (i0 + 1 >= qEnd) pull();
      var f = srcPos - i0;
      var a = queue[i0 & MASK], b = queue[(i0 + 1) & MASK];
      out[i] = a + (b - a) * f;
      srcPos += ratio;
    }
    qStart = Math.floor(srcPos);
  }

  function unlockAudio() {
    var Ctx = window.AudioContext || window.webkitAudioContext;
    if (!Ctx) return;
    try {
      if (!audioCtx) {
        try {
          audioCtx = new Ctx({sampleRate: sourceRate});
        } catch (err) {
          audioCtx = new Ctx();
        }
        ratio = sourceRate / audioCtx.sampleRate;
        audioNode = audioCtx.createScriptProcessor(2048, 0, 1);
        audioNode.onaudioprocess = onAudio;
        audioNode.connect(audioCtx.destination);
        // iOS: a short silent buffer started inside the gesture unlocks output
        var silent = audioCtx.createBuffer(1, 1, audioCtx.sampleRate);
        var src = audioCtx.createBufferSource();
        src.buffer = silent;
        src.connect(audioCtx.destination);
        src.start(0);
      }
      if (audioCtx.state === "suspended") audioCtx.resume();
    } catch (err) {
      audioCtx = null; // play silently
    }
  }

  // ------------------------------------------------------------ loop

  function loop(ts) {
    pollGamepads();
    measure();
    fns.frame(ts);
    window.requestAnimationFrame(loop);
  }

  window.TimeMazeHost = {
    init: function (frame, tap, key, fill, resize, pause, touches, audio, rate) {
      fns = {frame: frame, tap: tap, key: key, fill: fill, resize: resize, pause: pause};
      sharedTouches = touches;
      sharedAudio = audio;
      sourceRate = rate;
      stage = document.getElementById("stage");
      view = document.getElementById("view");
      ctx = view.getContext("2d", {alpha: false});
      frameCanvas = document.createElement("canvas");
      frameCtx = frameCanvas.getContext("2d");

      view.addEventListener("touchstart", onTouchStart, {passive: false});
      view.addEventListener("touchmove", onTouchMove, {passive: false});
      view.addEventListener("touchend", onTouchEnd, {passive: false});
      view.addEventListener("touchcancel", onTouchEnd, {passive: false});
      view.addEventListener("mousedown", onMouseDown);
      window.addEventListener("mousemove", onMouseMove);
      window.addEventListener("mouseup", onMouseUp);
      view.addEventListener("contextmenu", function (e) { e.preventDefault(); });
      window.addEventListener("keydown", function (e) { onKey(e, true); });
      window.addEventListener("keyup", function (e) { onKey(e, false); });
      window.addEventListener("blur", function () { sharedTouches[0] = 0; });
      document.addEventListener("visibilitychange", function () {
        if (document.hidden) {
          fns.pause();
          if (audioCtx && audioCtx.suspend) audioCtx.suspend();
        } else if (audioCtx && audioCtx.resume) {
          audioCtx.resume();
        }
      });
      var loading = document.getElementById("loading");
      if (loading) loading.parentNode.removeChild(loading);
      measure();
      window.requestAnimationFrame(loop);
    },

    present: present,

    load: function (key) {
      try {
        var v = window.localStorage.getItem("timemaze." + key);
        if (v === null) return -2147483648;
        var n = parseInt(v, 10);
        return isNaN(n) ? -2147483648 : n;
      } catch (err) {
        return -2147483648;
      }
    },

    save: function (key, value) {
      try {
        window.localStorage.setItem("timemaze." + key, String(value));
      } catch (err) {
        // private mode: progress lasts for this visit only
      }
    }
  };

  if ("serviceWorker" in navigator && location.protocol === "https:") {
    window.addEventListener("load", function () {
      navigator.serviceWorker.register("sw.js").catch(function () {});
    });
  }
})();
