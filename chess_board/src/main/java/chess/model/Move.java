package chess.model;

/** Một nước đi tới ô target; capture=true nếu nước này ăn quân địch. */
public record Move(Square target, boolean capture) {
}
