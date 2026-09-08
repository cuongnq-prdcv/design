# Phase 4 Spec — Input sources × formats + BoardValidator + Registry

> Trạng thái: **IN PROGRESS** (spec-first). Phase khó nhất: 2 chiều trực giao source × format.
> Tham chiếu: `../design.md` (mục Trục 1, Xử lý lỗi, Fitness function), `../plan.md`.

## 1. Scope

**4a — SOURCE dimension (where):**
- `InputSource { String read() }` — chỉ trả raw text, không biết format.
- `ConsoleInputSource` (đọc stdin đến EOF), `FileInputSource(Path)` (đọc file → String).

**4b — FORMAT dimension (how):**
- `PieceFormatParser { List<Piece> parse(String raw) }` — text → model, không biết nguồn.
- `NotationParser` (compact `W: KE2, QD1...`), `YamlParser`, `JsonParser`.
- Cả 3 trả **cùng** `List<Piece>`.

**4c — Validation một chỗ:**
- `BoardValidator.validate(List<Piece>) -> Board`: off-board (đã chặn ở Square), trùng ô.
- KHÔNG lặp validate trong từng parser.

**4d — Error handling:**
- `ChessInputException` (unchecked) cho mọi lỗi input/validation.
- `Main` bắt, in stderr, exit code ≠ 0.

**4e — Composition root: registry + CLI:**
- `sourceRegistry`, `parserRegistry`, `writerRegistry` : `Map<String, Supplier<...>>`.
- CLI: `--source console|file`, `--file <path>`, `--format notation|yaml|json`, `--out console|json`.
- Thay `switch(out)` tạm của Phase 3 bằng writerRegistry.

**4f — Fitness function:**
- Test khẳng định `engine`/`rules`/`output` KHÔNG import `chess.input`.

**Scope-out:** không auto-detect format; không FEN (Phase 5).

## 2. Inputs / Outputs

- **Notation:** `W: KE2, QD1, RA1` / `B: KE8, PA7` — mỗi dòng 1 phe, token `<Letter><File><Rank>`,
  letter = `K Q R B N P` (N=Knight).
- **YAML:** `pieces:` → list `{ type, side, position }`.
- **JSON:** `{ "pieces": [ { "type","side","position" } ] }`.
- **Output:** như Phase 3 (Console/JSON) — không đổi.

## 3. Cases phải xử lý (checklist truy vết)

| # | Case | Input | Expected | Test cover | SOLID | ✓ |
|---|------|-------|----------|-----------|-------|---|
| 1 | Notation parse hợp lệ | `W: KE2, RA1` + `B: KE8` | 3 Piece đúng type/side/pos | `NotationParserTest.parsesValidTokens` | SRP | ✅ |
| 2 | Notation N = Knight | `W: NB1` | KNIGHT ở B1 | `NotationParserTest.knightLetterIsN` | — | ✅ |
| 3 | Notation lỗi piece letter | `W: XE2` | ChessInputException | `NotationParserTest.rejectsUnknownPieceLetter` | error clarity | ✅ |
| 4 | Notation lỗi toạ độ | `W: KZ9` | ChessInputException | `NotationParserTest.rejectsOffBoard` | — | ✅ |
| 5 | YAML parse hợp lệ | yaml 3 pieces | 3 Piece đúng | `YamlParserTest.parsesValidYaml` | OCP (thêm format) | ✅ |
| 6 | YAML cú pháp sai | yaml hỏng | ChessInputException | `YamlParserTest.rejectsMalformedYaml` | — | ✅ |
| 7 | JSON parse hợp lệ | json 3 pieces | 3 Piece đúng | `JsonParserTest.parsesValidJson` | OCP | ✅ |
| 8 | JSON cú pháp sai | json hỏng | ChessInputException | `JsonParserTest.rejectsMalformedJson` | — | ✅ |
| 9 | 3 format ra cùng model | notation/yaml/json cùng nội dung | List<Piece> bằng nhau | `ParserEquivalenceTest.allFormatsProduceSameModel` | SRP/OCP (format độc lập) | ✅ |
| 10 | Validator bắt trùng ô | 2 piece cùng E2 | ChessInputException | `BoardValidatorTest.rejectsDuplicateSquare` | validation 1 chỗ | ✅ |
| 11 | Validator chấp nhận board hợp lệ | pieces khác ô | Board dựng được | `BoardValidatorTest.acceptsValidBoard` | — | ✅ |
| 12 | FileInputSource đọc file | file tạm | nội dung khớp | `FileInputSourceTest.readsFileContent` | ISP (chỉ read) | ✅ |
| 13 | FileInputSource file thiếu | path sai | ChessInputException | `FileInputSourceTest.missingFileFails` | error clarity | ✅ |
| 14 | source × format tự do tổ hợp | JSON text từ 1 source giả | parse ok qua parser.parse(source.read()) | `SourceFormatOrthogonalTest.anyFormatFromAnySource` | DIP | ✅ |
| 15 | Ranh giới engine ⊥ input | source code | engine/rules/output không import input | `ArchitectureBoundaryTest.coreDoesNotDependOnInput` | DIP (fitness) | ✅ |

*(Bonus test: `FileInputSourceTest.consoleReadsStdinStream` — ConsoleInputSource đọc stream bơm vào.)*

## 4. Acceptance criteria

- [x] `InputSource` và `PieceFormatParser` là 2 interface tách biệt, ranh giới `String`.
- [x] 3 parser (notation/yaml/json) trả cùng model; thêm 1 parser KHÔNG đụng parser khác/source.
- [x] `BoardValidator` là nơi DUY NHẤT validate board (off-board/trùng ô).
- [x] Mọi lỗi input hội tụ `ChessInputException`; `Main` in clear error + exit ≠ 0.
- [x] Registry cho source/format/writer; CLI chọn được mọi tổ hợp.
- [x] Fitness test: core không phụ thuộc `input`.
- [x] `gradle test` xanh; chạy thử JSON-từ-file và YAML-từ-stdin.

## 5. Trace / Verification log

- **Tổ hợp source × format (chạy launcher thật `build/install`):**
  - notation ⟵ stdin → console: `WHITE KING E2 -> [...]`, Rook A1 ăn `A7(x)`. OK.
  - JSON ⟵ file → console: Rook A1 ăn `A5(x)`, Pawn đen A5 -> [A4]. OK.
  - YAML ⟵ file → JSON out: JSON hợp lệ (Knight B1, Bishop C8). OK.
- **Error handling (đều exit code 1, clear error ra stderr):**
  - file thiếu → `Input error: Cannot read input file: ...`
  - JSON hỏng → `Input error: JSON syntax error: ...`
  - trùng ô → `Input error: Two pieces on the same square: E2`
  - format lạ → `Input error: Unknown --format 'toml'. Valid: [notation, json, yaml]`
  - piece letter lạ → `Input error: Unknown piece letter: 'X' in token XE2`
- **Test:** `gradle test --rerun-tasks` → **39/39 PASSED** (23 cũ + 16 mới).
- **Friction:** THẤP. `MoveEngine`/`*Rule`/`model`/`output` KHÔNG đổi. `Main` được viết lại (đúng
  vai composition root) từ hard-code board → registry + pipeline read/parse/validate/compute/write.
  Đây là thay đổi *dự kiến* ở composition root, không phải sửa lõi. Việc gom `TreeFormatParser`
  cho JSON/YAML tránh lặp (2 format chỉ khác ObjectMapper). Không có tangle source↔format:
  `ParserEquivalenceTest` + `SourceFormatOrthogonalTest` chứng minh tính độc lập; `ArchitectureBoundaryTest`
  khóa ranh giới engine ⊥ input.

## 6. Files chạm tới

- **Mới (input package):** `InputSource`, `ConsoleInputSource`, `FileInputSource`,
  `PieceFormatParser`, `NotationParser`, `TreeFormatParser`, `JsonParser`, `YamlParser`,
  `PieceFields`, `BoardValidator`, `ChessInputException`.
- **Mới (test):** `NotationParserTest`, `JsonParserTest`, `YamlParserTest`, `ParserEquivalenceTest`,
  `BoardValidatorTest`, `FileInputSourceTest`, `SourceFormatOrthogonalTest`,
  `chess/arch/ArchitectureBoundaryTest`.
- **Sửa:** `build.gradle` (thêm Jackson pinned); `src/main/java/chess/app/Main.java` (viết lại thành
  registry + pipeline + error convergence).
- **KHÔNG đổi:** `MoveEngine`, mọi `*Rule`, `MovementRule`, `Direction`, toàn bộ `model`,
  `ConsoleWriter`, `JsonWriter`, `ResultWriter` → lõi bất biến, chứng minh trục input cắm thêm
  mà không chạm lõi (DIP/OCP).
