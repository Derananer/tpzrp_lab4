package com.lisetckiy.lab4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RestController("/stream")
@Slf4j
public class VideoStreamController {




    @PostMapping("/upload")
    public void uploadStream(@RequestParam MultipartFile[] multipartFiles){
        for(MultipartFile multipartFile:multipartFiles){
            try {
                InputStream inputStream = multipartFile.getInputStream();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    @GetMapping("/video")
    public StreamingResponseBody stream(){
        final InputStream videoFileStream = new FileInputStream(videoFile);
        return (os) -> {
            readAndWrite(videoFileStream, os);
        };
    }

    private void readAndWrite(final InputStream is, OutputStream os) throws IOException {
        byte[] data = new byte[2048];
        int read = 0;
        while ((read = is.read(data)) > 0) {
            os.write(data, 0, read);
        }
        os.flush();
    }

}
