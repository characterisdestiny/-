package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1"; // 서버 IP 주소
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[SecureFileGuard 클라이언트]");
        System.out.print("전송할 파일 경로를 입력하세요: ");
        String filePath = scanner.nextLine();

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println(" 파일이 존재하지 않거나 잘못된 경로입니다.");
            return;
        }

        try (
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                FileInputStream fis = new FileInputStream(file)
        ) {
            System.out.println("서버에 연결 중...");


            dos.writeUTF(file.getName());
            dos.writeLong(file.length());


            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }


            String response = dis.readUTF();
            System.out.println("서버 응답: " + response);

        } catch (IOException e) {
            System.err.println("파일 전송 중 오류: " + e.getMessage());
        }
    }
}
