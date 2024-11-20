package com.app.lab3.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class FTPDownloader {

    private final String server = "localhost";
    private final int port = 21;
    private final String user = "testuser";
    private final String pass = "testpass";

    public File downloadFileFromFTP(String remoteFilePath, String localFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        File localFile = new File(localFilePath);

        try {
            // Connect and log in to the FTP server
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            // Set file type to binary
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Download the file
            try (FileOutputStream outputStream = new FileOutputStream(localFile)) {
                boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
                if (success) {
                    System.out.println("File downloaded successfully: " + localFile.getName());
                } else {
                    System.err.println("Failed to download file: " + localFile.getName());
                }
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }

        return localFile;
    }
}

