package chess.input;

/**
 * Lỗi domain thống nhất cho MỌI vấn đề input/validation:
 * unknown piece type/side, off-board, trùng ô, cú pháp notation/YAML/JSON sai, file không đọc được.
 *
 * ĐIỂM HỘI TỤ LỖI: mọi tầng input/parser/validator ném đúng loại này (bọc nguyên nhân gốc nếu có).
 * Composition root (Main) là nơi DUY NHẤT bắt nó → in "clear error" ra stderr + exit code ≠ 0.
 *
 * MỞ RỘNG: nếu cần phân loại lỗi chi tiết (ví dụ tách "syntax" vs "semantic"), tạo subclass của
 * lớp này — KHÔNG đổi chữ ký các interface input, và Main vẫn bắt ở cùng một chỗ.
 */
public class ChessInputException extends RuntimeException {

    public ChessInputException(String message) {
        super(message);
    }

    public ChessInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
