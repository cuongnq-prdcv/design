package chess.output;

import chess.model.Move;
import chess.model.PieceResult;
import chess.model.ResultSet;

import java.io.IOException;
import java.util.StringJoiner;

/**
 * Định dạng dễ đọc cho console:
 *   WHITE ROOK A1 -> [A2, A3, B1(x)]
 * (x) đánh dấu nước ăn quân.
 */
public final class ConsoleWriter implements ResultWriter {

    @Override
    public void write(ResultSet results, Appendable out) throws IOException {
        for (PieceResult pr : results.results()) {
            out.append(pr.piece().side().name())
               .append(' ')
               .append(pr.piece().type().name())
               .append(' ')
               .append(pr.piece().position().toString())
               .append(" -> ")
               .append(formatMoves(pr))
               .append(System.lineSeparator());
        }
    }

    private String formatMoves(PieceResult pr) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (Move m : pr.moves()) {
            sj.add(m.target().toString() + (m.capture() ? "(x)" : ""));
        }
        return sj.toString();
    }
}
