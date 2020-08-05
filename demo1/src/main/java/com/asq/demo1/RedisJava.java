package com.asq.demo1;

import redis.clients.jedis.Jedis;

/**
 * @author liujian
 * @date 2020/8/5
 */
public class RedisJava {
	public static void main(String [] args){
		//连接本地的redis服务
		Jedis jedis=new Jedis("localhost");
		System.out.println("连接成功");
		//查看服务是否运行
		System.out.println("服务正在运行："+jedis.ping());
	}
}
