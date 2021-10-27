package br.com.brunoxkk0.server;

import br.com.brunoxkk0.common.OnJoin;
import br.com.brunoxkk0.common.OnQuit;
import br.com.brunoxkk0.common.OnRead;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection extends Thread {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    private final OnRead onRead;
    private final OnJoin onJoin;
    private final OnQuit onQuit;

    private String userName;

    public Connection(Socket socket, OnRead onRead, OnJoin onJoin, OnQuit onQuit) throws IOException {

        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.onRead = onRead;
        this.onJoin = onJoin;
        this.onQuit = onQuit;

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

        while(isAlive() && getSocket().isConnected() && !getSocket().isClosed()){

            try {

                if(reader.ready()){

                    String message = reader.readLine();

                    if(message != null){
                        if(userName != null){
                            onRead.onRead(this, message);
                        } else {
                            userName = message;
                            onJoin.onJoin(this);
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        onQuit.onQuit(this);
    }
}
