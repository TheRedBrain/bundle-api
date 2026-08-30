# 1.1.1

- Fix a class-initialisation cycle that could deadlock under parallel registry loading (NeoForge).
- Minecraft 26.1.2 support (Java 25).
- Bundle contents are now stored as `ItemStackTemplate`, mirroring vanilla `BundleContents`.
- `Content.items()` returns `List<ItemStackTemplate>`; use `stream()` / `iterateCopy()` for stacks, `iterate()` is deprecated.
- Tooltip subclasses override `extractImage` (with `GuiGraphicsExtractor`) instead of `renderImage`.
- Bundles can supply a custom empty-state tooltip description.
- Removed the leftover bundle fullness tooltip line.

# 1.1.0

- migrated to Architectury (Thanks Daedelus!)

# 1.0.4

- fixed an issue where the bundle size would be set to 1 after some interactions

# 1.0.3

- now works with Minecraft 1.21

# 1.0.2

- internal refactor which fixes all known issues

# 1.0.1

- fixed a crash
- removed debug log spam
- lowered fabric loader dependency to 0.16.5

# 1.0.0

First release.

#