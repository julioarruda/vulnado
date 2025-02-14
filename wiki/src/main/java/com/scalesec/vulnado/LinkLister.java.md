# LinkLister

## Overview
The `LinkLister` class provides functionality to extract links from a given URL. It offers two methods for retrieving links, with the second method (`getLinksV2`) including additional security checks.

## Dependencies
- JSoup library for HTML parsing
- Java standard libraries (java.util, java.io, java.net)

## Class Methods

### getLinks
```java
public static List<String> getLinks(String url) throws IOException
```

Extracts all links from the provided URL.

#### Parameters
- `url`: String - The URL to extract links from

#### Returns
- `List<String>`: A list of absolute URLs found in the page

#### Exceptions
- `IOException`: If there's an error connecting to or parsing the URL

#### Process
1. Creates an empty ArrayList to store the results
2. Connects to the URL using JSoup and retrieves the HTML document
3. Selects all `<a>` elements from the document
4. Extracts the absolute URL of each link's `href` attribute
5. Adds each absolute URL to the result list
6. Returns the list of links

### getLinksV2
```java
public static List<String> getLinksV2(String url) throws BadRequest
```

A more secure version of `getLinks` that includes checks for private IP addresses.

#### Parameters
- `url`: String - The URL to extract links from

#### Returns
- `List<String>`: A list of absolute URLs found in the page

#### Exceptions
- `BadRequest`: If the URL uses a private IP address or if any other exception occurs

#### Process
1. Parses the input URL
2. Extracts the host from the URL
3. Checks if the host starts with private IP address ranges (172., 192.168, or 10.)
4. If a private IP is detected, throws a BadRequest exception
5. If the URL is valid, calls the `getLinks` method to retrieve the links
6. Catches any exceptions and wraps them in a BadRequest exception

## Insights
- The class uses JSoup for HTML parsing, which is a popular and efficient library for web scraping in Java.
- `getLinksV2` method adds a security layer by preventing access to private IP addresses, which is a good practice for preventing potential security vulnerabilities.
- The use of absolute URLs (`absUrl`) ensures that all returned links are fully qualified, regardless of how they were specified in the original HTML.
- The class handles exceptions and wraps them in a custom `BadRequest` exception, providing a consistent error handling mechanism.

## Security Considerations
- The `getLinksV2` method includes checks for private IP addresses, which helps prevent unauthorized access to internal networks.
- Care should be taken when using the `getLinks` method directly, as it does not include the same security checks as `getLinksV2`.
