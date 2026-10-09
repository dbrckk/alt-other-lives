# Repository agent instructions

## Shared development policy — 88 validated rules (2026-10-09)

The project adopts the [88-rule standard](https://github.com/dbrckk/repo-standards/blob/db2f86657ada74a0561e07189f9942d6b66ebb4a/standards/88-rules.md), the [operational agent skill](https://github.com/dbrckk/repo-standards/blob/db2f86657ada74a0561e07189f9942d6b66ebb4a/skills/repo-excellence-88/SKILL.md), and the [educational wiki](https://github.com/dbrckk/repo-standards/blob/db2f86657ada74a0561e07189f9942d6b66ebb4a/docs/WIKI-88.md). Read the relevant parts before substantial work and apply conditional rules only where appropriate.

**Owner preference: do not create new unit tests.** Existing tests may be run for diagnostics; prioritize real functional and integration verification, lint, build, and reproducible checks. Never claim an unexecuted check passed.

Preserve repository-specific constraints and authorized scope. The pinned policy commit above governs the 88 rules; `.repo-standards.yml` continues to configure existing repository intelligence and reusable workflows independently. Do not change workflow refs merely to adopt these rules.


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
