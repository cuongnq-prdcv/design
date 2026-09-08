package chess.input;

import chess.model.Piece;

import java.util.List;

/**
 * TRỤC FORMAT (how): raw text → model (List<Piece>), KHÔNG biết text đến từ nguồn nào.
 * Mọi parser phải trả về CÙNG một model — downstream (validator, engine, writer) mù về format.
 *
 * PARSER CHỈ TẠO MODEL, KHÔNG VALIDATE BOARD. Việc validate (off-board/trùng ô) là của
 * BoardValidator — một chỗ duy nhất, không lặp trong từng parser.
 *
 * MỞ RỘNG (thêm định dạng mới, ví dụ FEN, XML):
 *   1. Tạo class mới `implements PieceFormatParser`, biến String thành List<Piece>.
 *   2. Đăng ký 1 dòng vào parserRegistry trong Main.
 *   KHÔNG đụng source, các parser khác, hay engine.
 *
 * Lỗi cú pháp / giá trị không hợp lệ → ném ChessInputException với thông điệp rõ ràng.
 */
public interface PieceFormatParser {
    List<Piece> parse(String raw);
}
