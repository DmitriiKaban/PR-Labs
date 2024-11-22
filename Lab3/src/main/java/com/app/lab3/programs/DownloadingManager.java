package com.app.lab3.programs;

import com.app.lab3.utils.FTPDownloader;
import com.app.lab3.utils.MultipartUploader;
import com.app.lab3.utils.UDPListener;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DownloadingManager {

    private final FTPDownloader ftpDownloader;
    private final MultipartUploader multipartUploader;
    private final UDPListener udpListener;

    private static final String REMOTE_FILE_PATH = "/products.json";
    private static final String LOCAL_FILE_PATH = "downloaded_products.json";
    private static final String SERVER_URL = "http://localhost:";
    private static final String SERVER_METHOD = "/uploadJson";

    @Scheduled(fixedRateString = "30", timeUnit = TimeUnit.SECONDS)
    public void fetchAndSendFile() {
        try {
            File downloadedFile = ftpDownloader.downloadFileFromFTP(REMOTE_FILE_PATH, LOCAL_FILE_PATH);

            // first - send request to receive the port of the leader server
            // then - send the file to the leader server

            if (udpListener.getLeaderPort() != null) {
                multipartUploader.uploadFilePOST(SERVER_URL + udpListener.getLeaderPort() + SERVER_METHOD, downloadedFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
