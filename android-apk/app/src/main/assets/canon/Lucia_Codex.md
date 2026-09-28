# Lucia Lục / Hứa Thuý Mai

## Identity
<!-- canon: core=true; aliases=Lucia,Lucia Lục,Hứa Thuý Mai,Hứa Thúy Mai -->
Lucia Lục là callsign của Hứa Thuý Mai, một nữ quân nhân trẻ người Việt Nam gốc Hoa. Cô là con người được huấn luyện tốt theo vai trò tactical riflewoman, không phải tu sĩ hay nhân vật siêu nhiên.

**HARD LOCK:** Lucia Lục / Hứa Thuý Mai và Lục Trầm là hai nhân vật khác nhau. Runtime id của Lucia là `lucia`; runtime id của Lục Trầm là `luc_tram`. Không alias, rename, merge hoặc chuyển canon, trang bị, quan hệ hay xưng hô giữa hai người.

Quan hệ và cách xưng hô giữa Lucia với Cao Minh là OPEN cho tới khi live campaign state xác lập. Khi Core kích hoạt Lucia lần đầu trong continuity chưa có lịch sử gặp mặt, đó là first contact.

## Runtime
CharacterEncounterCore sở hữu toàn bộ spawn/join của Lucia. Lucia chỉ có candidate ở **Level 0 gốc** (`currentLevelKey = "0"`) với tỷ lệ **10%**; không roll ở 0.1 hoặc các Level khác. GM/narration không được tự spawn Lucia hoặc tự sửa Party.

Progression, HP và combat stats hiện hành do V3 Core quản lý; không dùng dữ liệu legacy để ghi đè stat state.

## Equipment and combat
Trang bị runtime mặc định đã xác nhận:
- M4A1 cá nhân hóa.
- Dao găm chiến đấu.
- Đồng hồ định vị quân sự.

Poker Dice presentation:
- Active Skill 1 — M4A1 Joint Attack: 150% damage.
- Active Skill 2 — M4A1 Tactical Burst: 175% damage.
- Core chọn một active skill làm `currentSkill` cho lượt Lucia; các hand Skill dùng skill đó với multiplier Poker Dice hiện hành.
- Toxic Burst: +25% damage, Trúng độc 2 turn, value 3.
- Armor-Piercing Burst: +20% damage, Xuyên giáp 2 turn, value 10.
- Concussive Burst: +15% damage, Choáng 1 turn.
- Rending Burst: +20% damage, Chảy máu 2 turn, value 3.
- Corrosive Burst: +20% damage, Trúng độc 2 turn, value 4.
- Too Young To Die: Ultimate đúng 60 phát, dùng cùng công thức current-DMG + 15% bonus-per-hit của runtime hiện hành; SSF dùng 100% Ultimate multiplier và FSF dùng 200%.

Các proc/status trên là gameplay projection; không tự biến Lucia thành nhân vật siêu nhiên.

## Visual
Visual lock R03: gương mặt nữ trẻ, tóc đen dài buộc đuôi ngựa cao, mắt nâu ấm, trang bị camouflage/tactical hiện đại, không đội mũ giáp; sử dụng M4A1 cá nhân hóa. Avatar runtime: `avatars/lucia_avatar.jpg`.
