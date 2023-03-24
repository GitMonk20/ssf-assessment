package ibf2022.batch2.ssf.frontcontroller.util;

import ch.qos.logback.core.util.Duration;
import jakarta.servlet.http.HttpSession;


public class RedisService {

    public static void disableUser(String username) {
        String key = "disabled:" + username;
        Object redisTemplate;
        ((Object) redisTemplate).opsForValue().set(key, "disabled",  Duration.ofMinutes(30));
    }
}

