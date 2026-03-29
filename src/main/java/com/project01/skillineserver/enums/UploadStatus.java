package com.project01.skillineserver.enums;

public enum UploadStatus {
    PENDING,     // đã tạo presigned URL, chờ frontend upload
    UPLOADED,    // frontend đã upload xong lên S3
    FAILED       // upload thất bại
}