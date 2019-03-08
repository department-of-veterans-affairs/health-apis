package gov.va.api.health.sentinel.mockcdw;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;

/**
 * The mock clob implementation that is loaded with constant string. See {@link
 * MockEntityReturnDriver}
 */
@RequiredArgsConstructor(staticName = "of")
class MockEntityReturnClob implements Clob {
  private final String value;

  @Override
  public void free() {
    /* noop */
  }

  @Override
  public InputStream getAsciiStream() throws SQLException {
    throw notSupported();
  }

  @Override
  public Reader getCharacterStream() throws SQLException {
    throw notSupported();
  }

  @Override
  public Reader getCharacterStream(long pos, long length) throws SQLException {
    throw notSupported();
  }

  @Override
  public String getSubString(long pos, int length) {
    /* Don't support substring */
    return value;
  }

  @Override
  public long length() {
    return value.length();
  }

  private SQLException notSupported() {
    return new SQLException("functionality not supported in mock CDW driver");
  }

  @Override
  public long position(String searchstr, long start) throws SQLException {
    throw notSupported();
  }

  @Override
  public long position(Clob searchstr, long start) throws SQLException {
    throw notSupported();
  }

  @Override
  public OutputStream setAsciiStream(long pos) throws SQLException {
    throw notSupported();
  }

  @Override
  public Writer setCharacterStream(long pos) throws SQLException {
    throw notSupported();
  }

  @Override
  public int setString(long pos, String str) throws SQLException {
    throw notSupported();
  }

  @Override
  public int setString(long pos, String str, int offset, int len) throws SQLException {
    throw notSupported();
  }

  @Override
  public void truncate(long len) throws SQLException {
    throw notSupported();
  }
}
