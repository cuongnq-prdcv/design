# Specs — spec chi tiết & vết truy xuất theo từng Phase

Mỗi phase có một file `phase-N.md`. Mục đích: có **spec chi tiết trước khi code** (từ Phase 3 trở đi)
và **vết truy xuất** (đã xử lý gì, test nào cover, kết quả run) để đối chiếu & review.

## Quy ước mỗi file spec

1. **Scope** — làm gì, cố tình chưa làm gì (scope-out).
2. **Inputs / Outputs** — dữ liệu vào/ra cụ thể + ví dụ.
3. **Cases phải xử lý** — bảng checklist truy vết:
   `# | Case | Input | Expected | Test cover | SOLID | ✓`
   - Cột **Test cover** ghi đúng tên `ClassTest.methodName` để truy ngược tới test.
   - Cột **SOLID** map case tới nguyên lý nó minh chứng (để phục vụ design review).
   - Cột **✓** tick khi đã verify.
4. **Acceptance criteria** — điều kiện nghiệm thu (checkbox).
5. **Trace / Verification log** — sau khi làm: kết quả `gradle run`/`gradle test`, friction ghi lại.
6. **Files chạm tới** — mới / sửa / KHÔNG đổi (chứng minh OCP: phase sau không sửa code phase trước).

## Quy trình từ Phase 3 trở đi

1. Viết `phase-N.md` phần **Scope + Inputs/Outputs + Cases + Acceptance** TRƯỚC khi code.
2. Code theo spec.
3. Chạy test, điền **Trace/Verification log** + tick cột ✓ + liệt kê **Files chạm tới**.

## Trạng thái

| Phase | File | Trạng thái |
|-------|------|-----------|
| 1 | `phase-1.md` | DONE (backfill) |
| 2 | `phase-2.md` | DONE (backfill) |
| 3 | `phase-3.md` | DONE (JSON output) |
| 4 | `phase-4.md` | chưa bắt đầu (input sources + formats) |
| 5 | `phase-5.md` | chưa bắt đầu (stretch) |
