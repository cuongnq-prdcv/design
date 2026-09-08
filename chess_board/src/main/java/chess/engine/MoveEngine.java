package chess.engine;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceResult;
import chess.model.PieceType;
import chess.model.ResultSet;
import chess.rules.MovementRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Phase 1 — xem specs/phase-1.md (Chặng ② trong docs/diagrams.md).
 *
 * Tính result set cho cả bàn cờ.
 * KHÔNG biết quân cụ thể — tra Map<PieceType, MovementRule> được inject.
 * Không I/O, không switch(type). Test được với in-memory Board.
 *
 * ỔN ĐỊNH QUA CÁC PHASE: class này KHÔNG đổi từ Phase 1 → Phase 4 dù thêm quân/format/input —
 */
public final class MoveEngine {

    private final Map<PieceType, MovementRule> rules;

    public MoveEngine(Map<PieceType, MovementRule> rules) {
        this.rules = Map.copyOf(rules);
    }

    public ResultSet compute(Board board) {
        List<PieceResult> results = new ArrayList<>();
        for (Piece piece : board.pieces()) {
            MovementRule rule = rules.get(piece.type());
            if (rule == null) {
                throw new IllegalStateException(
                        "No movement rule registered for piece type: " + piece.type());
            }
            results.add(new PieceResult(piece, rule.movesFor(piece, board)));
        }
        return new ResultSet(results);
    }
}
