package com.project01.skillineserver.service;

import com.project01.skillineserver.dto.request.TemplateMailReq;
import com.project01.skillineserver.entity.EmailTemplate;
import com.project01.skillineserver.enums.EmailType;

public interface TemplateMailService {
    void saveTemplateMail(TemplateMailReq templateMailReq);

    EmailTemplate getTemplateMail(EmailType emailType);
}
