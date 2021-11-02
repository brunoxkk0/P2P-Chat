package br.com.brunoxkk0.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class Server extends Thread {

    private final Logger logger = Logger.getLogger("Server");

    private final int port;
    private final String host;

    private final ServerSocket serverSocket;
    private final ArrayList<Connection> connections = new ArrayList<>();

    public Logger getLogger() {
        return logger;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public Server(int port, String host) throws IOException {
        this.port = port;
        this.host = (host != null) ? host : "127.0.0.1";
        this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
    }

    @Override
    public void run() {

        logger.info("Server running on: " + getHost() + ":" + getPort());

        while (serverSocket.isBound() && !isInterrupted()){

            try {

                Connection connection = new Connection(serverSocket.accept(), this::onRead, this::onJoin, this::onQuit);
                connections.add(connection);
                logger.info(connection.getSocket().getInetAddress() + " connected... (now we have " + connections.size() + " connections)");

                connection.setDaemon(false);
                connection.start();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    private void onJoin(Connection connection){
        sendToAll(connection, String.format("(%s entrou...)", connection.getUserName()), true);
    }

    private void onQuit(Connection connection){
        sendToAll(connection, String.format("(%s saiu...)", connection.getUserName()));
    }

    private void onRead(Connection connection, String message){
        logger.info(connection.getUserName() + " send a Message...");
        sendToAll(connection, String.format("[%s] -> %s", connection.getUserName(), message));
    }

    private void sendToAll(Connection connection, String message){
        sendToAll(connection, message, false);
    }

    private void sendToAll(Connection connection, String message, boolean self){
        Iterator<Connection> connectionIterator = connections.iterator();

        while (connectionIterator.hasNext()){
            Connection con = connectionIterator.next();

            if(con.equals(connection) && !self)
                continue;

            BufferedWriter bufferedWriter = con.getWriter();

            if(bufferedWriter != null){
                try {
                    bufferedWriter.write(message + "\r\n");
                    bufferedWriter.flush();
                    logger.info("message from " + connection.getUserName() + " send to " + con.getUserName());
                } catch (IOException e) {
                    connectionIterator.remove();
                    onQuit(con);
                    logger.warning(e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        Server server = new Server(1234, "127.0.0.1");
        server.setDaemon(false);
        server.start();

    }
}
