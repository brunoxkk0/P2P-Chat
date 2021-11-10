package br.com.brunoxkk0.client;

import br.com.brunoxkk0.common.KeyShareStatus;
import br.com.brunoxkk0.common.SecurityUtils;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

public class Client extends Thread{

    private final String userName;
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private boolean isIntroduced;

    private final BufferedReader input;

    private final KeyPair keyPair;

    private SecretKey AESKey;

    public Client(BufferedReader input, String userName, String host, int ip) throws Exception {

        this.userName = userName;
        this.socket = new Socket(host, ip);
        this.input = input;

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.keyPair = SecurityUtils.genKeyPair();

    }


    public String getUserName() {
        return userName;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {

        KeyShareStatus keyShareStatus = KeyShareStatus.SEND_CLIENT_RSA;

        try {

            while (socket.isConnected() && !socket.isInputShutdown()){

                if(!isIntroduced && keyShareStatus == KeyShareStatus.FINISH){
                    writer.write(encode(getUserName()) + "\r\n");
                    writer.flush();
                    isIntroduced = true;
                    continue;
                }

                if(keyShareStatus == KeyShareStatus.SEND_CLIENT_RSA){

                    writer.write(SecurityUtils.asBase64ToString(keyPair.getPublic().getEncoded()) + "\r\n");
                    writer.flush();

                    keyShareStatus = KeyShareStatus.RECEIVE_SERVER_AES;
                    continue;
                }


                String message;

                if(reader.ready()){

                    String socket_message = reader.readLine();

                    if(keyShareStatus == KeyShareStatus.RECEIVE_SERVER_AES){

                        byte[] encrypted = SecurityUtils.fromBase64(socket_message.getBytes());

                        encrypted = SecurityUtils.decrypt(keyPair.getPrivate(), encrypted);

                        AESKey = SecurityUtils.aesKeyFromBase64(SecurityUtils.asBase64ToString(encrypted));

                        keyShareStatus = KeyShareStatus.FINISH;
                        continue;
                    }

                    System.out.println(SecurityUtils.decryptAES(AESKey, socket_message.getBytes()));

                }

                if(input.ready()){
                    message = input.readLine();

                    if(message != null){
                        writer.write(encode(message) + "\r\n");
                        writer.flush();
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String encode(String message) throws Exception {
        return SecurityUtils.encryptAES(AESKey, message.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {

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
