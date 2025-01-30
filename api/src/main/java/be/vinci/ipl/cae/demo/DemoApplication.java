package be.vinci.ipl.cae.demo;

import lombok.experimental.UtilityClass;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class of the application.
 */
@UtilityClass
@SpringBootApplication
public class DemoApplication {

  /**
   * Main method of the application.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

}
