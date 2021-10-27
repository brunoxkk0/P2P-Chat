package br.com.brunoxkk0.common;

import br.com.brunoxkk0.server.Connection;

@FunctionalInterface
public interface OnQuit {
    void onQuit(Connection connection);

}
