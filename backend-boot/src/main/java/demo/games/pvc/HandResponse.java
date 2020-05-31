package demo.games.pvc;

import demo.games.shared.Hand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandResponse {
  private Hand hand;
}
