package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("[SecureFileGuard] 서버 시작 중...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[대기 중] 포트 " + PORT + "에서 클라이언트 접속을 기다립니다...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[연결됨] 클라이언트: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("[서버 오류] " + e.getMessage());
        }
    }
}
