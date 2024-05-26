/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
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
package l9g.web.pictureviewer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/systeminfo")
public class ApiSysteminfoController
{
  @GetMapping()
  public LinkedHashMap<String, String> buildinfo()
  {
    LinkedHashMap<String, String> properties = new LinkedHashMap<>();
    String[] keys = System.getProperties().keySet().toArray(String[]::new);
    Arrays.sort(keys);
    for (String key : keys)
    {
      if ( 
        !key.startsWith("rebel.") 
        && !key.startsWith("catalina.") 
        && !key.startsWith("user.") 
        && !key.endsWith(".tmpdir")
        && !key.endsWith(".path")
        )
      {
        properties.put(key, System.getProperty(key));
      }
    }
    return properties;
  }
}
