package vip.justlive.storm.cas.management;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import vip.justlive.common.base.util.HttpUtils;

/**
 * cas扩展配置类
 * 
 * @author wubo
 *
 */
@Configuration("stormCasManagementConfig")
public class CasManagementConfig {

  @PostConstruct
  void init() {
    HttpUtils.trustAllManager();
  }
}
