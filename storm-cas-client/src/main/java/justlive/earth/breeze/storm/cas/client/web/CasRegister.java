package justlive.earth.breeze.storm.cas.client.web;

import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.ANY_PATH;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.CAS_SERVER_LOGINURL;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.CAS_SERVER_LOGINURL_FIELD;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.CAS_SERVER_URL_PREFIX;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.CAS_SERVER_URL_PREFIX_FIELD;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.DEFAULT_CONFIG_PATH;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.IGNORES_URLS;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.IGNORES_URLS_FIELD;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.SERVER_NAME;
import static justlive.earth.breeze.snow.common.web.base.ConfigProperties.SERVER_NAME_FIELD;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.util.WebUtils;
import justlive.earth.breeze.snow.common.web.util.PropertiesWrapper;
import justlive.earth.breeze.storm.cas.client.util.Cas20ProxyReceivingTicketValidationFilter4Http;
import justlive.earth.breeze.storm.cas.client.util.MultipleAntPathMatcher;

/**
 * 注册filter和listener
 * 
 * @author wubo
 *
 */
public class CasRegister implements WebApplicationInitializer {

  @Override
  public void onStartup(ServletContext ctx) throws ServletException {

    WebUtils.setWebAppRootSystemProperty(ctx);

    PropertiesWrapper props = new PropertiesWrapper(DEFAULT_CONFIG_PATH);

    Map<String, String> params = new HashMap<>(5);
    params.put(CAS_SERVER_URL_PREFIX_FIELD, props.getProperty(CAS_SERVER_URL_PREFIX));
    params.put(CAS_SERVER_LOGINURL_FIELD, props.getProperty(CAS_SERVER_LOGINURL));
    params.put(SERVER_NAME_FIELD, props.getProperty(SERVER_NAME));
    params.put(IGNORES_URLS_FIELD, props.getProperty(IGNORES_URLS));
    params.put(ConfigurationKeys.IGNORE_URL_PATTERN_TYPE.getName(),
        MultipleAntPathMatcher.class.getName());

    // 单点退出
    ctx.addListener(SingleSignOutHttpSessionListener.class);

    SingleSignOutFilter signOut = new SingleSignOutFilter();
    signOut.setCasServerUrlPrefix(props.getProperty(CAS_SERVER_URL_PREFIX));
    signOut.setIgnoreInitConfiguration(true);

    ctx.addFilter(SingleSignOutFilter.class.getName(), signOut).addMappingForUrlPatterns(null, true,
        ANY_PATH);

    // cas认证
    Dynamic auth = ctx.addFilter(AuthenticationFilter.class.getName(), AuthenticationFilter.class);
    auth.setInitParameters(params);
    auth.addMappingForUrlPatterns(null, true, ANY_PATH);

    // ticket
    Dynamic ticket = ctx.addFilter(Cas20ProxyReceivingTicketValidationFilter4Http.class.getName(),
        Cas20ProxyReceivingTicketValidationFilter4Http.class);
    ticket.setInitParameters(params);
    ticket.addMappingForUrlPatterns(null, true, ANY_PATH);

    // 获取登录名
    ctx.addFilter(AssertionThreadLocalFilter.class.getName(), AssertionThreadLocalFilter.class)
        .addMappingForUrlPatterns(null, true, ANY_PATH);
  }
}
