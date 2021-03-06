/*
 * License from https://github.com/wearenodev
 */
package wearenodev.java.examples.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author harisk
 */
public class SingleThreadedServer implements Runnable {

    private ServerSocket serverSocket;
    private final int serverPort;
    private boolean isStopped = false;

    public SingleThreadedServer(int port) {
        this.serverPort = port;
    }

    private void processClientRequest(Socket clientSocket) throws Exception {
        InputStream input = clientSocket.getInputStream();
        OutputStream output = clientSocket.getOutputStream();
        long time = System.currentTimeMillis();

        byte[] responseDocument = ("<html><body>"
                + "SingleThreadedServer time: "
                + time
                + "</body></html>").getBytes("UTF-8");

        byte[] responseHeader = ("HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + responseDocument.length
                + "\r\n\r\n").getBytes("UTF-8");

        output.write(responseHeader);
        output.write(responseDocument);
        output.close();
        input.close();
        System.out.println("Request processed: " + time);
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();

        } catch (IOException e) {
            throw new RuntimeException("Error on stop server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
            System.out.println("Server is running on port " + this.serverPort);

        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }

    @Override
    public void run() {

        openServerSocket();

        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server stopped");
                    return;
                }
                throw new RuntimeException("Error on accept client connection", e);

            }

            try {
                processClientRequest(clientSocket);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("Server stopped");
    }

    public static void main(String[] args) {

        SingleThreadedServer server = new SingleThreadedServer(8080);

        /**
         * Run server in single thread
         */
        new Thread(server).start();

        /**
         * Waiting 20s before stopping server
         */
        try {
            Thread.sleep(20 * 1000);

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        /**
         * Stop server and exit processR
         */
        server.stop();
        System.exit(0);
    }

}
