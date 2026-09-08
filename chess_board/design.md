# Chess Board — Design Document (SOLID Exercise)

> Ngôn ngữ: **Java**. Build: **Gradle**.

---

## 1. Nguyên tắc xuyên suốt

Luồng dữ liệu **một chiều**. Không khối nào biết về khối trước/sau ngoài kiểu dữ liệu đi qua.

```
        ┌─────────────┐   raw String   ┌──────────────────┐  List<Piece>
args ──▶ │ InputSource │ ─────────────▶ │ PieceFormatParser│ ───────────┐
        └─────────────┘                └──────────────────┘            │
        Console/File                    Notation/Yaml/Json             ▼
                                                              ┌──────────────────┐
                                                              │  BoardValidator  │  (only)
                                                              └──────────────────┘
                                                                       │ Board
                                                                       ▼
                          Map<PieceType,MovementRule>          ┌──────────────┐
                          (nạp ở composition root)  ──────────▶│  MoveEngine  │
                                                              └──────────────┘
                                                                       │ ResultSet (data)
                                                                       ▼
                                                              ┌──────────────┐   Appendable
                                                              │ ResultWriter │ ─────────────▶ out
                                                              └──────────────┘
                                                              Console/Json/...
```

---

## 2. Các quyết định thiết kế đã chốt

### Mục 1 — INPUT (2 chiều trực giao)
- Tách **"đọc bytes"** (`InputSource`) khỏi **"hiểu bytes"** (`PieceFormatParser`).
- **Ranh giới giữa 2 chiều là `String`** (đọc hết một lần — input nhỏ, không cần streaming).
- Bất kỳ source nào × bất kỳ parser nào = tự do tổ hợp: `parser.parse(source.read())`.
- **Chọn parser/source/writer bằng tay** ở composition root — KHÔNG auto-detect format.
  - Cơ chế: **registry + cờ CLI** (Mức 2). `Map<String, Supplier<...>>` nạp ở `main`.
  - Thêm format mới = thêm 1 dòng registry (OCP), không sửa code khác.
- `Piece` do parser trả ra là **model thuần** (type, side, position) — KHÔNG gắn Board, KHÔNG gắn luật đi.

### Mục 2 — MOVEMENT RULES
- `MovementRule` **tách rời** khỏi `Piece`. Engine giữ `Map<PieceType, MovementRule>` nạp ở
  composition root. Engine chỉ **tra map**, KHÔNG `switch(pieceType)`.
- Gom 2 "họ" luật để giảm trùng lặp (vẫn không có switch, thể hiện LSP):
  - **SlidingRule** — trượt theo danh sách hướng đến khi bị chặn. Rook = 4 hướng thẳng,
    Bishop = 4 hướng chéo, Queen = cả 8 hướng.
  - **SteppingRule** — offset cố định, 1 bước. King = 8 ô quanh, Knight = 8 offset chữ L.
  - **PawnRule** — để riêng (luật đặc thù: đi thẳng không ăn, ăn chéo, nước đôi từ hàng xuất phát).
- `Move { Square target; boolean capture; }` — đủ để đánh dấu `(x)` theo yêu cầu đề.

### Mục 3 — OUTPUT (pluggable, tách format khỏi đích đến)
- `ResultWriter.write(ResultSet, Appendable out)` — **Lựa chọn Y**.
  - `Appendable` chuẩn Java: `System.out`, `StringBuilder`, `FileWriter` đều implement.
  - Test dùng `StringBuilder` (zero I/O — đúng "DIP proof"); production dùng `System.out`/`FileWriter`.
  - Tách **format** (writer làm) khỏi **đích đến** (Appendable) — nhất quán với cách tách source×format.
- `ResultSet` là **data thuần**, KHÔNG có `toJson()` (nếu có thì tangle rules với output).

### Handle error
- **Một kiểu lỗi domain thống nhất:** `ChessInputException` (unchecked) cho MỌI lỗi input/validation:
  unknown piece type/side, off-board, trùng ô, cú pháp notation/YAML/JSON sai, file không đọc được.
- **Hội tụ về một chỗ:** `Main` bắt `ChessInputException`, in thông báo rõ ra `stderr`, thoát với
  exit code ≠ 0. Phần còn lại KHÔNG bắt lỗi lẻ tẻ, chỉ ném với thông điệp rõ ràng.
- `Square` vẫn validate biên; `BoardValidator` là nơi gom validate board (off-board/trùng ô) và
  ném `ChessInputException` — KHÔNG lặp validate trong từng parser.

---

## 3. Kiểu dữ liệu (model thuần)

```java
enum PieceType { PAWN, BISHOP, KNIGHT, ROOK, QUEEN, KING }
enum Side      { WHITE, BLACK }

record Square(int file, int rank) { }          // file 0..7 (A..H), rank 0..7 (1..8)
record Piece(PieceType type, Side side, Square position) { }

record Move(Square target, boolean capture) { }
record PieceResult(Piece piece, List<Move> moves) { }
record ResultSet(List<PieceResult> results) { }   // Not include toJson()
```

## 4. Abstraction

```java
// Input — ranh giới là String
interface InputSource       { String read(); }
interface PieceFormatParser { List<Piece> parse(String raw); }

// Board + validation (1 chỗ duy nhất - cũng như chỉ có 1 bàn cơ)
class Board          { /* giữ pieces; pieceAt(Square), isEmpty(Square)... */ }
class BoardValidator { Board validate(List<Piece> pieces); }  // off-board, trùng ô

// Rules — Not switch
interface MovementRule { List<Move> movesFor(Piece piece, Board board); }
abstract class SlidingRule  implements MovementRule { /* List<Direction> */ }
abstract class SteppingRule implements MovementRule { /* List<offset> */ }
class PawnRule implements MovementRule { }

// Output — format tách khỏi đích đến
interface ResultWriter { void write(ResultSet results, Appendable out); }
```

## 5. Engine — sử dụng map

```java
class MoveEngine {
    private final Map<PieceType, MovementRule> rules;
    MoveEngine(Map<PieceType, MovementRule> rules) { this.rules = rules; }

    ResultSet compute(Board board) {
        // for each piece: rules.get(p.type()).movesFor(p, board) -> PieceResult
    }
}
```

## 6. Root - tổng hợp các phần + triển khai

```java
class Main {
    public static void main(String[] args) {
        InputSource source        = sourceRegistry.get(cfg.source).get();   // console|file
        PieceFormatParser parser  = parserRegistry.get(cfg.format).get();   // notation|yaml|json
        ResultWriter writer       = writerRegistry.get(cfg.out).get();      // console|json

        // thêm quân = thêm 1 dòng ở đây
        Map<PieceType, MovementRule> rules = Map.of(
            ROOK,   new SlidingRule(ORTHOGONAL),
            BISHOP, new SlidingRule(DIAGONAL),
            QUEEN,  new SlidingRule(ALL_8),
            KING,   new SteppingRule(ALL_8_ONE_STEP),
            KNIGHT, new SteppingRule(KNIGHT_OFFSETS),
            PAWN,   new PawnRule()
        );

        List<Piece> pieces = parser.parse(source.read());
        Board board        = new BoardValidator().validate(pieces);
        ResultSet result   = new MoveEngine(rules).compute(board);
        writer.write(result, System.out);
    }
}
```

---

## 7. Definition of Done

| Tiêu chí (đề) | Đáp ứng |
|---|---|
| Thêm quân mới = 1 class + 1 dòng | Rule class mới + 1 dòng trong `rules` map |
| Thêm output format = 1 writer class | `ResultWriter` mới + 1 dòng writer registry |
| Source × format độc lập | Ghép ở `parser.parse(source.read())`, ranh giới `String` |
| Validation 1 chỗ | `BoardValidator` duy nhất, không lặp theo format |
| Engine không dính I/O | Engine nhận `Board`, trả `ResultSet`; test in-memory + `StringBuilder` |
| Không `switch(type)` | Engine tra `Map<PieceType, MovementRule>` |

---

## 8. Ánh xạ SOLID (cho PR write-up)

- **SRP** — Parsing (`PieceFormatParser`), tính nước đi (`MovementRule`/`MoveEngine`),
  render (`ResultWriter`) là 3 type riêng. Model (`Piece`/`Board`) chỉ giữ state.
- **OCP** — Thêm quân/format = thêm class + 1 dòng registry/map, không sửa code cũ.
- **LSP** — Engine đối xử mọi quân qua `MovementRule`, không `if (piece is Knight)`.
  `SlidingRule`/`SteppingRule` thay thế nhau qua cùng contract.
- **ISP** — Interface nhỏ, role-focused: `InputSource` (cho raw text), `PieceFormatParser`
  (text → pieces), `MovementRule` (moves cho board), `ResultWriter` (ghi result set).
- **DIP** — `MoveEngine` phụ thuộc abstraction (`MovementRule`), concretions inject ở `main`.
  Proof: unit-test move calc với in-memory `Board` + `StringBuilder`, zero I/O.

### Table: class/interface <=> SOLID (cho design review)

| Nguyên lý | Type embody nó | Bằng chứng cụ thể |
|---|---|---|
| **SRP** | `PieceFormatParser`, `MovementRule`/`MoveEngine`, `ResultWriter` | 3 lý do thay đổi ở 3 package tách biệt; `model` chỉ giữ state |
| **OCP** | `MovementRule` (+ `RULES` map), `ResultWriter` (+ `WRITERS`), `PieceFormatParser` (+ `PARSERS`) | Phase 2/3/4 thêm quân/format/parser mà `MoveEngine` không đổi 1 dòng |
| **LSP** | `MovementRule` ← `SlidingRule`/`SteppingRule`/`PawnRule` | Engine gọi mọi quân qua interface; không `if (piece is X)` |
| **ISP** | `InputSource` (`read`), `PieceFormatParser` (`parse`), `MovementRule` (`movesFor`), `ResultWriter` (`write`) | Mỗi interface 1 method, không fat interface |
| **DIP** | `MoveEngine`←`MovementRule`; core ⊥ `chess.input` | Concretion inject ở `Main`; khoá bằng `ArchitectureBoundaryTest` |

### Extension points (cắm thêm ở đâu — đã có comment "MỞ RỘNG" trong code)

| Muốn thêm | Làm gì | Chạm code cũ? |
|---|---|---|
| Quân mới (Archbishop) | thêm `PieceType` + 1 dòng `RULES` (chọn Sliding/Stepping/rule mới) | Không |
| Output format (XML/text) | class mới `implements ResultWriter` + 1 dòng `WRITERS` | Không |
| Input format (FEN) | class mới `implements PieceFormatParser` + 1 dòng `PARSERS` | Không |
| Input source (URL) | class mới `implements InputSource` + 1 dòng `SOURCES` | Không |
| Luật board-level (đúng 1 vua) | thêm 1 bước trong `BoardValidator` | Chỉ `BoardValidator` |
| Phân loại lỗi chi tiết | subclass `ChessInputException` | Không (Main vẫn bắt 1 chỗ) |

Tất cả extension đều là **thêm class + 1 dòng wiring ở composition root**, không sửa lõi

---

## 9. Kế hoạch giao hàng theo Phase (đi tuần tự, KHÔNG refactor trước)

Chủ đích: cảm nhận **friction** khi phase sau buộc sửa code phase trước — ghi lại vào PR.

- **Phase 1** — Walking skeleton: chỉ **Rook + King**, board **hard-code**, in **Console**, end-to-end.
- **Phase 2** — Thêm Bishop, Queen, Knight, Pawn. Quan sát: có phải sửa switch trung tâm?
- **Phase 3** — Thêm **JSON** output cạnh Console. Quan sát: có đụng movement code?
- **Phase 4** — Thêm **file** source; rồi **YAML + JSON** parser cạnh notation.
  Quan sát: engine có phụ thuộc "nơi lấy piece"? Thêm format có đụng notation parser?
- **Phase 5 - Optional (stretch)** — XML/plain-text output hoặc FEN input, sửa càng ít code cũ càng tốt.

---

## 10. Anti-patterns cần tránh (nhắc lại từ đề)

Mục 6 của đề
