package chess.app;

import chess.engine.MoveEngine;
import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.ResultSet;
import chess.model.Side;
import chess.model.Square;
import chess.output.ConsoleWriter;
import chess.output.JsonWriter;
import chess.output.ResultWriter;
import chess.rules.Direction;
import chess.rules.MovementRule;
import chess.rules.PawnRule;
import chess.rules.SlidingRule;
import chess.rules.SteppingRule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Composition root — nơi DUY NHẤT ráp mọi thứ.
 * PHASE 1: board hard-code, chỉ Rook + King, in Console.
 * Chưa có registry/parser/file — cố tình, để phase sau cảm nhận friction.
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        // 1. Board hard-code (hợp lệ, chưa cần validator)
        Board board = new Board(List.of(
                new Piece(PieceType.ROOK, Side.WHITE, sq("A1")),
                new Piece(PieceType.KNIGHT, Side.WHITE, sq("B1")),
                new Piece(PieceType.BISHOP, Side.WHITE, sq("C4")),
                new Piece(PieceType.QUEEN, Side.WHITE, sq("D1")),
                new Piece(PieceType.KING, Side.WHITE, sq("E1")),
                new Piece(PieceType.PAWN, Side.WHITE, sq("E2")),
                new Piece(PieceType.PAWN, Side.WHITE, sq("B2")),
                new Piece(PieceType.ROOK, Side.BLACK, sq("A8")),
                new Piece(PieceType.KING, Side.BLACK, sq("E8")),
                new Piece(PieceType.PAWN, Side.BLACK, sq("D5"))
        ));

        // 2. Bảng luật — thêm quân = thêm 1 dòng ở đây (engine KHÔNG đổi)
        Map<PieceType, MovementRule> rules = Map.of(
                PieceType.ROOK, new SlidingRule(Direction.ORTHOGONAL),
                PieceType.BISHOP, new SlidingRule(Direction.DIAGONAL),
                PieceType.QUEEN, new SlidingRule(Direction.ALL_8),
                PieceType.KING, new SteppingRule(Direction.ALL_8),
                PieceType.KNIGHT, new SteppingRule(Direction.KNIGHT),
                PieceType.PAWN, new PawnRule()
        );

        // 3. Chạy pipeline
        ResultSet result = new MoveEngine(rules).compute(board);

        // 4. Chọn writer theo --out (mặc định console); registry đầy đủ để Phase 4
        String out = argValue(args, "--out", "console");
        ResultWriter writer = switch (out) {
            case "json" -> new JsonWriter();
            case "console" -> new ConsoleWriter();
            default -> throw new IllegalArgumentException("Unknown --out: " + out);
        };

        StringBuilder sb = new StringBuilder();
        writer.write(result, sb);
        System.out.println(sb);
    }

    /** Đọc giá trị của một cờ dạng "--key value"; trả default nếu không có. */
    private static String argValue(String[] args, String key, String def) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) {
                return args[i + 1];
            }
        }
        return def;
    }

    /** Helper Phase 1: "E2" -> Square. */
    private static Square sq(String s) {
        int file = s.charAt(0) - 'A';
        int rank = s.charAt(1) - '1';
        return new Square(file, rank);
    }
}
