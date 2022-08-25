package com.tianyi.community.controller;

import com.google.code.kaptcha.Producer;
import com.tianyi.community.entity.User;
import com.tianyi.community.service.UserService;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @return register page
     */
    @RequestMapping(path = "/register", method= RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * @return login page
     */
    @RequestMapping(path = "/login", method= RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * @return register account
     */
    @RequestMapping(path = "/register", method=RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "Successfully registered, please check your email account to activate your account.");
            model.addAttribute("target", "/index"); // return to homepage
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * Account Activation page
     */
    //http://localhost:8080/community/activation/{userId}/{code}
    @RequestMapping(path = "/activation/{userId}/{code}", method=RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "Successfully activate your account, you can now browse posts on Bear Coder Community.");
            model.addAttribute("target", "/login"); // return to login page
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "Your account has been activated already.");
            model.addAttribute("target", "/index"); // return to homepage
        } else {
            model.addAttribute("msg", "Failed to activate the account, the activation code provided is wrong.");
            model.addAttribute("target", "/index"); // return to homepage
        }
        return "/site/operate-result";
    }

    /**
     * return kaptcha photo to the client， store captcha text into redis
     * */
    @RequestMapping(path="/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);


        // generate a key to uniquely identify the client in order to get kaptcha in redis, stored in cookie for a very short of time
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // get kaptcha from Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);


        // output the captcha photo to the browser
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            // spring mvc will close streams
            ImageIO.write(image, "png", os);
        } catch (Exception e) {
            logger.error("Error when response with kaptcha image: " + e.getMessage());
        }
    }

    /**
     * login the user
     */
    @RequestMapping(path="/login", method=RequestMethod.POST)
    public String login(String username, String password, String code, boolean isRemember,
                        Model model, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha =(String) redisTemplate.opsForValue().get(redisKey);
        }
        // check if kaptcha is blank
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code)
            || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "captcha code is incorrect!");
            return "/site/login";
        }

        // check username, password
        int expirationSeconds = isRemember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expirationSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expirationSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
    * logout the user
    */
    @RequestMapping(path="/logout", method=RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        // when logout, clean the authentication token
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }





}
