package com.compiler;

import com.compiler.utils.Runner;
import com.compiler.utils.WebServer;

import java.awt.Desktop;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            Runner.run(args[0]);
        } else {
            int port = 8080;
            WebServer server = new WebServer(port);
            server.start();

            // Try to open browser automatically
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                }
            } catch (Exception e) {
                System.out.println("Could not open browser automatically. Please visit http://localhost:" + port + " manually.");
            }
        }
    }
}