# LDLib2 Architectury 26.1

A modern Minecraft modding library for UI, rendering, synchronization, persistence, and in-game editors.

## Project Restructure Summary

This repository is a fork of LDLib2 redesigned for NeoForge 26.1, focusing on UI, rendering, synchronization, persistence, and in-game editors.

### JPMS Module Boundary Fixes

**Problem:** NeoForge 26.1's JPMS module system prevents mixin accessor interface casts across module boundaries. Classes in the `minecraft` module cannot be cast to mixin accessor interfaces from the `ldlib2` module, even though the mixins were applied at the bytecode level.

**Crashes Fixed:**
- `GameRendererAccessor` ClassCastException at `ModularUIWindow.ensureRenderer()` - floating windows crashed immediately
- `BufferBuilderAccessor` instanceof silently returning false → "Missing elements in vertex: RectParams, Radius" - rendering crash
- `BufferBuilderAccessor` instanceof for HSB_ALPHA → same vertex crash
- `PictureInPictureRendererAccessor` casts - visual layer sub-renderers
- `IGuiRendererExt` casts - visual layer rendering
- `MeshDataAccessor`, `DelegatingOpsAccessor`, `MinecraftAccessor`, `MouseHandlerAccessor`, `IAbstractContainerScreenExt` - various UI and data accessors

**Solution:** Created `AccessorHelper.java` - a centralized reflection utility with cached `MethodHandle` and `Field` objects that bypass JPMS module boundary restrictions at runtime. All 20+ files needing accessor casts were updated to use this utility instead of direct `instanceof`/cast patterns.

### Files Modified (1445 tracked)

**New Utility:**
- `AccessorHelper.java` - reflection bypass for JPMS module boundaries

**Render State Fixes:**
- `FloatRoundedRectRenderState.java` - replaced `vc instanceof BufferBuilderAccessor` with `AccessorHelper.beginElement()`
- `FloatHSBRectRenderState.java` - same fix for HSB_ALPHA element writing

**UI Window Fixes:**
- `ModularUIWindow.java` - replaced `GameRendererAccessor` cast with `AccessorHelper.getGuiRenderer()`
- `VisualLayerPipRenderer.java` - replaced multiple accessor casts with `AccessorHelper` methods

**Accessor Fixes Across Modules:**
- `MeshDataSortResult.java` - `MeshDataAccessor` → `AccessorHelper.setIndexBuffer()`
- `PersistedParser.java` - `DelegatingOpsAccessor` → `AccessorHelper.getDelegate()`
- `ModularUIWidget.java` - `MinecraftAccessor`/`IAbstractContainerScreenExt` → `AccessorHelper`
- `ScenarioBuilder.java` and `InputDriver.java` - `MinecraftAccessor`/`MouseHandlerAccessor` → `AccessorHelper`

### OpenSpec (Spec-Driven Development)

The `openspec/` directory enables spec-driven development with:

- `openspec/config.yaml` - Project context shown to AI when creating artifacts. Contains tech stack, conventions, style guides, and domain knowledge. Example rules keep proposals under 500 words and break tasks into max 2-hour chunks.

- `openspec/specs/` - Specification files defining the project domain, artifacts, and rules.

- `openspec/changes/` - Change log archive tracking modifications to artifacts over time.

### `.gitignore` (On Disk, Not Tracked in Git)

A comprehensive `.gitignore` exists on disk but is **not tracked in git** to preserve the ability to restructure ignores without committing AI/IDE artifacts. It excludes:

- **AI artifacts**: `/thoughts/`, `.opencode/`, `.claude/`, `.codex/`, `logs/`, `run/`, `crash-reports/`, `screenshots/`, etc.
- **Build output**: `build/`, `.gradle/`, `*.class`, `*.jar`
- **IDE files**: `/.idea/`, `*.ipr`, `*.iws`, `*.iml`
- **Generated/runtime**: `run/`, `logs/`, `crash-reports/`, `saves/`, `*.log`
- **Node/OpenCode**: `node_modules/`, `.opencode/`, `package-lock.json`

### How to Build

```bash
# Gradle wrapper is tracked - use it to build
./gradlew build

# Or per-module:
./gradlew :neoforge:build
./gradlew :common:build
./gradlew :fabric:build
```

### Test Results

Most rendering test scenarios now PASS that previously crashed:
- `floating_scene`, `floating_view` - floating window rendering ✅
- `graph_lod` - rounded rect rendering ✅
- `wiki_component_gallery` - UI rendering ✅
- Other UI test scenarios ✅

The `window_tooltip_bounds` ERROR at step 3 is an Xvfb surface initialization timing issue unrelated to code fixes ("The window has no surface yet").

### Author

`TheyCallMeAbstract <231887419+TheyCallMeAbstract@users.noreply.github.com>`

### OpenSpec Schema

```yaml
schema: spec-driven
# Project context shown to AI when creating artifacts
# Per-artifact rules customize behavior for proposals, tasks, etc.
```

## Credits & Upstream

LDLib2 and its documentation are created and maintained by **[KilaBash](https://github.com/Yefancy)** and the **[LowDragMC](https://github.com/Low-Drag-MC)** project. All credit for the library and the original documentation belongs to them.

- Official NeoForge project: <https://github.com/Low-Drag-MC/LDLib2>
- Official documentation: <https://low-drag-mc.github.io/LowDragMC-Doc/en/ldlib2/>
- Official Discord: <https://discord.com/invite/sDdf2yD9bh>

The Fabric Edition documentation site lives in [`site/`](site/README.md); it is an independent, community, Fabric-only build derived from `Low-Drag-MC/LowDragMC-Doc` (MIT) and is not affiliated with or endorsed by the original authors.