package zy.redis.lua.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;
import zy.redis.lua.config.LuaConfig;
import zy.redis.lua.domain.User;
import zy.redis.lua.util.ObjectToMap;
import zy.redis.lua.util.ShortUrlGenerator;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 * @date 2021/1/7 9:48
 */
@RestController
@Api("文档")
public class DemoController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private DefaultRedisScript<Long> testScript;
    @Autowired
    private HttpServletResponse servletResponse;


    @GetMapping("/test")
    @ApiOperation("测试接口")
    public String test(HttpServletRequest request){
        String remoteHost = request.getRemoteHost();
//        redisTemplate.opsForValue().set("aaa","bbb");
//        System.out.println((String)redisTemplate.opsForValue().get("aaa"));
        System.out.println(remoteHost);
        List<String> keys = Arrays.asList("testAPI");
        Long n = stringRedisTemplate.execute(new LuaConfig().testScript(), keys, "30", "10");
        if(n == 0){
            return "非法请求";
        }
//        redisTemplate.opsForValue().set("aaa","bbb");
        return "success";
    }

    @PostMapping("/create")
    @ApiOperation("新增")
    public void create(@RequestBody User user){
        System.out.println(user);
        String key = "user:"+user.getId();
        redisTemplate.opsForHash().putAll(key, ObjectToMap.exchange(user));
        System.out.println(redisTemplate.opsForHash().get("user:"+user.getId(),"name"));
    }

    @PostMapping("/addAge")
    @ApiOperation("年龄增长接口")
    public void addAge(int id,int age){
        redisTemplate.opsForHash().increment("user:"+id,"age",age);
        System.out.println(redisTemplate.opsForHash().get("user:"+id,"age"));
    }

    @GetMapping("/short")
    @ApiOperation("生成短链接")
    public String shortUrl(String url){
        String[] shortUrl = ShortUrlGenerator.shortUrl(url);
        redisTemplate.opsForHash().put("short:url",shortUrl[0],url);
        return "http://localhost:8383/"+shortUrl[0];
    }

    @GetMapping(value = "/{key}")
    @ApiOperation("重定向")
    public void change(@PathVariable String key) throws IOException {
        String url = (String)redisTemplate.opsForHash().get("short:url",key);
        servletResponse.sendRedirect(url);
    }
}
