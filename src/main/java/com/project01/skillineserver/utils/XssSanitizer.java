package com.project01.skillineserver.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * Lam sach du lieu nguoi dung truoc khi luu xuong CSDL (phong chong Stored XSS).
 * Duoc khai bao la sanitizer trong rule semgrep-rules/springboot-xss.yaml.
 */
public final class XssSanitizer {

    private static final Document.OutputSettings OUTPUT_SETTINGS =
            new Document.OutputSettings().prettyPrint(false);

    private XssSanitizer() {
    }

    /** Loai bo toan bo the HTML - dung cho comment, title, name, address, description. */
    public static String cleanText(String input) {
        return input == null ? null : Jsoup.clean(input, "", Safelist.none(), OUTPUT_SETTINGS);
    }

    /** Giu the dinh dang an toan (table, img, a...) nhung loai bo script/thuoc tinh su kien - dung cho template email. */
    public static String cleanRichHtml(String input) {
        return input == null ? null : Jsoup.clean(input, "", Safelist.relaxed(), OUTPUT_SETTINGS);
    }
}
