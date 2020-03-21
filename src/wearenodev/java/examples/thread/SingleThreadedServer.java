/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wearenodev.java.examples.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author huynhha
 */
public class SingleThreadedServer implements Runnable {

    private ServerSocket serverSocket = null;
    private int serverPort = -1;
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

        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            // initial
        }

        openServerSocket();

        System.out.println("Server is running on port " + this.serverPort);

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
                Logger.getLogger(SingleThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(SingleThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * R
         * Stop server and exit processR
         */
        server.stop();
        System.exit(0);
    }

}
