# Campaign authority

<!-- canon-item
id: campaign.live_state.authority
owner: core:state
type: CAMPAIGN
status: DYNAMIC
sourceRef: android-apk/app/src/main/java/com/rabpit/backroom/core/GameCoreFacade.java
revision: runtime
scope: ["campaign"]
knownBy: SYSTEM
sourceKind: repo
sourceAvailability: AVAILABLE
refs: []
requires: []
core: false
-->
Campaign canon là trạng thái và kết quả mà Core đã commit trong save hiện hành. Canon nền không được reset vị trí, quan hệ, thương tích, inventory, tri thức đã học, lời hứa hoặc hậu quả đã commit. Build canon không sinh save mặc định mới và không áp baseline lên state động.
