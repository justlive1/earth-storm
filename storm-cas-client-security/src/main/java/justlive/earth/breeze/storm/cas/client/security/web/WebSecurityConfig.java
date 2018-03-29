package justlive.earth.breeze.storm.cas.client.security.web;

import java.util.Arrays;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import justlive.earth.breeze.snow.common.web.base.ConfigProperties;
import justlive.earth.breeze.storm.cas.client.security.auth.CustomAuthenticationFailureHandler;
import justlive.earth.breeze.storm.cas.client.security.auth.CustomAuthenticationSuccessHandler;

/**
 * security配置
 * 
 * @author wubo
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  ConfigProperties configProps;

  @Autowired(required = false)
  CustomAuthenticationSuccessHandler authenticationSuccessHandler;

  @Autowired(required = false)
  CustomAuthenticationFailureHandler authenticationFailureHandler;

  @Autowired
  AuthenticationEntryPoint authenticationEntryPoint;

  @Autowired
  AuthenticationProvider authenticationProvider;

  @Autowired
  ServiceProperties props;

  @Autowired
  ProxyGrantingTicketStorage storage;

  @Override
  public void configure(WebSecurity web) throws Exception {
    // 设置不拦截规则
    web.ignoring().antMatchers(configProps.ignoreMatchers).antMatchers(configProps.accessDeniedUrl);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {


    http.csrf().disable().authorizeRequests().expressionHandler(expressionHandler()).and()
        .exceptionHandling().accessDeniedPage(configProps.accessDeniedUrl).and().httpBasic()
        .authenticationEntryPoint(authenticationEntryPoint).and()
        .addFilter(casAuthenticationFilter())
        .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class).authorizeRequests()
        .anyRequest().authenticated().and().logout().logoutUrl(configProps.logoutUrl)
        .logoutSuccessUrl(configProps.logoutSuccessUrl)
        .invalidateHttpSession(configProps.logoutSessionInvalidate).permitAll().and()
        .sessionManagement().sessionFixation().changeSessionId()
        .maximumSessions(configProps.sessionMaximum).expiredUrl(configProps.sessionExpireUrl);


  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {

    auth.authenticationProvider(authenticationProvider);
  }

  @Override
  protected AuthenticationManager authenticationManager() throws Exception {

    return new ProviderManager(Arrays.asList(authenticationProvider));
  }

  DefaultWebSecurityExpressionHandler expressionHandler() {

    return new DefaultWebSecurityExpressionHandler();
  }

  /**
   * 单点登出过滤器
   * 
   * @return
   */
  SingleSignOutFilter singleSignOutFilter() {

    SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
    singleSignOutFilter.setIgnoreInitConfiguration(true);
    singleSignOutFilter.setCasServerUrlPrefix(configProps.casServerPrefixUrl);
    return singleSignOutFilter;
  }

  /**
   * cas认证过滤器
   * 
   * @return
   * @throws Exception
   */
  CasAuthenticationFilter casAuthenticationFilter() throws Exception {

    CasAuthenticationFilter filter = new CasAuthenticationFilter();
    filter.setServiceProperties(props);
    filter.setAuthenticationManager(authenticationManager());

    filter.setFilterProcessesUrl(configProps.securityFilterProcessesUrl);
    filter.setAuthenticationDetailsSource(new ServiceAuthenticationDetailsSource(props));

    if (configProps.useProxyReceptor) {
      filter.setProxyGrantingTicketStorage(storage);
      filter.setProxyReceptorUrl(configProps.proxyReceptorUrl);
    }

    AuthenticationFailureHandler failureHandler = authenticationFailureHandler;
    if (failureHandler == null) {
      failureHandler = new SimpleUrlAuthenticationFailureHandler(configProps.accessDeniedUrl);
    }
    filter.setAuthenticationFailureHandler(failureHandler);

    AuthenticationSuccessHandler successHandler = authenticationSuccessHandler;
    if (successHandler == null) {

      SavedRequestAwareAuthenticationSuccessHandler handler =
          new SavedRequestAwareAuthenticationSuccessHandler();
      handler.setDefaultTargetUrl(configProps.defaultSuccessUrl);
      successHandler = handler;
    }
    filter.setAuthenticationSuccessHandler(successHandler);

    return filter;
  }

}
