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

    /**
     * Socket da conexão.
     */
    private final Socket socket;

    /**
     * Buffer de leitura do cliente para o servidor.
     */
    private final BufferedReader reader;

    /**
     * Buffer de escrita do servidor para o cliente.
     */
    private final BufferedWriter writer;

    /**
     * Interface funcional com a função a ser executada ao realizar uma leitura.
     */
    private final OnRead onRead;

    /**
     * Interface funcional com a função a ser executada ao quando um cliente se conecta.
     */
    private final OnJoin onJoin;

    /**
     * Interface funcional com a função a ser executada ao quando um cliente se desconecta.
     */
    private final OnQuit onQuit;

    /**
     * Nome do cliente.
     */
    private String userName;

    /**
     * Chave AES do servidor.
     */
    private final SecretKey AESKey;

    /**
     * Cliente "server-side" usado para lidar com a conexão do socket do lado do servidor,
     * como implementado no {@link br.com.brunoxkk0.client.Client}, ao criar a conexão
     * inicializa o {@link #reader} e {@link #writer} utilizando o
     * charset {@link StandardCharsets#UTF_8}, também, gera a chave AES a ser usada.
     *
     * @param socket socket conectado.
     * @param onRead interface funcional a ser chamada quando realizado uma leitura.
     * @param onJoin interface funcional a ser chamado quando um cliente se conecta.
     * @param onQuit interface funcional a ser chamada quando um cliente se desconecta.
     *
     * @throws Exception caso ocorra algum erro ao criar o objeto de conexão.
     */
    public Connection(Socket socket, OnRead onRead, OnJoin onJoin, OnQuit onQuit) throws Exception {

        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        this.onRead = onRead;
        this.onJoin = onJoin;
        this.onQuit = onQuit;

        this.AESKey = SecurityUtils.genAESKey();
    }

    /**
     * Retorna o nome do cliente.
     *
     * @return nome do cliente
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Retorna o socket do cliente.
     *
     * @return socket do cliente
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Retorna o buffer de escrita do socket conectado.
     *
     * @return buffer de escrita.
     */
    public BufferedWriter getWriter() {
        return writer;
    }

    @Override
    public void run() {

        KeyShareStatus keyShareStatus = KeyShareStatus.SEND_SERVER_AES;

        while(isAlive() && getSocket().isConnected() && !getSocket().isClosed()){

            try {

                if(keyShareStatus == KeyShareStatus.SEND_SERVER_AES){

                    byte[] encrypted = AESKey.getEncoded();

                    writer.write(SecurityUtils.asBase64ToString(encrypted) + "\r\n");
                    writer.flush();

                    keyShareStatus = KeyShareStatus.FINISH;
                    continue;
                }

                if(reader.ready()){

                    String message = reader.readLine();

                    if(message != null){

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

    /**
     * Criptografa a mensagem informada utilizando a chave AES, e codifica em Base64.
     *
     * @param message a ser criptografada
     *
     * @return mensagem criptografada e codificada em Base64
     *
     * @throws Exception caso ocorra algum problema ao executar a função.
     */
    public String encrypt(String message) throws Exception {
        return SecurityUtils.encryptAES(AESKey, message.getBytes(StandardCharsets.UTF_8));
    }

}
