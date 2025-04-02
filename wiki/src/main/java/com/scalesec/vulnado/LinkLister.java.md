# LinkLister

## Method Summary

| Modifier and Type | Method Signature | Description |
| :--- | :--- | :--- |
| `static List<String>` | `getLinks(String url)` |  |
| `static List<String>` | `getLinksV2(String url)` |  |

## Method Details

### getLinks

```java
public static List<String> getLinks(String url) throws IOException
```

This method takes a URL as input and returns a list of all the links on the page.

### getLinksV2

```java
public static List<String> getLinksV2(String url) throws BadRequest
```

This method takes a URL as input and returns a list of all the links on the page.
This method includes basic input validation to prevent SSRF vulnerabilities.

## Insights

- The code defines a class `LinkLister` with two methods for extracting links from a given URL using the Jsoup library.
- `getLinks` method retrieves all links from the provided URL without any validation.
- `getLinksV2` method implements input validation to prevent Server-Side Request Forgery (SSRF) vulnerabilities by blocking requests to private IP addresses.
- The code utilizes the Jsoup library for parsing HTML content and extracting links.
- The code handles potential `IOException` during URL connection and HTML parsing.
- The `BadRequest` exception suggests a custom exception type, likely for handling invalid input or requests.

