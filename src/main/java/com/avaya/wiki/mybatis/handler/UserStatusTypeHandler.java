package com.avaya.wiki.mybatis.handler;

import com.avaya.wiki.domain.UserStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserStatusTypeHandler extends BaseTypeHandler<UserStatus> {

    /**
     * Set a non-null Java parameter to PreparedStatement (Java -> DB)
     * Called when inserting or updating values in database.
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UserStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getDbValue());
    }

    /**
     * Get result from ResultSet by column name (DB -> Java)
     * Called when querying by column name.
     */
    @Override
    public UserStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return UserStatus.fromDbValue(rs.getString(columnName));
    }

    /**
     * Get result from ResultSet by column index (DB -> Java)
     * Called when querying by column index (slightly faster).
     */
    @Override
    public UserStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return UserStatus.fromDbValue(rs.getString(columnIndex));
    }

    /**
     * Get result from CallableStatement (DB -> Java)
     * Called when reading output parameters from stored procedures.
     */
    @Override
    public UserStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return UserStatus.fromDbValue(cs.getString(columnIndex));
    }
}
