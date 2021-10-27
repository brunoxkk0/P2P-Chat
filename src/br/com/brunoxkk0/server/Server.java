package br.com.brunoxkk0.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
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
        getConnections().forEach(con -> {
            try {
                BufferedWriter writer = con.getWriter();
                writer.write(String.format("(%s entrou...)", connection.getUserName()) + "\n");
                writer.flush();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        });
    }

    private void onQuit(Connection connection){
        getConnections().forEach(con -> {
            try {
                BufferedWriter writer = con.getWriter();
                writer.write(String.format("(%s saiu...)", connection.getUserName()) + "\n");
                writer.flush();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        });
    }

    private void onRead(Connection connection, String message){

        logger.info(connection.getUserName() + " send a Message...");

        getConnections().stream().filter(con -> !con.equals(connection)).forEach(con -> {
            try {
                BufferedWriter writer = con.getWriter();
                writer.write(String.format("[%s] -> %s", connection.getUserName(), message) + "\r\n");
                writer.flush();
                System.out.println("message from " + connection.getUserName() + " send to " + con.getUserName());
            } catch (IOException e) {
                onQuit(con);
                logger.warning(e.getMessage());
            }
        });

        getConnections().removeIf(con -> con.isInterrupted() || con.getSocket().isClosed() || !con.getSocket().isConnected());

    }

    public static void main(String[] args) throws IOException {

        Server server = new Server(1234, "127.0.0.1");
        server.setDaemon(false);
        server.start();

    }
}
