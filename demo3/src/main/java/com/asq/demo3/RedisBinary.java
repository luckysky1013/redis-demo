package com.asq.demo3;


import redis.clients.jedis.Jedis;

/**
 * @author liujian
 * @date 2020/8/7
 * 子串和二进制位的命令
 */
public class RedisBinary {

	public static void main(String []args){
		Jedis jedis=new Jedis("localhost");
		System.out.println(jedis.append("new-String-key","hello"));
		System.out.println(jedis.append("new-String-key","world"));
		System.out.println(jedis.substr("new-String-key",3,7));
		System.out.println(jedis.setrange("new-String-key",0,"H"));
		System.out.println(jedis.setrange("new-String-key",6,"W"));
		System.out.println(jedis.get("new-String-key"));
		System.out.println(jedis.setrange("new-String-key",11,",hwo are you"));
		System.out.println(jedis.get("new-String-key"));
		System.out.println(jedis.setbit("another-key",2,"dsf"));
		System.out.println(jedis.get("another-key"));
	}
}
