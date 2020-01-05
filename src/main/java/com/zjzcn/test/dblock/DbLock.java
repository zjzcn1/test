package com.zjzcn.test.dblock;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public class DbLock {

    private static final String LOCK_SQL = "select * from %s where %s='%s' for update";

    private DataSource dataSource;
    private String lockTable;
    private String lockColumn;

    public DbLock(DataSource dataSource, String lockTable, String lockColumn) {
        this.dataSource = dataSource;
        this.lockTable = lockTable;
        this.lockColumn = lockColumn;
    }

    public void lock(String lockCode, LockFunction func) {
        Connection conn = null;
        boolean connAutoCommit = true;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            connAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement(String.format(LOCK_SQL, lockTable, lockColumn, lockCode));
            preparedStatement.execute();

            func.apply();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        } finally {
            // commit
            if (conn != null) {
                try {
                    conn.commit();
                    conn.setAutoCommit(connAutoCommit);
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }

            // close PreparedStatement
            if (null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}