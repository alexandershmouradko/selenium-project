package uk.ndc.csa.utilities.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import uk.ndc.csa.utilities.common.FrameworkProperties;
import uk.ndc.csa.utilities.common.ThreadContext;

/** Thread-safe JDBC/Apache DbUtils facade. A new connection is used per operation. */
public final class DBUtilsHelper<T> {
    private static final QueryRunner RUNNER = new QueryRunner();

    private DBUtilsHelper() {
    }

    public static Connection createConn() throws SQLException, IOException {
        FrameworkProperties props = ThreadContext.getInstance().getEnvironmentProps();
        return createConn(
                props.getString("DBurl"),
                props.getString("DBDriver"),
                props.getString("DBusr"),
                props.getString("DBpwd"));
    }

    public static Connection createConn(String url, String driver, String user, String password)
            throws SQLException, IOException {
        if (driver != null && !driver.isBlank()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new IOException("JDBC driver is not available: " + driver, e);
            }
        }
        return DriverManager.getConnection(url, user, password);
    }

    /** Compatibility no-op: connections are closed at the end of every operation. */
    public static void closeConn() {
        // Deliberately empty. Kept for source compatibility with legacy consumers.
    }

    public static Object[] getDBArray(String sql, Object... params) throws SQLException, IOException {
        return query(sql, new ArrayHandler(), params);
    }

    public static List<Object[]> getDBArrayList(String sql, Object... params) throws SQLException, IOException {
        return query(sql, new ArrayListHandler(), params);
    }

    public static Map<String, Object> getDBMap(String sql, Object... params) throws SQLException, IOException {
        return query(sql, new MapHandler(), params);
    }

    public static List<Map<String, Object>> getDBMapList(String sql, Object... params)
            throws SQLException, IOException {
        return query(sql, new MapListHandler(), params);
    }

    public static <T> T getDBBean(String sql, Class<T> type, Object... params)
            throws SQLException, IOException {
        return query(sql, new BeanHandler<>(type), params);
    }

    public static <T> List<T> getDBBeanList(String sql, Class<T> type, Object... params)
            throws SQLException, IOException {
        return query(sql, new BeanListHandler<>(type), params);
    }

    public static int update(String sql, Object... params) throws SQLException, IOException {
        try (Connection connection = createConn()) {
            connection.setAutoCommit(false);
            try {
                int affectedRows = params == null || params.length == 0
                        ? RUNNER.update(connection, sql)
                        : RUNNER.update(connection, sql, params);
                connection.commit();
                return affectedRows;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private static <R> R query(String sql, ResultSetHandler<R> handler, Object... params)
            throws SQLException, IOException {
        try (Connection connection = createConn()) {
            return params == null || params.length == 0
                    ? RUNNER.query(connection, sql, handler)
                    : RUNNER.query(connection, sql, handler, params);
        }
    }
}
