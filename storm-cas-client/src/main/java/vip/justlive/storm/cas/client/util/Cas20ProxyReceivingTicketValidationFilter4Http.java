package vip.justlive.storm.cas.client.util;

import javax.net.ssl.HostnameVerifier;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import vip.justlive.common.base.util.HostnameVerifierAlwaysTrue;

/**
 * 不校验 https证书
 * 
 * @author wubo
 *
 */
public class Cas20ProxyReceivingTicketValidationFilter4Http
    extends Cas20ProxyReceivingTicketValidationFilter {

  private static final HostnameVerifier VERIFIER = new HostnameVerifierAlwaysTrue();

  @Override
  protected HostnameVerifier getHostnameVerifier() {
    return VERIFIER;
  }
}
