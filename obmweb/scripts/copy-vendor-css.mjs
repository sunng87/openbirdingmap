// Copies Astryx design-system CSS from node_modules into
// resources/public/css/vendor so index.html can <link> it.
// Run via `npm run copy-vendor-css` (also part of watch/release).
import { cpSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const out = join(root, "resources/public/css/vendor");
mkdirSync(out, { recursive: true });

const files = [
  ["@astryxdesign/core/src/reset.css", "astryx-reset.css"],
  ["@astryxdesign/core/dist/astryx.css", "astryx.css"],
  ["@astryxdesign/theme-neutral/dist/theme.css", "astryx-theme-neutral.css"],
  ["@astryxdesign/theme-neutral/fonts.css", "astryx-fonts.css"],
];

for (const [from, to] of files) {
  const src = join(root, "node_modules", from);
  cpSync(src, join(out, to));
  console.log(`copied ${from} -> resources/public/css/vendor/${to}`);
}
