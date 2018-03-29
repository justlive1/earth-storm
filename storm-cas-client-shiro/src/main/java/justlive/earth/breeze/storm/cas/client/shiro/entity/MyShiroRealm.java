/**
 * 
 */
package justlive.earth.breeze.storm.cas.client.shiro.entity;

import javax.annotation.PostConstruct;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cas.CasRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import justlive.earth.breeze.snow.common.web.base.ConfigProperties;

/**
 * @author liuwenlai
 *
 */
public class MyShiroRealm extends CasRealm {

	@Autowired
	ConfigProperties configProps;

	@PostConstruct
	public void initProperty() {
		setCasServerUrlPrefix(configProps.casServerPrefixUrl);
		setCasService(configProps.casService);
	}

	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection paramPrincipalCollection) {
		return null;
	}

	/**
	 * 登录验证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken paramAuthenticationToken)
			throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) paramAuthenticationToken;
		// TODO
		if (Strings.isNullOrEmpty(token.getUsername())) {
			return new SimpleAuthenticationInfo("", "", getName());
		} else {
			throw new AuthenticationException();
		}
	}

}
