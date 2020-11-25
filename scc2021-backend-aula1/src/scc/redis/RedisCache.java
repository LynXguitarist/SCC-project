package scc.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import scc.utils.AzureProperties;

public class RedisCache {

	static RedisCache cache;
	private JedisPool jedisPool;

	public static RedisCache getCache() {
		if (cache == null) {
			cache = new RedisCache();
		}
		return cache;
	}

	RedisCache() {
		String RedisHostname = AzureProperties.getProperties().getProperty(AzureProperties.REDIS_URL);
		String cacheKey = AzureProperties.getProperties().getProperty(AzureProperties.REDIS_KEY);
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
		jedisPool = new JedisPool(poolConfig, RedisHostname, 6380, 1000, cacheKey, true);
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	/**
	 * Adds an item or list of items to cache with expire = expireTime
	 * 
	 * @param <T>
	 * 
	 * @param key
	 * @param listOfItems
	 * @param expireTime
	 */
	public <T> void addListToCache(String key, List<T> listOfItems, int expireTime) {
		ObjectMapper mapper = new ObjectMapper();

		try (Jedis jedis = getJedisPool().getResource()) {
			jedis.lpush(key, mapper.writeValueAsString(listOfItems));
			jedis.expire(key, expireTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds an item to cache with expire = expireTime. Diff between this method and
	 * addListToCache is that is suppose to be only one value for the key
	 * 
	 * @param <T>
	 * @param key
	 * @param item
	 * @param expireTime
	 */
	public <T> void addItemToCache(String key, T item, int expireTime) {
		ObjectMapper mapper = new ObjectMapper();

		try (Jedis jedis = getJedisPool().getResource()) {
			jedis.set(key, mapper.writeValueAsString(item));
			jedis.expire(key, expireTime);
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

		try (Jedis jedis = getJedisPool().getResource()) {
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
			try (Jedis jedis = getJedisPool().getResource()) {
				items = jedis.lrange(key, 0, -1);
				return items;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}

}