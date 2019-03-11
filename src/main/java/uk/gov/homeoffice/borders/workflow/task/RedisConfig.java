package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Configuration
@Profile("!test")
@Slf4j
public class RedisConfig {


    @Value("#{environment.PRIVATE_REDIS_URL?:'localhost'}")
    public String redisHostName;
    @Value("#{environment.PRIVATE_REDIS_PORT?:6379}")
    public int redisPort;
    @Value("#{environment.PRIVATE_REDIS_TOKEN}")
    private String redisAuthToken;
    @Autowired
    Environment env;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Initialising redis host: '{}' on port '{}'", redisHostName, redisPort);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);

        RedisStandaloneConfiguration
                redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setPort(redisPort);
        redisStandaloneConfiguration.setHostName(redisHostName);
        ofNullable(redisAuthToken).ifPresent(token -> redisStandaloneConfiguration.setPassword(RedisPassword.of(token)));


        Optional<String> isLocal = Arrays.stream(env.getActiveProfiles()).filter(
                (profile) -> profile.equalsIgnoreCase("local")
        ).findFirst();

        JedisClientConfiguration jedisClientConfiguration;
        if (isLocal.isPresent()) {
            log.info("In local mode redis configuration");
            jedisClientConfiguration = JedisClientConfiguration
                    .builder()
                    .usePooling()
                    .poolConfig(poolConfig)
                    .build();
        } else {
            log.info("In non local mode redis configuration");
            jedisClientConfiguration = JedisClientConfiguration
                    .builder()
                    .usePooling()
                    .poolConfig(poolConfig)
                    .and()
                    .useSsl()
                    .build();
        }


        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration,
                jedisClientConfiguration);
        log.info("Initialised redis: '{}' on port '{}'", redisHostName, redisPort);
        return jedisConnectionFactory;
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

}


