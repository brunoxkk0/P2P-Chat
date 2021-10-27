package br.com.brunoxkk0.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client extends Thread{

    private final String userName;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private boolean isIntroduced;

    private final BufferedReader input;

    public Client(BufferedReader input, String userName, String host, int ip) throws IOException {
        this.userName = userName;
        this.socket = new Socket(host, ip);
        this.input = input;

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }


    public String getUserName() {
        return userName;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {

        try {

            if(!isIntroduced){
                writer.write(getUserName() + "\r\n");
                writer.flush();
                isIntroduced = true;
            }

            while (socket.isConnected() && !socket.isInputShutdown()){
                String message;

                if(reader.ready()){
                    System.out.println(reader.readLine());
                }

                if(input.ready()){
                    message = input.readLine();

                    if(message != null){
                        writer.write(message + "\r\n");
                        writer.flush();
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {

        BufferedReader systemInput = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        System.out.println("Digite a host do servidor: (Obs: caso fique em branco sera usado 127.0.0.1)");
        String host = systemInput.readLine();

        if(host == null || host.isEmpty()){
            host = "127.0.0.1";
        }

        System.out.println("Digite a porta do servidor: ");
        String port = systemInput.readLine();

        if(port == null || port.isEmpty()){
            throw new IllegalArgumentException("Port cannot be null");
        }


        System.out.println("Digite seu nome de usuário: (Obs: caso fique em branco sera usado anon)");
        String userName = systemInput.readLine();

        if(userName == null || userName.isEmpty()){
            userName = "Anon - " + System.currentTimeMillis();
        }


        Client client = new Client(systemInput, userName, host, Integer.parseInt(port));
        client.setDaemon(false);
        client.start();

    }

}
