package com.asq.demo1;

import java.util.Iterator;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * @author liujian
 * @date 2020/8/5
 */
public class RedisKeyJava {

	public static void main(String[]args){
		Jedis jedis=new Jedis("localhost");
		Set<String> keys=jedis.keys("*");
		Iterator<String> it=keys.iterator();
		while(it.hasNext()){
			String key=it.next();
			System.out.println(key);
		}
	}
}
