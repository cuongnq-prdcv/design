package chess.output;

import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceResult;
import chess.model.PieceType;
import chess.model.ResultSet;
import chess.model.Side;
import chess.model.Square;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonWriterTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    private final JsonWriter json = new JsonWriter();

    private String render(ResultSet rs) throws IOException {
        StringBuilder sb = new StringBuilder(); // zero I/O
        json.write(rs, sb);
        return sb.toString();
    }

    @Test
    void writesSinglePieceWithMoves() throws IOException {
        ResultSet rs = new ResultSet(List.of(
                new PieceResult(
                        new Piece(PieceType.ROOK, Side.WHITE, sq("A1")),
                        List.of(new Move(sq("A2"), false), new Move(sq("A8"), true)))));

        String out = render(rs);
        assertTrue(out.contains("\"side\": \"WHITE\""));
        assertTrue(out.contains("\"type\": \"ROOK\""));
        assertTrue(out.contains("\"position\": \"A1\""));
        assertTrue(out.contains("\"to\": \"A2\""));
        assertTrue(out.contains("\"to\": \"A8\""));
    }

    @Test
    void marksCaptureFlag() throws IOException {
        ResultSet rs = new ResultSet(List.of(
                new PieceResult(
                        new Piece(PieceType.BISHOP, Side.WHITE, sq("C4")),
                        List.of(new Move(sq("D5"), true)))));

        String out = render(rs);
        assertTrue(out.contains("\"to\": \"D5\", \"capture\": true"));
    }

    @Test
    void emptyMovesRendersEmptyArray() throws IOException {
        ResultSet rs = new ResultSet(List.of(
                new PieceResult(
                        new Piece(PieceType.KING, Side.WHITE, sq("E1")),
                        List.of())));

        String out = render(rs);
        assertTrue(out.contains("\"moves\": []"));
    }

    @Test
    void writesMultiplePiecesValidJson() throws IOException {
        ResultSet rs = new ResultSet(List.of(
                new PieceResult(new Piece(PieceType.ROOK, Side.WHITE, sq("A1")),
                        List.of(new Move(sq("A2"), false))),
                new PieceResult(new Piece(PieceType.KING, Side.BLACK, sq("E8")),
                        List.of(new Move(sq("E7"), false)))));

        String out = render(rs);
        // không có dấu phẩy thừa trước ] hoặc }
        assertFalse(out.replaceAll("\\s", "").contains(",]"));
        assertFalse(out.replaceAll("\\s", "").contains(",}"));
        // hai quân đều xuất hiện
        assertTrue(out.contains("\"position\": \"A1\""));
        assertTrue(out.contains("\"position\": \"E8\""));
        // cân bằng ngoặc
        assertBalanced(out);
    }

    @Test
    void sameResultSetRendersBothFormats() throws IOException {
        ResultSet rs = new ResultSet(List.of(
                new PieceResult(new Piece(PieceType.ROOK, Side.WHITE, sq("A1")),
                        List.of(new Move(sq("A2"), false)))));

        StringBuilder jsonOut = new StringBuilder();
        new JsonWriter().write(rs, jsonOut);
        StringBuilder consoleOut = new StringBuilder();
        new ConsoleWriter().write(rs, consoleOut);

        // cùng ResultSet, hai format khác nhau, đều chạy qua interface ResultWriter
        assertTrue(jsonOut.toString().contains("\"position\": \"A1\""));
        assertTrue(consoleOut.toString().contains("WHITE ROOK A1 ->"));
    }

    private static void assertBalanced(String s) {
        int brace = 0, bracket = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
                case '{' -> brace++;
                case '}' -> brace--;
                case '[' -> bracket++;
                case ']' -> bracket--;
                default -> { }
            }
        }
        assertTrue(brace == 0 && bracket == 0, "unbalanced braces/brackets");
    }
}
