# Phase 2 Spec — Thêm piece types (Bishop, Queen, Knight, Pawn)

> Trạng thái: **DONE (verified)**. Backfill từ code + test thực tế.
> Tham chiếu: `../design.md`, `../plan.md`, `phase-1.md`.

## 1. Scope

**Làm:**
- `PawnRule` (class mới) — luật Pawn đơn giản hoá.
- Đăng ký Bishop/Queen/Knight qua `SlidingRule`/`SteppingRule` có sẵn (chỉ thêm dòng map).
- Cập nhật board hard-code trong `Main` có đủ 6 loại quân để minh hoạ.

**Cố tình CHƯA làm:** vẫn chưa có parser/source/validator/registry (để Phase 3–4).

**Scope-out luật cờ:** không en passant, không phong cấp, không nhập thành, không chiếu/pin.

## 2. Inputs / Outputs

- **Input:** board hard-code (6 loại quân).
- **Output:** như Phase 1, thêm các dòng cho Bishop/Queen/Knight/Pawn, ví dụ:
  ```
  WHITE BISHOP C4 -> [D5(x), D3, B5, A6, B3, A2]
  WHITE QUEEN D1 -> [C1, D2, D3, D4, D5(x), C2, B3, A4]
  WHITE PAWN E2 -> [E3, E4]
  BLACK PAWN D5 -> [D4, C4(x)]
  ```

## 3. Cases phải xử lý (checklist truy vết)

| # | Case | Input | Expected | Test cover | SOLID | ✓ |
|---|------|-------|----------|-----------|-------|---|
| 1 | Bishop chỉ đi chéo | Bishop C1, bàn trống | có A3, H6; KHÔNG C2 | `SlidingSteppingReuseTest.bishopSlidesDiagonalsOnly` | OCP (tái dùng SlidingRule) | ✅ |
| 2 | Queen = Rook + Bishop | Queen D4, bàn trống | 27 nước | `SlidingSteppingReuseTest.queenIsRookPlusBishop` | OCP/LSP | ✅ |
| 3 | Knight 8 nước chữ L | Knight D4, bàn trống | 8 nước | `SlidingSteppingReuseTest.knightJumpsEightLShapes` | OCP (tái dùng SteppingRule) | ✅ |
| 4 | Knight nhảy qua quân chặn | Knight B1 + chặn B2,C2 | vẫn có A3,C3,D2 | `SlidingSteppingReuseTest.knightJumpsOverBlockers` | — | ✅ |
| 5 | Pawn trắng ở hàng xuất phát đi 1 hoặc 2 | Pawn E2 | E3, E4 | `PawnRuleTest.whitePawnOnStartCanMoveOneOrTwo` | SRP (luật Pawn tách riêng) | ✅ |
| 6 | Pawn không ở hàng xuất phát chỉ đi 1 | Pawn E3 | chỉ E4 | `PawnRuleTest.whitePawnNotOnStartMovesOneOnly` | — | ✅ |
| 7 | Pawn bị chặn ngay trước mặt | Pawn E2 + enemy E3 | rỗng (không đi, không ăn thẳng) | `PawnRuleTest.pawnBlockedCannotMoveForward` | — | ✅ |
| 8 | Pawn nước đôi bị chặn ô thứ 2 | Pawn E2 + enemy E4 | chỉ E3 | `PawnRuleTest.pawnTwoSquareBlockedBySecondSquare` | — | ✅ |
| 9 | Pawn ăn chéo, không ăn thẳng | Pawn E2 + enemy D3,F3 | D3(x),F3(x),E3,E4 | `PawnRuleTest.pawnCapturesDiagonallyNotForwardEnemy` | — | ✅ |
| 10 | Pawn không ăn quân cùng phe chéo | Pawn E2 + friendly D3 | KHÔNG D3 | `PawnRuleTest.pawnDoesNotCaptureFriendlyDiagonal` | — | ✅ |
| 11 | Pawn đen đi xuống | Pawn(B) D7 | D6, D5 | `PawnRuleTest.blackPawnMovesDownward` | — | ✅ |

## 4. Acceptance criteria

- [x] Cả 6 quân tính đúng nước đi.
- [x] `MoveEngine` KHÔNG đổi so với Phase 1 (OCP đạt cho engine).
- [x] Thêm Bishop/Queen/Knight chỉ là thêm dòng map (0 class mới).
- [x] Pawn có class riêng, cover đủ: đi 1, đi 2, chặn, ăn chéo, không ăn cùng phe, phe đen.
- [x] `gradle test` xanh (18/18).

## 5. Trace / Verification log

- **Run:** `gradle run` → in đúng 6 loại quân (Bishop D5(x), Queen thẳng+chéo, Knight nhảy qua chặn,
  Pawn E2->[E3,E4], Pawn đen D5->[D4, C4(x)]).
- **Test:** 18/18 PASSED (7 cũ + 7 PawnRuleTest + 4 SlidingSteppingReuseTest).
- **Friction:** gần như không. `MoveEngine` không đổi 1 dòng. 3/4 quân mới chỉ tốn 1 dòng map.
  Pawn cần class riêng vì luật khác bản chất (hướng đi ≠ hướng ăn) — abstraction worth it.

## 6. Files chạm tới

- **Mới:** `src/main/java/chess/rules/PawnRule.java`
- **Sửa:** `src/main/java/chess/app/Main.java` (thêm 4 dòng map + board 6 quân + import PawnRule)
- **Test mới:** `src/test/java/chess/rules/{PawnRuleTest,SlidingSteppingReuseTest}.java`
- **KHÔNG đổi:** `MoveEngine`, `MovementRule`, `SlidingRule`, `SteppingRule`, toàn bộ `model`, `output`.
