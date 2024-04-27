package com.slow3586.highload_0224_skv.debug;

import com.github.javafaker.Faker;
import com.slow3586.highload_0224_skv.repository.write.UserWriteRepository;
import com.slow3586.highload_0224_skv.service.PasswordService;
import jakarta.annotation.PostConstruct;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
@Slf4j
class DbDebug {
    final UserWriteRepository userWriteRepository;
    final PasswordService passwordService;
    final JdbcTemplate jdbcTemplate;
    @NonFinal
    @Value("${app.debug.fillDbWithRandomData:false}")
    boolean fillDbWithRandomData;
    @NonFinal
    @Value("${app.debug.fillDbWithRandomDataThreads:1000}")
    int fillDbWithRandomDataThreads;
    final Random random = new Random(123);

    @Autowired
    public DbDebug(
        UserWriteRepository userWriteRepository,
        PasswordService passwordService,
        @Qualifier("writeJdbcTemplate") JdbcTemplate jdbcTemplate
    ) {
        this.userWriteRepository = userWriteRepository;
        this.passwordService = passwordService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    protected void postConstruct() {
        if (fillDbWithRandomData) {
            this.fillDbWithRandomData();
        }
    }

    protected void fillDbWithRandomData() {
        log.info("#fillDbWithRandomData: started!");
        Faker faker = new Faker(new Locale("ru-RU"), random);
        if (userWriteRepository.count() > 0) {
            log.error("Database has data! Not doing #fillDbWithRandomData");
            return;
        }
        final int THREADS = fillDbWithRandomDataThreads;
        final AtomicInteger count = new AtomicInteger();
        IntStream.range(0, THREADS).parallel()
            .forEach(i -> {
                final int thread = count.getAndIncrement();
                log.info("#fillDbWithRandomData: users table: thread {}/{} started", thread, THREADS);
                jdbcTemplate.batchUpdate("INSERT INTO users(" +
                        "first_name, second_name, birthdate, biography, city, password)" +
                        " VALUES (?, ?, ?, ?, ?, ?);",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, faker.name().firstName());
                            ps.setString(2, faker.name().lastName());
                            ps.setDate(3, Date.valueOf(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                            ps.setString(4, faker.gameOfThrones().quote());
                            ps.setString(5, faker.address().city());
                            ps.setString(6, "$2a$04$yMttW2dTG.9/2S.JO9ac..nbjm9stVYmNv9Ve2Dzv2b0QeEugEbly"); //12345 strength = 4 rnd = 123
                        }

                        @Override
                        public int getBatchSize() {
                            return 100;
                        }
                    }
                );
            });
        log.info("#fillDbWithRandomData: quering userIds");
        List<UUID> userIds = jdbcTemplate.queryForList("SELECT id FROM users LIMIT " + (THREADS * 100))
            .stream()
            .flatMap(m -> m.values().stream())
            .map(o -> (UUID) o)
            .toList();
        count.set(0);
        userIds.parallelStream().forEach(userId -> {
            final int thread = count.getAndIncrement();
            log.info("#fillDbWithRandomData: friendships table: thread {}/{} started", thread, userIds.size());
            jdbcTemplate.batchUpdate("INSERT INTO friendships(" +
                    "user_id, friend_id)" +
                    " VALUES (?, ?);",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        UUID friendId = null;
                        while (friendId == null || friendId.equals(userId)) {
                            friendId = userIds.get(random.nextInt(userIds.size()));
                        }
                        ps.setObject(1, userId);
                        ps.setObject(2, friendId);
                    }

                    @Override
                    public int getBatchSize() {
                        return 100;
                    }
                }
            );
        });
        count.set(0);
        jdbcTemplate.execute("ALTER TABLE posts DISABLE TRIGGER posts_insert_trig");
        userIds.parallelStream().forEach(userId -> {
            final int thread = count.getAndIncrement();
            log.info("#fillDbWithRandomData: posts table: thread {}/{} started", thread, userIds.size());
            jdbcTemplate.batchUpdate("INSERT INTO posts(" +
                    "author_user_id, date_created, text)" +
                    " VALUES (?, ?, ?);",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, userId);
                        ps.setDate(2, Date.valueOf(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                        ps.setString(3, faker.gameOfThrones().quote());
                    }

                    @Override
                    public int getBatchSize() {
                        return 100;
                    }
                }
            );
        });
        jdbcTemplate.execute("ALTER TABLE posts ENABLE TRIGGER posts_insert_trig");
        log.info("#fillDbWithRandomData: finished!");
        log.info("#fillDbWithRandomData: LOGIN: {} PW: 12345", userIds.get(0));
    }
}
