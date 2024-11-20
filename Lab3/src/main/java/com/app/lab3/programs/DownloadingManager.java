package com.app.lab3.programs;

import com.app.lab3.utils.FTPDownloader;
import com.app.lab3.utils.MultipartUploader;
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

    private static final String REMOTE_FILE_PATH = "/products.json";
    private static final String LOCAL_FILE_PATH = "downloaded_products.json";
    private static final String SERVER_URL = "http://localhost:8081/uploadJson";

    @Scheduled(fixedRateString = "30", timeUnit = TimeUnit.SECONDS)
    public void fetchAndSendFile() {
        try {
            File downloadedFile = ftpDownloader.downloadFileFromFTP(REMOTE_FILE_PATH, LOCAL_FILE_PATH);

            multipartUploader.uploadFilePOST(SERVER_URL, downloadedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
