package br.com.brunoxkk0.server;

import br.com.brunoxkk0.common.*;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public class Connection extends Thread {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final OnRead onRead;
    private final OnJoin onJoin;
    private final OnQuit onQuit;

    private String userName;

    private final SecretKey AESKey;

    private PublicKey clientPublicKey;

    public Connection(Socket socket, OnRead onRead, OnJoin onJoin, OnQuit onQuit) throws Exception {

        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.onRead = onRead;
        this.onJoin = onJoin;
        this.onQuit = onQuit;

        this.AESKey = SecurityUtils.genAESKey();
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUserName() {
        return userName;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    @Override
    public void run() {

        KeyShareStatus keyShareStatus = KeyShareStatus.RECEIVE_CLIENT_RSA;

        while(isAlive() && getSocket().isConnected() && !getSocket().isClosed()){

            try {

                if(keyShareStatus == KeyShareStatus.SEND_SERVER_AES){

                    byte[] encrypted = SecurityUtils.encrypt(clientPublicKey, AESKey.getEncoded());

                    writer.write(SecurityUtils.asBase64ToString(encrypted) + "\r\n");
                    writer.flush();

                    keyShareStatus = KeyShareStatus.FINISH;
                    continue;
                }

                if(reader.ready()){

                    String message = reader.readLine();

                    if(message != null){

                        if(keyShareStatus == KeyShareStatus.RECEIVE_CLIENT_RSA){

                            clientPublicKey = SecurityUtils.publicKeyFromString(SecurityUtils.fromBase64(message));

                            keyShareStatus = KeyShareStatus.SEND_SERVER_AES;
                            continue;
                        }

                        message = SecurityUtils.decryptAES(AESKey, message.getBytes(StandardCharsets.UTF_8));

                        if(userName != null){
                            onRead.onRead(this, message);
                        } else {
                            userName = message;
                            onJoin.onJoin(this);
                        }
                    }

                }
            } catch (Exception e) {
                onQuit.onQuit(this);
                this.interrupt();
                break;
            }
        }

        onQuit.onQuit(this);
    }

    public String encode(String message) throws Exception {
        return SecurityUtils.encryptAES(AESKey, message.getBytes(StandardCharsets.UTF_8));
    }

}
