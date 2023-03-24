package ibf2022.batch2.ssf.frontcontroller.respositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthenticationRepository {

	private RedisTemplate<String, Object> redisTemplate;
    private static final String AUTH_KEY_PREFIX = "AUTH:";

    @Autowired
    public AuthenticationRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isLocked(String username) {
        String key = AUTH_KEY_PREFIX + username;
        Object isLocked = redisTemplate.opsForValue().get(key);
        return isLocked == null;
    }
	
	// TODO Task 5
	// Use this class to implement CRUD operations on Redis

}
