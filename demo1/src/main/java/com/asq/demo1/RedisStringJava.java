package com.asq.demo1;

import redis.clients.jedis.Jedis;

/**
 * @author liujian
 * @date 2020/8/5
 */
public class RedisStringJava {

	public static void main(String[] args){
		Jedis jedis=new Jedis("localhost");
		jedis.set("asq","www.baidu.com");
		System.out.println("redis 存储的字符串为："+jedis.get("asq"));
	}
}
