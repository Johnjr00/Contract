# The voice

`tts/` holds the SupertonicTTS model the television reads terms aloud with — seven files, about
139 MB. It is deliberately **outside** `app/src/main/assets/`, so Gradle does not package it.

That is not an aesthetic choice. Bundled, the APK is 152.6 MiB, which is past the limit of every
route I have for handing a build over. Left out, it is 17.7 MiB and can simply be sent. So the
model is fetched once, on the first television that needs it, from this directory over HTTPS —
see `VoiceModel` and `VoiceInstaller` in the app module. After that one download the app is
offline forever, exactly as it was when the model shipped inside the APK.

Two consequences worth knowing about:

- **This directory is a published artefact, not scratch space.** `VoiceModel.COMMIT` pins the
  download to a specific commit, so the files here are load-bearing at that revision. Moving or
  rewriting them does not break existing installs — the pinned commit still serves the old
  content — but it does mean the pin has to be updated in step with any replacement.
- **The repository must stay reachable.** The download comes from
  `raw.githubusercontent.com`, so the repository has to remain public and the pinned commit has
  to remain an ancestor of some branch. Squash-merging this branch and then deleting it would
  eventually strand the commit, and a fresh install would have nowhere to fetch from. Building
  with the files copied back into `app/src/main/assets/tts/` always works regardless — the app
  prefers bundled assets when it finds them.

The model is Supertonic, MIT licensed; `tts/LICENSE` is the upstream licence text, and the
original bundle is `sherpa-onnx-supertonic-3-tts-int8-2026-05-11` from the sherpa-onnx
`tts-models` release.
