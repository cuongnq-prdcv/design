package chess.model;

import java.util.List;

/** Kết quả cho một quân: bản thân quân + danh sách nước đi khả dĩ. */
public record PieceResult(Piece piece, List<Move> moves) {
}
