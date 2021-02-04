package org.commonjava.util.sidecar;

import io.quarkus.runtime.annotations.QuarkusMain;
import java.io.IOException;
import java.net.*;


@QuarkusMain
public class ProxyServer{
    private int portNumber = 9090;
    private ServerSocket serverSocket = new ServerSocket(this.portNumber);

    ProxyServer() throws IOException {
        try {//Initiating ServerSocket with TCP port
            startListening();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


    private void startListening() {
        try {
            Socket socket;
            while (true) {
                System.out.println("Server is listening on port : " + this.portNumber);
                socket = serverSocket.accept(); // server is in listening mode
                System.out.println("Request arrived..");// diverting the request to processor with the socket reference
                new RequestProcessor(socket);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String... args){
        //int portNumber = 8080;
        try {
            ProxyServer server = new ProxyServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("hello");
    }
}