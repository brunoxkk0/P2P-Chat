package br.dev.brunoxkk0.p2p.common;

import br.dev.brunoxkk0.p2p.server.Connection;

@FunctionalInterface
public interface OnRead {

    /**
     * Função a ser executada quando uma conexão faz a leitura de dados.
     * @param connection cliente que foi lido.
     * @param message mensagem que foi lida.
     */
    void onRead(Connection connection, String message);

}
