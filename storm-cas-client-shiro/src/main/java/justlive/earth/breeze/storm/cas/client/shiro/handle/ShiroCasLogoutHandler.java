package justlive.earth.breeze.storm.cas.client.shiro.handle;

import org.pac4j.cas.logout.DefaultCasLogoutHandler;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.store.Store;
import io.buji.pac4j.profile.ShiroProfileManager;

/**
 * 登出处理
 * 
 * @author wubo
 *
 * @param <C>
 */
public class ShiroCasLogoutHandler<C extends WebContext> extends DefaultCasLogoutHandler<C> {

  public ShiroCasLogoutHandler() {}

  public ShiroCasLogoutHandler(final Store<String, Object> store) {
    super(store);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void destroy(final C context, final SessionStore sessionStore, final String channel) {
    // remove profiles
    final ShiroProfileManager manager = new ShiroProfileManager(context);
    manager.logout();
    // and optionally the web session
    if (isDestroySession()) {
      @SuppressWarnings("unchecked")
      final boolean invalidated = sessionStore.destroySession(context);
      if (!invalidated) {
        logger.error("The session has not been invalidated for {} channel logout", channel);
      }
    }
  }
}
