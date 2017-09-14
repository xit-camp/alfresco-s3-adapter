package org.redpill.alfresco.s3;

import org.alfresco.service.cmr.site.SiteInfo;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractComponentIT;

/**
 *
 * @author Marcus Svartmark - Redpill Linpro AB
 */
public class BootstrapComponentIT extends AbstractComponentIT {

  @Test
  public void testNothing() {
    SiteInfo createSite = createSite();
    
    deleteSite(createSite);
  }
}
