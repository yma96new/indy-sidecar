package org.commonjava.util.sidecar;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RequestProcessor extends Thread //for multi-threaded server
{
    private Socket socket;

    RequestProcessor(Socket socket) {
        this.socket = socket;
        start(); // will load the run method
    }

    public void run() {
        try {//Declaring properties and streams
            OutputStream outputStream;
            OutputStreamWriter outputStreamWriter;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            StringBuffer stringBuffer;
            String response;
            String request;
            int x;
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            stringBuffer = new StringBuffer();
            while (true) {
                x = inputStreamReader.read();
                if (x == '#' || x == -1) break; //reads until terminator
                stringBuffer.append((char) x);
            }
            request = stringBuffer.toString();
            System.out.println("Request : " + request);//parsing and extracting Request data

            // handle data//sending response
            response = "Data saved#";//get output stream and its writer, for sending response or acknowledgement
            outputStream = socket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(response);
            outputStreamWriter.flush(); // response sent
            System.out.println("Response sent");
            socket.close(); //terminating connection
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }
}
