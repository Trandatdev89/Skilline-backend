package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.reponse.InitUploadResponse;
import com.project01.skillineserver.dto.reponse.MediaAssetResponse;
import com.project01.skillineserver.dto.request.ConfirmUploadRequest;
import com.project01.skillineserver.dto.request.InitUploadRequest;

public interface MediaService {

    //upload file
    InitUploadResponse initUploadFile(InitUploadRequest initUploadRequest);

    MediaAssetResponse confirmUpload(ConfirmUploadRequest confirmUploadRequest);

    MediaAssetResponse getAssetById(String assetId);

}
