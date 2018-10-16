package uk.gov.homeoffice.borders.workflow.task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import static java.util.Optional.ofNullable;

@Configuration
@Profile("!test")
public class RedisConfig {


    @Value("${redis.hostname:localhost}")
    public String redisHostName;
    @Value("${redis.port:6379}")
    public int redisPort;
    @Value("${redis.authToken:#{null}}")
    private String redisAuthToken;


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);

        RedisStandaloneConfiguration
                redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setPort(redisPort);
        redisStandaloneConfiguration.setHostName(redisHostName);
        ofNullable(redisAuthToken).ifPresent(token -> redisStandaloneConfiguration.setPassword(RedisPassword.of(token)));

        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration
                .builder()
                .usePooling()
                .poolConfig(poolConfig)
                .build();

        return new JedisConnectionFactory(redisStandaloneConfiguration,
                jedisClientConfiguration);
    }

}


