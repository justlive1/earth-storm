package justlive.earth.breeze.storm.cas.client.shiro.util;

import org.apache.shiro.session.Session;

/**
 * 
 * @author wubo
 *
 */
public class MyShiroProvidedSessionStore extends MyShiroSessionStore {

  private Session session;

  public MyShiroProvidedSessionStore(Session session) {
    this.session = session;
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  @Override
  protected Session getSession(final boolean createSession) {
    return getSession();
  }


}
