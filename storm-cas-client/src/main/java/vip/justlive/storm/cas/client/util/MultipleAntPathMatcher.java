package vip.justlive.storm.cas.client.util;

import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

/**
 * 多重url路径matcher
 * 
 * @author wubo
 *
 */
public class MultipleAntPathMatcher implements UrlPatternMatcherStrategy {

  static final String SEPARATOR = ",";

  private AntPathMatcher matcher = new AntPathMatcher();

  private String[] patterns;

  @Override
  public boolean matches(String url) {
    for (String pattern : patterns) {
      if (matcher.match(pattern, url)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setPattern(String pattern) {
    this.patterns = StringUtils.split(pattern, SEPARATOR);
  }
}
