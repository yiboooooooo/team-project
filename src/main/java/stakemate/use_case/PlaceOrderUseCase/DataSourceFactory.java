package stakemate.use_case.PlaceOrderUseCase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {

    public static DataSource create() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://aws-1-ca-central-1.pooler.supabase.com:6543/postgres");
        config.setUsername("postgres.huqjovbougtvwlqtxppo");
        config.setPassword("stakematedb");
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }
}
