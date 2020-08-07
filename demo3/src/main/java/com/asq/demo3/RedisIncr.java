package com.asq.demo3;

import redis.clients.jedis.Jedis;

/**
 * @author liujian
 * @date 2020/8/7
 * 数值的自增和自减
 */
public class RedisIncr {

	public static void main(String []args) {
		Jedis jedis=new Jedis("localhost");
		System.out.println(jedis.get("key"));
		System.out.println(jedis.incr("key"));
		System.out.println(jedis.incrBy("key",15));
		System.out.println(jedis.decrBy("key",5));
		System.out.println(jedis.set("key","13"));
		System.out.println(jedis.incr("key"));
	}
}
