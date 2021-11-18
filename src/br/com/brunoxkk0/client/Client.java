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


public class Client extends Thread{

    /**
     * Nome do cliente.
     */
    private final String userName;

    /**
     * Socket da conexão.
     */
    private final Socket socket;

    /**
     * Buffer de leitura do servidor para o cliente.
     */
    private final BufferedReader reader;

    /**
     * Buffer de escrita do cliente para o servidor.
     */
    private final BufferedWriter writer;

    /**
     * Controla se o cliente ja foi apresentado, um cliente é considerado apresentado,
     * se, o seu nome ja foi enviado ao servidor.
     */
    private boolean isIntroduced;

    /**
     * Buffer de entrada do sistema.
     */
    private final BufferedReader input;

    /**
     * Chave AES.
     */
    private SecretKey AESKey;

    /**
     * Cliente usado para se comunicar ao servidor através dos parâmetros informados,
     * quando criado, inicializa o {@link #reader} e {@link #writer} utilizando o
     * charset {@link StandardCharsets#UTF_8}, também, gera um par de chaves RSA.
     *
     * @param input entrada de dados do sistema.
     * @param userName nome do cliente.
     * @param host endereço do servidor.
     * @param port porta do servidor.
     *
     * @throws Exception caso algum erro ocorra na criação.
     */
    public Client(BufferedReader input, String userName, String host, int port) throws Exception {

        this.userName = userName;
        this.socket = new Socket(host, port);
        this.input = input;

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

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


    @Override
    public void run() {

        KeyShareStatus keyShareStatus = KeyShareStatus.RECEIVE_SERVER_AES;

        try {

            while (socket.isConnected() && !socket.isInputShutdown()){

                if(!isIntroduced && keyShareStatus == KeyShareStatus.FINISH){
                    writer.write(encrypt(getUserName()) + "\r\n");
                    writer.flush();
                    isIntroduced = true;
                    continue;
                }


                String message;

                if(reader.ready()){

                    String socket_message = reader.readLine();

                    if(keyShareStatus == KeyShareStatus.RECEIVE_SERVER_AES){

                        byte[] encrypted = SecurityUtils.fromBase64(socket_message.getBytes());

                        AESKey = SecurityUtils.aesKeyFromBase64(SecurityUtils.asBase64ToString(encrypted));

                        keyShareStatus = KeyShareStatus.FINISH;
                        continue;
                    }

                    System.out.println(SecurityUtils.decryptAES(AESKey, socket_message.getBytes()));

                }

                if(input.ready()){
                    message = input.readLine();

                    if(message != null){
                        writer.write(encrypt(message) + "\r\n");
                        writer.flush();
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
