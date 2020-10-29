package scc.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache<T> {
	private static final String RedisHostname = "scc-group-cache.redis.cache.windows.net";
	private static final String RedisKey = "Nl31CTkaOIK3SsXUnbQzcWJwUM3jihjHvYwRZ2jYv2Y=";

	private static JedisPool instance;

	// Generic type
	private Class<T> t;

	public synchronized static JedisPool getCachePool() {
		if (instance != null)
			return instance;

		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
		poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		instance = new JedisPool(poolConfig, RedisHostname, 6380, 1000, RedisKey, true);
		return instance;
	}

	/**
	 * Adds an object item(Entity,Calendar,Forum...) to cache with the key 'key'
	 * 
	 * @param key
	 * @param item
	 */
	public void addObjectItemToCache(String key, T item) {
		ObjectMapper mapper = new ObjectMapper();

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			Long cnt = jedis.lpush(key, mapper.writeValueAsString(item));
			if (cnt > 5)
				jedis.ltrim(key, 0, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds an item to cache with the key 'key'
	 * 
	 * @param key
	 * @param item
	 */
	public void addItemToCache(String key, String item) {
		ObjectMapper mapper = new ObjectMapper();

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.set(key, mapper.writeValueAsString(item));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the value in cache for the key 'key'
	 * 
	 * @param key
	 * @return value from cache with key
	 */
	public String getItemFromCache(String key) {
		String item = "";

		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			item = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return item;
	}

	/**
	 * returns a list filled by cache
	 * 
	 * @param key
	 * @return list from cache
	 */
	public List<String> getListFromCache(String key) {
		// list to be filled from cache and returned
		List<String> items = new ArrayList<>();
		try {
			try (Jedis jedis = RedisCache.getCachePool().getResource()) {
				items = jedis.lrange(key, 0, -1);
				return items;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}

}