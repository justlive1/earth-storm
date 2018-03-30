package justlive.earth.breeze.storm.cas.client.shiro.web;

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.logout.CasLogoutHandler;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import justlive.earth.breeze.snow.common.web.base.ConfigProperties;
import justlive.earth.breeze.storm.cas.client.shiro.handle.ShiroCasLogoutHandler;
import justlive.earth.breeze.storm.cas.client.shiro.util.MyShiroSessionStore;

/**
 * pac4j配置
 * 
 * @author wubo
 *
 */
@Configuration
public class Pac4jConfig {

  @Autowired
  ConfigProperties configProps;

  /**
   * 登出处理
   * 
   * @return
   */
  @Bean
  CasLogoutHandler<WebContext> logoutHandler() {
    ShiroCasLogoutHandler<WebContext> logoutHandler = new ShiroCasLogoutHandler<>();
    logoutHandler.setDestroySession(true);
    return logoutHandler;
  }

  /**
   * cas服务端配置
   * 
   * @return
   */
  @Bean
  CasConfiguration casConfig(CasLogoutHandler<WebContext> logoutHandler) {

    CasConfiguration casConfig = new CasConfiguration();
    casConfig.setLoginUrl(configProps.logoutUrl);
    casConfig.setPrefixUrl(configProps.casServerPrefixUrl);
    casConfig.setLogoutHandler(logoutHandler);

    return casConfig;
  }

  /**
   * cas客户端配置
   * 
   * @param casConfig
   * @return
   */
  @Bean
  CasClient casClient(CasConfiguration casConfig) {
    CasClient client = new CasClient(casConfig);
    client.setCallbackUrl(configProps.proxyReceptorUrl + configProps.appName);
    client.setName(configProps.appName);
    return client;
  }

  /**
   * session Store
   * 
   * @return
   */
  @Bean
  SessionStore<J2EContext> sessionStore() {
    return new MyShiroSessionStore();
  }

  /**
   * pac4j配置
   * 
   * @param casClient
   * @param sessionStore
   * @return
   */
  @Bean
  Config authConfig(CasClient casClient, SessionStore<J2EContext> sessionStore) {
    Config authConfig = new Config(casClient);
    authConfig.setSessionStore(sessionStore);
    return authConfig;
  }

  
}
