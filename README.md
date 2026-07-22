# Asemic Codex Generator

Turn real poems into a convincing, untranslatable illuminated manuscript. The app invents a deterministic fictional script from a seed, applies small phonetic and ligature rules, and paints an aged codex page with marginalia, a rubric, gilded initials, and procedural glyphs.

## Run

Requires Java 17+ and Maven 3.9+.

```bash
mvn compile exec:java
```

Or build a portable class directory:

```bash
mvn package
java -cp target/classes io.fuckshitpoet.asemic.AsemicCodexApp
```

## Use

1. Write or paste a poem.
2. Set a seed (the same seed recreates the same alphabet).
3. Press **Transmute Codex**. The left panel reveals the translated script; the right panel is the page.
4. Press **Export PNG** to save an archival page image.

The lower legend exposes a few invented grammar rules, but the page itself remains beautifully illegible.

## Design notes

Everything is Java2D—no font files, image assets, or external libraries. Each glyph is assembled from seeded strokes, arcs, knots, and diacritics; repeated digraphs form ligatures, while poem punctuation becomes ornamental marks.
