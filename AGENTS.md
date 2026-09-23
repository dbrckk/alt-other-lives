# Repository agent instructions

ALT adopts shared standards from `dbrckk/repo-standards` as recorded in `.repo-standards.yml`.

Before substantial work:
1. Read `.ai/session-state.json`.
2. Read `.ai/project-state.md`.
3. Read `.ai/index.md`.
4. Inspect only the source files relevant to the current task.
5. Check the latest CI status before claiming a change is complete.

Project-specific rules:
- Reliability and a working end-to-end Android flow come before feature count.
- Fix build/test blockers before new product work.
- Keep user photos private by default; remote processing requires explicit user action.
- Do not add an unused dependency.
- Prefer official Android APIs and narrowly scoped open-source libraries.
- Keep the core experience solo-first; no social graph is required.
- Every generated/exported result must remain usable through Android's standard share flow.
- Update `.ai/project-state.md` and `.ai/session-state.json` after substantial work.
