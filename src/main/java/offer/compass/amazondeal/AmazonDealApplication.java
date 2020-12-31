package offer.compass.amazondeal;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AmazonDealApplication {
	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(AmazonDealApplication.class);
		builder.headless(false);
		builder.run(args);
	}
}
