package chess.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Phase 1 — xem specs/phase-1.md (Chặng ① trong docs/diagrams.md).
 * Bàn cờ: giữ các quân theo ô và trả lời truy vấn "ô X có gì?".
 * Chỉ giữ STATE — không I/O, không luật đi, không định dạng.
 */
public final class Board {

    private final Map<Square, Piece> bySquare;

    public Board(List<Piece> pieces) {
        Map<Square, Piece> map = new LinkedHashMap<>();
        for (Piece p : pieces) {
            map.put(p.position(), p);
        }
        this.bySquare = map;
    }

    /** Quân ở ô (nếu có). */
    public Optional<Piece> pieceAt(Square square) {
        return Optional.ofNullable(bySquare.get(square));
    }

    /** Ô có trống không. */
    public boolean isEmpty(Square square) {
        return !bySquare.containsKey(square);
    }

    /** Tất cả quân trên bàn, giữ thứ tự đưa vào. */
    public List<Piece> pieces() {
        return List.copyOf(bySquare.values());
    }
}
