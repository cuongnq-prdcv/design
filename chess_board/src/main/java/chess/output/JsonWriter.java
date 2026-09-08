package chess.output;

import chess.model.Move;
import chess.model.PieceResult;
import chess.model.ResultSet;

import java.io.IOException;

/**
 * Phase 3 — xem specs/phase-3.md (6 case, verify JSON parse hợp lệ bằng json.load).
 *
 * Render ResultSet ra JSON hợp lệ, ghi vào Appendable.
 * Viết JSON tay (không dependency, không reflection) — đủ cho shape đơn giản của bài.
 * Shape:
 * {
 *   "results": [
 *     { "side": "WHITE", "type": "ROOK", "position": "A1",
 *       "moves": [ { "to": "A2", "capture": false } ] }
 *   ]
 * }
 */
public final class JsonWriter implements ResultWriter {

    @Override
    public void write(ResultSet results, Appendable out) throws IOException {
        out.append("{\n  \"results\": [");

        var list = results.results();
        for (int i = 0; i < list.size(); i++) {
            PieceResult pr = list.get(i);
            out.append(i == 0 ? "\n" : ",\n");
            appendPiece(pr, out);
        }

        if (!list.isEmpty()) {
            out.append("\n  ");
        }
        out.append("]\n}");
    }

    private void appendPiece(PieceResult pr, Appendable out) throws IOException {
        out.append("    {\n");
        out.append("      \"side\": ").append(str(pr.piece().side().name())).append(",\n");
        out.append("      \"type\": ").append(str(pr.piece().type().name())).append(",\n");
        out.append("      \"position\": ").append(str(pr.piece().position().toString())).append(",\n");
        out.append("      \"moves\": [");

        var moves = pr.moves();
        for (int j = 0; j < moves.size(); j++) {
            Move m = moves.get(j);
            out.append(j == 0 ? "\n" : ",\n");
            out.append("        { \"to\": ").append(str(m.target().toString()))
               .append(", \"capture\": ").append(Boolean.toString(m.capture()))
               .append(" }");
        }
        if (!moves.isEmpty()) {
            out.append("\n      ");
        }
        out.append("]\n    }");
    }

    /** Bọc chuỗi trong ngoặc kép + escape ký tự JSON. */
    private String str(String raw) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
