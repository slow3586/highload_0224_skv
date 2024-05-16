package com.slow3586.highload_0224_skv.mainapp.debug;

import com.github.javafaker.Faker;
import com.slow3586.highload_0224_skv.mainapp.repository.write.UserWriteRepository;
import com.slow3586.highload_0224_skv.mainapp.service.DialogService;
import com.slow3586.highload_0224_skv.mainapp.service.PasswordService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
class DbDebug {
    UserWriteRepository userWriteRepository;
    PasswordService passwordService;
    JdbcTemplate jdbcTemplate;
    DialogService dialogService;
    @NonFinal
    @Value("${app.debug.fillDbWithRandomData:false}")
    boolean fillDbWithRandomData;
    @NonFinal
    @Value("${app.debug.fillDbWithRandomDataThreads:100}")
    int fillDbWithRandomDataThreads;
    @NonFinal
    @Value("${app.debug.fillDialogDb:false}")
    boolean fillDialogDb;
    Random random = new Random(123);

    @PostConstruct
    protected void postConstruct() {
        if (fillDbWithRandomData) {
            this.fillDbWithRandomData();
        }
        if (fillDialogDb) {
            this.fillDialogDb();
        }
    }

    protected void fillDbWithRandomData() {
        log.info("#fillDbWithRandomData: started!");
        final Faker faker = new Faker(new Locale("ru-RU"), random);
        if (userWriteRepository.count() > 0) {
            log.error("Database has data! Not doing #fillDbWithRandomData");
            return;
        }
        final int THREADS = fillDbWithRandomDataThreads;
        final AtomicInteger count = new AtomicInteger();
        IntStream.range(0, THREADS).parallel()
            .forEach(i -> {
                final int thread = count.getAndIncrement();
                if (thread % 100 == 0) {
                    log.info("#fillDbWithRandomData: users table: thread {}/{} started", thread, THREADS);
                }
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

        final List<UUID> userIds = getUserIds();

        count.set(0);
        userIds.parallelStream().forEach(userId -> {
            final int thread = count.getAndIncrement();
            if (thread % 100 == 0) {
                log.info("#fillDbWithRandomData: friendships table: thread {}/{} started", thread, userIds.size());
            }
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
            if (thread % 100 == 0) {
                log.info("#fillDbWithRandomData: posts table: thread {}/{} started", thread, userIds.size());
            }
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

        log.info("#fillDbWithRandomData: dialogs table");

        log.info("#fillDbWithRandomData: finished!");
        log.info("#fillDbWithRandomData: LOGIN: {} PW: 12345", userIds.get(0));
    }

    protected List<UUID> getUserIds() {
        log.info("#fillDbWithRandomData: quering userIds");
        return jdbcTemplate.queryForList("SELECT id FROM users LIMIT " + (fillDbWithRandomDataThreads * 100))
            .stream()
            .flatMap(m -> m.values().stream())
            .map(o -> (UUID) o)
            .toList();
    }

    @Deprecated
    protected void fillDialogDb() {
        final AtomicInteger count = new AtomicInteger();
        final List<UUID> userIds = getUserIds();
        final Faker faker = new Faker(new Locale("ru-RU"), random);
        Flux.fromIterable(userIds)
            .flatMap(userId -> {
                final int thread = count.getAndIncrement();
                if (thread % 100 == 0) {
                    log.info("#fillDbWithRandomData: dialogs table: thread {}/{} started", thread, userIds.size());
                }

                return Flux.range(0, 5)
                    .flatMap(i -> {
                        UUID friendId = null;
                        while (friendId == null || friendId.equals(userId)) {
                            friendId = userIds.get(random.nextInt(userIds.size()));
                        }
                        return dialogService.sendDialogPost(userId, friendId, faker.gameOfThrones().quote());
                    });
            })
            .subscribeOn(Schedulers.boundedElastic())
            .doOnComplete(() -> log.info("#fillDbWithRandomData: dialogs table finish"))
            .subscribe();
    }
}
