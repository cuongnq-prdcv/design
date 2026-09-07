package chess.output;

import chess.model.ResultSet;

import java.io.IOException;

/**
 * Ghi result set ra một Appendable (System.out, StringBuilder, FileWriter...).
 * Tách FORMAT (writer làm) khỏi ĐÍCH ĐẾN (Appendable) — test được zero I/O.
 */
public interface ResultWriter {
    void write(ResultSet results, Appendable out) throws IOException;
}
