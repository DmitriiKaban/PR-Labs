package com.app.lab3.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class FTPUploader {

    private String server = "localhost";
    private int port = 21;
    private String user = "testuser";
    private String pass = "testpass";

    public void uploadFileToFTP(File file) throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            // Connect and log in to the FTP server
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            try (FileInputStream inputStream = new FileInputStream(file)) {
                String remoteFilePath = "/" + file.getName();
                boolean success = ftpClient.storeFile(remoteFilePath, inputStream);

                if (success) {
                    System.out.println("File uploaded successfully: " + file.getName());
                } else {
                    System.out.println("Failed to upload file: " + file.getName());
                }
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
