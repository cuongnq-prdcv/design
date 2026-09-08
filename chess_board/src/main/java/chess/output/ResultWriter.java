package chess.output;

import chess.model.ResultSet;

import java.io.IOException;

/**
 * Phase 1 (Console) + Phase 3 (JSON) — xem specs/phase-1.md, specs/phase-3.md (Chặng ④).
 *
 * Ghi result set ra một Appendable (System.out, StringBuilder, FileWriter...).
 * Tách FORMAT (writer làm) khỏi ĐÍCH ĐẾN (Appendable) — test được zero I/O.
 *
 * MỞ RỘNG (thêm output format, ví dụ XML/plain-text):
 *   1. Tạo class mới `implements ResultWriter`, ghi resultSet ra `out` theo format của bạn.
 *   2. Đăng ký 1 dòng vào WRITERS registry trong Main.
 *   KHÔNG đụng engine/luật đi/parser. ResultSet là dữ liệu thuần, không tự định dạng.
 */
public interface ResultWriter {
    void write(ResultSet results, Appendable out) throws IOException;
}
