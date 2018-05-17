package vip.justlive.storm.cas;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import vip.justlive.common.base.util.HttpUtils;

/**
 * cas扩展配置类
 * 
 * @author wubo
 *
 */
@Configuration("stormCasConfig")
public class CasConfig {

  @PostConstruct
  void init() {
    HttpUtils.trustAllManager();
  }

}
