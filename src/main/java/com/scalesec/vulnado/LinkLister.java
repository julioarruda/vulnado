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
    List<String> result = new ArrayList<String>();
    Document doc = Jsoup.connect(url).get();
    Elements links = doc.select("a");
    for (Element link : links) {
      result.add(link.absUrl("href"));
    }
    return result;
  }

  public static List<String> getLinksV2(String url) throws BadRequest {
    try {
      URL aUrl = new URL(url);
      String host = aUrl.getHost();
      String protocol = aUrl.getProtocol();
      
      System.out.println("Checking URL: " + url + ", Host: " + host + ", Protocol: " + protocol);
      
      // Only allow HTTP and HTTPS protocols
      if (!protocol.equals("http") && !protocol.equals("https")) {
        throw new BadRequest("Only HTTP and HTTPS protocols are allowed");
      }
      
      // Block private IP ranges and localhost
      if (host.startsWith("172.") || host.startsWith("192.168") || host.startsWith("10.") || 
          host.equals("localhost") || host.equals("127.0.0.1") || host.startsWith("169.254")) {
        throw new BadRequest("Use of Private IP or localhost is not allowed");
      }
      
      // Block common internal hostnames
      if (host.contains("internal") || host.contains("local") || host.contains("private")) {
        throw new BadRequest("Internal hostnames are not allowed");
      }
      
      return getLinks(url);
    } catch(Exception e) {
      throw new BadRequest(e.getMessage());
    }
  }
}
