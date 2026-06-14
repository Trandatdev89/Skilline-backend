package com.project01.skillineserver.repository;

import com.project01.skillineserver.entity.EmailTemplate;
import com.project01.skillineserver.enums.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateMailRepository extends JpaRepository<EmailTemplate,Long> {
    Optional<EmailTemplate> findByType(EmailType type);

    Optional<EmailTemplate> findEmailTemplateByTypeAndActive(EmailType type, boolean isActive);
}
