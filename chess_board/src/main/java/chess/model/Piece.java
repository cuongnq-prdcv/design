package chess.model;

/** Một quân cờ: type + side + vị trí. Model thuần, không biết luật đi của chính nó. */
public record Piece(PieceType type, Side side, Square position) {
}
