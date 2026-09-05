# 1.1.0.001

- Minecraft 1.20.1 port (Fabric + Forge 47). Contents are stored in the stack NBT `Items` list (same layout as the vanilla bundle) instead of a data component.
- `CustomBundleItem` takes the size multiplier as a constructor argument: `new CustomBundleItem(tag, sizeMultiplier, settings)`.
- Tooltip is rendered by vanilla's `BundleTooltipComponent`; the item-predicate (`custom_bundle_contents`) integration is not available on this line.

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