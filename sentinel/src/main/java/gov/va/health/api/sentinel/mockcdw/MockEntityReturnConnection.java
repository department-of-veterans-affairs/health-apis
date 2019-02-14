package gov.va.health.api.sentinel.mockcdw;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * The mock connection that supports calling the prc_Entity_Return procedure.
 *
 * <p>See {@link MockEntityReturnDriver}
 */
@Value
@AllArgsConstructor(staticName = "of")
class MockEntityReturnConnection implements Connection {
  MockResponses responses;

  @Override
  public void abort(Executor executor) {
    /* noop */
  }

  @Override
  public void clearWarnings() {
    /* noop */
  }

  @Override
  public void close() {
    /* noop */
  }

  @Override
  public void commit() {
    /* noop */
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    throw notSupported();
  }

  @Override
  public Blob createBlob() throws SQLException {
    throw notSupported();
  }

  @Override
  public Clob createClob() throws SQLException {
    throw notSupported();
  }

  @Override
  public NClob createNClob() throws SQLException {
    throw notSupported();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    throw notSupported();
  }

  @Override
  public Statement createStatement() throws SQLException {
    throw notSupported();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw notSupported();
  }

  @Override
  public Statement createStatement(
      int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    throw notSupported();
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    throw notSupported();
  }

  @Override
  public boolean getAutoCommit() {
    return true;
  }

  @Override
  public void setAutoCommit(boolean autoCommit) {
    /* noop */
  }

  @Override
  public String getCatalog() {
    return "mockcdw";
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    throw notSupported();
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    throw notSupported();
  }

  @Override
  public Properties getClientInfo() {
    return new Properties();
  }

  @Override
  public void setClientInfo(Properties properties) {
    /* noop */
  }

  @Override
  public void setClientInfo(String name, String value) {
    /* noop */
  }

  @Override
  public int getHoldability() throws SQLException {
    throw notSupported();
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    throw notSupported();
  }

  @Override
  public DatabaseMetaData getMetaData() {
    return null;
  }

  @Override
  public int getNetworkTimeout() {
    return 0;
  }

  @Override
  public String getSchema() {
    return "mockcdw";
  }

  @Override
  public void setSchema(String schema) {
    /* noop */
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    throw notSupported();
  }

  @Override
  public void setTransactionIsolation(int level) {
    /* noop */
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    throw notSupported();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    throw notSupported();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    throw notSupported();
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    /* noop */
  }

  @Override
  public boolean isValid(int timeout) {
    return false;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return false;
  }

  @Override
  public String nativeSQL(String sql) {
    return sql;
  }

  private SQLException notSupported() {
    return new SQLException("functionality not supported in mock CDW driver");
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    if (sql.startsWith("{call [") && sql.endsWith("].[prc_Entity_Return](?,?,?,?,?,?,?)}")) {
      return MockEntityReturnCallableStatement.of(responses);
    }
    throw new SQLException("Only prc_Entity_Return calls are supported");
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw notSupported();
  }

  @Override
  public CallableStatement prepareCall(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    throw notSupported();
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    throw notSupported();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    throw notSupported();
  }

  @Override
  public PreparedStatement prepareStatement(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    throw notSupported();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    throw notSupported();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    throw notSupported();
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    throw notSupported();
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    throw notSupported();
  }

  @Override
  public void rollback() {
    /* noop */
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    throw notSupported();
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) {
    /* noop */
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    throw notSupported();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    throw notSupported();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw notSupported();
  }
}
