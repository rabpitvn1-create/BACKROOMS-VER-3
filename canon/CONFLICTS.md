# Canon conflict register

Tài liệu này ghi quyết định migration có căn cứ. Ngày sửa file không được dùng làm authority mặc định; OPEN/UNKNOWN không được lấp.

## RESOLVED — Cao Minh Ultimate

Nguồn runtime hiện hành trong `CombatChoiceEngine` khóa Huyết Ma Nhị Thập Tứ Trảm ở 24 hit và `ULTIMATE_BONUS_DAMAGE_PERCENT = 15`; record `CHAR.CAO_MINH.HUYET_MA_24` trong knowledge database cũng mô tả mỗi hit là 100% current DMG + 15% Bonus DMG. Trường `damageHpPerHit: 10` trong snapshot `characters_current.json` là projection cũ, mâu thuẫn cả Core lẫn knowledge record. Nguồn biên tập mới bỏ fixed 10 HP và giữ runtime behavior hiện tại: 24 × (100% current DMG + 15% Bonus DMG), SSF/FSF vẫn do Core xử lý.

## RESOLVED — Lucia và Lục Trầm

`CharacterEncounterCore` và Lucia Codex hiện hành khóa `lucia` và `luc_tram` là hai character id độc lập. Những câu trong Lục Trầm R05 nói retcon “thay thế toàn bộ Character Canon cũ của Lucia” được thu hẹp đúng phạm vi: chỉ retire dữ liệu legacy từng gán alias/loadout Lucia vào slot lịch sử của Lục Trầm. Không retcon, alias, rename hoặc merge nhân vật runtime `lucia`.

## PARTITIONED — Táng Kiếm Cốc

Nguồn R05 đã tự phân biệt hồ sơ Chính Đạo, quan sát trực tiếp, belief và writer-secret. Chúng được tách thành các stable item riêng trong `canon/knowledge/tang_kiem_coc.md`. Backstage truth là `knownBy: SECRET` và không được đưa vào narration prompt. “Sự thật hoàn chỉnh” và trách nhiệm cuối cùng vẫn OPEN.

## UNRESOLVED SOURCE AVAILABILITY — giữ runtime hiện tại

- Cao Minh và Syvial đang trỏ tới `02_CHARACTERS/Cao_Minh_Codex.docx` và `02_CHARACTERS/Syvial_Codex.docx`; hai file gốc không nằm trong repository. Snapshot hiện hành được giữ, metadata đánh dấu `EXTERNAL_SNAPSHOT`; không tự thêm canon ngoài snapshot.
- Nhiều Level legacy record trỏ tới `01_WORLD/level.md` / `01_WORLD/entity.md`, nhưng các path đó không tồn tại trên HEAD. `level_knowledge` hiện hành có provenance riêng (bao gồm URL/checked date nơi có), nên runtime behavior được giữ và origin thiếu được báo trong validation report thay vì đoán.
- Một relationship record còn trỏ tới `android-apk/DIEP_MINH_CANON.md`, path không tồn tại trên HEAD. Stable relationship record vẫn được giữ; origin được đánh dấu `MISSING_ORIGIN` cho tới khi nguồn gốc được đưa lại vào repo hoặc retcon hợp lệ thay thế nó.

## Prompt/Core duplication

Core tiếp tục là authority duy nhất cho damage/proc/spawn/loot/Party/route/outcome. Canon index chỉ cung cấp lore/knowledge đã lọc. Các hard safety contract trong Java được giữ khi chúng bảo vệ ownership; narrative canon không được dùng để ghi đè Core.
