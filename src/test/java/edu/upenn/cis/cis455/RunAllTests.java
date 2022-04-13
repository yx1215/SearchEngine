package edu.upenn.cis.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase 
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
              Class.forName(TestStorage.class.getName()),
              Class.forName(TestCrawler.class.getName()),
              Class.forName(TestPathMatcher.class.getName()),
              Class.forName(TestDOMParser.class.getName())
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
