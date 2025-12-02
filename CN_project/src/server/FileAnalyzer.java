package server;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileAnalyzer {

    private static final List<String> MALICIOUS_EXT =
            Arrays.asList(".exe", ".bat", ".scr", ".js", ".dll");

    private static final List<String> MALICIOUS_KEYWORDS =
            Arrays.asList("encrypt", "ransom", "decryptor", "payload");

    public static boolean analyze(File file) {
        System.out.println("[분석 시작] " + file.getName());

        if (isZipFile(file)) {
            return analyzeZip(file);
        }

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
            } catch (Exception e) {
                System.err.println("[인코딩 경고] UTF-8 실패 → MS949 재시도");
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


    private static boolean isZipFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] signature = new byte[2];
            if (fis.read(signature) != 2) return false;

            return signature[0] == 0x50 && signature[1] == 0x4B; // 'P''K'
        } catch (IOException e) {
            return false;
        }
    }
    private static boolean analyzeZip(File zipFile) {
        System.out.println("[ZIP 분석 시작] " + zipFile.getName());

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                if (entry.isDirectory()) continue;

                File tempFile = File.createTempFile("zip_", "_" + entry.getName());
                tempFile.deleteOnExit();

                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                System.out.println(" └▶ ZIP 내부 파일 분석: " + entry.getName());

                if (analyze(tempFile)) {
                    System.out.println("[🚨 ZIP 내부 악성 파일 탐지]");
                    return true;
                }
            }

        } catch (IOException e) {
            System.err.println("[ZIP 분석 실패] " + e.getMessage());
        }

        System.out.println("[✅ ZIP 분석 완료] 이상 없음");
        return false;
    }
}
