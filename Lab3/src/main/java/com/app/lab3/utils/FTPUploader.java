//package com.app.lab3.utils;
//
//import com.app.lab3.models.EmailDetails;
//import com.app.lab3.services.SMTPService;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class FTPUploader {
//
//    private String server = "ftp-server";
//    private int port = 21;
//    private String user = "testuser";
//    private String pass = "testpass";
//
//    private final SMTPService smtpService;
//
//    public void uploadFileToFTP(File file) throws IOException {
//        FTPClient ftpClient = new FTPClient();
//
//        try {
//            ftpClient.connect(server, port);
//            ftpClient.login(user, pass);
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//            try (FileInputStream inputStream = new FileInputStream(file)) {
//                String remoteFilePath = "/" + file.getName();
//                boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
//
//                if (success) {
//                    System.out.println("File uploaded successfully: " + file.getName());
//
//                    String msg = smtpService.sendSimpleMail(new EmailDetails("ligom46277@cpaurl.com", "Hello, file uploaded successfully!!! Congrats", "File uploaded successfully: " + file.getName()));
//                    System.out.println(msg);
//                } else {
//                    System.out.println("Failed to upload file: " + file.getName());
//                }
//            }
//        } finally {
//            if (ftpClient.isConnected()) {
//                ftpClient.logout();
//                ftpClient.disconnect();
//            }
//        }
//    }
//}
