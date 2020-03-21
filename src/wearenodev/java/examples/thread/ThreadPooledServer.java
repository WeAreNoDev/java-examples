/*
 * License from https://github.com/wearenodev
 */
package wearenodev.java.examples.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author harisk
 */
public class ThreadPooledServer implements Runnable {

    /**
     * Worker Thread
     */
    public class RunnableWorker implements Runnable {

        private final Socket clientSocket;

        public RunnableWorker(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
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

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int serverPort;
    private ServerSocket serverSocket;
    private boolean isStopped = false;
    private final ExecutorService threadPool;

    public ThreadPooledServer(int port, int poolSize) {
        this.serverPort = port;
        /**
         * Initial new thread pool with fix size
         */
        this.threadPool = Executors.newFixedThreadPool(poolSize);
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

            /**
             * Push job to threadpool, threadpool will select a free thread
             * to handle this job, if all threads're busy, job will be in queue
             * and wait until exist a free thread
             */
            this.threadPool.execute(
                    new RunnableWorker(clientSocket)
            );
        }

        /**
         * Shutdown threadpool
         */
        this.threadPool.shutdown();

        System.out.println("Server stopped");
    }

    public static void main(String[] args) {

        int poolSize = 10;
        ThreadPooledServer server = new ThreadPooledServer(8080, poolSize);

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
