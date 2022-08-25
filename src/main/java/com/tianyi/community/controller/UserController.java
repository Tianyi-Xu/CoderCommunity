package com.tianyi.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.tianyi.community.annotation.LoginRequired;
import com.tianyi.community.entity.User;
import com.tianyi.community.service.FollowService;
import com.tianyi.community.service.LikeService;
import com.tianyi.community.service.UserService;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.media.name}")
    private String mediaBucketName;

    @Value("${qiniu.bucket.media.url}")
    private String mediaBucketUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * @return user setting page
     */
    @LoginRequired
    @RequestMapping(path="/setting", method= RequestMethod.GET)
    public String getSettingPage(Model model) {
        // media file name
        String fileName = CommunityUtil.generateUUID();

        // expected return info from qiniu if uploaded
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        
        // generate qiniu upload token, expires in 1h
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(mediaBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    @RequestMapping(path="/header/url", method=RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "file name cannot be empty");
        }

        String url = mediaBucketUrl + "/" + fileName;
        userService.updateProfilePhoto(hostHolder.getUser().getId(), url);
        return CommunityUtil.getJSONString(0);
    }



//    /**
//     * upload photo
//     */
//    @LoginRequired
//    @RequestMapping(path="/upload", method=RequestMethod.POST)
//    public String uploadProfilePhoto(MultipartFile profilePhoto, Model model) {
//        if (profilePhoto == null) {
//            model.addAttribute("error", "Please upload your profile photo.");
//            return "/site/setting";
//        }
//
//        String filename = profilePhoto.getOriginalFilename();
//        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
//        if (StringUtils.isBlank(suffix)) {
//            model.addAttribute("error", "File format is not supported.");
//            return "site/setting";
//        }
//
//        // generate random fileName
//        filename = CommunityUtil.generateUUID() + suffix;
//        // upload file to server
//        File filePath = new File(uploadPath + "/" + filename);
//        try {
//            profilePhoto.transferTo(filePath);
//        } catch (IOException e) {
//            logger.error("Error uploading the file: " + filename + e.getMessage());
//            throw new RuntimeException("Error uploading the file: " + filename, e);
//        }
//
//        // update the user's profile photo path (web path)
//        User user = hostHolder.getUser();
//        String profilePhotoURL = domain + contextPath + "/user/header/" + filename;
//        userService.updateProfilePhoto(user.getId(), profilePhotoURL);
//        return "redirect:/index";
//    }
//
    /**
     * return photo file stored at local (deprecated)
     */
    @RequestMapping(path="/header/{filename}", method=RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename,
                          HttpServletResponse response) {
        // filepath on the server
        filename = uploadPath + "/" + filename;
        // get suffix
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        // get photo from desk and return
        response.setContentType("image/" + suffix);
       try (
               OutputStream os = response.getOutputStream();
               FileInputStream fis = new FileInputStream(filename); // auto close streams
       ){
           // write in batch
           byte[] buffer = new byte[1024];
           int b = 0;
           // read from fileStream into buffer, return byte length
           while ((b = fis.read(buffer)) != -1) {
               // write to outputStream
               os.write(buffer, 0, b);
           }
       }catch (IOException e) {
           logger.error("Failed to read file: " + filename + e.getMessage());
       }
    }

    /**
     * update password
     */
    @LoginRequired
    @RequestMapping(path="/password", method=RequestMethod.POST)
    public String updatePassword(String currentPassword, String newPassword,
                                 Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), currentPassword, newPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/index";
        } else {
            model.addAttribute("currentPasswordMsg", map.get("currentPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "site/setting";
        }
    }


    /**
     * profile page, can visit the user and other user's profile
     */
    @RequestMapping(path = "/profile/{userId}", method=RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // user
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("The user doesn't exist");
        }
        model.addAttribute("user", user);

        // user like count
        int likeCount = likeService.findUserLikeCount(userId);

        // user followed user count
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // user follower count
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // if host holder user has followed the user
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

}
