package vip.justlive.storm.cas.client.security.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * security对应的用户工具类
 *
 * @author wubo
 *
 */
public class SercurityUserUtils {

  private SercurityUserUtils() {}

  /**
   * 获取当前登陆用户
   * 
   * @return
   */
  public static User loginUser() {

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal != null && User.class.isInstance(principal)) {
      return (User) principal;
    }

    return null;
  }

  /**
   * 获取当前登陆用户名
   * 
   * @return
   */
  public static String loginUserName() {

    User user = loginUser();
    if (user != null) {
      return user.getUsername();
    }

    return null;
  }
}
