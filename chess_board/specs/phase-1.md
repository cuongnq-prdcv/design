# Phase 1 Spec — Walking Skeleton (Rook + King, hard-code, Console)

> Trạng thái: **DONE (verified)**. Backfill từ code + test thực tế.
> Tham chiếu: `../design.md`, `../plan.md`.

## 1. Scope

**Làm:**
- Model thuần: `PieceType`, `Side`, `Square`, `Piece`, `Move`, `PieceResult`, `ResultSet`, `Board`.
- `MovementRule` + `SlidingRule` (Rook) + `SteppingRule` (King), có luật chặn/ăn chung.
- `MoveEngine` tra `Map<PieceType, MovementRule>` (không switch).
- `ResultWriter` + `ConsoleWriter` ghi vào `Appendable`.
- `Main`: board hard-code (Rook+King), in Console, end-to-end.

**Cố tình CHƯA làm (để phase sau cảm nhận friction):**
- Không registry/CLI, không parser, không file I/O, không `BoardValidator`.
- Chỉ 2 loại quân: ROOK, KING.

## 2. Inputs / Outputs

- **Input:** board hard-code trong `Main` (không đọc ngoài).
- **Output:** text ra console, mỗi quân 1 dòng:
  ```
  WHITE ROOK A1 -> [B1, C1, D1, A2, A3, A4, A5, A6, A7, A8(x)]
  WHITE KING E1 -> [F1, D1, E2, F2, D2]
  ```
  `(x)` = nước ăn quân.

## 3. Cases phải xử lý (checklist truy vết)

| # | Case | Input | Expected | Test cover | SOLID | ✓ |
|---|------|-------|----------|-----------|-------|---|
| 1 | Rook trên bàn trống tới các cạnh | Rook A1, bàn trống | 14 nước; có A8, H1 | `SlidingRuleRookTest.rookOnEmptyBoardReachesEdges` | LSP (rule qua abstraction) | ✅ |
| 2 | Rook dừng trước quân cùng phe | Rook A1 + King(W) A4 | tới A2,A3; KHÔNG A4,A5 | `SlidingRuleRookTest.rookStopsBeforeFriendlyAndDoesNotEnter` | — | ✅ |
| 3 | Rook ăn quân địch đầu tiên rồi dừng | Rook A1 + Rook(B) A4 | A2,A3,A4(x); KHÔNG A5 | `SlidingRuleRookTest.rookCapturesFirstEnemyThenStops` | — | ✅ |
| 4 | King ở giữa có 8 nước | King D4, bàn trống | 8 nước | `SteppingRuleKingTest.kingInCenterHasEightMoves` | — | ✅ |
| 5 | King ở góc bị giới hạn biên | King A1, bàn trống | chỉ B1,A2,B2 | `SteppingRuleKingTest.kingInCornerIsClampedToBoard` | — | ✅ |
| 6 | King không vào ô cùng phe, ăn địch | King D4 + friendly D5 + enemy E5 | KHÔNG D5; E5(x) | `SteppingRuleKingTest.kingDoesNotEnterFriendlyButCapturesEnemy` | — | ✅ |
| 7 | Pipeline compute+render zero I/O | Rook A1 + King(W) E1 | text chứa các dòng, D1 | `MoveEnginePipelineTest.pipelineComputesAndRendersWithoutRealIo` | DIP (StringBuilder, no I/O) | ✅ |

## 4. Acceptance criteria

- [x] `gradle run` in đúng nước đi Rook + King cho board hard-code.
- [x] Rook chặn đúng bởi quân cùng/khác phe; King đúng biên.
- [x] `MoveEngine` không có `switch`/`if` theo piece type.
- [x] Test compute+render chạy zero I/O (DIP proof).
- [x] `gradle test` xanh (7/7 tại thời điểm Phase 1).

## 5. Trace / Verification log

- **Run:** `gradle run` →
  ```
  WHITE ROOK A1 -> [B1, C1, D1, A2, A3, A4, A5, A6, A7, A8(x)]
  WHITE KING E1 -> [F1, D1, E2, F2, D2]
  BLACK ROOK A8 -> [B8, C8, D8, A7, A6, A5, A4, A3, A2, A1(x)]
  BLACK KING E8 -> [F8, D8, E7, F7, D7]
  ```
- **Test:** 7/7 PASSED.
- **Friction:** baseline, chưa có. Đã chủ động gom `SlidingRule`/`SteppingRule` theo danh sách
  hướng (không viết `RookRule`/`KingRule` cứng) — đặt nền cho Phase 2.

## 6. Files chạm tới

- `src/main/java/chess/model/*` (toàn bộ record + Board)
- `src/main/java/chess/rules/{MovementRule,Direction,SlidingRule,SteppingRule}.java`
- `src/main/java/chess/engine/MoveEngine.java`
- `src/main/java/chess/output/{ResultWriter,ConsoleWriter}.java`
- `src/main/java/chess/app/Main.java`
- `src/test/java/chess/rules/{SlidingRuleRookTest,SteppingRuleKingTest}.java`
- `src/test/java/chess/engine/MoveEnginePipelineTest.java`
