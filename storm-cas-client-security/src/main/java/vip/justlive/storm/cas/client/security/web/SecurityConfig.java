package vip.justlive.storm.cas.client.security.web;

import java.sql.Driver;
import javax.servlet.http.HttpSessionEvent;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.EhCacheBasedTicketCache;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import net.sf.ehcache.Cache;
import vip.justlive.common.base.util.HttpUtils;
import vip.justlive.common.base.util.ReflectUtils;
import vip.justlive.common.web.base.ConfigProperties;

/**
 * security配置
 * 
 * @author wubo
 *
 */
@Configuration
public class SecurityConfig {

  static {
    HttpUtils.trustAllManager();
  }

  @Autowired
  ConfigProperties configProps;

  /**
   * cas 地址
   * 
   * @return
   */
  @Bean
  public ServiceProperties serviceProperties() {
    ServiceProperties serviceProperties = new ServiceProperties();
    serviceProperties.setService(configProps.casService);
    serviceProperties.setAuthenticateAllArtifacts(true);
    serviceProperties.setSendRenew(false);
    return serviceProperties;
  }

  /**
   * cas认证切入点，声明cas服务端登录地址
   * 
   * @param props
   * @return
   */
  @Bean
  @Primary
  public AuthenticationEntryPoint authenticationEntryPoint(ServiceProperties props) {

    CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
    entryPoint.setLoginUrl(configProps.casLoginUrl);
    entryPoint.setServiceProperties(props);
    return entryPoint;
  }

  /**
   * PGT
   * 
   * @return
   */
  @Bean
  ProxyGrantingTicketStorage storage() {
    return new ProxyGrantingTicketStorageImpl();
  }

  /**
   * 票据验证
   * 
   * @param storage
   * @return
   */
  @Bean
  public TicketValidator ticketValidator(ProxyGrantingTicketStorage storage) {

    if (configProps.useProxyReceptor) {

      Cas20ProxyTicketValidator validator =
          new Cas20ProxyTicketValidator(configProps.casServerPrefixUrl);
      validator.setAcceptAnyProxy(true);
      validator.setProxyCallbackUrl(configProps.appHost + ConfigProperties.URL_PATH_SEPARATOR
          + configProps.appName + configProps.proxyReceptorUrl);
      validator.setProxyGrantingTicketStorage(storage);
      return validator;
    }

    return new Cas30ServiceTicketValidator(configProps.casServerPrefixUrl);
  }

  /**
   * cas认证提供器，定义客户端的验证方式
   * 
   * @param props
   * @param validator
   * @param userDetailsService
   * @return
   */
  @Bean
  public CasAuthenticationProvider casAuthenticationProvider(ServiceProperties props,
      TicketValidator validator, UserDetailsService userDetailsService) {

    CasAuthenticationProvider provider = new CasAuthenticationProvider();
    provider.setServiceProperties(props);
    provider.setTicketValidator(validator);
    provider.setUserDetailsService(userDetailsService);
    provider.setKey(configProps.casAuthProviderKey);
    EhCacheBasedTicketCache statelessTicketCache = new EhCacheBasedTicketCache();
    Cache cache = new Cache(EhCacheBasedTicketCache.class.getName(), 50, true, false, 3600, 900);
    statelessTicketCache.setCache(cache);
    provider.setStatelessTicketCache(statelessTicketCache);
    return provider;
  }

  /**
   * 获取用户信息（mock）
   * 
   * @return
   */
  @Bean
  @Profile("mock")
  UserDetailsService userDetailsService() {
    return name -> new User(name, name, true, true, true, true,
        AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
  }

  /**
   * 获取用户信息（jdbc）
   * 
   * @return
   */
  @Bean
  @Profile("jdbc")
  UserDetailsService jdbcUserDetailsService() {

    Driver driver = ReflectUtils.fromName(configProps.authDataSourceDriver);
    SimpleDriverDataSource dataSource =
        new SimpleDriverDataSource(driver, configProps.authDataSourceUrl,
            configProps.authDataSourceUsername, configProps.authDataSourcePassword);

    JdbcDaoImpl jdbcDao = new JdbcDaoImpl();

    jdbcDao.setDataSource(dataSource);
    jdbcDao.setUsersByUsernameQuery(configProps.usersByUsernameQuery);

    jdbcDao.setEnableAuthorities(configProps.authoritiesEnabled);
    if (configProps.authoritiesEnabled) {
      jdbcDao.setAuthoritiesByUsernameQuery(configProps.authoritiesByUsernameQuery);
    }

    jdbcDao.setEnableGroups(configProps.groupAuthoritiesEnabled);
    if (configProps.groupAuthoritiesEnabled) {
      jdbcDao.setGroupAuthoritiesByUsernameQuery(configProps.groupAuthoritiesByUsernameQuery);
    }

    return jdbcDao;
  }

  /**
   * 单点登出监听
   * 
   * @param event
   * @return
   */
  @EventListener
  public SingleSignOutHttpSessionListener singleSignOutHttpSessionListener(HttpSessionEvent event) {
    return new SingleSignOutHttpSessionListener();
  }
}
