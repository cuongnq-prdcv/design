package chess.input;

import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import chess.model.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FORMAT: compact notation. Mỗi dòng một phe:
 *   W: KE2, QD1, RA1, NB1, PA2
 *   B: KE8, QH1, RA8, PA7
 * Token = <PieceLetter><File><Rank>, letter ∈ {K Q R B N P} (N = Knight).
 *
 * Chỉ TẠO model. Không validate board (BoardValidator lo). Lỗi → ChessInputException.
 */
public final class NotationParser implements PieceFormatParser {

    // Bảng chữ cái quân — KHÔNG phải switch hành vi, chỉ là bảng tra ký hiệu → enum.
    private static final Map<Character, PieceType> LETTER = Map.of(
            'K', PieceType.KING,
            'Q', PieceType.QUEEN,
            'R', PieceType.ROOK,
            'B', PieceType.BISHOP,
            'N', PieceType.KNIGHT,
            'P', PieceType.PAWN);

    @Override
    public List<Piece> parse(String raw) {
        List<Piece> pieces = new ArrayList<>();
        String[] lines = raw.split("\\R"); // mọi kiểu xuống dòng

        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colon = trimmed.indexOf(':');
            if (colon < 0) {
                throw new ChessInputException(
                        "Notation line missing side prefix (expect 'W: ...' or 'B: ...'): " + line);
            }
            Side side = parseSidePrefix(trimmed.substring(0, colon));
            String body = trimmed.substring(colon + 1);

            for (String token : body.split(",")) {
                String t = token.strip();
                if (!t.isEmpty()) {
                    pieces.add(parseToken(t, side));
                }
            }
        }
        return pieces;
    }

    private Side parseSidePrefix(String prefix) {
        return switch (prefix.strip().toUpperCase()) {
            case "W" -> Side.WHITE;
            case "B" -> Side.BLACK;
            default -> throw new ChessInputException("Unknown side prefix: '" + prefix + "'");
        };
    }

    private Piece parseToken(String token, Side side) {
        // <Letter><File><Rank> => đúng 3 ký tự
        if (token.length() != 3) {
            throw new ChessInputException(
                    "Invalid token '" + token + "' (expect <Letter><File><Rank>, e.g. KE2)");
        }
        char letter = Character.toUpperCase(token.charAt(0));
        PieceType type = LETTER.get(letter);
        if (type == null) {
            throw new ChessInputException("Unknown piece letter: '" + letter + "' in token " + token);
        }
        Square square = PieceFields.parseSquare(token.substring(1));
        return new Piece(type, side, square);
    }
}
