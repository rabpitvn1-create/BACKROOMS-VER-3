# Level canon projection source

> EDITABLE SOURCE. Runtime output: `android-apk/app/src/main/assets/knowledge/level_knowledge.json`. Run `python3 tools/canon.py generate` after editing.

```json
{
  "schemaVersion": 2,
  "authority": "BACKROOMsV2_LEVEL_CANON",
  "instruction": "This asset is the authoritative runtime environment knowledge for implemented gameplay Levels. LevelCore must load only the current node. Fields describe environment canon, not automatic character knowledge. Entity spawning, item spawning, stats, combat and hidden route progress remain Core-owned. variationPool contains permitted scene motifs, not guaranteed persistent objects.",
  "sectionOrder": [
    "identity",
    "architecture",
    "zones",
    "sensory",
    "anomalies",
    "hazards",
    "resources",
    "entities",
    "navigation",
    "entrancesExits",
    "gameplayOverride",
    "gmConstraints",
    "variationPool",
    "canonicalFacts",
    "microLocations",
    "environmentStates",
    "stateTransitions",
    "environmentEvents",
    "interactionRules",
    "actionConsequences",
    "persistenceRules",
    "evidenceRules",
    "navigationPatterns",
    "routeProgressionCues",
    "encounterStaging",
    "hazardEscalation",
    "quietTurnPatterns",
    "narrativeGrammar",
    "antiRepetition",
    "forbiddenInventions",
    "sceneSeeds"
  ],
  "levels": {
    "0": {
      "name": "Level 0 — ngưỡng / The Lobby",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 0 — ngưỡng",
        "status": "project-authoritative with current Wikidot môi trường canon",
        "url": "https://backrooms-wiki.wikidot.com/level-0",
        "checked": "2026-09-19",
        "note": "Tuyến hiện tại đi từ Level 0 qua Hui's Family Level 1–16 rồi đến Level 1."
      },
      "identity": [
        "Level 0 là điểm khởi đầu chủ đạo của Backrooms và trong BACKROOMsV2 là môi trường Cao Minh xuất hiện sau khi mặt đất dưới chân biến mất và hắn rơi khỏi thế giới nguyên sinh.",
        "Không gian giống các khu hậu trường của văn phòng hoặc cửa hàng: vàng bệnh, trống trải, vô tận về cảm giác, lặp lại nhưng không hoàn toàn giống nhau.",
        "Bản sắc kinh dị của Level 0 đến từ sự cô lập, đơn điệu kéo dài, hình học thay đổi khi không được quan sát, thiếu tài nguyên và sự bất định về những gì nghe hoặc thấy ở xa.",
        "Level 0 không phải tutorial an toàn. Người không chuẩn bị có thể chết vì mất nước, đói, kiệt sức, nhiễm bẩn, ngã hoặc mất phương hướng dù không gặp Entity."
      ],
      "canonicalFacts": [
        "Kiến trúc phổ biến gồm các phòng và hành lang phân đoạn ngẫu nhiên, cầu thang hiếm, giấy dán tường vàng/beige, trần thả và đèn huỳnh quang.",
        "Các cấu trúc aberration chính của ngưỡng hiện hành gồm các phòng vòm, các phòng cột, các bãi hố, các vùng mất sáng và bố cục changes do Peripheral Shift.",
        "Thảm gần như phủ toàn Level, là dạng Berber-like liên tục, ẩm dai dẳng và có thể chứa nhiều loại chất lỏng khác nhau; không được coi nước trong thảm là nước uống.",
        "Isolation Effect ngăn người ở Level 0 dễ dàng gặp nhau hoặc liên lạc trực tiếp; shouting, note, dấu đánh dấu và phá tường không phải cách đáng tin để tìm người khác.",
        "Không có resident Entity được xác nhận chắc chắn. Dark figures, tiếng cào, whispering hoặc familiar voices phải giữ trạng thái chưa xác minh trừ khi EntityCore xác nhận cuộc chạm trán.",
        "Arches có xu hướng ổn định hơn các khu khác và có giá trị định hướng cục bộ.",
        "các phòng cột có thể cực lớn; đường phía sau thường dịch chuyển khiến các cột mất đối xứng và phá khả năng quay lại.",
        "các bãi hố tạo thành cụm/lưới của các hố tối sâu. Không có dữ liệu sống sót đáng tin sau khi rơi vào.",
        "các vùng mất sáng không có lighting, có tường thô hơn, im lặng hơn và nền có thể trũng với chất lỏng nông.",
        "Peripheral Shift có thể làm bố cục warp, stretch hoặc rearrange khi không được quan sát trực tiếp; hiện tượng không xảy ra liên tục và có vùng ổn định hơn.",
        "tiếng ù huỳnh quang có thể dao động mạnh về âm lượng; các đợt cực lớn có thể gây đau tai hoặc nguy cơ tổn thương thính giác nếu kéo dài.",
        "Các anomalous noises như tiếng thì thầm, familiar giọng nói và tiếng cào có thể theo hành lang nhưng nguyên nhân không được xác nhận.",
        "Red Rooms và Manila Room không nằm trong tuyến chơi hiện tại sau Level 0."
      ],
      "architecture": [
        "Phòng cơ bản dùng giấy dán tường vàng/beige, chân tường đơn giản, trần ô vuông, đèn huỳnh quang âm trần và các lối mở không có cửa.",
        "Không gian thay đổi từ hành lang hẹp, phòng chữ nhật nhỏ, giao lộ lệch góc đến các hall rộng hàng chục hoặc hàng trăm mét.",
        "trần nhà height thường thấp kiểu thương mại nhưng có thể tăng đáng kể ở các phòng cột hoặc các khoang chuyển tiếp.",
        "các phòng vòm có các vòm mở trên tường pale-yellow, thường nằm ở ngõ cụt hoặc transition room; thảm tại đây có thể sâu và giữ nhiều chất lỏng hơn.",
        "các phòng cột có cột lớn xếp theo lưới/lattice, tầm nhìn xa và thảm nông hơn tương đối.",
        "các bãi hố gồm các hố tối dạng lưới; ánh sáng chỉ xuyên xuống rất ít và đáy không được xác nhận.",
        "các vùng mất sáng giữ mô-típ Level 0 nhưng mất toàn bộ đèn, tường thô hơn và chỗ trũng trên sàn có thể giữ ankle-deep chất lỏng.",
        "Không được tự thêm cửa sổ nhìn ra Frontrooms, elevator hoạt động, signage thương mại có nghĩa rõ ràng hoặc phòng chức năng hiện đại trừ khi trạng thái/canon riêng xác nhận."
      ],
      "zones": [
        "Yellow Rooms: nền kiến trúc mặc định, vàng/beige, thảm ẩm, tiếng ù huỳnh quang và cấu trúc không gian phân đoạn.",
        "các phòng vòm: khu tương đối ổn định, có vòm tường và dead-end/transition hình học; có thể cho Cao Minh nghỉ ngắn trên mép khô nếu điều kiện vật lý cho phép.",
        "các phòng cột: hall rất lớn với lưới cột, dễ giữ một hướng di chuyển nhưng khó đánh giá khoảng cách; backtracking đặc biệt không đáng tin.",
        "các bãi hố: vùng có hố lưới, yêu cầu chú ý footing; cực nguy hiểm khi kiệt sức, bị thương hoặc thiếu sáng.",
        "các vùng mất sáng: vùng tối hoàn toàn hoặc gần hoàn toàn, không còn tiếng ù cục bộ, tường thô và chất lỏng nông có thể hiện diện.",
        "các vùng ổn định: những đoạn Peripheral Shift xảy ra ít hơn, nơi mốc định hướng có thể giữ giá trị lâu hơn nhưng không vĩnh viễn.",
        "Memory Rooms: BACKROOMsV2 giữ như một phenomenon hiếm của project canon; chúng có thể gợi, tiếng vọng hoặc làm méo ký ức nhưng không xác nhận ký ức đó là sự thật khách quan."
      ],
      "sensory": [
        "Âm nền chủ đạo là tiếng ù huỳnh quang liên tục, nhưng âm lượng, cao độ và hướng cảm nhận có thể thay đổi.",
        "Thảm ẩm tạo cảm giác abrasive/soggy dưới chân, tăng mệt mỏi và khiến giày ướt lâu.",
        "Mùi nấm mốc, thảm sợi tổng hợp, nước tù, bụi tường và không khí kín có thể thay đổi theo room.",
        "Nhiệt độ thường không cực đoan nhưng cảm giác bí, ẩm và thiếu thông gió làm thời gian ở lâu khó chịu hơn.",
        "Trong các phòng vòm, chất lỏng trong thảm có thể sâu hơn và bước chân nặng hơn.",
        "Trong các phòng cột, tiếng vọng và tiếng bước chân có khoảng trễ lớn hơn do thể tích phòng.",
        "Trong các vùng mất sáng, sự biến mất của tiếng ù tạo cảm giác im lặng bất thường; tiếng chất lỏng và bề mặt tường trở thành dấu hiệu chính.",
        "đợt tăng tiếng ồn của đèn phải được kể như hiện tượng âm thanh vật lý, không mặc định là attack hay giọng nói siêu nhiên."
      ],
      "anomalies": [
        "Peripheral Shift: bố cục có thể thay đổi khi nằm ngoài direct observation. Không dùng nó để dịch chuyển Cao Minh hoặc đồ vật đang được nhìn thấy.",
        "Isolation Effect: người cùng vào Level 0 có thể bị tách khỏi nhau; liên lạc trực tiếp và dấu vết gửi cho nhau không đáng tin.",
        "Anomalous Audio: tiếng thì thầm, familiar giọng nói, tiếng cào hoặc tiếng bước chân có thể tồn tại mà không có nguồn xác định.",
        "huỳnh quang Pressure: phơi nhiễm kéo dài với tiếng ù/ánh sáng có thể gây đau đầu, mất ngủ, khó chịu hoặc suy giảm tập trung; không phải mind control.",
        "Memory Rooms: nếu dùng, chỉ được mô tả bằng dấu hiệu gợi nhớ/nhận thức và không được retroactively viết lại lịch sử nhân vật.",
        "bố cục Resistance: mapping chi tiết suy giảm giá trị theo thời gian vì Peripheral Shift, nhưng các vùng ổn định vẫn cho phép cục bộ định hướng có ích.",
        "Không được biến anomaly thành hệ thống có ý thức, đang 'chơi đùa' với Cao Minh hoặc cố tình chống lại Cao Minh nếu không có bằng chứng."
      ],
      "hazards": [
        "Dehydration và starvation là nguy cơ lâu dài chính; Level không đảm bảo nhu yếu phẩm.",
        "ướt thảm làm chân lạnh/ẩm, tăng phồng rộp, đau bàn chân, nguy cơ skin irritation và giảm hiệu quả di chuyển nếu kéo dài.",
        "thảm chất lỏng có thể chứa salt/brackish water, tainted Almond Water, Liquid Pain, formalin hoặc biological material theo nguồn canon; tuyệt đối không coi là potable.",
        "các bãi hố có nguy cơ rơi chết hoặc mất tích; mệt mỏi/dehydration làm nguy cơ này tăng mạnh.",
        "các vùng mất sáng gây mất phương hướng, vấp/ngã, chất lỏng phơi nhiễm và khó thoát vì biến đổi bố cục.",
        "các đợt tăng tiếng ồn có thể gây đau tai; phơi nhiễm kéo dài mới được mô tả có nguy cơ thính giác, không gây hư hại tức thì tùy tiện.",
        "Sleep deprivation, microsleep và cognitive mệt mỏi làm định hướng/error risk tăng nhưng không dùng như cớ để ép Cao Minh hành động ngu ngốc.",
        "Mold hoặc mùi ẩm có thể gây khó chịu đường hô hấp; không tự chẩn đoán infection nếu chưa có symptom/bằng chứng."
      ],
      "resources": [
        "Không có resource ecology ổn định. nhu yếu phẩm không xuất hiện vì cốt truyện cần cứu người chơi.",
        "ItemCore là authority tuyệt đối cho consumable, chest và loot; Level knowledge không được tự mutate inventory.",
        "Arch ledges hoặc vùng thảm nông có thể cung cấp chỗ nghỉ tương đối khô hơn nhưng không phải safe camp tuyệt đối.",
        "Electrical outlets, ánh sáng các bộ đèn hoặc vật liệu tường có thể tồn tại như scenery/interactable nhưng không đảm bảo điện sử dụng được cho thiết bị của Cao Minh.",
        "thảm chất lỏng không phải nguồn Almond Water hợp lệ dù sample có thể chứa thành phần tương tự.",
        "Nếu ItemCore cấp một item trong Level 0, GM nên đặt nó vào bối cảnh vật lý hợp lý thay vì mô tả nó xuất hiện từ hư không."
      ],
      "entities": [
        "Không có resident Entity được xác nhận chắc chắn tại Level 0.",
        "Dark figures ở xa, cảm giác bị nhìn, tiếng cào, tiếng thì thầm hoặc familiar giọng nói phải là chưa xác minh cho đến khi EntityCore/trạng thái chứng minh.",
        "EntityCore là authority duy nhất cho đang hoạt động Entity cuộc chạm trán.",
        "Nếu EntityCore không có cuộc chạm trán, GM không được biến anomalous sound thành Smiler, Hound, Skin-Stealer hoặc sinh vật mới.",
        "Nếu EntityCore có cuộc chạm trán, Isolation Effect không được dùng để xóa cuộc chạm trán đã được Core xác nhận."
      ],
      "navigation": [
        "cục bộ mốc định hướng có giá trị: arch hình học, pillar khoảng cách, thảm độ sâu, tường hư hại, nhịp tiếng ù, trần nhà tile defect và vùng ổn định.",
        "toàn cục bản đồ không đáng tin vì Peripheral Shift; GM không được cho Cao Minh đạt độ chắc chắn tuyệt đối chỉ bằng việc vẽ sơ đồ.",
        "Trong các phòng cột, chọn một hướng di chuyển nhất quán là chiến lược hợp lý hơn cố backtrack.",
        "Trong các phòng vòm, hình học ổn định hơn nên mốc định hướng có thể tồn tại qua nhiều lượt.",
        "Trong các vùng mất sáng, tiếng ù từ vùng sáng, luồng khí, tường-following và floor bề mặt có thể trở thành dấu hiệu; hướng vẫn không chắc chắn.",
        "Ở các bãi hố, lộ trình phải xét mệt mỏi, ánh sáng và khoảng cách giữa hố; không biến lưới thành obstacle vô nghĩa dễ dàng vượt qua.",
        "lộ trình thành công/failure thuộc Core. Lore chỉ cung cấp cách biểu hiện tiến triển hoặc vòng lặp."
      ],
      "entrancesExits": [
        "Cao Minh bắt đầu trong môi trường tương ứng Level 0 sau khi rơi khỏi thế giới nguyên sinh; bản thân Cao Minh không mặc định biết tên hoặc số thứ tự Level này.",
        "Nguồn Wiki hiện hành có lối ra sang Level 1 qua flickering tường; BACKROOMsV2 cố ý override lộ trình này.",
        "Level 0 chỉ chuyển sang Hui's Family Level 1 khi LevelCore mở lối ra và người chơi chọn đi qua.",
        "Không chuyển thẳng sang Level 1 hay các sublevel cũ từ Level 0."
      ],
      "gameplayOverride": [
        "Chuỗi bắt buộc bắt đầu: Level 0 → Hui's Family Level 1–16 → Level 1.",
        "Red Rooms không phải một node gameplay trong tuyến hiện tại.",
        "Manila Room không phải một node gameplay trong tuyến hiện tại.",
        "Lucia chỉ có thể roll cuộc chạm trán ở Level 0 gốc theo CharacterEncounterCore.",
        "Hidden lộ trình roll/streak/exitAvailable do LevelCore sở hữu và tuyệt đối không được tiết lộ.",
        "GM không được tự đổi currentLevel/currentLevelKey trước khi Core cho phép."
      ],
      "gmConstraints": [
        "Không tự spawn Entity, survivor, NPC, faction, base, settlement hoặc rescue signal.",
        "Không tự cấp item, chest, Almond Water, food, medical nhu yếu phẩm hay usable trang bị.",
        "Không dùng Red Rooms hoặc Manila Room như scene ở Level 0.",
        "Không nhắc tên Backrooms 1900 trước khi người chơi tìm ra lối thoát và bước qua.",
        "Không giải thích nguồn gốc Backrooms, Isolation Effect, Peripheral Shift, thảm chất lỏng hoặc familiar voices như fact.",
        "Không biến Level thành chủ thể có ý thức, nói chuyện hoặc cố tình thử thách Cao Minh.",
        "Không ép Cao Minh hoảng loạn, bỏ chạy, mất ý chí hoặc tin một ảo giác; chỉ mô tả stimuli và phản ứng cơ thể trực tiếp hợp lý.",
        "Không lặp nguyên cùng một room bố cục/phrase hai lượt liên tiếp nếu không phải tính liên tục cố ý.",
        "Không tự đặt lại dấu vết, injury, resource usage hoặc hậu quả người chơi tạo ra chỉ vì biến đổi bố cục.",
        "Không dùng biến đổi bố cục để phủ nhận thành công đã được Core/trạng thái xác nhận.",
        "Mọi từ mô tả môi trường bằng tiếng Anh còn tồn tại trong dữ liệu canon chỉ là thuật ngữ nội bộ. Khi kể cho người chơi, phải diễn đạt bằng tiếng Việt tự nhiên; không được trộn các từ tiếng Anh thông thường vào câu tiếng Việt."
      ],
      "variationPool": [
        "hành lang vàng hẹp với trần thấp và một hàng đèn có độ sáng lệch nhau.",
        "Phòng chữ nhật rộng hơn bình thường, không đồ nội thất, giấy dán tường có hai tone vàng khác nhau.",
        "giao lộ ba hướng với thảm ẩm không đều và một góc tường bong lớp giấy ngoài.",
        "ngõ cụt dẫn vào phòng vòm có vòm pale và thảm sâu hơn.",
        "Pillar hall mở rộng tầm nhìn đến mức các cột cuối cùng chìm trong haze ánh sáng.",
        "bãi hố nhỏ với hố lưới thưa, có lối đi rõ nếu người chơi tỉnh táo và quan sát.",
        "bãi hố dày hơn, hố gần nhau, yêu cầu bước chậm hoặc đổi hướng.",
        "vùng ổn định nơi dấu dấu đánh dấu cũ vẫn còn nguyên sau khi quay lại.",
        "Một đoạn hallway mà tiếng ù giảm dần nhưng đèn vẫn sáng.",
        "Một đèn duy nhất nhấp nháy lệch nhịp, không phải lối ra nếu Core chưa mở.",
        "thảm gần như khô trong vài mét rồi trở lại ẩm.",
        "Một patch nấm mốc đen ở chân tường nhưng không có creature.",
        "trần nhà tile hơi lệch tạo luồng khí nhẹ nhưng không mở thành lối đi.",
        "giấy dán tường có vệt đỏ nâu do chất bẩn; không được gọi là Red Rooms.",
        "Một phòng có nhiều ổ điện nhưng không có dấu hiệu thiết bị từng được cắm.",
        "tiếng cào mơ hồ sau tường di chuyển theo một đoạn rồi biến mất.",
        "Familiar giọng nói chỉ đủ rõ để gợi nhận biết, không cung cấp câu thoại xác nhận người thật.",
        "Một đợt tăng tiếng ồn làm tiếng ù tăng mạnh trong thời gian ngắn rồi trở lại baseline.",
        "Dấu giày cũ trên thảm kết thúc vô lý ở giữa phòng; chỉ là bằng chứng chưa xác minh.",
        "Một vết kéo dài trên tường cho thấy từng có va chạm, không xác định nguồn.",
        "Một section tối phía xa nhưng vẫn còn ánh sáng phản chiếu, chưa phải vùng mất sáng.",
        "Blackout ngưỡng nơi tiếng ù và ánh sáng giảm qua vài room thay vì cắt tức thì.",
        "Blackout hành lang với tường thô và ankle-deep chất lỏng.",
        "phòng vòm nhỏ tương đối ổn định đủ để kiểm tra trang bị hoặc nghỉ ngắn.",
        "Pillar lưới bị lệch sau khi Cao Minh quay đầu, biểu hiện Peripheral Shift.",
        "Một staircase ngắn chỉ nối hai độ cao trong cùng Level, không phải lối ra.",
        "Một tường lối mở quá thấp, buộc cúi người nhưng không phải crawlspace dài.",
        "Một hành lang dài có cùng kiểu mẫu nhưng khoảng cách giữa đèn thay đổi dần.",
        "Không gian mở ra một chamber rộng rồi thu lại thành hành lang quen thuộc.",
        "Một mùi chemical lạ từ thảm chất lỏng, đủ để cảnh báo không tiếp xúc."
      ],
      "canonicalFactsVersion": "ngưỡng-2026-08-09",
      "microLocations": [
        "Standard Segmented Room: phòng vàng cơ bản, 1-4 lối mở, thảm ẩm và tiếng ù ổn định; dùng làm nền chuyển tiếp chứ không kể như discovery lớn.",
        "Offset giao lộ: giao lộ lệch góc khiến line-of-sight bị cắt; tốt cho định hướng choice mà không cần anomaly.",
        "Arch Dead-End: cụm vòm pale ở ngõ cụt, thảm sâu hơn và tương đối ổn định; cho phép reorientation hoặc rest ngắn.",
        "Arch khoang chuyển tiếp: vòm nối hai kiểu room, thường ổn định hơn hành lang lân cận; mốc định hướng cục bộ tốt.",
        "Small Pillar Gallery: lưới cột vài chục mét, đủ để test hướng di chuyển/dấu đánh dấu.",
        "Grand Pillar Expanse: hall cực lớn, cột lặp đến tầm nhìn xa, dễ gây mệt mỏi và mất cảm giác tiến độ.",
        "Sparse Hole lưới: hố cách nhau rộng, có lối vòng rõ nếu đủ ánh sáng và tỉnh táo.",
        "Dense Hole lưới: hố gần nhau, bề mặt đi lại thu hẹp; mệt mỏi làm risk tăng rõ.",
        "Blackout ngưỡng: vùng tiếng ù nhỏ dần, ánh sáng suy giảm và bề mặt đổi trước khi vào darkness.",
        "Deep Blackout hành lang: không ánh sáng, tường thô, chất lỏng nông; hướng phụ thuộc tactile/audio dấu hiệu.",
        "tiếng ù Spike Room: cấu trúc bình thường nhưng đèn phát âm lượng bất thường, tạo nguy cơ auditory chứ không phải cuộc chạm trán.",
        "Dry thảm vùng nhỏ: thảm ít ẩm hơn, có thể là chỗ dừng chân nhưng không tạo nhu yếu phẩm.",
        "Flooded thảm vùng nhỏ: thảm giữ nhiều chất lỏng hơn, tăng discomfort và ô nhiễm risk.",
        "ổn định Mapping vùng nhỏ: bố cục ít shift, dấu đánh dấu có giá trị lâu hơn và giúp tính liên tục.",
        "Shift-Prone hành lang: nhiều corner/line-of-sight break, dễ thể hiện Peripheral Shift sau khi bị bỏ ngoài quan sát.",
        "nấm mốc tường cụm: mốc tập trung ở chân tường, dấu hiệu môi trường và respiratory discomfort chứ không phải Entity nest.",
        "Low trần nhà Run: trần hạ thấp, đèn gần đầu hơn, tăng noise/heat discomfort.",
        "Open Yellow Chamber: ít partition hơn, tiếng vọng lớn và dễ đánh giá room hiện tại nhưng không đảm bảo lộ trình.",
        "False Familiarity Room: kiểu mẫu gần giống room trước nhưng có 2-3 khác biệt vật lý cụ thể; dùng để tạo déjà vu mà không khẳng định memory anomaly.",
        "Memory tiếng vọng vùng nhỏ: project-only rare zone, stimulus gợi ký ức nhưng mọi chi tiết nguồn gốc phải giữ uncertain."
      ],
      "environmentStates": [
        "BASELINE: ánh sáng ổn định tương đối, tiếng ù vừa, thảm ẩm, visibility tốt, Peripheral Shift chỉ xảy ra khi area rời direct observation.",
        "FATIGUE_PRESSURE: khi sinh tồn suy giảm, GM tăng emphasis vào đau chân, chú ý giảm, footing và decision cost nhưng không điều khiển quyết định.",
        "NOISE_SPIKE: tiếng ù tăng mạnh tại một cụm đèn; duration hữu hạn, không phải toàn cục trạng thái.",
        "SHIFT_ACTIVE: một khu vực đã rời tầm nhìn có thể thay đổi cấu trúc không gian hoặc mốc định hướng relation khi revisited.",
        "STABLE_POCKET: một khu vực giữ bố cục tốt hơn, dấu đánh dấu đáng tin hơn và cho phép người chơi xây cục bộ mental bản đồ.",
        "BLACKOUT_APPROACH: ánh sáng/tiếng ù giảm dần, bề mặt thô hơn, dấu hiệu warning tăng.",
        "BLACKOUT_DEEP: visual information rất thấp, tiếng ù vắng, tactile/audio các dấu hiệu chiếm ưu thế.",
        "HOLE_HAZARD: hố density trở thành ràng buộc chính của movement.",
        "PILLAR_NAVIGATION: hướng di chuyển commitment và large-scale mệt mỏi là ràng buộc chính.",
        "ARCH_REST: hình học ổn định tương đối và vị trí khô/raised có thể tạo pause ngắn.",
        "ROUTE_EMERGENCE: chỉ khi Core exitAvailable, cấu trúc văn phòng với hồ sơ và các dấu hiệu của tầng đầu tiên mới bắt đầu hiện ra.",
        "RECOVERY_AFTER_EVENT: noise/blackout/shift có thể giảm intensity, tránh escalation vô hạn."
      ],
      "stateTransitions": [
        "BASELINE -> NOISE_SPIKE khi một cụm ánh sáng trở nên quá lớn; sau một thời gian/di chuyển đủ xa -> BASELINE.",
        "BASELINE -> BLACKOUT_APPROACH phải có warning sensory; BLACKOUT_APPROACH -> BLACKOUT_DEEP nếu Cao Minh tiếp tục vào vùng tối.",
        "BLACKOUT_DEEP -> BASELINE khi Cao Minh thật sự đến vùng sáng; không teleport khỏi darkness.",
        "BASELINE -> PILLAR_NAVIGATION khi room mở rộng và lưới cột xuất hiện dần.",
        "BASELINE -> HOLE_HAZARD khi hố đầu tiên có thể quan sát/kiểm tra trước khi dense field xuất hiện.",
        "BASELINE -> ARCH_REST qua transition room/ngõ cụt có arch mô-típ.",
        "Bất kỳ visible khu vực -> SHIFT_ACTIVE chỉ sau khi khu vực không còn direct observation; shift phải ảnh hưởng cấu trúc không gian phía sau/chỗ cũ, không rewrite vật đang được Cao Minh nhìn.",
        "SHIFT_ACTIVE -> STABLE_POCKET nếu lộ trình đi vào arch/ổn định khu vực hoặc kiểu mẫu duy trì qua nhiều lượt.",
        "Bất kỳ trạng thái -> ROUTE_EMERGENCE chỉ khi LevelCore báo LEVEL TRANSITION: AVAILABLE.",
        "ROUTE_EMERGENCE không được dùng để bỏ qua hành động crossing boundary; người chơi vẫn phải thực sự đi qua transition."
      ],
      "environmentEvents": [
        "huỳnh quang âm lượng Surge: tiếng ù tăng dần đến mức đau tai; Cao Minh có thể che tai, rời vùng hoặc chịu phơi nhiễm lâu hơn.",
        "Single-ánh sáng Failure: một bộ đèn tắt hoặc chập, thay đổi cục bộ shadow nhưng không tạo vùng mất sáng ngay.",
        "cụm chớp tắt: vài đèn chớp tắt không đồng bộ; chỉ là môi trường dấu hiệu nếu Core chưa mở lối ra.",
        "Peripheral Rearrangement: sau khi Cao Minh rời một giao lộ và quay lại, một hành lang phía sau đổi chiều dài/góc/connection.",
        "Mark Displacement: dấu đánh dấu cũ vẫn tồn tại nhưng vị trí tương đối với giao lộ đã đổi, chứng minh mapping toàn cục không đáng tin.",
        "Arch Stability: một phòng vòm vẫn giữ nguyên qua revisit, cung cấp bằng chứng rằng shift không đồng đều.",
        "Pillar Misalignment: lưới phía sau trở nên asymmetric sau khi không được quan sát.",
        "thảm Saturation Change: vùng thảm dần ướt hơn/khô hơn theo vài room, ảnh hưởng footing và comfort.",
        "Unknown chất lỏng mùi: thảm phát mùi salt/chemical/formalin-like; chỉ là dấu hiệu tránh tiếp xúc, không xác định chính xác mẫu nếu không test.",
        "Familiar giọng nói sự kiện: giọng quen gọi mơ hồ từ xa; không trả lời thay Cao Minh và không xác nhận người nói.",
        "tường tiếng cào sự kiện: tiếng cào đi song song một đoạn rồi dừng; không cho nguồn lộ diện nếu không EntityCore.",
        "False Footstep tiếng vọng: bước chân dường như trễ/không khớp cadence của Cao Minh, nguyên nhân không xác minh.",
        "luồng khí vùng nhỏ: luồng khí qua trần nhà/tường seam gợi hình học phía sau nhưng không tự tạo lối ra.",
        "Blackout Silence: tiếng ù biến mất khi đi sâu vào dark khu vực, làm mọi tiếng nước/bước chân nổi bật.",
        "Shallow chất lỏng Contact: chân chạm chất lỏng trong chỗ trũng trên sàn, tạo ô nhiễm concern nhưng không hư hại tức thời tự động.",
        "Hole Discovery: hố đầu tiên được nhận biết qua visibility/edge/luồng khí; cho người chơi cơ hội phản ứng.",
        "Long-Hall mệt mỏi: hall cực dài gây cảm giác tiến độ thấp; phải gắn với physical distance, không chỉ prose lặp.",
        "ổn định mốc định hướng Return: Cao Minh gặp lại một arch/defect thật sự còn nguyên, củng cố tính liên tục thay vì mọi thứ luôn đổi.",
        "bằng chứng Without nguyên nhân: dấu chân, smear, dent hoặc scrape xuất hiện nhưng không đủ dữ liệu để kết luận Entity/human.",
        "Quiet Nothing-Major sự kiện: không có anomaly lớn, nhưng một chi tiết vật lý mới phải thay đổi hiểu biết cục bộ bản đồ hoặc nguy cơ."
      ],
      "interactionRules": [
        "MARK tường: cho phép tạo cục bộ mark; mark có thể tồn tại, nhưng Peripheral Shift có thể đổi vị trí tương đối của hành lang quanh nó. Không tự xóa mark nếu không có lý do.",
        "DRAW bản đồ: bản đồ hữu ích trong vùng ổn định/phòng vòm và ngắn hạn; không cho toàn cục độ chắc chắn.",
        "FOLLOW tường: hợp lý ở darkness/hành lang nhưng không bảo đảm lối ra do cấu trúc không gian shift.",
        "LISTEN: trả về layered audio bằng chứng gồm tiếng ù, tiếng vọng, possible anomalous sound; không biến nghe thành radar toàn tri.",
        "INSPECT thảm: có thể xác định độ ẩm, bề mặt, màu, mùi và ô nhiễm dấu hiệu; không xác định chemical composition nếu không có tool phù hợp.",
        "INSPECT tường: có thể thấy giấy dán tường seams, mold, hư hại, arch material hoặc luồng khí; không tự mở secret passage.",
        "INSPECT ánh sáng: có thể thấy chớp tắt, heat, hum, bộ đèn hư hại; phá đèn không tạo lối ra trong BACKROOMsV2.",
        "REST: chỉ hợp lý nếu footing/location tương đối an toàn; SurvivalCore quyết định recovery thực tế nếu có.",
        "RUN: tăng tốc traversal nhưng tăng risk ở bãi hố, blackout, ướt thảm và mệt mỏi.",
        "MOVE SLOWLY: giảm footing risk và tăng detail discovery nhưng không tự tăng lộ trình streak.",
        "CALL OUT: Isolation Effect khiến không thể coi response là người thật chỉ vì nghe giọng nói.",
        "BREAK tường: vật liệu có thể hư nếu khả năng/đồ nghề đủ, nhưng không bypass lộ trình hoặc tạo Level transition.",
        "CLIMB ARCH/PILLAR: xử lý theo vật lý cụ thể; không cho nhìn thấy 'toàn Level' hoặc bản đồ tổng thể.",
        "SAMPLE chất lỏng: mô tả thao tác lấy mẫu nếu trang bị hợp lệ; không tự thêm item nếu inventory system không hỗ trợ.",
        "FOLLOW FAMILIAR giọng nói: cho phép movement choice nhưng giữ nguồn chưa xác minh và lộ trình Core authoritative."
      ],
      "actionConsequences": [
        "Đi nhanh qua thảm ướt: tiết kiệm thời gian narratively nhưng tăng mệt mỏi/footing dấu hiệu, không tự trừ stat ngoài Core.",
        "Quay lại ngay sau một góc: khu vực vẫn có thể còn giống cũ nếu chưa đủ thời gian/occlusion; Peripheral Shift không phải jumpscare bắt buộc.",
        "Rời một giao lộ lâu rồi quay lại: có cơ sở mạnh hơn để bố cục phía sau khác.",
        "Đánh dấu nhiều điểm: giúp nhận ra shift khi mark xuất hiện ở relation mới; không phải biện pháp vô dụng tuyệt đối.",
        "Nghỉ ở phòng vòm: giảm narrative pressure và cho cơ hội kiểm tra trạng thái/trang bị, nhưng không spawn nhu yếu phẩm.",
        "Đi vào blackout: mất visual detail, tăng tactile/audio lời kể và định hướng sự bất định.",
        "Đi qua phòng cột: tiến trình nên đo bằng row/lưới/mốc định hướng thay vì mô tả 'đi mãi'.",
        "Tiếp cận bãi hố khi mệt: lời kể phải nhấn footing và decision space thay vì tự làm Cao Minh rơi.",
        "Chạm thảm chất lỏng bằng da/vết thương: mô tả phơi nhiễm concern; actual injury/infection cần system/bằng chứng.",
        "Đi theo anomalous giọng nói: scene có thể dẫn đến vòng lặp/bằng chứng/quiet disappearance, không bắt buộc Entity reveal.",
        "Bịt tai/rời đợt tăng tiếng ồn: giảm phơi nhiễm hợp lý khi distance tăng.",
        "Cố phá lộ trình boundary trước khi exitAvailable: environment phải vẫn giữ Level 0, không tạo fake lối ra."
      ],
      "persistenceRules": [
        "Injury, ướt clothing, used item, mệt mỏi và consequences đã được trạng thái ghi phải tồn tại qua room change.",
        "người chơi-created physical mark không tự biến mất; Peripheral Shift chỉ thay đổi spatial relation hoặc khiến lộ trình không quay lại được mark.",
        "Vật bị Cao Minh di chuyển trong cùng observed/ổn định room phải giữ vị trí cho đến khi có nguyên nhân thay đổi.",
        "Một room đã được mô tả là arch/ổn định không nên đổi hoàn toàn ngay lượt kế tiếp khi vẫn đang được quan sát.",
        "Blackout khu vực không tự sáng lại chỉ vì phản hồi mới; cần movement/sự kiện/nguyên nhân.",
        "bãi hố đã phát hiện vẫn là nguy cơ nếu Cao Minh còn ở cùng khu vực.",
        "Anomalous sound có thể biến mất mà không reveal nguyên nhân, nhưng không được retroactively nói nó chưa từng xảy ra.",
        "bằng chứng như dấu chân/smear/hư hại phải giữ sự bất định label; lượt sau không được tự upgrade thành fact.",
        "Level transition không xóa trạng thái sinh tồn, party, inventory hoặc combat consequence.",
        "biến đổi bố cục không được dùng để undo người chơi thành công như tìm cover, tránh hố hoặc vượt một room đã hoàn tất."
      ],
      "evidenceRules": [
        "OBSERVED FACT: vật liệu, ánh sáng, kích thước gần đúng, hố, chất lỏng, mark, physical hư hại trực tiếp thấy/chạm/nghe tại chỗ.",
        "STRONG INFERENCE: hướng luồng khí, room stability qua revisit, ô nhiễm risk từ mùi/appearance; vẫn không phải độ chắc chắn.",
        "chưa xác minh: giọng nói, tiếng cào, dark figure, dấu chân không rõ nguồn, feeling watched, familiar sound.",
        "CORE-CONFIRMED: Entity cuộc chạm trán, item, party member, currentLevelKey, lộ trình availability và stat consequences.",
        "BACKSTAGE CANON không tự trở thành kiến thức của Cao Minh. GM được dùng để giữ thế giới đúng nhưng không cho Cao Minh biết tên 'Peripheral Shift' hoặc 'Isolation Effect' nếu anh chưa có nguồn in-world hợp lệ.",
        "Một dark figure thoáng qua không được tự biến thành Entity ID.",
        "Một chất lỏng giống Almond Water không được gọi là Almond Water an toàn.",
        "Một flickering tường không được gọi là lối ra nếu LevelCore đang LOCKED.",
        "Một familiar giọng nói không chứng minh người đó hiện diện ở Level 0.",
        "Một cục bộ bản đồ đúng trong vài room không chứng minh cấu trúc không gian toàn cục ổn định."
      ],
      "navigationPatterns": [
        "COMMIT hướng di chuyển: phù hợp phòng cột; kể tiến trình bằng số row tương đối, acoustic change và mệt mỏi.",
        "mốc định hướng CHAIN: dùng 2-4 mốc định hướng cục bộ liên tiếp thay vì một marker duy nhất; shift có thể phá một phần chain.",
        "tường FOLLOWING: hữu ích ở blackout/hành lang nhưng có thể vòng lặp khi hình học shift.",
        "ARCH ANCHOR: dùng phòng vòm ổn định làm anchor quay lại/so sánh thay đổi.",
        "SOUND SEEKING: trong blackout có thể theo tiếng ù về vùng sáng, nhưng apparent hướng có thể thay đổi; không gian vẫn cần traversal.",
        "AVOID nguy cơ lưới: bãi hố buộc chọn line an toàn; thành công/failure dựa trên action/trạng thái, không prose random.",
        "RETRACE TEST: người chơi cố quay lại để kiểm tra bố cục; đây là cơ hội tự nhiên để reveal vùng ổn định hoặc Peripheral Shift.",
        "EDGE SCOUTING: ở open/pillar area có thể kiểm tra perimeter trước khi commit hướng.",
        "SLOW VERIFICATION: dừng nghe/chạm/đánh dấu trước giao lộ để tăng quality của cục bộ bằng chứng, không tăng lộ trình odds.",
        "lộ trình EMERGENCE: khi exitAvailable, các dấu hiệu của Zenith Station xuất hiện dần và nhất quán hơn qua nhiều observation."
      ],
      "routeProgressionCues": [
        "thành công sớm: mốc định hướng mới nhưng vẫn thuần Level 0, room relation bớt vòng lặp trong lượt đó.",
        "thành công giữa: cấu trúc variation rõ hơn, ổn định dấu hiệu hoặc thay đổi vật liệu nhỏ nhưng chưa có Zenith Station.",
        "Khi gần tìm ra lối ra: xuất hiện dấu hiệu giấy tờ và hành lang văn phòng lạ, nhưng chưa xác nhận tầng kế.",
        "EXIT_AVAILABLE: dấu hiệu chuyển tiếp có thể trở nên bền vững: panel kim loại, kính, machine hum khác huỳnh quang tiếng ù, doorway hình học không còn thuần Level 0.",
        "đặt lại: mốc định hướng lộ trình vừa theo trở nên không còn nối đúng, hoặc người chơi quay lại một anchor cũ; không xóa mọi dấu vết.",
        "đặt lại trong blackout: tiếng ù dẫn sai/vòng lặp về vùng đã biết thay vì teleport.",
        "đặt lại trong phòng cột: hướng di chuyển đưa về lưới/mốc định hướng quen theo cấu trúc không gian shift.",
        "Không dùng màu đỏ, Manila Room hoặc Level 1 làm dấu hiệu mở lối từ Level 0."
      ],
      "encounterStaging": [
        "Nếu EntityCore không đang hoạt động: môi trường tension phải tự đứng vững, không thêm creature.",
        "Nếu EntityCore đang hoạt động trong standard rooms: dùng corner, narrow lối mở, line-of-sight break và ướt thảm làm terrain thực tế.",
        "Nếu EntityCore đang hoạt động trong phòng cột: cột tạo cover/occlusion nhưng không teleport Entity.",
        "Nếu EntityCore đang hoạt động gần bãi hố: hố là shared nguy cơ cho cả Cao Minh/Entity; không tự one-shot đối thủ bằng hố.",
        "Nếu EntityCore đang hoạt động trong blackout: visibility ràng buộc áp dụng cho lời kể; không cho GM biết chính xác vị trí Entity nếu trạng thái không cung cấp.",
        "Nếu đang hoạt động cuộc chạm trán có feedbackEvents từ Combat Core, terrain chỉ contextualize animation/hư hại, không sửa kết quả combat.",
        "Isolation Effect không được dùng để loại companion đã joined hoặc đang hoạt động Entity đã được Core đưa vào trạng thái.",
        "Sau combat, blood/hư hại/debris hợp lý có thể tồn tại cục bộ nếu không mâu thuẫn trạng thái; loot vẫn do ItemCore."
      ],
      "hazardEscalation": [
        "LOW: damp thảm, monotony, mild tiếng ù variation, minor định hướng sự bất định.",
        "MODERATE: prolonged mệt mỏi, ướt giày, ambiguous noises, shift bằng chứng, larger pillar/arch traversal.",
        "HIGH: blackout, dense hole lưới, strong đợt tăng tiếng ồn, ô nhiễm phơi nhiễm hoặc severe survival depletion.",
        "CRITICAL chỉ khi trạng thái/hành động hỗ trợ: mất footing cạnh hố, serious phơi nhiễm, severe dehydration/exhaustion, đang hoạt động combat.",
        "Không nhảy từ LOW sang CRITICAL chỉ để tạo drama.",
        "Sau một HIGH sự kiện nên có khả năng RECOVERY/QUIET interval nếu trạng thái cho phép, tránh mọi lượt đều leo thang."
      ],
      "quietTurnPatterns": [
        "tính liên tục turn: cùng room/khu vực nhưng người chơi phát hiện một relation mới giữa mốc định hướng.",
        "Verification turn: kiểm tra một dấu hiệu cũ và xác nhận nó vẫn còn hoặc đã đổi.",
        "Resting observation: không có danger mới; body/environment detail phản ánh thời gian ở Level.",
        "Tool-use turn: người chơi dùng trang bị để đo/đánh dấu/kiểm tra, kết quả có giới hạn rõ.",
        "False alarm: anomalous sound dừng mà không reveal nguồn; sự bất định được giữ.",
        "ổn định interval: không có shift đáng kể, cho phép người chơi tin tạm cục bộ bản đồ.",
        "môi trường decay: một bulb, giấy dán tường edge hoặc thảm condition thay đổi nhỏ mà không thành sự kiện lớn.",
        "Choice-space turn: cảnh tạo 2-3 hướng khác nhau về hình học/nguy cơ, không ép một lộ trình 'đúng'."
      ],
      "narrativeGrammar": [
        "Toàn bộ lời kể hướng tới người chơi phải là tiếng Việt tự nhiên. Chỉ giữ nguyên tên riêng hoặc tên chính thức; các thuật ngữ mô tả môi trường nội bộ phải được chuyển sang tiếng Việt trước khi viết.",
        "Mỗi phản hồi ưu tiên 1-3 chi tiết môi trường mới thật sự liên quan đến hành động, không checklist mọi giác quan.",
        "Mô tả Level 0 bằng sự khác biệt nhỏ giữa những thứ giống nhau, không chỉ lặp 'vàng, ẩm, tiếng ù'.",
        "Khi cấu trúc không gian shift, mô tả bằng contradiction cụ thể: giao lộ thiếu một nhánh, mark ở sai relation, row cột lệch; tránh câu mơ hồ 'mọi thứ thay đổi'.",
        "Khi quiet, cho người chơi thông tin usable về room/nguy cơ/định hướng thay vì prose trống.",
        "Khi anomalous sound xuất hiện, giữ khoảng cách/ngữ nghĩa mơ hồ và không reveal nguyên nhân.",
        "Khi mệt mỏi quan trọng, mô tả hậu quả cơ thể trực tiếp như chân đau, giày ướt, khó tập trung; không kể nội tâm thay Cao Minh.",
        "Không gọi tên mechanic backstage như Peripheral Shift/Isolation Effect trong lời kể nếu Cao Minh chưa học thuật ngữ.",
        "Không dùng 'vô tận' trong mọi lượt; biểu đạt scale qua sightline, repetition, travel time và mốc định hướng scarcity.",
        "Không kết thúc phản hồi bằng cliffhanger giả tạo nếu không có sự kiện thật."
      ],
      "antiRepetition": [
        "Không dùng cùng lối mở sentence kiểu mẫu hai lượt liên tiếp.",
        "Không nhắc tiếng ù + thảm + giấy dán tường đủ bộ trong mọi phản hồi; chọn chi tiết phù hợp hành động.",
        "Sau khi một cấu trúc type đã được mô tả chi tiết, các lượt tiếp theo trong cùng type chỉ nêu delta hoặc mốc định hướng mới.",
        "Không dùng dark figure/tiếng thì thầm/tiếng cào như spice lặp lại. Anomalous audio phải thưa hơn môi trường observation.",
        "Không tạo chớp tắt ở mọi lượt; đa số đèn có thể chỉ sáng đều và khó chịu.",
        "Không làm Peripheral Shift sau mọi corner; ổn định intervals là canon hợp lệ.",
        "Không biến mỗi ngõ cụt thành secret anomaly.",
        "Không lặp 'không có gì xảy ra'. Quiet turn phải cung cấp concrete cục bộ information.",
        "Không lặp cùng một hố/arch/pillar mô-típ liên tiếp nếu người chơi đã rời khu vực.",
        "Không dùng cùng một bằng chứng hook hai lần liên tiếp: dấu chân, blood smear, giọng nói, tiếng cào phải luân phiên hoặc vắng mặt.",
        "Không tăng horror bằng adjective; tăng bằng ràng buộc, bằng chứng và consequence."
      ],
      "forbiddenInventions": [
        "Red Rooms xuất hiện trực tiếp trong Level 0 runtime.",
        "Manila Room xuất hiện trực tiếp trong Level 0 runtime.",
        "Level 1 lối ra hoặc flickering tường hoạt động trước LevelCore exitAvailable.",
        "M.E.G./B.N.T.G./Camp/Base/Tom's Diner hoặc settlement tự xuất hiện.",
        "Resident Hound/Smiler/Skin-Stealer hoặc Entity mới khi EntityCore không đang hoạt động.",
        "Almond Water an toàn lấy từ thảm.",
        "thảm có thể ăn được.",
        "bãi hố chắc chắn dẫn tới một Level cụ thể.",
        "Một AI, narrator hoặc consciousness điều khiển Level 0.",
        "Security camera đang có người theo dõi nếu trạng thái không chứng minh.",
        "ổ điện cấp điện tương thích chắc chắn với trang bị.",
        "Bản đồ toàn cục chính xác.",
        "Cao Minh tự biết thuật ngữ lore chỉ vì GM biết.",
        "Peripheral Shift thay đổi room đang được nhìn trực tiếp.",
        "Isolation Effect tự xóa party member đã joined.",
        "Memory Room chứng minh ký ức sai/đúng hoặc thay đổi backstory.",
        "Familiar giọng nói chắc chắn là người quen thật.",
        "Dấu vết chắc chắn do Entity/human tạo.",
        "Break tường hoặc break ánh sáng bypass lộ trình.",
        "Một lối trở về thế giới nguyên sinh tự xuất hiện lại để giải cứu."
      ],
      "sceneSeeds": [
        "Một giao lộ bình thường trở nên đáng nhớ vì một ổ điện bị cháy sém và thảm khô bất thường quanh nó.",
        "phòng vòm có hai vòm usable và một vòm chỉ là hốc nông, giúp người chơi phân biệt khi quay lại.",
        "Pillar hall có một cột nứt ngang ở tầm vai làm anchor hướng di chuyển.",
        "bãi hố thưa bắt đầu bằng một hố đơn, rồi lưới chỉ rõ sau khi người chơi tiến thêm.",
        "Blackout ngưỡng được báo trước bởi ba bộ đèn liên tiếp phát tiếng ù yếu dần.",
        "Trong blackout, một strip tường thô hơn giúp Cao Minh biết mình chưa quay về yellow room.",
        "Một tường mark do Cao Minh tạo vẫn tồn tại nhưng hành lang bên cạnh nó từ hai nhánh thành một.",
        "Một arch anchor giữ nguyên qua revisit, chứng minh không phải mọi khu vực đều shift.",
        "Một đợt tăng tiếng ồn buộc người chơi cân nhắc che tai/rời vùng nhưng không có creature.",
        "Một familiar giọng nói dùng đúng nhịp/âm sắc quen thuộc nhưng từ quá xa để nhận chữ.",
        "Một tiếng cào sound chạy song song với hành lang rồi dừng ngay khi người chơi dừng, nhưng không reveal nguồn.",
        "thảm chất lỏng có mùi hóa chất rõ hơn ở một room, khiến tránh tiếp xúc là lựa chọn hợp lý.",
        "Một long hall có đèn đều nhưng trần nhà tile kiểu mẫu thay đổi dần, tạo dấu hiệu distance.",
        "Một vùng ổn định cho phép người chơi bản đồ ba room liên tiếp chính xác.",
        "Một false-familiar room gần giống anchor cũ nhưng ổ điện/trần nhà/corner count không khớp.",
        "Một stair flight ngắn chỉ đưa lên landing trong cùng Level, phá kỳ vọng lối ra.",
        "Pillar rows phía sau lệch khỏi lưới sau khi người chơi quay lại từ giao lộ.",
        "Một dry patch đủ để ngồi nghỉ nhưng nằm gần đợt tăng tiếng ồn zone, tạo tradeoff.",
        "Một blackout recess có chất lỏng nông khiến tốc độ chạy trở thành quyết định rủi ro.",
        "Một hole lưới có safe path rõ nhưng yêu cầu đi vòng xa hơn.",
        "Một nấm mốc cụm làm mùi thay đổi trước khi hình học thay đổi, dùng sensory dấu hiệu thay visual.",
        "Một lối mở thấp dẫn sang room cùng mô-típ, tạo posture ràng buộc không cần anomaly.",
        "Một wide chamber có tiếng vọng khiến anomalous footstep khó phân biệt với chính Cao Minh.",
        "Một mark chain cho thấy hai mark còn đúng nhưng mark thứ ba nằm sai relation, bằng chứng shift từng phần.",
        "Một quiet room không có anomaly nhưng cho thấy thảm độ sâu giảm, giúp người chơi suy luận đang gần pillar-type khu vực.",
        "Khi lộ trình gần hoàn tất, một seam kim loại vô hại xuất hiện trong tường nhưng chưa đủ xác định Zenith Station.",
        "Khi exitAvailable, machine hum khác hẳn huỳnh quang tiếng ù trở thành dấu hiệu bền vững qua nhiều room.",
        "Chuyển tầng chỉ hoàn thành khi Cao Minh bước qua ranh giới vào Hui's Family Level 1."
      ],
      "_canon": {
        "id": "level.0.foundation",
        "owner": "world:level:0",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "https://backrooms-wiki.wikidot.com/level-0",
        "revision": "2026-09-19",
        "scope": [
          "level:0"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "1": {
      "name": "Level 1 — Parking Zone",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 1 Habitable Zone",
        "status": "project-name override"
      },
      "identity": [
        "Một hệ hall bê tông khổng lồ gợi parking garage/warehouse, có cột, đường dốc, ống trên trần và các sector đủ khác nhau để tạo landmark.",
        "Trong BACKROOMsV2 tên gameplay giữ là Parking Zone, dù nguồn Wiki hiện đại gọi Level 1 là Habitable Zone."
      ],
      "architecture": [
        "The Halls rộng, bê tông xám, cột lớn, trần có ống; một số vùng giống bãi đỗ xe hơn warehouse.",
        "The Corridors là mạng lối hẹp qua cửa push-door, hình học ổn định hơn halls và có nhiều phòng nhỏ vô dụng.",
        "Xe hỏng có thể hiếm gặp theo project canon; không mặc định nhiên liệu hoặc chức năng."
      ],
      "zones": [
        "Open Halls/Parking Sectors: không gian lớn, dễ quan sát hơn nhưng chịu Flickering và encounter.",
        "Corridors: hẹp, labyrinthine nhưng hình học đáng tin hơn; phù hợp nghỉ hơn, tài nguyên ít.",
        "Maintenance Halls: vùng có điện/hạ tầng tốt hơn tương đối nhưng không phải safe base tuyệt đối.",
        "Luxury Lots: sạch hơn, sương lạnh hơn và có khả năng supplies cao hơn tương đối theo project canon."
      ],
      "sensory": [
        "Âm bước chân và tiếng kim loại/ống vang xa trên bê tông.",
        "Nhiệt độ phần lớn ấm hoặc trung tính nhưng có túi sương lạnh.",
        "Ánh sáng công nghiệp có thể tắt đột ngột trong Flickering."
      ],
      "anomalies": [
        "Non-Euclidean halls làm khoảng cách cảm nhận và tuyến nhìn không hoàn toàn đáng tin.",
        "Flickering tắt toàn bộ đèn tự nhiên trong thời lượng không dự đoán được và làm encounter risk tăng.",
        "Crate/resource behavior tồn tại trong source lore, nhưng BACKROOMsV2 ItemCore sở hữu spawn loot."
      ],
      "hazards": [
        "Blackout/Flickering, mất phương hướng ở halls, con người thù địch nếu state có, và roaming Entity từ EntityCore.",
        "Nước rò từ ống không mặc định uống được.",
        "Corridor kín có nguy cơ smoke/thiếu thông gió nếu người chơi tạo lửa."
      ],
      "resources": [
        "Project canon cho phép supply crates hiếm hơn/đậm hơn ở một số sector, nhưng spawn thực tế chỉ từ ItemCore.",
        "Vật liệu môi trường như pallet, thùng, đồ nội thất có thể làm cover/barrier nếu hợp vật lý."
      ],
      "entities": [
        "Entity active chỉ do EntityCore.",
        "Flickering có thể làm tăng cảm giác nguy hiểm nhưng không được GM tự spawn creature."
      ],
      "navigation": [
        "Halls: dùng cột, dốc, ống, biển/mark và thay đổi sector; đừng tin khoảng cách tuyệt đối.",
        "Corridors: route có thể được retrace bằng mark vật lý tốt hơn.",
        "BACKROOMsV2 transition sang Level 2 nên xuất hiện dần khi xe/cột giảm và pipes/machinery chiếm ưu thế."
      ],
      "entrancesExits": [
        "Đến từ Hui's Family Level 16 theo progression game.",
        "Điểm đến chuẩn tiếp theo của route chính là Level 2; các exit phụ ngoài route không được tự mở."
      ],
      "gameplayOverride": [
        "Giữ tên Parking Zone và project progression dù Wiki hiện đại dùng Habitable Zone.",
        "Không tự tạo settlement hoặc Base Alpha nếu state chưa đưa vào campaign."
      ],
      "gmConstraints": [
        "Không cho crate/item ngoài ItemCore.",
        "Không biến corridors thành hoàn toàn safe nếu active encounter/state có danger.",
        "Không cho player skip thẳng tới Level 3+."
      ],
      "variationPool": [
        "Dãy cột bê tông mất hút trong sương lạnh cục bộ.",
        "Một ramp xuống nhưng quay về cùng cao độ do topology.",
        "Cửa đôi mở vào corridor nhỏ với phòng phụ trống.",
        "Ống trần nhỏ giọt tạo puddle không uống được.",
        "Một cụm đèn tắt tạo pocket darkness trước khi Flickering toàn khu.",
        "Bãi xe hiếm với một thân xe không hoạt động và kính bám bụi."
      ],
      "_canon": {
        "id": "level.1.foundation",
        "owner": "world:level:1",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "BACKROOMsV2 canon + Backrooms Wiki Level 1 Habitable Zone",
        "revision": "runtime-current",
        "scope": [
          "level:1"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "2": {
      "name": "Level 2 — Pipe Dreams",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 2 Abandoned Utility Halls",
        "status": "project-name override"
      },
      "identity": [
        "Mạng utility tunnel gần như vô tận, chủ yếu Euclidean nhưng cực kỳ phức tạp, hẹp và bị máy móc/ống dẫn chiếm không gian.",
        "BACKROOMsV2 giữ tên Pipe Dreams và nhấn mạnh nhiệt, hơi nước, địa chấn cục bộ và rủi ro từ chất lỏng trong ống."
      ],
      "architecture": [
        "Tường bê tông/bạch gạch cũ, bề mặt bột/chalky; corridor thường rẽ theo góc cứng và kích thước thay đổi từ vừa đủ đi đến rất chật.",
        "Pipes, dây, machinery và electrical fixtures phủ tường/trần; nhiều máy không có chức năng phối hợp rõ.",
        "Door rooms có thể là kho lớn, room nhỏ, office-like loops hoặc không gian tối bất thường; GM không được mở void room nếu không cần và không có state."
      ],
      "zones": [
        "Narrow Pipe Corridors: chật, nóng, nhiều cạnh kim loại và âm máy.",
        "Machinery Bays: phòng rộng hơn có thiết bị lớn, dây điện và vật tư công nghiệp.",
        "Dark Circuits: cụm ánh sáng mất do hệ dây nối chuỗi hoặc hư hỏng.",
        "Steam/Heat Sections: vùng ống nóng, hơi nước và thay đổi nhiệt mạnh theo project canon."
      ],
      "sensory": [
        "Tiếng whir, rung, nước/chất lỏng chảy trong ống, fluorescent buzz và tiếng kim loại giãn nhiệt.",
        "Không khí có thể nóng ẩm hoặc đổi đột ngột sang lạnh.",
        "Bụi chalky bám tay/quần áo ở corridor sát tường."
      ],
      "anomalies": [
        "Máy và pipes nối với nhau theo cách không có hệ thống chức năng rõ nhưng vẫn có điện/chất truyền bên trong.",
        "Một lỗi dây có thể làm tối cả cụm đèn không dễ dự đoán.",
        "Earthquake/collapse/ruptured pipes trong project canon có thể thay route nhưng phải để lại hậu quả vật lý."
      ],
      "hazards": [
        "Nhiệt, steam, điện, machinery, corridor kẹp người, kính/kim loại vỡ và collapse.",
        "Chất lỏng giống Almond Water trong pipe không được uống; có nguy cơ ô nhiễm sinh học/hóa học/anomaly.",
        "Roaming entities chỉ qua EntityCore."
      ],
      "resources": [
        "Toolboxes, scrap, parts và container có thể tồn tại làm scenery; item usable phải do Core.",
        "Không tự cho Almond Water từ pipe dù màu/mùi tương tự."
      ],
      "entities": [
        "Project canon liên hệ Level 2 với nhiều Entity types nhưng runtime vẫn do EntityCore.",
        "Tiếng máy hoặc movement sau tường không tự là Biological Pipeline hay Entity khác."
      ],
      "navigation": [
        "Pipe màu, loại máy, nhiệt độ, hướng steam và door style là landmark.",
        "Transition sang Level 3 khi high-voltage infrastructure, transformer/cables và electrical rooms trở nên chủ đạo."
      ],
      "entrancesExits": [
        "Route chính vào từ Level 1.",
        "Route chính ra Level 3; các door exit khác của Wiki không tự kích hoạt."
      ],
      "gameplayOverride": [
        "Giữ tên Pipe Dreams và transition project canon.",
        "Không thay hệ route chính bằng lore exit hiện đại của Wiki."
      ],
      "gmConstraints": [
        "Không cho đường ống tự cấp item.",
        "Không dùng earthquake mỗi lượt.",
        "Không làm máy móc 'có ý thức' nếu chưa có evidence."
      ],
      "variationPool": [
        "Corridor vừa đủ vai người với pipe lớn ép sát một bên.",
        "Dãy đèn trắng/cam lệch màu và một cụm tắt.",
        "Van xả hơi làm mờ tầm nhìn trong vài giây.",
        "Trolley cũ có scrap không phải loot tự động.",
        "Door thép khóa với ô kính nhìn vào phòng tối.",
        "Một đoạn pipe rung mạnh trước khi áp suất giảm."
      ],
      "_canon": {
        "id": "level.2.foundation",
        "owner": "world:level:2",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "BACKROOMsV2 canon + Backrooms Wiki Level 2 Abandoned Utility Halls",
        "revision": "runtime-current",
        "scope": [
          "level:2"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "3": {
      "name": "Level 3 — The Electrical Station",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 3 Electrical Station",
        "status": "active"
      },
      "identity": [
        "Một facility điện công nghiệp cực nguy hiểm, dày đặc điện năng, máy móc và resources nhưng có risk cao.",
        "Hành lang gạch/bê tông tối, fluorescent yếu, dây/cable/box và tiếng điện tạo nền âm thanh liên tục."
      ],
      "architecture": [
        "Brick walls màu không đồng nhất, sàn bê tông bụi, corridor có thể thấp/hẹp bất thường.",
        "Cables, conductors, electrical boxes, transformer, coils, fans và pipes phủ bề mặt.",
        "Pipes chứa chất lỏng đen bí ẩn xuất hiện trong một số khu; không được chạm/uống khi chưa kiểm tra."
      ],
      "zones": [
        "Generator Rooms: phòng lớn giàu thiết bị điện và tài nguyên nền.",
        "Assembly Lines: khu công nghiệp rộng hơn với machine/workstation.",
        "Boiler Rooms: heat/pipes và nguy cơ nhiệt.",
        "Sanctums/blocked rooms: các phòng/khối kiến trúc khó tiếp cận; không tự cho rằng safe."
      ],
      "sensory": [
        "Buzz, crackle, whir và rung điện chồng lên nhau; mùi ozone/kim loại nóng có thể xuất hiện khi có arcing.",
        "Ánh sáng dim, vùng tối xen kẽ và flash từ thiết bị.",
        "Heat thay đổi gần machinery; lower utility trong project canon có thể lạnh sâu."
      ],
      "anomalies": [
        "Project canon có blackout làm route thay đổi khi điện trở lại.",
        "Nguồn Wiki có các anomaly như black sludge/purpification/paranoia hotspots; chỉ dùng khi record/state cụ thể hỗ trợ, không nhồi tất cả trong mọi scene.",
        "Các thanh chắn/khối inaccessible không được phá chỉ vì player mạnh nếu canon xác định anomaly chứ không phải vật liệu thường."
      ],
      "hazards": [
        "Live electricity, arc, moving machinery, hot pipes, black liquid không rõ tính chất và corridor chật.",
        "Lower Utility Tunnels theo project canon dưới mức đóng băng, có standing water bất thường và disappearance risk.",
        "EntityCore sở hữu mọi combat encounter."
      ],
      "resources": [
        "Thiết bị điện, parts, Almond Water/Firesalt trong một số room theo source lore có thể tồn tại, nhưng inventory mutation thuộc Core.",
        "Không cho thiết bị tự hoạt động theo yêu cầu nếu state không xác nhận."
      ],
      "entities": [
        "Source lore mô tả Level 3 có hostile entities đáng kể; runtime spawn vẫn chỉ EntityCore.",
        "Không tự biến every blackout thành attack."
      ],
      "navigation": [
        "Dùng generator noise, cable density, brick type, heat và signage công nghiệp làm landmark.",
        "Transition route project canon sang Level 4 khi office-sector doors/elevators và kiến trúc hành chính tăng dần.",
        "Một số dark passages có thể gợi Level 6 trong project lore nhưng route game không được skip nếu Core không cho."
      ],
      "entrancesExits": [
        "Route chính vào từ Level 2.",
        "Route chính ra Level 4; direct Level 6 chỉ nếu GameCoreRules/Core thật sự cho phép theo state."
      ],
      "gameplayOverride": [
        "Project route/state có quyền cao hơn danh sách exit Wiki.",
        "Không tự dựng Base Gamma hoặc community nếu campaign chưa encounter."
      ],
      "gmConstraints": [
        "Không cho Cao Minh hack toàn bộ facility chỉ từ một terminal.",
        "Không giải thích nguồn điện tổng thể.",
        "Không auto-spawn resource vì Level nổi tiếng giàu tài nguyên."
      ],
      "variationPool": [
        "Generator room lớn với dây được tháo một phần và đèn emergency.",
        "Corridor thấp buộc thay đổi tư thế di chuyển.",
        "Pipe đen rung nhẹ với tiếng chất lỏng bên trong.",
        "Một bộ contactor đóng cắt gây flash và tiếng nổ khô.",
        "Bar chắn một phòng sáng phía sau nhưng không có gate.",
        "Khu office nhỏ xen giữa machinery báo hiệu transition dần."
      ],
      "_canon": {
        "id": "level.3.foundation",
        "owner": "world:level:3",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "BACKROOMsV2 canon + Backrooms Wiki Level 3 Electrical Station",
        "revision": "runtime-current",
        "scope": [
          "level:3"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "4": {
      "name": "Level 4 — The Abandoned Office",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 4 Abandoned Office",
        "status": "wiki-under-rewrite; project-authoritative"
      },
      "identity": [
        "Mạng office/cubicle bỏ hoang có geometry ổn định hơn Level 0–3, ánh sáng một phần và nhiều vùng yên tĩnh.",
        "Project canon coi đây là vùng nghỉ/salvage tương đối chứ không tuyệt đối an toàn."
      ],
      "architecture": [
        "Phòng office, cubicle, hallway, meeting room và khu utility với ít đồ nội thất hơn một văn phòng hoạt động.",
        "Windows phần lớn blacked out; những window hiển thị cảnh mưa hoặc view bất thường không được tự coi là đường ra.",
        "Geometry tương đối ổn cho phép map cục bộ có giá trị."
      ],
      "zones": [
        "Cubicle Fields: cụm bàn/vách ngăn và corridor office.",
        "Window Corridors: dãy cửa sổ cần thận trọng, có thể là trap theo source lore.",
        "Supply/Utility Rooms: water cooler/vending/fountain hoặc đồ văn phòng có thể hiện diện, nhưng Core sở hữu consumables.",
        "Quiet Rest Areas: vùng ít anomaly/entity hơn, không bảo đảm an toàn dài hạn."
      ],
      "sensory": [
        "Fluorescent hum nhẹ hơn các Level công nghiệp; điều hòa hoặc ventilation có thể chạy cục bộ.",
        "Không khí khô hơn và ít mùi ẩm; mùi giấy, bụi và nhựa/vải văn phòng.",
        "Cửa sổ có thể cho cảm giác mưa liên tục nhưng không cung cấp bằng chứng về outside world."
      ],
      "anomalies": [
        "Một số windows là trap/không đáng tin.",
        "Một khu đã clear hôm nay không được xem là vĩnh viễn clear; route/resources có thể drift theo world canon nhưng không reset tức thời."
      ],
      "hazards": [
        "False safety làm người chơi giảm cảnh giác, windows nguy hiểm, resource không ổn định và incursion từ roaming Entity.",
        "Large settlement không được tự dựng vì project canon tránh mặc định ổn định tài nguyên/boundary."
      ],
      "resources": [
        "Almond Water có thể phổ biến hơn theo source lore, nhưng BACKROOMsV2 ItemCore quyết định item thực tế.",
        "Office furniture và vật liệu có thể dùng làm cover/barrier tùy vật lý."
      ],
      "entities": [
        "Project canon không xác nhận resident population ổn định.",
        "Active encounter chỉ EntityCore; sightings ngoài Core phải được giữ là dấu hiệu chưa xác minh."
      ],
      "navigation": [
        "Room numbering, cubicle pattern, carpet type và window placement có thể làm landmark đáng tin hơn Level 0.",
        "Transition sang Level 5 nên diễn ra qua corridor ngày càng giống hotel: woodwork, carpet, brass, door proportions."
      ],
      "entrancesExits": [
        "Route chính vào từ Level 3.",
        "Route chính ra Level 5 theo progression game."
      ],
      "gameplayOverride": [
        "Không dùng các base/community Wiki làm mặc định campaign.",
        "Route game ưu tiên Level 5, không mở menu exit tự do."
      ],
      "gmConstraints": [
        "Không tự cấp Almond Water dù có water cooler.",
        "Không cho player bước qua window sang Frontrooms.",
        "Không biến Level 4 thành safe hub tuyệt đối."
      ],
      "variationPool": [
        "Cubicle row có vài ghế đổ nhưng mặt bàn gần như sạch.",
        "Meeting room với bảng trắng cũ và đèn chỉ sáng một nửa.",
        "Window corridor với kính tối hoàn toàn và mưa chỉ thấy ở một ô xa.",
        "Vending machine có điện nhưng inventory không xác định.",
        "Office carpet đổi dần sang pattern khách sạn ở vùng transition.",
        "Một stairwell yên tĩnh nhưng door signage không khớp tầng."
      ],
      "_canon": {
        "id": "level.4.foundation",
        "owner": "world:level:4",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "BACKROOMsV2 canon + Backrooms Wiki Level 4 Abandoned Office",
        "revision": "runtime-current",
        "scope": [
          "level:4"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "5": {
      "name": "Level 5 — Terror Hotel",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 5 Terror Hotel",
        "status": "active"
      },
      "identity": [
        "Một hotel complex phi Euclid vô tận với thẩm mỹ đầu thế kỷ 20, sạch đến mức bất tự nhiên và có cảm giác được duy trì dù bỏ hoang.",
        "Horror đến từ sự sang trọng quá nguyên vẹn, âm thanh không có nguồn rõ, cảm giác bị quan sát và các khu chức năng chuyển nhau bất hợp lý."
      ],
      "architecture": [
        "The Main Hall: wallpaper mahogany-red/gold, sàn gỗ/marble/carpet, furniture cổ, room doors với số không theo quy luật.",
        "The Beverly Room/Eternal Ballroom: ballroom rộng với nhiều cửa, chandelier và một bàn nhỏ trung tâm.",
        "The Boiler Room: machinery cổ, pipe đan dày, nóng và claustrophobic; pressure/leak là hazard.",
        "Project canon cũng cho phép restaurant/pool/maintenance/hotel-room wings nếu giữ cùng thẩm mỹ."
      ],
      "zones": [
        "Main Hall: khu phổ biến, furnished nhất và nhiều doors/elevators.",
        "Beverly Room: hub-like ballroom, nhiều cửa nhưng không phải exit menu tự do.",
        "Boiler/Maintenance: nóng, máy móc dày, route sang Level 6 theo project canon.",
        "Guest Wings: room/corridor có thể furnished hoặc trống, dùng cho exploration và rest risk."
      ],
      "sensory": [
        "Smooth jazz/vintage audio có thể phát không rõ nguồn và đổi bài bất chợt.",
        "Party chatter sau tường, whisper, touch sensation hoặc cảm giác tranh đang nhìn đã được báo cáo nhưng không phải mọi thứ đều là Entity.",
        "Hotel thường sạch, ít bụi; Boiler Room nóng, ồn và ẩm hơn."
      ],
      "anomalies": [
        "Vết bẩn có thể biến mất sau một thời gian như thể level tự làm sạch.",
        "Room numbers/elevator logic không theo quy luật Frontrooms.",
        "Perceptual effects tăng khi ở lâu nhưng BACKROOMsV2 không biến thành sanity meter."
      ],
      "hazards": [
        "Paranoia/sleep loss, wrong doors/elevators, Boiler heat/pressure và roaming Entity.",
        "Không cho hallucination trực tiếp gây damage vật lý nếu chưa có causal evidence."
      ],
      "resources": [
        "Furniture, hotel service objects và pipe contents có thể tồn tại; item gameplay vẫn qua Core.",
        "Không tự cho đồ ăn/uống từ bàn ballroom hoặc hotel room là an toàn."
      ],
      "entities": [
        "Source lore có nhiều Entities và Beast of Level 5; runtime spawn vẫn chỉ EntityCore.",
        "Eyes/whispers/party noise không tự là Beast."
      ],
      "navigation": [
        "Dùng wallpaper, flooring, room placards, elevator style và âm thanh để phân biệt wing.",
        "Transition sang Level 6 qua Boiler/Maintenance: ánh sáng giảm, nhiệt có thể đổi, architecture trở nên trần trụi và tối."
      ],
      "entrancesExits": [
        "Route chính vào từ Level 4.",
        "Route chính ra Level 6; exit Wiki khác không tự mở."
      ],
      "gameplayOverride": [
        "Project canon giữ psychological ambiguity, không áp sanity stat.",
        "Không tự dựng Homely Hotel/M.E.G. outpost nếu state chưa xác nhận."
      ],
      "gmConstraints": [
        "Không làm mọi painting thật sự có mắt sống.",
        "Không cho elevator button tùy ý teleport qua các Level.",
        "Không tự giải thích ai đang duy trì hotel."
      ],
      "variationPool": [
        "Hall gỗ tối với brass lamp và room placard không tuần tự.",
        "Một lounge sạch hoàn hảo dù ghế bọc vải đã rất cũ.",
        "Jazz đổi bài khi không ai ở gần speaker.",
        "Ballroom có cửa ở vị trí phi lý nhưng tất cả vẫn đóng.",
        "Boiler corridor với valve rung và condensate.",
        "Một guest room vừa đủ ở nhưng dấu hiệu cho thấy không nên ngủ quá lâu."
      ],
      "_canon": {
        "id": "level.5.foundation",
        "owner": "world:level:5",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "BACKROOMsV2 canon + Backrooms Wiki Level 5 Terror Hotel",
        "revision": "runtime-current",
        "scope": [
          "level:5"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "6": {
      "name": "Level 6 — Lights Out",
      "source": {
        "basis": "BACKROOMsV2 canon + Backrooms Wiki Level 6 Lights Out",
        "status": "wiki-outdated; project-authoritative"
      },
      "identity": [
        "Một mạng hallway tối hoàn toàn, lạnh, im lặng và cô lập. Trong source lore, ánh sáng mang vào có thể không hoạt động.",
        "BACKROOMsV2 nhấn mạnh danger từ bóng tối, lạnh, thiếu supplies, sleep deprivation, microsleep, hallucination và navigation failure."
      ],
      "architecture": [
        "Hallway chật bằng vật liệu nhẵn lạnh, thường được suy đoán là concrete.",
        "Vì không có ánh sáng đáng tin, topology phải được mô tả qua chạm, bước, luồng khí, âm vang hiếm và bề mặt.",
        "Không dựng landmark thị giác nếu Cao Minh không có nguồn quan sát hợp lệ."
      ],
      "zones": [
        "Blind Corridors: dạng chủ đạo, không ánh sáng.",
        "Cold Pockets: vùng nhiệt giảm sâu, có thể làm cơ thể mất nhiệt nhanh hơn.",
        "Standing-Water Sections: nếu project state/scene đưa vào, nước phải được phát hiện bằng xúc giác/âm thanh chứ không nhìn thấy.",
        "Boundary Zones: khu dấu hiệu về exit chỉ xuất hiện khi Core cho phép."
      ],
      "sensory": [
        "Bóng tối tuyệt đối hoặc gần tuyệt đối; im lặng có thể giống phòng cách âm.",
        "Bề mặt lạnh và luồng khí trở thành thông tin định hướng chính.",
        "Auditory hallucination như breathing, whisper hoặc scuttling có thể xảy ra; không xác nhận nguồn khi chưa có evidence."
      ],
      "anomalies": [
        "Light source có thể mất chức năng theo source lore; game không được tự cho night vision phá hoàn toàn tính chất Level nếu thiết bị chưa có canon tương tác.",
        "Cảm giác 'thứ gì đó nhìn thấy mình trong khi mình không thấy nó' là report/perception, không bằng chứng resident Entity."
      ],
      "hazards": [
        "Hypothermia, thiếu nước/thức ăn, sleep deprivation, microsleep, collision/fall và navigation failure.",
        "No confirmed resident Entity không đồng nghĩa an toàn.",
        "Nếu có external Entity từ EntityCore, không dùng source claim để xóa encounter đang active."
      ],
      "resources": [
        "Resources rất hiếm; không tự tạo supply cache để cứu player.",
        "Mọi item vẫn thuộc ItemCore."
      ],
      "entities": [
        "Project canon không xác nhận resident Entity trong Level 6.",
        "EntityCore roaming policy có thể tạo active encounter; nếu không active, tiếng động phải giữ uncertain."
      ],
      "navigation": [
        "Đếm bước, bám tường, đánh dấu vật lý, luồng khí và gradient nhiệt có thể hỗ trợ nhưng không bảo đảm route.",
        "Không mô tả bản đồ từ góc nhìn toàn tri; chỉ thông tin Cao Minh cảm nhận được.",
        "Route progression cần tinh tế vì visual cue gần như không tồn tại."
      ],
      "entrancesExits": [
        "Route chính vào từ Level 5.",
        "Game hiện chưa định nghĩa Level 7 trong LevelCore 0–6; không tự chuyển sang Level 7 chỉ vì source Wiki có exit đó."
      ],
      "gameplayOverride": [
        "Level 6 là node cuối trong tập Level hiện được implement.",
        "Mọi exit ngoài phạm vi Core phải bị khóa cho đến khi hệ Level được mở rộng."
      ],
      "gmConstraints": [
        "Không spawn monster để giải thích every sound.",
        "Không mô tả màu sắc/chi tiết nhìn thấy nếu không có cơ sở cảm nhận.",
        "Không cho flashlight tự hoạt động như ở Level thường nếu điều đó phá canon ánh sáng."
      ],
      "variationPool": [
        "Bức tường lạnh chuyển từ nhẵn sang có khe nối nhưng vẫn không nhìn thấy.",
        "Luồng khí mỏng gợi một junction phía trước.",
        "Một tiếng thở có vẻ gần rồi biến mất khi Cao Minh dừng lại.",
        "Nền thay đổi độ dốc rất nhẹ chỉ nhận ra qua bước chân.",
        "Nước nông được phát hiện bởi tiếng chạm trước khi chân chạm vào.",
        "Một đoạn hoàn toàn mất mọi âm vang khiến khoảng cách trở nên khó đánh giá."
      ],
      "_canon": {
        "id": "level.6.foundation",
        "owner": "world:level:6",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "BACKROOMsV2 canon + Backrooms Wiki Level 6 Lights Out",
        "revision": "runtime-current",
        "scope": [
          "level:6"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_0": {
      "name": "Hui's Family Level 1",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 1 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Văn phòng hành chính vô tận, giấy dán tường vàng úa, thảm nỉ ẩm và hồ sơ ghi lại hành động vừa xảy ra."
      ],
      "canonicalFacts": [
        "Văn phòng hành chính vô tận, giấy dán tường vàng úa, thảm nỉ ẩm và hồ sơ ghi lại hành động vừa xảy ra.",
        "Quy luật của Level: Bản đồ không đáng tin; không mở cửa đỏ khi có tiếng gõ phía sau, vì tiếng gõ có thể phát ra từ chính phía người mở."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Văn phòng hành chính vô tận, giấy dán tường vàng úa, thảm nỉ ẩm và hồ sơ ghi lại hành động vừa xảy ra."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Tìm cánh cửa đỏ đứng riêng và mở đúng lúc không có tiếng gõ.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_0.foundation",
        "owner": "world:level:hua_1900_0",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_0"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_1": {
      "name": "Hui's Family Level 2",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 2 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Hành lang khách sạn hẹp với hàng triệu cửa đỏ có số thứ tự liên tục thay đổi."
      ],
      "canonicalFacts": [
        "Hành lang khách sạn hẹp với hàng triệu cửa đỏ có số thứ tự liên tục thay đổi.",
        "Quy luật của Level: Người Gõ Cửa chỉ tồn tại khi cửa khép; tay nắm nóng chỉ an toàn trước khi bị chạm."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Hành lang khách sạn hẹp với hàng triệu cửa đỏ có số thứ tự liên tục thay đổi."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Bước qua cánh cửa không số xuất hiện giữa hai cửa mang cùng một con số.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_1.foundation",
        "owner": "world:level:hua_1900_1",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_1"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_2": {
      "name": "Hui's Family Level 3",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 3 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Nhà ga mái kính không có đường ray, bảng giờ ghi những nơi không tồn tại và một đầu máy vô hình đi xuyên đại sảnh."
      ],
      "canonicalFacts": [
        "Nhà ga mái kính không có đường ray, bảng giờ ghi những nơi không tồn tại và một đầu máy vô hình đi xuyên đại sảnh.",
        "Quy luật của Level: Không trả lời câu hỏi về thời gian bằng bất kỳ con số nào; người trả lời sẽ bị nhận diện là chuyến tàu."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Nhà ga mái kính không có đường ray, bảng giờ ghi những nơi không tồn tại và một đầu máy vô hình đi xuyên đại sảnh."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Lên toa tàu không có đầu máy và không chọn toa có hành khách nhìn ra ngoài.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_2.foundation",
        "owner": "world:level:hua_1900_2",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_2"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_3": {
      "name": "Hui's Family Level 4",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 4 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Khách sạn Belle Époque vô tận với phòng đã chuẩn bị sẵn cho người chưa từng đến."
      ],
      "canonicalFacts": [
        "Khách sạn Belle Époque vô tận với phòng đã chuẩn bị sẵn cho người chưa từng đến.",
        "Quy luật của Level: Ngủ trong phòng khóa kín sẽ tỉnh ở hành lang; ngủ giữa hành lang sẽ tỉnh trong phòng bị khóa từ ngoài."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Khách sạn Belle Époque vô tận với phòng đã chuẩn bị sẵn cho người chưa từng đến."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Đi thang máy xuống tầng hầm bằng cách nói tên một tầng không tồn tại.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_3.foundation",
        "owner": "world:level:hua_1900_3",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_3"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_4": {
      "name": "Hui's Family Level 5",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 5 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Nhà máy gạch đỏ co giãn như lồng ngực; máy móc chỉ vận hành máy móc khác mà không tạo sản phẩm."
      ],
      "canonicalFacts": [
        "Nhà máy gạch đỏ co giãn như lồng ngực; máy móc chỉ vận hành máy móc khác mà không tạo sản phẩm.",
        "Quy luật của Level: Mỗi nhịp thở ra phủ hơi nóng kín hành lang; tiếng động cho phép Thợ Máy Rỗng tạo công cụ săn mồi từ cơ thể chúng."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Nhà máy gạch đỏ co giãn như lồng ngực; máy móc chỉ vận hành máy móc khác mà không tạo sản phẩm."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Chui qua một nồi hơi đã nguội, bên trong lớn hơn toàn bộ nhà máy.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_4.foundation",
        "owner": "world:level:hua_1900_4",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_4"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_5": {
      "name": "Hui's Family Level 6",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 6 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Bệnh viện gạch trắng có giường lõm, chuông gọi từ phòng bị xây kín và bệnh án về những chứng bệnh không thể tồn tại."
      ],
      "canonicalFacts": [
        "Bệnh viện gạch trắng có giường lõm, chuông gọi từ phòng bị xây kín và bệnh án về những chứng bệnh không thể tồn tại.",
        "Quy luật của Level: Người bị thương ít bị săn hơn người khỏe; tự gây thương tích khiến bệnh viện coi người đó là nhân viên."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Bệnh viện gạch trắng có giường lõm, chuông gọi từ phòng bị xây kín và bệnh án về những chứng bệnh không thể tồn tại."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Đẩy một giường vào phòng phẫu thuật số 0 khi toàn bộ đèn cùng tắt.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_5.foundation",
        "owner": "world:level:hua_1900_5",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_5"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_6": {
      "name": "Hui's Family Level 7",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 7 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Nhà hát không có sân khấu, mọi ghế quay về một hố trống và dàn nhạc vô hình luôn chơi sai một nốt."
      ],
      "canonicalFacts": [
        "Nhà hát không có sân khấu, mọi ghế quay về một hố trống và dàn nhạc vô hình luôn chơi sai một nốt.",
        "Quy luật của Level: Mỗi nốt sai tạo thêm khán giả; gây tiếng động khiến toàn khán phòng vỗ tay và thực thể bò qua ghế."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Nhà hát không có sân khấu, mọi ghế quay về một hố trống và dàn nhạc vô hình luôn chơi sai một nốt."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Ngồi vào chiếc ghế duy nhất quay khỏi hố và chờ màn hạ từ trần.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_6.foundation",
        "owner": "world:level:hua_1900_6",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_6"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_7": {
      "name": "Hui's Family Level 8",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 8 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Kho hồ sơ vô tận nơi mực bò khỏi trang và việc đọc xóa từ, khái niệm hoặc ký ức tương ứng."
      ],
      "canonicalFacts": [
        "Kho hồ sơ vô tận nơi mực bò khỏi trang và việc đọc xóa từ, khái niệm hoặc ký ức tương ứng.",
        "Quy luật của Level: Đọc làm mất ký ức; đốt tài liệu trả chữ lại nhưng chữ xuất hiện trên tường, da hoặc bên trong mí mắt."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Kho hồ sơ vô tận nơi mực bò khỏi trang và việc đọc xóa từ, khái niệm hoặc ký ức tương ứng."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Tìm bản đồ trắng và gấp thành hình cánh cửa.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_7.foundation",
        "owner": "world:level:hua_1900_7",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_7"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_8": {
      "name": "Hui's Family Level 9",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 9 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Thành phố thương mại dưới mái kính, mưa bay ngược và biển hiệu đổi ngôn ngữ sau mỗi lần chớp mắt."
      ],
      "canonicalFacts": [
        "Thành phố thương mại dưới mái kính, mưa bay ngược và biển hiệu đổi ngôn ngữ sau mỗi lần chớp mắt.",
        "Quy luật của Level: Bóng đổ có thể tách khỏi chủ thể, mọc cơ thể và tiếp tục tồn tại độc lập."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Thành phố thương mại dưới mái kính, mưa bay ngược và biển hiệu đổi ngôn ngữ sau mỗi lần chớp mắt."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Vào ngôi trường chỉ mở khi tiếng chuông nhà thờ vang dưới lòng đất.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_8.foundation",
        "owner": "world:level:hua_1900_8",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_8"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_9": {
      "name": "Hui's Family Level 10",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 10 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Trường học nằm trong đêm bất biến; bảng đen tự viết bài học về người đang có mặt."
      ],
      "canonicalFacts": [
        "Trường học nằm trong đêm bất biến; bảng đen tự viết bài học về người đang có mặt.",
        "Quy luật của Level: Giám thị xuất hiện khi có người chạy, nói lớn hoặc mở cửa không được phép, nhưng không ai biết cửa nào được phép."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Trường học nằm trong đêm bất biến; bảng đen tự viết bài học về người đang có mặt."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Viết tên mình lên bảng rồi xóa trước khi viên phấn tự viết tên lần thứ hai.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_9.foundation",
        "owner": "world:level:hua_1900_9",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_9"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_10": {
      "name": "Hui's Family Level 11",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 11 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Tàu viễn dương bị nhét vào hành lang đá, cửa sổ nhìn ra đại dương dựng đứng và nước rơi từ trần."
      ],
      "canonicalFacts": [
        "Tàu viễn dương bị nhét vào hành lang đá, cửa sổ nhìn ra đại dương dựng đứng và nước rơi từ trần.",
        "Quy luật của Level: Không gian rộng làm thủy thủ phình lên; khe hẹp ép chúng mỏng như giấy."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Tàu viễn dương bị nhét vào hành lang đá, cửa sổ nhìn ra đại dương dựng đứng và nước rơi từ trần."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Leo xuống đáy khoang cho đến khi thang biến thành giếng gạch.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_10.foundation",
        "owner": "world:level:hua_1900_10",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_10"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_11": {
      "name": "Hui's Family Level 12",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 12 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Cống, hầm lò và đường than đan nhau trong không khí nóng đặc; xe goòng chở giày, răng người và cửa đỏ."
      ],
      "canonicalFacts": [
        "Cống, hầm lò và đường than đan nhau trong không khí nóng đặc; xe goòng chở giày, răng người và cửa đỏ.",
        "Quy luật của Level: Lửa xua phần lớn quái vật nhưng làm đường hầm dài thêm."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Cống, hầm lò và đường than đan nhau trong không khí nóng đặc; xe goòng chở giày, răng người và cửa đỏ."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Đi theo đường ống có luồng khí lạnh tới cổng triển lãm dát vàng.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_11.foundation",
        "owner": "world:level:hua_1900_11",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_11"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_12": {
      "name": "Hui's Family Level 13",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 13 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Khu triển lãm về một thế kỷ mới không bao giờ đến, đầy máy móc có hình dạng hợp lý nhưng chức năng vô nghĩa."
      ],
      "canonicalFacts": [
        "Khu triển lãm về một thế kỷ mới không bao giờ đến, đầy máy móc có hình dạng hợp lý nhưng chức năng vô nghĩa.",
        "Quy luật của Level: Người Tham Quan Bằng Sáp bất động khi nhìn trực diện nhưng chạy trong kính phản chiếu."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Khu triển lãm về một thế kỷ mới không bao giờ đến, đầy máy móc có hình dạng hợp lý nhưng chức năng vô nghĩa."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Bước vào gian trưng bày mang tên năm sinh của chính mình dù năm đó chưa tới.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_12.foundation",
        "owner": "world:level:hua_1900_12",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_12"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_13": {
      "name": "Hui's Family Level 14",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 14 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Thánh đường đá đen có đồng hồ chỉ về phía người nhìn và chuông xóa sự kiện khỏi lịch sử cá nhân."
      ],
      "canonicalFacts": [
        "Thánh đường đá đen có đồng hồ chỉ về phía người nhìn và chuông xóa sự kiện khỏi lịch sử cá nhân.",
        "Quy luật của Level: Không quan sát thì tượng đứng sau lưng; nhìn quá lâu khiến da hóa đá."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Thánh đường đá đen có đồng hồ chỉ về phía người nhìn và chuông xóa sự kiện khỏi lịch sử cá nhân."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Leo lên tháp chuông có bóng đổ hướng lên trời.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_13.foundation",
        "owner": "world:level:hua_1900_13",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_13"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_14": {
      "name": "Hui's Family Level 15",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 15 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Thành phố không có mặt đất, gồm căn hộ và ban công chồng lên nhau trong vực sâu vô tận."
      ],
      "canonicalFacts": [
        "Thành phố không có mặt đất, gồm căn hộ và ban công chồng lên nhau trong vực sâu vô tận.",
        "Quy luật của Level: Đi xuống làm thành phố sáng hơn; đi lên chỉ nghe phố xá mà không bao giờ tới mặt đất."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Thành phố không có mặt đất, gồm căn hộ và ban công chồng lên nhau trong vực sâu vô tận."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Nhảy vào cửa sổ phản chiếu một căn phòng không tồn tại phía sau.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_14.foundation",
        "owner": "world:level:hua_1900_14",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_14"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    },
    "hua_1900_15": {
      "name": "Hui's Family Level 16",
      "source": {
        "basis": "Hua-s-Family/src/campaign-canon.js",
        "revision": "e2ca8d316517e19b03f1cfdbda9f8813f6ef1de2",
        "status": "project import: environment only"
      },
      "identity": [
        "Hui's Family Level 16 thuộc chuỗi 16 Level Hứa Gia giữa Backrooms Level 0 và Level 1.",
        "Mọi tầng trước bị ép chung vào một kiến trúc; ký ức sai trở thành hành lang và người quen trở thành thực thể."
      ],
      "canonicalFacts": [
        "Mọi tầng trước bị ép chung vào một kiến trúc; ký ức sai trở thành hành lang và người quen trở thành thực thể.",
        "Quy luật của Level: Tầng dùng ký ức người sống để xây thêm Backrooms; một bản sao thế giới thật có thể khiến cả nhóm tin rằng đã thoát."
      ],
      "navigation": [
        "Không gian và dấu hiệu định hướng: Mọi tầng trước bị ép chung vào một kiến trúc; ký ức sai trở thành hành lang và người quen trở thành thực thể."
      ],
      "entrancesExits": [
        "Lối ra theo lore nguồn: Tìm văn phòng vàng và quyết định có ký tờ giấy: Bạn chưa từng rời khỏi đây.",
        "Chỉ chuyển sang Level kế tiếp khi LevelCore xác nhận exitAvailable và người chơi chọn đi qua."
      ],
      "gmConstraints": [
        "Lore quy luật có thể nhắc đến thực thể; không tự tạo threats hoặc Boss từ mô tả. Chỉ EntityCore được kích hoạt encounter.",
        "Không tự mở lối ra hoặc bỏ qua Level; LevelCore quyết định chuyển tầng."
      ],
      "_canon": {
        "id": "level.hua_1900_15.foundation",
        "owner": "world:level:hua_1900_15",
        "type": "FOUNDATION",
        "status": "CURRENT",
        "sourceRef": "Hua-s-Family/src/campaign-canon.js",
        "revision": "runtime-current",
        "scope": [
          "level:hua_1900_15"
        ],
        "knownBy": "SCENE",
        "sourceKind": "external",
        "sourceAvailability": "EXTERNAL_SNAPSHOT",
        "refs": [],
        "requires": [],
        "core": true
      }
    }
  }
}
```
