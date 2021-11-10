package br.com.brunoxkk0.common;

/**
 * Define os estados para realizar a troca das chaves.
 */
public enum KeyShareStatus {

    SEND_CLIENT_RSA     (0),
    RECEIVE_CLIENT_RSA  (1),
    SEND_SERVER_AES     (2),
    RECEIVE_SERVER_AES  (3),
    FINISH              (4);

    int code;

    KeyShareStatus(int code){
        this.code = code;
    }

}
