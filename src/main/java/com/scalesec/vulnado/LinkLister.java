package com.scalesec.vulnado;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.*;


public class LinkLister {
  public static List<String> getLinks(String url) throws IOException {
    if (url == null || url.trim().isEmpty()) {
      throw new IllegalArgumentException("URL cannot be null or empty");
    }

    List<String> result = new ArrayList<String>();
    Document doc = Jsoup.connect(url).get();
    Elements links = doc.select("a");
    for (Element link : links) {
      result.add(link.absUrl("href"));
    }
    return result;
  }

  public static List<String> getLinksV2(String url) throws BadRequest {
    if (url == null || url.trim().isEmpty()) {
      throw new IllegalArgumentException("URL cannot be null or empty");
    }

    try {
      URL aUrl= new URL(url);
      String host = aUrl.getHost();
      System.out.println(host);
      if (host.startsWith("172.") || host.startsWith("192.168") || host.startsWith("10.")){
        throw new BadRequest("Use of Private IP");
      } else {
        return getLinks(url);
      }
    } catch(Exception e) {
      throw new BadRequest(e.getMessage());
    }
  }
}
