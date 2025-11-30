package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final static String SAVE_DIR = "uploads/";

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        new File(SAVE_DIR).mkdirs(); // 폴더 없으면 생성
    }

    @Override
    public void run() {
        try (
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            File file = new File(SAVE_DIR + fileName);
            System.out.println("[수신 대기] 파일명: " + fileName + ", 크기: " + fileSize + " bytes");


            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                long totalRead = 0;
                while (totalRead < fileSize && (read = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    totalRead += read;
                }
            }

            System.out.println("[수신 완료] " + file.getAbsolutePath());


            if (file.length() == 0) {
                String message = "전송된 파일이 비어 있어 분석할 수 없습니다.";
                dos.writeUTF(message);
                Logger.log("빈 파일 차단: " + file.getName() + " from " + clientSocket.getInetAddress());
                file.delete();
                return;
            }


            System.out.println("[보안 분석 시작] " + file.getName());
            boolean isMalicious = FileAnalyzer.analyze(file);

            if (isMalicious) {
                dos.writeUTF("의심 파일 탐지됨. 전송이 차단되었습니다.");
                Logger.log("의심 파일 차단: " + file.getName() + " from " + clientSocket.getInetAddress());
                file.delete();
            } else {
                dos.writeUTF("파일 정상 수신 완료");
                Logger.log("파일 저장 완료: " + file.getName());
            }

        } catch (IOException e) {
            System.err.println("[오류] 클라이언트 처리 실패: " + e.getMessage());
        }
    }
}
