package demo.games.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomService {

  public int nextInt( int bound ) {
    return random.nextInt( bound );
  }

  private final Random random = new Random();
}
