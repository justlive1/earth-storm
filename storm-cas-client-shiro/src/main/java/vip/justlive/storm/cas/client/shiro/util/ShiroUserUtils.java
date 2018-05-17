package vip.justlive.storm.cas.client.shiro.util;

import org.apache.shiro.SecurityUtils;
import io.buji.pac4j.subject.Pac4jPrincipal;

/**
 * Shiro用户工具类
 * 
 * @author wubo
 *
 */
public class ShiroUserUtils {

  private ShiroUserUtils() {}

  /**
   * 获取当前登陆用户名
   * 
   * @return
   */
  public static String loginUserName() {

    Pac4jPrincipal principal = (Pac4jPrincipal) SecurityUtils.getSubject().getPrincipal();
    if (principal != null) {
      return principal.getProfile().getId();
    }

    return null;
  }
}
