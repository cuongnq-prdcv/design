# Chess Board — Implementation Plan (theo Phase)

> Đi tuần tự Phase 1 → 5. **KHÔNG refactor trước** phase đang làm — friction là bài học, ghi lại vào PR.
> Tham chiếu thiết kế: `design.md`. Ngôn ngữ Java, build Gradle, test manual.

---

## Phase 0 — Bootstrap project (không tính điểm, chỉ hạ tầng)

**Mục tiêu:** có khung Gradle chạy được `./gradlew run` và `./gradlew test`.

**Việc làm:**
- Khởi tạo project Gradle (Java application plugin).
- Cấu trúc thư mục chuẩn:
  ```
  chess_board/
    build.gradle
    settings.gradle
    src/main/java/chess/...
    src/test/java/chess/...
  ```
- Java 17+ (dùng `record`, `switch` expression cho enum nội bộ nếu cần).
- JUnit 5 cho test.
- Package gốc: `chess` với các sub-package: `model`, `input`, `rules`, `engine`, `output`, `app`.

**Done khi:** `./gradlew build` xanh, chạy được một `main` in "hello".

---

## Phase 1 — Walking skeleton (Rook + King, hard-code, Console)

**Mục tiêu:** end-to-end result set chạy được với phạm vi nhỏ nhất.

**Phạm vi cố tình hẹp:** CHỈ Rook + King. Board **hard-code** trong `main`. In **Console**.
KHÔNG dựng registry, KHÔNG parser, KHÔNG file I/O — để phase sau cảm nhận friction.

**Việc làm:**
1. `model`: `PieceType`, `Side`, `Square`, `Piece`, `Move`, `PieceResult`, `ResultSet`.
   - `Square`: file 0..7, rank 0..7; helper `toString()` → "E2"; `onBoard(f,r)` check biên.
2. `Board`: giữ `Map<Square, Piece>`; `pieceAt(Square)`, `isEmpty(Square)`, `pieces()`.
   - (Phase 1 chưa cần `BoardValidator` — board hard-code hợp lệ.)
3. `rules`:
   - `MovementRule` interface.
   - `SlidingRule` (nhận danh sách hướng) → dùng cho Rook (4 hướng thẳng).
   - `SteppingRule` (offset cố định, 1 bước) → dùng cho King (8 ô quanh).
   - Áp luật chặn/ăn: dừng ở friendly (không vào), dừng+capture ở enemy đầu tiên.
4. `engine`: `MoveEngine` giữ `Map<PieceType, MovementRule>`, `compute(Board) -> ResultSet`.
5. `output`: `ResultWriter` interface + `ConsoleWriter` (ghi vào `Appendable`).
   - Định dạng: `WHITE ROOK A1 -> [A2, A3, ...]`, capture đánh dấu `(x)`.
6. `app.Main`: hard-code vài quân Rook/King, dựng `rules` map thủ công, chạy pipeline, in ra.

**Friction dự kiến (ghi lại):** chưa có, đây là baseline.

**Done khi:**
- Chạy `main` in đúng nước đi Rook + King cho board hard-code.
- Rook bị chặn đúng bởi quân cùng/khác phe; King đi đúng 8 ô và không ra ngoài biên.
- Test manual: ≥ 1 test Rook (blocking + capture), ≥ 1 test King (biên bàn cờ).

---

## Phase 2 — Thêm piece types (Bishop, Queen, Knight, Pawn)

**Mục tiêu:** chứng minh OCP — thêm quân = thêm class + 1 dòng map, KHÔNG sửa engine.

**Việc làm:**
1. `SlidingRule` tái dùng cho:
   - Bishop → 4 hướng chéo.
   - Queen → 8 hướng (thẳng + chéo).
2. `SteppingRule` tái dùng cho Knight → 8 offset chữ L.
3. `PawnRule` (class riêng): tiến 1 ô vào ô trống; tiến 2 ô từ hàng xuất phát nếu cả 2 trống;
   ăn chéo tiến 1 ô lên quân địch. White đi lên (+rank), Black đi xuống (−rank).
   Scope-out: không en passant, không phong cấp.
4. Đăng ký các rule mới vào `rules` map ở `Main` (mỗi quân 1 dòng).

**Friction quan sát:** thêm quân có buộc sửa `MoveEngine` không? (Kỳ vọng: KHÔNG — chỉ thêm class
+ dòng map.) Nếu phải sửa engine → đó là OCP violation cần fix. Ghi vào PR.

**Done khi:**
- Cả 6 quân tính đúng nước đi.
- Engine không đổi so với Phase 1.
- Test manual: mỗi quân ≥ 1 test; Pawn test đủ 3 case (đi 1, đi 2, ăn chéo);
  sliding test blocking + capture; test biên bàn cờ.

---

## Phase 3 — Thêm output format (JSON) cạnh Console

**Mục tiêu:** chứng minh format output plug được — KHÔNG đụng movement/parsing code.

**Việc làm:**
1. `JsonWriter implements ResultWriter` — ghi `ResultSet` ra JSON vào `Appendable`.
   - Cân nhắc: tự viết JSON tay (tránh phụ thuộc lib ở phase này) hoặc dùng lib nhẹ.
     Đề xuất Phase 3: viết tay cho gọn, chưa cần dependency.
2. `Main`: cho chọn writer (tạm thời có thể vẫn hard-code chọn, registry để Phase 4).

**Friction quan sát:** thêm JSON output có buộc sửa `MoveEngine` / `MovementRule` không?
(Kỳ vọng: KHÔNG.) Nếu có → axes bị tangle (SRP/OCP). Ghi vào PR.

**Done khi:**
- Cùng một `ResultSet` render được ra cả Console lẫn JSON qua cùng `ResultWriter`.
- Không đổi engine/rules.
- Test manual: JsonWriter ghi vào `StringBuilder`, assert cấu trúc JSON (zero I/O).

---

## Phase 4 — Thêm input sources + formats (điểm khó nhất: 2 chiều)

**Mục tiêu:** chứng minh source × format độc lập (DIP + SRP/OCP), validation 1 chỗ.

**Việc làm — làm theo 2 bước con để cảm nhận từng chiều:**

**4a. Thêm SOURCE dimension (where):**
- `InputSource` interface: `String read()`.
- `ConsoleInputSource` (đọc stdin), `FileInputSource` (đọc file → String).
- Quan sát: engine có phụ thuộc "nơi lấy piece" không? (KHÔNG được — DIP.)

**4b. Thêm FORMAT dimension (how):**
- `PieceFormatParser` interface: `List<Piece> parse(String raw)`.
- `NotationParser` (compact `W: KE2, QD1, ...`), `YamlParser`, `JsonParser`.
  - YAML/JSON có thể dùng lib (Jackson/SnakeYAML) — thêm dependency ở Gradle.
- Tất cả parser trả **cùng model** `List<Piece>`.
- Quan sát: thêm YAML/JSON có buộc sửa `NotationParser` hay code đọc source không?
  (KHÔNG được — nếu có thì 2 chiều đã leak vào nhau.)

**4c. Validation gom về 1 chỗ:**
- `BoardValidator.validate(List<Piece>) -> Board`: off-board, trùng ô, (unknown type/side
  đã chặn ở tầng parse). KHÔNG lặp validation theo từng format.

**4d. Composition root — registry + cờ CLI (Mức 2):**
- `sourceRegistry: Map<String, Supplier<InputSource>>` (console|file).
- `parserRegistry: Map<String, Supplier<PieceFormatParser>>` (notation|yaml|json).
- `writerRegistry: Map<String, Supplier<ResultWriter>>` (console|json).
- Parse args: `--source`, `--format`, `--out`, `--file <path>`.
- Ghép: `parser.parse(source.read())` → mọi format × mọi source.

**Friction quan sát:** đây là phase dễ lộ tangle nhất — ghi kỹ vào PR bất kỳ chỗ nào phải sửa
code cũ ngoài việc thêm class + dòng registry.

**Done khi:**
- JSON-từ-file, YAML-từ-stdin, notation-từ-file... mọi tổ hợp chạy.
- Thêm 1 format KHÔNG đụng parser khác / source code.
- `BoardValidator` là nơi DUY NHẤT check board.
- Engine không import gì thuộc `input` package (kiểm tra bằng mắt / test DIP).
- Test manual: mỗi parser ≥ 1 test hợp lệ + 1 test malformed; validator test trùng ô + off-board.

---

## Phase 5 — Stretch (optional, sửa ít code cũ nhất)

**Mục tiêu:** demo tối thượng của OCP — chỉ thêm class mới + 1 dòng wiring.

**Lựa chọn (làm 1 hoặc nhiều):**
- `XmlWriter` / `TextWriter` — thêm writer + 1 dòng writer registry.
- `FenParser` — thêm parser + 1 dòng parser registry (input format mới).
- Check detection như **decorator/filter** bọc ngoài pseudo-legal moves, KHÔNG đụng per-piece rule.
- GraalVM native image — ghi lại điều phải đổi (DIP-by-construction vs reflection).

**Done khi:** tính năng mới chạy mà diff vào code cũ ≈ chỉ 1 dòng wiring.

---

## Thứ tự implement tổng thể

1. Phase 0 — bootstrap Gradle + JUnit.
2. Phase 1 — skeleton Rook/King → chạy end-to-end.
3. Phase 2 — 4 quân còn lại.
4. Phase 3 — JsonWriter.
5. Phase 4 — sources + formats + validator + registry/CLI.
6. Phase 5 — stretch tùy chọn.

Sau mỗi phase: chạy test manual, và **ghi note friction** để phục vụ PR write-up.

---

## Ghi chú về "friction log" (cho PR)

Mỗi phase khi hoàn thành, ghi lại 1–2 câu:
- Có phải sửa code phase trước không? Ở đâu?
- Nếu có, refactor nào đã loại bỏ friction đó?
- Ánh xạ về nguyên lý SOLID bị vi phạm/được cứu.

---

## Friction log (cập nhật theo tiến độ)

### Phase 0 + 1 — DONE (verified)
- **Đã làm:** Gradle (Java 21 toolchain, JUnit 5, JaCoCo). Model thuần (`Square`/`Piece`/`Move`/
  `PieceResult`/`ResultSet`/`Board`). `MovementRule` + `SlidingRule` (Rook) + `SteppingRule` (King).
  `MoveEngine` tra `Map<PieceType,MovementRule>`. `ResultWriter`/`ConsoleWriter` ghi vào `Appendable`.
  `Main` hard-code board Rook+King, in Console.
- **Verify:** `gradle run` in đúng nước đi (Rook chặn bởi King cùng phe, ăn Rook địch ở cột A;
  King đúng biên). `gradle test` — 7/7 PASSED, gồm test pipeline zero-I/O (StringBuilder = DIP proof).
- **Friction:** chưa có — đây là baseline. Đáng chú ý: đã chủ động dựng sẵn `SlidingRule`/`SteppingRule`
  nhận danh sách hướng (thay vì viết `RookRule`/`KingRule` cứng), nên Phase 2 (Bishop/Queen/Knight)
  kỳ vọng chỉ cần thêm dòng map với bộ hướng khác — sẽ kiểm chứng ở Phase 2.

### Phase 2 — DONE (verified)
- **Đã làm:** thêm `PawnRule` (class mới duy nhất). Bishop/Queen/Knight KHÔNG cần class mới —
  chỉ thêm dòng map với bộ hướng khác: `BISHOP=SlidingRule(DIAGONAL)`, `QUEEN=SlidingRule(ALL_8)`,
  `KNIGHT=SteppingRule(KNIGHT)`. Cập nhật board hard-code có đủ 6 loại quân.
- **Verify:** `gradle run` in đúng cả 6 quân (Bishop ăn chéo D5(x), Queen kết hợp thẳng+chéo,
  Knight nhảy qua quân chặn, Pawn đi đôi E2->E3,E4, Pawn đen đi xuống + ăn chéo C4(x)).
  `gradle test` — 18/18 PASSED (thêm PawnRuleTest 7 case + SlidingSteppingReuseTest 4 case).
- **Friction:** GẦN NHƯ KHÔNG. `MoveEngine` KHÔNG đổi 1 dòng nào → OCP đạt cho engine.
  Quyết định gom `SlidingRule`/`SteppingRule` ở Phase 1 được đền đáp: 3/4 quân mới chỉ tốn 1 dòng map.
  Chỉ Pawn cần class riêng — hợp lý vì luật Pawn khác bản chất (hướng đi ≠ hướng ăn), không phải
  do thiết kế sai. Đây chính là "abstraction worth it" mà đề yêu cầu biện luận.
