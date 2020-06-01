package demo.games.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class GameCodeService {

  public String nextCode( int length ) {
    return RandomStringUtils.randomAlphanumeric( length );
  }
}
