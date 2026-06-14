package com.project01.skillineserver.enums;

import lombok.Getter;

@Getter
public enum ProcessStatus {
    PENDING,         // chờ xử lý (transcode HLS)
    PROCESSING,      // đang transcode
    COMPLETED,       // transcode xong, sẵn sàng stream
    FAILED           // transcode thất bại
}
