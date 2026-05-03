# JavaMark Migration Plan (Reduce Javadoc Internal API Dependence)

## Goal
Migrate JavaMark from `jdk.javadoc.internal.*`-driven rendering to a public-API-first architecture based on `jdk.javadoc.doclet`, `DocletEnvironment`, and `DocTrees`, while keeping a temporary compatibility path for current output behavior.

This reduces breakage risk on Java 25+ and improves long-term maintainability of both the doclet and the Gradle plugin.

## High-level approach
1. Inventory and isolate all internal API coupling.
2. Introduce stable rendering interfaces around existing builders/writers.
3. Implement a public-API rendering engine in parallel.
4. Ship dual-engine mode with parity checks and gradual default switch.
5. Remove legacy internal engine and `--add-exports`/`--add-opens` reliance.

## Phase Plan

### Phase 1 - Discovery and baseline
- Build a dependency matrix of all `jdk.javadoc.internal.*` usage under `doclet/src/main/java`.
- Capture golden Markdown output from the `example` module (package/type/member/tags/constants pages).
- Document plugin/runtime behavior for module exports/opens and Java version expectations.

### Phase 2 - Architecture hardening
- Define a render model (page, section, table, anchor, link, admonition, code block).
- Introduce adapter boundaries so existing internal-based code remains behind interfaces.
- Separate extraction logic from Markdown formatting.

### Phase 3 - Public API migration
- Reimplement model extraction using only public doclet APIs (`DocletEnvironment`, element/type model, `DocTrees`).
- Replace internals-based member summary/detail generation with public-model traversal.
- Rework constants rendering to scan elements directly instead of depending on internal member tables.

### Phase 4 - Compatibility and plugin transition
- Add engine mode option:
  - `legacy-internal`
  - `public-stable`
  - `auto`
- Default plugin to `auto`; only inject exports/opens in `legacy-internal`.
- Emit deprecation warnings when legacy mode is selected.

### Phase 5 - Stabilization and removal
- Set `public-stable` as default after parity threshold is met.
- Remove reflection-based/internal-only test code paths.
- Delete legacy internal adapters and obsolete JVM/module flags.

## Risks
1. Output regressions (anchors, ordering, wording, table layouts).
2. Temporary maintenance cost while dual engines coexist.
3. Edge cases in inherited members and tag rendering.
4. User build friction if legacy flags are removed too early.

## Compatibility strategy
1. Keep dual-engine support for at least one minor release.
2. Keep `legacy-internal` as explicit opt-in after default switch.
3. Publish a support matrix with Java 25+ as primary target.
4. Treat old JDK support as best-effort unless formally guaranteed.

## Testing strategy
1. Snapshot/golden tests comparing generated Markdown output.
2. Integration tests using `ToolProvider.getSystemDocumentationTool()`.
3. CI matrix including Java 25 and latest EA build.
4. Targeted tests for:
   - links and anchors
   - member summaries/details
   - constants tables
   - deprecation/preview/restriction blocks
   - nested/enclosed types

## Rollout plan
1. Release N: adapter boundaries + dual-engine framework, legacy default.
2. Release N+1: public engine default, legacy still available.
3. Release N+2: remove legacy engine and default internal module flags.

## Milestones
- M1 (week 1-2): dependency inventory + output baselines approved.
- M2 (week 3-5): adapter layer merged, no behavior change.
- M3 (week 6-9): public engine reaches agreed parity.
- M4 (week 10): default switch in plugin and migration notes.
- M5 (week 12+): legacy internals removed.

## Open decisions
1. Define parity threshold: strict output equality vs semantic equivalence.
2. Define legacy support duration (number of releases).
3. Define user-facing migration policy for deprecated flags/options.

