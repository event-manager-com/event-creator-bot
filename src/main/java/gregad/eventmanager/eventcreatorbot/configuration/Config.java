package gregad.eventmanager.eventcreatorbot.configuration;

import gregad.eventmanager.eventcreatorbot.bot.Bot;
import gregad.eventmanager.eventcreatorbot.bot.TelegramFacade;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.meta.generics.Webhook;
import org.telegram.telegrambots.meta.generics.WebhookBot;
import org.telegram.telegrambots.starter.TelegramBotInitializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Greg Adler
 */
@Configuration
public class Config {
    @Value("${bot.webHookPath}")
    private String webHookPath;
    @Value("${bot.userName}")
    private String botUserName;
    @Value("${bot.botToken}")
    private String botToken;
    @Value("${bot.proxyType}")
    private DefaultBotOptions.ProxyType proxyType;
    @Value("${bot.proxyHost}")
    private String proxyHost;
    @Value("${bot.proxyPort}")
    private int proxyPort;
    @Value("${rest.template.timeout}")
    private long timeout;
    
    public static final String TELEGRAM_REGISTRATION_URL="https://api.telegram.org/bot";
    
    
    @Bean
    public Bot bot() throws TelegramApiRequestException {
        DefaultBotOptions options = ApiContext
                .getInstance(DefaultBotOptions.class);

        options.setProxyHost(proxyHost);
        options.setProxyPort(proxyPort);
        options.setProxyType(proxyType);
        Bot bot = new Bot(options);
        bot.setBotToken(botToken);
        bot.setBotUserName(botUserName);
        bot.setBotWebHookUrl(webHookPath);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(TELEGRAM_REGISTRATION_URL+botToken+"/setWebhook?url="+webHookPath,
                HttpMethod.GET,null,Void.class);
        return bot;
    }
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
