package com.asq.demo2;

import java.util.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

/**
 * @author liujian
 * @date 2020/8/6
 * 网站文章
 */
public class Chapter01 {

	private static final int ONE_WEEK_IN_SECONDS = 7 * 86400;
	private static final int VOTE_SCORE = 432;
	private static final int ARTICLES_PER_PAGE = 25;

	public  static void main(String[] args){
		Chapter01 chapter01=new Chapter01();
		chapter01.run();
	}

	public void run() {
		Jedis conn = new Jedis("localhost");
		//Select 命令用于切换到指定的数据库
		conn.select(15);

		String articleId = postArticle(
				conn, "username", "A title", "http://www.google.com");
		System.out.println("We posted a new article with id: " + articleId);
		System.out.println("Its HASH looks like:");
		Map<String,String> articleData = conn.hgetAll("article:" + articleId);
		for (Map.Entry<String,String> entry : articleData.entrySet()){
			System.out.println("  " + entry.getKey() + ": " + entry.getValue());
		}

		System.out.println();

		articleVote(conn, "other_user", "article:" + articleId);
		String votes = conn.hget("article:" + articleId, "votes");
		System.out.println("We voted for the article, it now has votes: " + votes);
		assert Integer.parseInt(votes) > 1;

		System.out.println("The currently highest-scoring articles are:");
		List<Map<String,String>> articles = getArticles(conn, 1);
		printArticles(articles);
		assert articles.size() >= 1;

		addGroups(conn, articleId, new String[]{"new-group"},null);
		System.out.println("We added the article to a new group, other articles include:");
		articles = getGroupArticles(conn, "new-group", 1);
		printArticles(articles);
		assert articles.size() >= 1;
	}

	/**
	 * 投票功能
	 * @param conn
	 * @param user
	 * @param article
	 */
	public void articleVote(Jedis conn, String user, String article) {
		//计算文章的投票截止时间
		long cutoff = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECONDS;
		//检查是否还可以对文章进行投票
		// （虽然使用散列也可以获取文章的发布时间，但有序集合返回的文章发布时间为浮点数，可以不进行转换直接使用）
		if (conn.zscore("time:", article) < cutoff) {
			return;
		}
		//从article：id  标识符里面取出文章的id
		String articleId = article.substring(article.indexOf(':') + 1);
		//判断用户是否是第一次投票该文章
		if (conn.sadd("voted:" + articleId, user) == 1) {
			conn.zincrby("score:", VOTE_SCORE, article);
			conn.hincrBy(article, "votes", 1);
		}
	}

	/**
	 * 发布文章
	 * @param conn
	 * @param user
	 * @param title
	 * @param link
	 * @return
	 */
	public String postArticle(Jedis conn, String user, String title, String link) {
		//生成一个新的文章id
		String articleId = String.valueOf(conn.incr("article:"));
		//将发布文章的用户添加到文章的已投票用户名单里面
		String voted = "voted:" + articleId;
		conn.sadd(voted, user);
		//将这个名单的过期时间设置为一周
		conn.expire(voted, ONE_WEEK_IN_SECONDS);
		long now = System.currentTimeMillis() / 1000;
		String article = "article:" + articleId;
		//将文章信息存储到散列中
		HashMap<String, String> articleData = new HashMap<String, String>();
		articleData.put("title", title);
		articleData.put("link", link);
		articleData.put("user", user);
		articleData.put("now", String.valueOf(now));
		articleData.put("votes", "1");
		conn.hmset(article, articleData);
		//将文章添加到根据发布时间排序的有序集合和根据评分排序的有序集合里面
		conn.zadd("score:", now + VOTE_SCORE, article);
		conn.zadd("time:", now, article);
		return articleId;
	}

	/**
	 * 获取评分最高、最新发布的文章
	 * @param conn
	 * @param page
	 * @param order
	 * @return
	 */
	public List<Map<String,String>> getArticles(Jedis conn, int page, String order) {
		//设置获取文章的起始索引和结束索引
		int start = (page - 1) * ARTICLES_PER_PAGE;
		int end = start + ARTICLES_PER_PAGE - 1;
		//获取多个文章的id
		Set<String> ids = conn.zrevrange(order, start, end);
		List<Map<String,String>> articles = new ArrayList<Map<String,String>>();
		for (String id : ids){
			//根据文章id获取文章的详细信息
			Map<String,String> articleData = conn.hgetAll(id);
			articleData.put("id", id);
			articles.add(articleData);
		}
		return articles;
	}

	public List<Map<String,String>> getArticles(Jedis conn, int page) {
		return getArticles(conn, page, "score:");
	}

	/**
	 * 添加分组
	 * @param conn
	 * @param articleId
	 * @param toAdd
	 * @param toRemove
	 */
	public void addGroups(Jedis conn, String articleId, String[] toAdd,String [] toRemove) {
		//构建存储文章信息的键名
		String article = "article:" + articleId;
		for (String group : toAdd) {
			//将文章添加到它所属的群组里面
			conn.sadd("group:" + group, article);
		}
		for(String group:toRemove){
			//从群组里面移出文章
			conn.srem("group"+group,article);
		}
	}

	public List<Map<String,String>> getGroupArticles(Jedis conn, String group, int page) {
		return getGroupArticles(conn, group, page, "score:");
	}

	/**
	 * 获取所有文章
	 * @param conn
	 * @param group
	 * @param page
	 * @param order
	 * @return
	 */
	public List<Map<String,String>> getGroupArticles(Jedis conn, String group, int page, String order) {
		String key = order + group;
		if (!conn.exists(key)) {
			ZParams params = new ZParams().aggregate(ZParams.Aggregate.MAX);
			conn.zinterstore(key, params, "group:" + group, order);
			conn.expire(key, 60);
		}
		return getArticles(conn, page, key);
	}

	/**
	 * 打印所有文章
	 * @param articles
	 */
	private void printArticles(List<Map<String,String>> articles){
		for (Map<String,String> article : articles){
			System.out.println("  id: " + article.get("id"));
			for (Map.Entry<String,String> entry : article.entrySet()){
				if (entry.getKey().equals("id")){
					continue;
				}
				System.out.println("    " + entry.getKey() + ": " + entry.getValue());
			}
		}
	}

}
