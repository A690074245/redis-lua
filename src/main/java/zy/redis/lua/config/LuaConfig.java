package zy.redis.lua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @author Administrator
 * @version 1.0
 * @date 2021/1/7 17:10
 */
@Configuration
public class LuaConfig {
    @Bean
    public DefaultRedisScript<Long> testScript(){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/test.lua")));
        redisScript.setResultType(Long.class);
        System.out.println("===初始化===");
        return redisScript;
    }
}
