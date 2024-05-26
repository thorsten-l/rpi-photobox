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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
public class HomeController
{
  @GetMapping("/")
  public String home(
    @RequestParam(name = "page", required = false, defaultValue = "1") int page,
    Model model) throws IOException
  {
    page = page > 0 ? page : 1;
    log.debug("home page={}", page);
    int imageCount = 0;

    ArrayList<String> imageNames = new ArrayList<>();

    int pageCount = (imageCount + 3) / 4;

    File thumbsDirectory = new File(thumbsDirectoryName);
    if (thumbsDirectory.isDirectory() && thumbsDirectory.canRead())
    {
      String[] fileNames = thumbsDirectory.list(
        (File dir, String name) -> name.toLowerCase().endsWith(".jpg"));
      Arrays.sort(fileNames);
      imageCount = fileNames.length;

      pageCount = (imageCount + 3) / 4;
      page = page <= pageCount ? page : pageCount;

      int filePage = page - 1;
      for (int i = filePage * 4; i < (filePage * 4 + 4) && i < imageCount; i++)
      {
        log.debug("{}", fileNames[i]);
        imageNames.add(fileNames[i]);
      }
    }
    else
    {
      throw new IOException("'" + thumbsDirectoryName
        + "' can't read or is not a directory");
    }

    model.addAttribute("page", page);
    model.addAttribute("pageCount", pageCount);
    model.addAttribute("imageNames", imageNames);
    model.addAttribute("imageCount", imageCount);
    model.addAttribute("thumbsWidth", thumbsWidth);
    model.addAttribute("thumbsHeight", thumbsHeight);
    model.addAttribute("webBase", webBase);
    return "home";
  }

  @Value("${pictureviewer.thumbs.width}")
  private int thumbsWidth;

  @Value("${pictureviewer.thumbs.height}")
  private int thumbsHeight;

  @Value("${pictureviewer.thumbs.directory}")
  private String thumbsDirectoryName;

  @Value("${pictureviewer.web-base}")
  private String webBase;
}
