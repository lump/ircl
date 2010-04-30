// Copyright SOS Staffing 2010

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 * .
 *
 * @author troy
 * @version $Id: TestMultiple.java,v 1.1 2010/04/30 01:48:03 troy Exp $
 */
public class TestMultiple {
   private static final Logger logger = Logger.getLogger(TestConnect.class);

   static {
      BasicConfigurator.configure();
   }

   @Test
   public void TestIRCLogger() {
   }
}