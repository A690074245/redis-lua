package zy.redis.lua.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Administrator
 * @version 1.0
 * @date 2021/1/8 15:25
 */
@Data
@Setter
@Getter
@ToString
public class User {
    private int id;
    private String name;
    private int age;
    private String address;
}
