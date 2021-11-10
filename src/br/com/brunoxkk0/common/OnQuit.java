package br.com.brunoxkk0.common;

import br.com.brunoxkk0.server.Connection;

@FunctionalInterface
public interface OnQuit {

    /**
     * Função a ser executada quando um novo cliente se desconecta.
     * @param connection cliente desconectado.
     */
    void onQuit(Connection connection);

}
