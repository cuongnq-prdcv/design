package chess.engine;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import chess.model.Square;
import chess.output.ConsoleWriter;
import chess.rules.Direction;
import chess.rules.MovementRule;
import chess.rules.SlidingRule;
import chess.rules.SteppingRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** DIP proof: tính move + render với in-memory board + StringBuilder, không I/O thật. */
class MoveEnginePipelineTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    @Test
    void pipelineComputesAndRendersWithoutRealIo() throws IOException {
        Board board = new Board(List.of(
                new Piece(PieceType.ROOK, Side.WHITE, sq("A1")),
                new Piece(PieceType.KING, Side.WHITE, sq("E1"))
        ));

        Map<PieceType, MovementRule> rules = Map.of(
                PieceType.ROOK, new SlidingRule(Direction.ORTHOGONAL),
                PieceType.KING, new SteppingRule(Direction.ALL_8)
        );

        var result = new MoveEngine(rules).compute(board);

        StringBuilder out = new StringBuilder();
        new ConsoleWriter().write(result, out);
        String text = out.toString();

        assertTrue(text.contains("WHITE ROOK A1 ->"));
        assertTrue(text.contains("WHITE KING E1 ->"));
        // Rook trắng bị King trắng chặn ở E1 -> chỉ tới D1 theo hướng đông
        assertTrue(text.contains("D1"));
    }
}
