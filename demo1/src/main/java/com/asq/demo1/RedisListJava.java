package com.asq.demo1;

import java.util.List;

import redis.clients.jedis.Jedis;

/**
 * @author liujian
 * @date 2020/8/5
 */
public class RedisListJava {

	public static void main(String[] args){
		Jedis jedis=new Jedis("localhost");
		jedis.lpush("site-list","taobao");
		jedis.lpush("site-list","zhifubao");
		jedis.lpush("site-list","meituan");
		List<String> list=jedis.lrange("site-list",0,2);
		for (int i = 0; i <list.size() ; i++) {
			System.out.println("列表项为:"+list.get(i));
		}
		System.out.println("lpop:"+jedis.lpop("site-list"));
		System.out.println("rpop:"+jedis.rpop("site-list"));
	}
}
