package chess.input;

/**
 * Phase 4 — xem specs/phase-4.md (Chặng ⓪ trong docs/diagrams.md).
 * TRỤC SOURCE (where): chỉ trả về raw text, KHÔNG biết text có định dạng gì.
 * Ranh giới với trục format là String — bất kỳ source nào cũng ghép được với bất kỳ parser nào.
 *
 * MỞ RỘNG (thêm nguồn mới, ví dụ URL/HTTP, clipboard):
 *   1. Tạo class mới `implements InputSource`, trả String.
 *   2. Đăng ký 1 dòng vào sourceRegistry trong Main.
 *   KHÔNG đụng parser, engine, hay các source khác.
 *
 * Lỗi đọc (file thiếu, quyền...) → ném ChessInputException với thông điệp rõ ràng.
 */
public interface InputSource {
    String read();
}
