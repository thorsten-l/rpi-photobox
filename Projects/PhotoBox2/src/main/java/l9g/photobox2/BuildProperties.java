/*
 * Copyright 2022 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.photobox2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@ToString
public class BuildProperties
{  
  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(
    BuildProperties.class.getName());

  private final static BuildProperties SINGLETON = new BuildProperties();
  
  private BuildProperties()
  {
    LOGGER.debug( "Initialize build properties." );

    Properties p = new Properties();
    InputStream is = this.getClass().getResourceAsStream("/build.properties");
    try
    {
      p.load(is);
      javaVersion = p.getProperty("build.java.version");
      javaVendor = p.getProperty("build.java.vendor");
      projectName = p.getProperty("build.project.name");
      projectVersion = p.getProperty("build.project.version");
      timestamp = p.getProperty("build.timestamp");
      profile = p.getProperty("build.profile");
    }
    catch (IOException ex)
    {
      LOGGER.error( "Can't load properties. ", ex );
    }
  }
  
  public static BuildProperties getInstance()
  {
    return SINGLETON;
  }
  
  public static String toFormattedString()
  {
    return "\n" + SINGLETON.toString()
      .replaceFirst("\\(", "(\n - ")
      .replaceAll(", ", "\n - " );
  }
  
  @Getter
  private String javaVersion;

  @Getter
  private String javaVendor;

  @Getter
  private String projectName;

  @Getter
  private String projectVersion;

  @Getter
  private String timestamp;
  
  @Getter
  private String profile;
}
