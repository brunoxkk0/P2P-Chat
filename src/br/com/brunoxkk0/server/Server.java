package br.com.brunoxkk0.server;

import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class Server extends Thread {

    /**
     * Logger default do servidor.
     */
    private final Logger logger = Logger.getLogger("Server");

    /**
     * Porta qual será aberto o servidor.
     */
    private final int port;

    /**
     * Host onde será aberto o servidor.
     */
    private final String host;

    /**
     * Socket do servidor.
     */
    private final ServerSocket serverSocket;

    /**
     * Lista para manter as conexões recebidas do servidor.
     */
    private final ArrayList<Connection> connections = new ArrayList<>();


    /**
     * Logger do servidor.
     *
     * @return logger do servidor.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Socket do servidor.
     *
     * @return socket do servidor.
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Porta do servidor.
     *
     * @return porta do servidor.
     */
    public int getPort() {
        return port;
    }

    /**
     * Host do servidor.
     *
     * @return host do servidor.
     */
    public String getHost() {
        return host;
    }

    /**
     * Lista com as conexões recebidas pelo servidor.
     *
     * @return lista com todas as conexões.
     */
    public ArrayList<Connection> getConnections() {
        return connections;
    }

    /**
     * Servidor que vai ser usado para realizar a comunicação entre os clientes,
     * ao ser criado realiza o bind() e fica disponível para receber conexões.
     *
     * @param port porta do servidor.
     * @param host host do servidor.
     *
     * @throws Exception caso ocorra algum erro ao dar bind no servidor.
     */
    public Server(int port, String host) throws Exception {

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
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
    }

    /**
     * Quando um novo cliente se conecta, esta função e chamada, e dispara para todos os outros clientes
     * inclusive para ele mesmo que um novo cliente se conectou.
     *
     * @param connection conexão do novo cliente.
     */
    private void onJoin(Connection connection){
        sendToAll(connection, String.format("(%s entrou...)", connection.getUserName()), true);
    }

    /**
     * Quando um novo cliente se desconecta, esta função e chamada, e dispara para todos os outros clientes
     * que o cliente se desconectou.
     *
     * @param connection conexão do novo cliente.
     */
    private void onQuit(Connection connection){
        sendToAll(connection, String.format("(%s saiu...)", connection.getUserName()));
    }

    /**
     * Quando um cliente manda uma mensagem, esta função é chama, pegando a mensagem e disparando para todos os clientes.
     *
     * @param connection conexão do novo cliente.
     * @param message mensagem a ser disparada a todos os clientes.
     */
    private void onRead(Connection connection, String message){
        logger.info(connection.getUserName() + " send a Message...");
        sendToAll(connection, String.format("[%s] -> %s", connection.getUserName(), message));
    }

    /**
     * Dispara a mensagem para todos os clientes conectados, ignorando o cliente de origem.
     *
     * @param connection conexão de origem.
     * @param message mensagem.
     */
    private void sendToAll(Connection connection, String message){
        sendToAll(connection, message, false);
    }

    /**
     * Dispara a mensagem recebida para todos os clientes conectados, como cada conexão tem sua chave AES
     * a função chama a função de encrypt e criptógrafa a mensagem para cada cliente de maneira única.
     *
     * @param connection conexão de origem.
     * @param message mensagem a ser dispara a todos.
     * @param self quando marcado com true, envia a mensagem para a conexão de origem junto aos outros clientes.
     */
    private void sendToAll(Connection connection, String message, boolean self){
        Iterator<Connection> connectionIterator = connections.iterator();

        while (connectionIterator.hasNext()){
            Connection con = connectionIterator.next();

            if(con.equals(connection) && !self)
                continue;

            BufferedWriter bufferedWriter = con.getWriter();

            if(bufferedWriter != null){
                try {
                    bufferedWriter.write(con.encrypt(message) + "\r\n");
                    bufferedWriter.flush();
                    logger.info("message from " + connection.getUserName() + " send to " + con.getUserName());
                } catch (Exception e) {
                    connectionIterator.remove();
                    onQuit(con);
                    logger.warning(e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Server server = new Server(1234, "127.0.0.1");
        server.setDaemon(false);
        server.start();

    }
}
