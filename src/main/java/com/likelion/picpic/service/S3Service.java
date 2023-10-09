package com.likelion.picpic.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.likelion.picpic.domain.User;
import com.likelion.picpic.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;
    private final UserService userService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("&{jwt.secret}")
    private String secretKey;

    public Long getUserId(String token){
        //토큰으로 userId가져오기
        String userEmail=JwtUtil.getEmail(token, secretKey);
        Long userId=userService.findUserId(userEmail);
        //토큰으로 userId가져오기
        return userId;
    }


    /*
    getURl()을 통해 파일이 저장된 URL을 return 해주고,
    이 URL로 이동 시 해당 파일이 오픈됨(버킷 정책 변경 완료)
     */
    public String saveFile(String token, MultipartFile multipartFile) throws IOException {
        Long userId=getUserId(token);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String originalFilename = userId+"/"+timeStamp+"_"+multipartFile.getOriginalFilename();


        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3.putObject(bucket, originalFilename, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(bucket, originalFilename).toString();
    }

    // 이미지 다운로드, 리턴값 변경 필요한지 찾아봐야함.
    public ResponseEntity<UrlResource> downloadImage(String originalFilename) {
        UrlResource urlResource = new UrlResource(amazonS3.getUrl(bucket, originalFilename));

        String contentDisposition = "attachment; filename=\"" +  originalFilename + "\"";

        // header에 CONTENT_DISPOSITION 설정을 통해 클릭 시 다운로드 진행
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(urlResource);

    }

    // 버킷에 올라간 파일 삭제, 버킷내의 폴더를 두어 할 경우 수정해야할듯
    public void deleteImage(String originalFilename)  {
        amazonS3.deleteObject(bucket, originalFilename);
    }
}

