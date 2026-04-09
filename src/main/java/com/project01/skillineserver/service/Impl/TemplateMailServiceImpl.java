package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.request.TemplateMailReq;
import com.project01.skillineserver.entity.EmailTemplate;
import com.project01.skillineserver.enums.EmailType;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.repository.TemplateMailRepository;
import com.project01.skillineserver.service.TemplateMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateMailServiceImpl implements TemplateMailService {

    private final TemplateMailRepository templateMailRepository;

    @Override
    public void saveTemplateMail(TemplateMailReq templateMailReq) {

        EmailTemplate emailTemplate = templateMailRepository
                .findByType(templateMailReq.getType())
                .orElseGet(EmailTemplate::new);

        emailTemplate.setType(templateMailReq.getType());
        emailTemplate.setHtmlContent(templateMailReq.getHtmlContent());
        emailTemplate.setSubject(templateMailReq.getSubject());
        emailTemplate.setActive(templateMailReq.isActive());
        emailTemplate.setLanguage(templateMailReq.getLanguage());

        templateMailRepository.save(emailTemplate);
    }

    @Override
    public EmailTemplate getTemplateMail(EmailType emailType) {
        return templateMailRepository
                .findEmailTemplateByTypeAndActive(emailType, true)
                .orElseThrow(() -> new AppException(ErrorCode.MAIL_CONFIG_NOT_FOUND));
    }
}
