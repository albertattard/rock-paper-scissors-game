package test.utils;

import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.sha256;

public class Sha256Utils {

  public static String computeSha256( final String text ) {
    return sha256().hashString( text, StandardCharsets.UTF_8 ).toString();
  }

  private Sha256Utils() {
  }
}
