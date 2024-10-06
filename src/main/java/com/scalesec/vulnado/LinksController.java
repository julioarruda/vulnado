package com.scalesec.vulnado;

import org.springframework.boot.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.*;
import java.util.List;
import java.io.Serializable;
import java.io.IOException;


@RestController
@EnableAutoConfiguration
public class LinksController {
  @RequestMapping(value = "/links", produces = "application/json")
  List<String> links(@RequestParam String url) throws IOException{
    if (!isValidUrl(url)) {
      throw new BadRequest("Invalid URL");
    }
    return LinkLister.getLinks(url);
  }
  @RequestMapping(value = "/links-v2", produces = "application/json")
  List<String> linksV2(@RequestParam String url) throws BadRequest{
    if (!isValidUrl(url)) {
      throw new BadRequest("Invalid URL");
    }
    return LinkLister.getLinksV2(url);
  }

  private boolean isValidUrl(String url) {
    String regex = "^(https?|ftp)://[^\s/$.?#].[^\s]*$";
    return url.matches(regex);
  }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequest extends RuntimeException {
  public BadRequest(String exception) {
    super(exception);
  }
}
