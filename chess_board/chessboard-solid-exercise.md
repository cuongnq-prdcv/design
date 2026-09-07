# Practice Exercise — The Chess Board (SOLID Principles)

> **Learning goal:** This is a design exercise, not a chess-engine exercise. The chess rules are
> deliberately simplified so your attention stays on structure. You are judged on how cleanly your
> design isolates the three axes of change (input, movement rules, output) — not on whether you
> implement en passant.

---

## 1. Context

Place a set of chess pieces on an 8×8 board and, for each piece, calculate the squares it can move
to. Collect all results into a result set and render that result set to an output.

A naive solution puts parsing, move calculation, and formatting into one class with a big
`switch` on piece type. It works — and then it rots the moment you add a new piece type, a new
output format, or a new input source. Your job is to build it so each of those changes is *additive*.

---

## 2. Functional Requirements

### 2.1 Input

Every piece is described by the same three attributes, regardless of format:

- **Type** — `PAWN`, `BISHOP`, `KNIGHT`, `ROOK`, `QUEEN`, `KING`
- **Side** — `BLACK`, `WHITE`
- **Position** — a square on the board (file `A`–`H`, rank `1`–`8`)

Input has **two independent dimensions** — keep them independent in your design:

- **Source** — *where* the bytes come from: a **file** or the **console (stdin / command line)**.
- **Format** — *how* those bytes are structured: the **compact notation** below, **YAML**, or
  **JSON**. Support at least these three.

The two dimensions are orthogonal: any format can arrive from any source (a JSON file, a YAML
paste on stdin, …). If reading from a file forces you to know the format — or parsing JSON forces
you to know it came from a file — the axes are tangled. Fixing that tangle is part of the exercise.

Whatever the format, parsing must produce the **same internal model** (a list of typed, sided,
positioned pieces). Everything downstream (the engine, the writers) must be blind to which format
was used.

**Format A — compact notation.** One line per side. Each token is `<PieceLetter><File><Rank>`,
where the piece letter is `K Q R B N P` (N = Knight):

```
W: KE2, QD1, RA1, NB1, NF7, BC1, BC4, PA2, PB2, PC2, PD2, PG3
B: KE8, QH1, RA8, RH8, ND1, BC8, PA7, PB7, PC7, PD7, PE5, PG7, PH7
```

**Format B — YAML.**

```yaml
pieces:
  - { type: KING,   side: WHITE, position: E2 }
  - { type: QUEEN,  side: WHITE, position: D1 }
  - { type: KNIGHT, side: WHITE, position: B1 }
  - { type: KING,   side: BLACK, position: E8 }
  - { type: PAWN,   side: BLACK, position: A7 }
```

**Format C — JSON.**

```json
{
  "pieces": [
    { "type": "KING",   "side": "WHITE", "position": "E2" },
    { "type": "QUEEN",  "side": "WHITE", "position": "D1" },
    { "type": "KNIGHT", "side": "WHITE", "position": "B1" },
    { "type": "KING",   "side": "BLACK", "position": "E8" },
    { "type": "PAWN",   "side": "BLACK", "position": "A7" }
  ]
}
```

Regardless of format, the parser must reject malformed input with a clear error (unknown piece
type, off-board coordinate, two pieces on the same square, duplicate/garbage entries, invalid
YAML/JSON syntax). Validation of the *board* (e.g. two pieces on one square) belongs in one place
and must **not** be duplicated per format — the format parsers produce the model; a single validator
checks it.

### 2.2 Process — movement rules (simplified)

Compute the **pseudo-legal** moves for each piece: every square the piece could move to given the
current board, **ignoring king safety** (do not compute check, checkmate, or pins). This keeps the
rules local to each piece — which is the point.

| Piece  | Movement |
|--------|----------|
| Pawn   | One square forward to an empty square (White ↑, Black ↓). Two squares forward from its starting rank if both squares are empty. Captures diagonally forward one square onto an enemy piece. **Scope-out:** no en passant, no promotion. |
| Knight | The 8 L-shaped squares. Jumps over anything. |
| Bishop | Slides along diagonals until blocked. |
| Rook   | Slides along ranks and files until blocked. |
| Queen  | Rook + Bishop. |
| King   | One square in any of the 8 directions. **Scope-out:** no castling. |

**Blocking & capture rules (apply to all):**

- A square occupied by a **friendly** piece cannot be entered, and sliding pieces cannot pass
  through it.
- A sliding piece (bishop/rook/queen) stops at the **first enemy piece** and may capture it.
- A move that captures is still a valid move; mark captures in the output (see 2.3).

### 2.3 Output

Render the result set to a **pluggable** output. At minimum support **Console** and **JSON**;
**Plain text** and **XML** are stretch formats. Selecting the format must **not** require changing
movement logic or parsing logic.

Suggested per-piece result shape (formatting is up to each writer):

```
WHITE PAWN E2 -> [E3, E4]
WHITE KNIGHT B1 -> [A3, C3, D2]
WHITE BISHOP C4 -> [B3, A2(x), B5, A6, D3, E2, F1, D5, E6, F7(x)]   # (x) = capture
```

---

## 3. Delivery in Phases

Do the phases in order. **Do not** refactor ahead of the phase you're on — the friction you feel
when a later phase forces you to touch earlier code is the lesson. Note that friction in your PR
description.

- **Phase 1 — Walking skeleton.** Support **Rook** and **King** only, read from a **hard-coded**
  board, print to **Console**. Get an end-to-end result set working.
- **Phase 2 — Add piece types.** Add Bishop, Queen, Knight, Pawn. *Observe:* did adding a piece
  force you to edit existing pieces or a central `switch`? If yes, that's an OCP violation to fix.
- **Phase 3 — Add an output format.** Add **JSON** alongside Console. *Observe:* did you have to
  touch movement code to add a format? If yes, your axes are tangled (SRP/OCP).
- **Phase 4 — Add input sources and formats.** First add **file** input alongside console (the
  *source* dimension), then add **YAML** and **JSON** parsing alongside the compact notation (the
  *format* dimension). *Observe two things:* does the core engine depend on *where* pieces come from
  (it shouldn't — DIP)? And did adding YAML/JSON force you to touch the notation parser or the
  source-reading code (it shouldn't — SRP/OCP, and a sign the two dimensions leaked into each other)?
- **Phase 5 (stretch).** Add XML/plain-text output, or a further input format (e.g. FEN), changing
  **as little existing code as possible**. Ideally: only new classes + one line of wiring.

---

## 4. SOLID Focus — what each principle looks like here

This is the rubric for the design review. For each principle, be ready to point at the class/interface
that satisfies it.

- **SRP — Single Responsibility.** Parsing, move calculation, and rendering are three separate
  reasons to change. They must live in separate types. The board/piece model should hold *state*,
  not I/O or formatting.
- **OCP — Open/Closed.** Adding a **new piece type** or a **new output format** should mean *adding
  a class*, not editing an existing one. If you have a `when(type)` / `switch(type)` that grows with
  every piece, you haven't got OCP yet.
- **LSP — Liskov Substitution.** Every piece is usable through a common `Piece` / `MovementRule`
  abstraction, and the engine treats them uniformly — no `if (piece is Knight)` special-casing in
  the engine. A piece subtype must not weaken the contract (e.g. throw on a method the engine calls).
- **ISP — Interface Segregation.** Keep the abstractions small and role-focused: a piece exposes
  "give me your moves for this board"; an output writer exposes "write this result set"; an input
  source exposes "give me the pieces". No fat interface forcing a class to stub methods it can't
  honor.
- **DIP — Dependency Inversion.** The core engine depends on **abstractions** (`InputSource`,
  `MovementRule`, `ResultWriter`), never on concrete `FileReader` / `JsonWriter`. Concretions are
  injected at the composition root (`main`) and passed in via constructor — not `new`-ed inside the
  engine. **The proof:** you can unit-test move calculation with an in-memory board and a fake
  writer, with zero file/console I/O.

---

## 5. Definition of Done

**Design (the graded part)**
- [ ] Adding a hypothetical new piece (e.g. a custom "Archbishop") requires only a new class + one
      registration line. Demonstrate this, or explain exactly where the new code goes.
- [ ] Adding a new output format requires only a new writer class. Console and JSON both work via
      the same `ResultWriter` abstraction.
- [ ] Input **source** and input **format** are independent: any format parses from any source, and
      adding a format (notation → YAML → JSON) touches no source code and no other parser. Board
      validation (duplicate square, off-board) lives in exactly one place, not once per format.
- [ ] The move-calculation engine has **no direct dependency** on file/console I/O or on any
      concrete output format.
- [ ] No `switch`/`when`/`if-else` chain on piece type inside the engine or output layer.

**Quality gates (from the original problem)**
- [ ] Unit tests: **line coverage ≥ 80%, branch coverage ≥ 70%**.
- [ ] Static analysis: **no Critical or Major issues** (SonarQube, or your ecosystem's equivalent).
- [ ] Movement rules covered by tests, including blocking, capture, board edges, and the pawn
      two-square / diagonal-capture cases.

**PR write-up**
- [ ] One or two sentences per SOLID principle naming the type that embodies it.
- [ ] A short note on any friction felt during Phases 2–4 and how the refactor removed it.

---

## 6. Design Constraints & Anti-Patterns to Avoid

- **No God class.** If one class parses, calculates, and prints, you've failed the core objective.
- **No type-code branching for behavior.** Prefer polymorphism over `switch(pieceType)`.
- **Don't over-abstract.** This is the counter-warning. If an abstraction has exactly one
  implementation and no realistic second one, question it. The three axes here (input / rules /
  output) genuinely have multiple implementations — abstract *those*. Don't invent a
  `AbstractSquareCoordinateFactoryProvider`. In your review be ready to answer: *"was this
  abstraction worth it?"* for each interface you introduced.
- **Composition root.** All wiring (which input, which pieces, which writer) happens in one place
  (`main` / a small factory). The rest of the code receives its dependencies.

---

## 7. Suggested Shape (a hint, not a mandate)

```
InputSource        (interface)  -> ConsoleInputSource, FileInputSource   # where: yields raw text
PieceFormatParser  (interface)  -> NotationParser, YamlParser, JsonParser # how: raw text -> pieces
BoardValidator     (class)      -> one place: off-board / duplicate-square / etc.
Board              (class)      -> holds pieces, answers "what's on square X?"
MovementRule       (interface)  -> movesFor(piece, board): List<Move>
   PawnRule, KnightRule, BishopRule, RookRule, QueenRule, KingRule
MoveEngine         (class)      -> for each piece, ask its rule; build ResultSet
ResultWriter       (interface)  -> ConsoleWriter, JsonWriter, (XmlWriter, TextWriter)
Main / Composition -> picks concretions, injects them, runs the engine
```

There is more than one good design. If yours differs but keeps the three axes independent and
testable, that's a success.

---

## 8. Stretch Goals (optional)

- Add **check detection** as a *decorator/filter* over pseudo-legal moves, without touching the
  per-piece rules — a strong OCP/DIP demonstration.
- Support an alternate input format (FEN) behind the existing `InputSource` abstraction.
- If you're on the GraalVM track: build the CLI as a **native image**. Notice how reflection-heavy
  DI/serialization frameworks fight native compilation, while explicit constructor injection and
  code-generated (build-time) DI compile cleanly. Write up what you had to change — it's a real
  lesson in why DIP-by-construction beats DIP-by-reflection.
