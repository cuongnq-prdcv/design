# Diagrams

| Ký hiệu Mermaid | Loại quan hệ | Ý nghĩa |
|---|---|---|
| `A <\|.. B` | Realization | `B` implements interface `A` |
| `A --> B : label` | Association | `A` giữ tham chiếu tới `B` (label = vai trò) |
| `A ..> B : uses` | Dependency | `A` dùng `B` thoáng qua (tham số/biến cục bộ) |
| `A o-- "*" B` | Aggregation | `A` gom nhiều `B`; `"1" / "*"` là multiplicity |

Multiplicity: `1` = đúng một, `*` = không hoặc nhiều, `0..1` = tối đa một.

---

## Overview

```mermaid
flowchart LR
    Z["Input<br/>(source × format → Board)"] --> B["MoveEngine<br/>(hỏi luật cho từng quân)"]
    B --> C["MovementRule<br/>(tính nước đi 1 quân)"]
    C --> B
    B --> D["ResultWriter<br/>(in ra Console/JSON)"]
```

- **⓪ Input** đọc bytes (file/console) rồi parse (notation/YAML/JSON) → validate → `Board`.
- **① Board** (kết quả của chặng ⓪) giữ trạng thái: ô nào có quân gì.
- **② MoveEngine** duyệt từng quân, tra bảng luật, gom kết quả.
- **③ MovementRule** : cho 1 quân + bàn cờ → danh sách nước đi.
- **④ ResultWriter** in kết quả theo dạng dưới Console/JSON

Mỗi chặng = một trách nhiệm tách biệt (SRP). Dưới đây soi từng chặng.

---

## Mục ⓪ — Nạp input (2 chiều trực giao: source × format)

Trước khi có `Board`, phải lấy quân từ đâu đó. Đây là phần đề soi kỹ nhất: **"nơi lấy bytes"** và
**"cách hiểu bytes"** là hai chiều ĐỘC LẬP, gặp nhau ở `String`.

```mermaid
classDiagram
    direction LR
    class InputSource {
        <<interface>>
        +read() String
    }
    class ConsoleInputSource
    class FileInputSource

    class PieceFormatParser {
        <<interface>>
        +parse(String) List~Piece~
    }
    class NotationParser
    class TreeFormatParser {
        <<abstract>>
    }
    class JsonParser
    class YamlParser

    class BoardValidator {
        +validate(List~Piece~) Board
    }
    class ChessInputException {
        <<exception>>
    }

    InputSource <|.. ConsoleInputSource
    InputSource <|.. FileInputSource
    PieceFormatParser <|.. NotationParser
    PieceFormatParser <|.. TreeFormatParser
    TreeFormatParser <|-- JsonParser
    TreeFormatParser <|-- YamlParser

    ConsoleInputSource ..> ChessInputException : throws
    FileInputSource ..> ChessInputException : throws
    PieceFormatParser ..> ChessInputException : throws
    BoardValidator ..> ChessInputException : throws
    BoardValidator ..> Board : builds
```

**Cách đọc — hai chiều gặp nhau ở `String`:**
1. **Chiều SOURCE** (`InputSource`): chỉ trả `String`, KHÔNG biết định dạng. `ConsoleInputSource`
   (stdin), `FileInputSource` (file).
2. **Chiều FORMAT** (`PieceFormatParser`): nhận `String` → `List<Piece>`, KHÔNG biết nguồn.
   `NotationParser` (compact) đứng riêng; `JsonParser`/`YamlParser` chia sẻ `TreeFormatParser`
   (chỉ khác nhau ObjectMapper JSON vs YAML).
3. Ghép ở composition root: `parser.parse(source.read())` → **bất kỳ format nào × bất kỳ source nào**.
4. **Validate một chỗ**: `BoardValidator` biến `List<Piece>` → `Board`, kiểm tra trùng ô (off-board
   đã chặn khi tạo `Square`). KHÔNG lặp validate trong từng parser.
5. **Lỗi hội tụ**: mọi vấn đề (file thiếu, cú pháp sai, unknown type, trùng ô) → `ChessInputException`,
   `Main` bắt một chỗ, in clear error + exit ≠ 0.

> Điểm mấu chốt (đề soi): thêm một FORMAT mới (ví dụ FEN) KHÔNG đụng source hay parser khác; thêm
> một SOURCE mới (ví dụ URL) KHÔNG đụng parser. Đó là vì ranh giới giữa hai chiều chỉ là `String`.

### Pipeline nạp input → kết quả (đầy đủ)

```mermaid
sequenceDiagram
    autonumber
    participant M as Main (composition root)
    participant S as InputSource
    participant P as PieceFormatParser
    participant V as BoardValidator
    participant E as MoveEngine
    participant W as ResultWriter
    M->>S: read()
    S-->>M: raw String
    M->>P: parse(raw)
    P-->>M: List~Piece~
    M->>V: validate(pieces)
    V-->>M: Board  (hoặc ném ChessInputException)
    M->>E: compute(board)
    E-->>M: ResultSet
    M->>W: write(resultSet, out)
```

---

## Mục ① — Dữ liệu trên bàn cờ (Bàn cờ, quân cờ, vị trí ô)

Đây là các "danh từ" của bài: quân, ô, bàn cờ. Chúng **chỉ giữ dữ liệu**, không tính toán, không in.

```mermaid
classDiagram
    direction LR
    class Piece {
        +PieceType type
        +Side side
        +Square position
    }
    class Square {
        +int file  0..7 = A..H
        +int rank  0..7 = 1..8
        +onBoard(f,r)$ boolean
    }
    class Board {
        +pieceAt(Square) Optional~Piece~
        +isEmpty(Square) boolean
        +pieces() List~Piece~
    }
    Piece --> Square : occupies
    Board "1" o-- "*" Piece : contains
```

**Cách đọc:**
1. Một `Piece` biết 3 điều: loại (`type`), phe (`side`), đang đứng ở `Square` nào.
2. `Square` là toạ độ (file, rank) — có hàm `onBoard` để chặn ra ngoài bàn.
3. `Board` là "bản đồ" tra cứu: đưa một ô → trả quân ở đó (hoặc rỗng). Đây là thứ mọi luật đi
   sẽ hỏi để biết đường có bị chặn không.

> Ghi nhớ: `Board` chỉ trả lời câu hỏi "ô này có gì?", **không** tự tính nước đi.

---

## Mục ③ — Rule (MovementRule)

*(Xem chặng ③ trước ② vì hiểu luật 1 quân rồi mới thấy engine điều phối thế nào.)*

Mọi quân đều trả lời **cùng một câu hỏi**: "cho tôi bàn cờ này, tôi đi được những ô nào?".
Câu hỏi đó là interface `MovementRule`. Có 3 cách trả lời:

```mermaid
classDiagram
    direction TB
    class MovementRule {
        <<interface>>
        +movesFor(Piece, Board) List~Move~
    }
    class SlidingRule {
        -List~Direction~ directions
    }
    class SteppingRule {
        -List~Direction~ offsets
    }
    class PawnRule {
        +movesFor(Piece, Board) List~Move~
    }
    class Direction {
        +int df
        +int dr
    }
    MovementRule <|.. SlidingRule
    MovementRule <|.. SteppingRule
    MovementRule <|.. PawnRule
    SlidingRule ..> Direction : uses
    SteppingRule ..> Direction : uses
```

**Note:**
1. **SlidingRule** (trượt): đi theo mỗi hướng cho tới khi gặp mép bàn / quân cùng phe (dừng) /
   quân địch (ăn rồi dừng). Chỉ khác nhau ở **danh sách hướng**:
   - Rook = 4 hướng thẳng, Bishop = 4 hướng chéo, Queen = cả 8 hướng.
2. **SteppingRule** (nhảy 1 nhịp): thử từng offset cố định, đúng 1 bước:
   - King = 8 ô quanh, Knight = 8 offset chữ L.
3. **PawnRule** (riêng): đi thẳng vào ô trống, ăn chéo vào quân địch — hướng đi ≠ hướng ăn nên
   không nhét chung được vào 2 họ trên.

> Điểm mấu chốt: cùng 6 quân nhưng chỉ **3 class** nhờ 2 họ dùng lại theo "danh sách hướng"
> (`Direction`). Thêm quân mới chỉ là chọn bộ hướng — không viết engine lại (OCP).

### Một quân được tính như thế nào? (ví dụ Rook ở A1)

```mermaid
flowchart TD
    start(["Rook A1 gọi movesFor"]) --> dir{"với mỗi hướng<br/>(lên/xuống/trái/phải)"}
    dir --> step["bước 1 ô theo hướng"]
    step --> onb{"còn trong bàn?"}
    onb -- không --> dir
    onb -- có --> occ{"ô đó có gì?"}
    occ -- trống --> add["thêm nước đi<br/>đi tiếp cùng hướng"]
    add --> step
    occ -- quân cùng phe --> stop["dừng hướng này<br/>(không vào)"]
    occ -- quân địch --> cap["thêm nước ĂN (x)<br/>rồi dừng hướng này"]
    stop --> dir
    cap --> dir
```

**Note:**
1. Với mỗi hướng, bước tiếp khi ô trống.
2. Gặp quân cùng phe thì dừng.
3. Gặp quân địch thì thêm nước ăn `(x)` rồi dừng.

---

## Mục ② — MoveEngine

Engine **không biết** luật của từng quân. Nó chỉ có một bảng tra `PieceType → MovementRule` và
lần lượt hỏi từng quân.

```mermaid
classDiagram
    direction LR
    class MoveEngine {
        -Map~PieceType,MovementRule~ rules
        +compute(Board) ResultSet
    }
    class MovementRule {
        <<interface>>
    }
    MoveEngine o-- "*" MovementRule : dispatches to
    note for MoveEngine "Depends on MovementRule abstraction.\nNo switch(type) — same treatment for every piece."
```

```mermaid
sequenceDiagram
    autonumber
    participant E as MoveEngine
    participant B as Board
    participant R as MovementRule (của quân)
    E->>B: pieces()  (lấy danh sách quân)
    loop mỗi quân
        E->>E: rules.get(piece.type)  (tra bảng luật)
        E->>R: movesFor(piece, board)
        R->>B: pieceAt / isEmpty  (kiểm tra chặn & ăn)
        R-->>E: List~Move~
        E->>E: gói thành PieceResult
    end
    E-->>E: trả về ResultSet (gộp tất cả)
```

**Note:**
1. Engine lấy danh sách quân từ `Board`.
2. Với mỗi quân: tra bảng luật theo `type` → được đúng `MovementRule` của quân đó.
3. Hỏi rule "đi được đâu?" → nhận `List<Move>`, gói thành `PieceResult`.
4. Gộp tất cả thành `ResultSet`.

> Vì engine chỉ nói chuyện với interface `MovementRule` (không `if quân là Knight`), thêm quân
> mới **không** đụng engine. Đây là LSP + OCP + DIP thể hiện cùng lúc.

---

## Mục ④ — Result

`ResultSet` là dữ liệu thuần. Việc "biến thành chữ" giao cho writer — mỗi format một class.

```mermaid
classDiagram
    direction LR
    class ResultWriter {
        <<interface>>
        +write(ResultSet, Appendable)
    }
    class ConsoleWriter {
        +write(ResultSet, Appendable)
    }
    class JsonWriter {
        +write(ResultSet, Appendable)
    }
    ResultWriter <|.. ConsoleWriter
    ResultWriter <|.. JsonWriter
```

**Note:**
1. Cùng một `ResultSet`, đưa cho `ConsoleWriter` ra text dễ đọc, đưa cho `JsonWriter` ra JSON.
2. `write` ghi vào `Appendable` (một "cái phễu" chung): production dùng `System.out`, test dùng
   `StringBuilder` → **test không cần I/O thật** (DIP proof).
3. Thêm format mới (XML/text...) = thêm 1 class writer, không đụng engine/luật đi.

---

## Mục ⑤ — Main (Ghép các đối tượng + rule...)

Đây là chỗ **duy nhất** biết tất cả các mảnh ghép cụ thể và nối chúng lại. Ba trục chọn qua
**registry + cờ CLI**; lỗi input hội tụ và được bắt tại đây.

```mermaid
flowchart TD
    Main["Main.main(args)"]
    Main --> src["resolveSource(--source, --file)<br/>SOURCES registry → InputSource"]
    Main --> par["PARSERS registry[--format]<br/>→ PieceFormatParser"]
    Main --> wr["WRITERS registry[--out]<br/>→ ResultWriter"]
    src --> read["source.read() → String"]
    par --> parse["parser.parse(String) → List~Piece~"]
    read --> parse
    parse --> val["BoardValidator.validate() → Board"]
    val --> run["MoveEngine(RULES).compute(board) → ResultSet"]
    run --> write["writer.write(resultSet, out)"]
    wr --> write
    write --> stdout["System.out"]
    Main -. catch .-> err["ChessInputException<br/>→ stderr + exit ≠ 0"]
```

**Note:** `Main` (1) tra 3 registry theo cờ CLI để lấy source/parser/writer, (2) chạy pipeline
`read → parse → validate → compute → write`, (3) bọc toàn bộ trong try/catch để mọi
`ChessInputException` in clear error + thoát mã ≠ 0. Các mảnh còn lại **nhận** dependency, không tự
`new` lẫn nhau.

> Extension point: thêm 1 source/format/writer = thêm 1 dòng vào registry tương ứng (`SOURCES`,
> `PARSERS`, `WRITERS`); thêm 1 quân = thêm 1 dòng vào `RULES`. Không sửa lõi.

---

## Follow step (Hiểu logic)

1. **Overview** — nắm 5 chặng.
2. **Mục ⓪** — hiểu cách nạp input (source × format → Board).
3. **Mục ①** — hiểu dữ liệu (Piece/Square/Board).
4. **Mục ③** — hiểu 1 quân tính nước đi ra sao (kèm flowchart Rook).
5. **Mục ②** — hiểu engine điều phối nhiều quân.
6. **Mục ④** — hiểu xuất kết quả nhiều format.
7. **Mục ⑤** — hiểu nơi ráp nối (registry + pipeline + xử lý lỗi).

Mỗi mục là một trách nhiệm (SRP); các mũi tên giữa chặng luôn đi qua **interface** (OCP/LSP/DIP).

---

## 

```mermaid
flowchart LR
    app["chess.app<br/>Main (composition root)"]
    input["chess.input<br/>InputSource, PieceFormatParser,<br/>Notation/Json/Yaml, BoardValidator,<br/>ChessInputException"]
    model["chess.model<br/>Piece, Board, Move,<br/>ResultSet, Square..."]
    rules["chess.rules<br/>MovementRule, Sliding/Stepping,<br/>PawnRule, Direction"]
    engine["chess.engine<br/>MoveEngine"]
    output["chess.output<br/>ResultWriter,<br/>ConsoleWriter, JsonWriter"]

    app --> input
    app --> engine
    app --> rules
    app --> output
    app --> model
    input --> model
    engine --> model
    engine --> rules
    rules --> model
    output --> model
```

**Chiều phụ thuộc:** mọi thứ hướng về `model` (state thuần), không phụ thuộc vòng.
Đặc biệt `engine`/`rules`/`output` **KHÔNG** phụ thuộc `chess.input` — ranh giới này được khoá bằng
`ArchitectureBoundaryTest` (fitness function). `app` là nơi duy nhất biết tất cả concretion.
