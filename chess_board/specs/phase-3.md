# Phase 3 Spec — Thêm output format JSON (cạnh Console)

> Trạng thái: **DONE (verified)**.
> Tham chiếu: `../design.md`, `../plan.md`, `phase-1.md`, `phase-2.md`.

## 1. Scope

**Làm:**
- `JsonWriter implements ResultWriter` — render `ResultSet` ra JSON vào `Appendable`.
- Viết JSON **tay** (không thêm dependency ở phase này) để giữ gọn và tránh reflection.
- Cho phép `Main` chọn writer (tạm thời hard-code chọn hoặc chọn qua 1 biến; registry để Phase 4).

**Cố tình CHƯA làm:** chưa có registry/CLI đầy đủ, chưa parser/source/validator (Phase 4).

**Ràng buộc OCP/SRP:** thêm JSON output **KHÔNG** được đụng `MoveEngine`, `MovementRule`,
bất kỳ `*Rule`, hay `model`. Chỉ thêm 1 class writer + wiring ở `Main`.

## 2. Inputs / Outputs

- **Input:** cùng `ResultSet` mà engine sinh ra (không đổi nguồn).
- **Output JSON** (đề xuất shape):
  ```json
  {
    "results": [
      {
        "side": "WHITE",
        "type": "ROOK",
        "position": "A1",
        "moves": [
          { "to": "A2", "capture": false },
          { "to": "A8", "capture": true }
        ]
      }
    ]
  }
  ```
- JSON phải hợp lệ: escape đúng, không dấu phẩy thừa, parse lại được.

## 3. Cases phải xử lý (checklist truy vết)

| # | Case | Input | Expected | Test cover | SOLID | ✓ |
|---|------|-------|----------|-----------|-------|---|
| 1 | Render 1 quân có moves | ResultSet 1 piece, vài move | JSON có side/type/position/moves đúng | `JsonWriterTest.writesSinglePieceWithMoves` | SRP (render tách khỏi engine) | ✅ |
| 2 | Đánh dấu capture đúng | move có capture=true | `"capture": true` cho move đó | `JsonWriterTest.marksCaptureFlag` | — | ✅ |
| 3 | Quân không có nước đi | ResultSet 1 piece, moves rỗng | `"moves": []` (mảng rỗng hợp lệ) | `JsonWriterTest.emptyMovesRendersEmptyArray` | — | ✅ |
| 4 | Nhiều quân phân tách đúng | ResultSet 2 piece | 2 phần tử, JSON hợp lệ (không phẩy thừa) | `JsonWriterTest.writesMultiplePiecesValidJson` | — | ✅ |
| 5 | Cùng ResultSet render được cả Console lẫn JSON | 1 ResultSet | cả 2 writer chạy qua cùng `ResultWriter` | `JsonWriterTest.sameResultSetRendersBothFormats` | OCP (pluggable format) | ✅ |
| 6 | Ghi zero I/O (StringBuilder) | ResultSet | không cần I/O thật | (mọi test trên dùng StringBuilder) | DIP | ✅ |

## 4. Acceptance criteria

- [x] `JsonWriter` render ra JSON hợp lệ (escape, không phẩy thừa, parse lại được).
- [x] Cùng `ResultSet` render được cả Console lẫn JSON qua cùng interface `ResultWriter`.
- [x] KHÔNG đổi `MoveEngine` / `*Rule` / `model` (xem "Files chạm tới").
- [x] Test JSON dùng `StringBuilder` (zero I/O).
- [x] `gradle test` xanh.

## 5. Trace / Verification log

- **Run JSON:** `gradle run --args="--out json"` → JSON đúng shape; pipe qua `python3 json.load`
  báo **VALID JSON, pieces = 10**.
- **Run Console:** `gradle run` (mặc định `--out console`) → vẫn in đúng như Phase 2.
- **Test:** `gradle test --rerun-tasks` → **23/23 PASSED** (18 cũ + 5 `JsonWriterTest`).
- **Friction:** KHÔNG. Thêm JSON output chỉ tốn 1 class `JsonWriter` + wiring `--out` ở `Main`.
  KHÔNG đụng `MoveEngine`/`*Rule`/`model` → SRP/OCP đạt: format tách hẳn khỏi movement/engine.
  Lưu ý: `switch(out)` ở `Main` là chọn FORMAT ở composition root, không phải `switch(pieceType)`
  trong engine/output layer — sẽ thay bằng registry ở Phase 4.

## 6. Files chạm tới

- **Mới:** `src/main/java/chess/output/JsonWriter.java`; `src/test/java/chess/output/JsonWriterTest.java`
- **Sửa:** `src/main/java/chess/app/Main.java` (thêm chọn `--out`, helper `argValue`, import JsonWriter)
- **KHÔNG đổi:** `MoveEngine`, mọi `*Rule`, `MovementRule`, toàn bộ `model`, `ConsoleWriter`,
  `ResultWriter` (interface giữ nguyên → chứng minh format cắm thêm mà không sửa abstraction).
