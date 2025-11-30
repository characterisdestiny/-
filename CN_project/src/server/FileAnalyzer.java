package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FileAnalyzer {


    private static final List<String> MALICIOUS_EXT =
            Arrays.asList(".exe", ".bat", ".scr", ".js", ".dll");


    private static final List<String> MALICIOUS_KEYWORDS =
            Arrays.asList("encrypt", "ransom", "decryptor", "payload");

    public static boolean analyze(File file) {
        System.out.println("[분석 시작] " + file.getName());


        if (file.length() == 0) {
            System.out.println("[⚠ 경고] 파일이 비어 있음: " + file.getName());
            return false;
        }


        String fileName = file.getName().toLowerCase();
        for (String ext : MALICIOUS_EXT) {
            if (fileName.endsWith(ext)) {
                System.out.println("[탐지] 위험 확장자 발견: " + ext);
                return true;
            }
        }


        try {
            byte[] rawBytes = Files.readAllBytes(file.toPath());
            String content;

            try {

                content = new String(rawBytes, "UTF-8");
            } catch (Exception utfEx) {

                System.err.println("[인코딩 경고] UTF-8 디코딩 실패 → CP949로 재시도");
                content = new String(rawBytes, "MS949");
            }

            String lowerContent = content.toLowerCase();
            for (String keyword : MALICIOUS_KEYWORDS) {
                if (lowerContent.contains(keyword)) {
                    System.out.println("[탐지] 악성 키워드 포함됨: " + keyword);
                    return true;
                }
            }

        } catch (IOException e) {
            System.err.println("[파일 분석 실패] " + e.getMessage());
        }

        System.out.println("[✅ 분석 완료] 이상 없음");
        return false;
    }
}
