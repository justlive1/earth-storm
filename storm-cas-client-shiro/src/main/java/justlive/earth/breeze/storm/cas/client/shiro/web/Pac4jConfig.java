package justlive.earth.breeze.storm.cas.client.shiro.web;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
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
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.LogoutFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import justlive.earth.breeze.snow.common.base.constant.BaseConstants;
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

  /**
   * cookie
   * 
   * @return
   */
  Cookie sessionIdCookie() {
    SimpleCookie cookie = new SimpleCookie(SimpleCookie.class.getName());
    cookie.setHttpOnly(false);
    cookie.setMaxAge(180_000);
    cookie.setPath(BaseConstants.ROOT_PATH);
    return cookie;
  }

  /**
   * sessionDAO<br>
   * 默认JavaUuidSessionIdGenerator
   * 
   * @return
   */
  @Bean
  SessionDAO sessionDAO() {
    return new MemorySessionDAO();
  }

  /**
   * session管理
   * 
   * @param sessionDAO
   * @param sessionIdCookie
   */
  @Bean
  SessionManager sessionManager(SessionDAO sessionDAO, Cookie sessionIdCookie) {
    DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
    sessionManager.setGlobalSessionTimeout(1_800_000);
    sessionManager.setDeleteInvalidSessions(true);
    sessionManager.setSessionDAO(sessionDAO);
    sessionManager.setSessionIdCookieEnabled(true);
    sessionManager.setSessionIdCookie(sessionIdCookie);
    return sessionManager;
  }

  /**
   * 基于pac4j的Subject工厂
   * 
   * @return
   */
  @Bean
  SubjectFactory subjectFactory() {
    return new Pac4jSubjectFactory();
  }


  /**
   * pac4j realm
   * 
   * @return
   */
  @Bean
  Realm realm() {
    Pac4jRealm realm = new Pac4jRealm();
    // TODO cache
    realm.setCachingEnabled(false);
    realm.setAuthenticationCachingEnabled(false);
    realm.setAuthorizationCachingEnabled(false);
    return realm;
  }

  /**
   * 安全管理器
   * 
   * @param realm
   */
  @Bean
  WebSecurityManager webSecurityManager(Realm realm, SubjectFactory subjectFactory) {
    DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
    manager.setRealm(realm);
    manager.setSubjectFactory(subjectFactory);
    return manager;
  }

  /**
   * SecurityFilter，用于拦截受保护的url
   * 
   * @param config
   * @param clients
   * @return
   */
  @Bean
  SecurityFilter securityFilter(Config config) {
    SecurityFilter filter = new SecurityFilter();
    filter.setConfig(config);
    filter.setClients(CasClient.class.getSimpleName());
    return filter;
  }

  /**
   * 回调过滤器，完成ticket认证
   * 
   * @return
   */
  @Bean
  CallbackFilter callbackFilter(Config config) {
    CallbackFilter filter = new CallbackFilter();
    filter.setConfig(config);
    filter.setDefaultUrl(configProps.defaultSuccessUrl);
    return filter;
  }

  /**
   * 登出过滤器
   * 
   * @param config
   * @return
   */
  @Bean
  LogoutFilter logoutFilter(Config config) {
    LogoutFilter filter = new LogoutFilter();
    filter.setConfig(config);
    filter.setDefaultUrl(configProps.logoutUrl);
    filter.setCentralLogout(true);
    filter.setLocalLogout(false);
    return filter;
  }

  @Bean
  ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
    ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
    bean.setSecurityManager(securityManager);
    // TODO
    // bean.setFilters(filters);
    // bean.setFilterChainDefinitions(definitions);
    return bean;
  }

  /**
   * 生命周期处理
   * 
   * @return
   */
  @Bean
  LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
    return new LifecycleBeanPostProcessor();
  }
}
