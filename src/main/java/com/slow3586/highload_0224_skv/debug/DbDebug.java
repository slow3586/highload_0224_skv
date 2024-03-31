package com.slow3586.highload_0224_skv.debug;

import com.github.javafaker.Faker;
import com.slow3586.highload_0224_skv.repository.UserRepository;
import com.slow3586.highload_0224_skv.service.PasswordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
class DbDebug {
    final UserRepository userRepository;
    final PasswordService passwordService;
    final JdbcTemplate jdbcTemplate;
    @NonFinal
    @Value("${app.debug.fillDbWithRandomData:false}")
    boolean fillDbWithRandomData;

    @PostConstruct
    protected void postConstruct() {
        if (fillDbWithRandomData) {
            this.fillDbWithRandomData();
        }
    }

    protected void fillDbWithRandomData() {
        log.info("#fillDbWithRandomData: started!");
        Faker faker = new Faker(new Locale("ru-RU"));
        if (userRepository.count() > 0) {
            log.error("Database has data! Not doing #fillDbWithRandomData");
            return;
        }
        final int THREADS = 1000;
        final AtomicInteger count = new AtomicInteger();
        IntStream.range(0, THREADS).parallel()
            .forEach(i -> {
                final int thread = count.getAndIncrement();
                log.info("#fillDbWithRandomData: thread {}/{} started", thread, THREADS);
                jdbcTemplate.batchUpdate("INSERT INTO users(" +
                        "first_name, second_name, birthdate, biography, city, password)" +
                        " VALUES (?, ?, ?, ?, ?, ?);",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setString(1, faker.name().firstName());
                            ps.setString(2, faker.name().lastName());
                            ps.setDate(3, Date.valueOf(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
                            ps.setString(4, faker.lorem().sentence());
                            ps.setString(5, faker.address().city());
                            ps.setString(6, "$2a$04$yMttW2dTG.9/2S.JO9ac..nbjm9stVYmNv9Ve2Dzv2b0QeEugEbly"); //12345 strength = 4 rnd = 123
                        }

                        @Override
                        public int getBatchSize() {
                            return 100;
                        }
                    }
                );
                log.info("#fillDbWithRandomData: thread {}/{} finished", thread, THREADS);
            });
        log.info("#fillDbWithRandomData: finished!");
    }
}
