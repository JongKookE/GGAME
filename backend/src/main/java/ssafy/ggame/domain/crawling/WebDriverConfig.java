package ssafy.ggame.domain.crawling;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {
    @Bean
    public WebDriver chromeDriver() {

        // ChromeOptions 설정 (예: 헤드리스 모드)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");

        // ChromeDriver 생성
        return new ChromeDriver(options);
    }
}