package com.antcolonywars.server;

import java.util.Map;

final class ErrorMessages {
    private static final Map<String, String> MESSAGES = Map.ofEntries(
            Map.entry("MATCH_FINISHED", "Trận đấu đã kết thúc."),
            Map.entry("UNIT_NOT_FOUND", "Không tìm thấy quân được chọn."),
            Map.entry("NOT_OWNER", "Quân không thuộc đội của bạn."),
            Map.entry("DUPLICATE_COMMAND", "Lệnh đã được gửi trước đó."),
            Map.entry("OUT_OF_BOUNDS", "Vị trí nằm ngoài bản đồ."),
            Map.entry("CELL_OCCUPIED", "Ô đích đang bị chiếm."),
            Map.entry("TARGET_NOT_FOUND", "Không tìm thấy mục tiêu."),
            Map.entry("FRIENDLY_TARGET", "Không thể tấn công mục tiêu cùng đội."),
            Map.entry("TARGET_DESTROYED", "Mục tiêu đã bị phá hủy."),
            Map.entry("OUT_OF_RANGE", "Mục tiêu nằm ngoài tầm."),
            Map.entry("HEADQUARTERS_NOT_FOUND", "Không tìm thấy sở chỉ huy."),
            Map.entry("ENTITY_ID_TAKEN", "ID đã được sử dụng."),
            Map.entry("INVALID_DISPLAY_NAME", "Tên hiển thị không hợp lệ."),
            Map.entry("INVALID_UNIT_TYPE", "Loại quân không hợp lệ."),
            Map.entry("PRODUCTION_SOURCE_NOT_FOUND", "Không tìm thấy công trình sản xuất."),
            Map.entry("INVALID_PRODUCTION_SOURCE", "Công trình không thể sản xuất loại quân này."),
            Map.entry("UNIT_LIMIT_REACHED", "Đội đã đạt giới hạn quân."),
            Map.entry("RESOURCE_NOT_FOUND", "Không tìm thấy tài nguyên."),
            Map.entry("RESOURCE_DEPLETED", "Tài nguyên đã cạn."),
            Map.entry("HARVEST_OUT_OF_RANGE", "Tài nguyên nằm ngoài tầm khai thác."),
            Map.entry("INSUFFICIENT_RESOURCES", "Đội không đủ tài nguyên."),
            Map.entry("BUILDING_ID_TAKEN", "ID công trình đã được sử dụng."),
            Map.entry("INVALID_BUILD_POSITION", "Vị trí xây dựng không hợp lệ."),
            Map.entry("BUILDING_TYPE_UNSUPPORTED", "Loại công trình không được hỗ trợ."),
            Map.entry("BATCH_LIMIT_EXCEEDED", "Một bot chỉ được gửi tối đa 10 hành động trong mỗi batch."),
            Map.entry("COMMAND_QUEUE_FULL", "Hàng đợi lệnh đang đầy."),
            Map.entry("INVALID_COMMAND", "Lệnh không hợp lệ."),
            Map.entry("INVALID_BATCH", "Batch không hợp lệ."),
            Map.entry("INVALID_JSON", "Dữ liệu gửi lên không phải JSON hợp lệ."),
            Map.entry("UNSUPPORTED_SCHEMA_VERSION", "Phiên bản giao thức không được hỗ trợ."),
            Map.entry("MATCH_NOT_FOUND", "Không tìm thấy trận đấu."),
            Map.entry("INVALID_MESSAGE_TYPE", "Loại thông điệp không hợp lệ."),
            Map.entry("UNAUTHORIZED", "Token đội không hợp lệ.")
    );

    private ErrorMessages() {
    }

    static String vietnamese(String code) {
        return MESSAGES.getOrDefault(code, "Lệnh không thể thực hiện.");
    }
}
