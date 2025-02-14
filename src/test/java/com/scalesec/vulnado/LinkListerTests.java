import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

@ExtendWith(MockitoExtension.class)
class LinkListerTest {

    @Mock
    private Document mockDocument;

    @Mock
    private Elements mockElements;

    @Mock
    private Element mockElement;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLinks_ShouldReturnListOfLinks() throws IOException {
        // Arrange
        String url = "https://example.com";
        when(Jsoup.connect(url).get()).thenReturn(mockDocument);
        when(mockDocument.select("a")).thenReturn(mockElements);
        when(mockElements.iterator()).thenReturn(List.of(mockElement).iterator());
        when(mockElement.absUrl("href")).thenReturn("https://example.com/page");

        // Act
        List<String> result = LinkLister.getLinks(url);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result should contain one link");
        assertEquals("https://example.com/page", result.get(0), "The link should match the mocked value");
    }

    @Test
    void getLinks_ShouldHandleEmptyLinkList() throws IOException {
        // Arrange
        String url = "https://example.com";
        when(Jsoup.connect(url).get()).thenReturn(mockDocument);
        when(mockDocument.select("a")).thenReturn(new Elements());

        // Act
        List<String> result = LinkLister.getLinks(url);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty list");
    }

    @Test
    void getLinks_ShouldThrowIOException() throws IOException {
        // Arrange
        String url = "https://example.com";
        when(Jsoup.connect(url).get()).thenThrow(new IOException("Connection failed"));

        // Act & Assert
        assertThrows(IOException.class, () -> LinkLister.getLinks(url),
                "Should throw IOException when connection fails");
    }

    @Test
    void getLinksV2_ShouldReturnLinks_WhenValidPublicIP() throws Exception {
        // Arrange
        String url = "https://8.8.8.8";
        List<String> expectedLinks = List.of("https://example.com/page");

        // Mock the URL and its behavior
        URL mockUrl = mock(URL.class);
        when(mockUrl.getHost()).thenReturn("8.8.8.8");

        // Use PowerMockito to mock the URL constructor
        mockStatic(URL.class);
        when(URL.class.getConstructor(String.class).newInstance(url)).thenReturn(mockUrl);

        // Mock the getLinks method
        mockStatic(LinkLister.class);
        when(LinkLister.getLinks(url)).thenReturn(expectedLinks);

        // Act
        List<String> result = LinkLister.getLinksV2(url);

        // Assert
        assertEquals(expectedLinks, result, "Should return links for valid public IP");
    }

    @Test
    void getLinksV2_ShouldThrowBadRequest_WhenPrivateIP() {
        // Arrange
        String[] privateIPs = {"172.16.0.1", "192.168.1.1", "10.0.0.1"};

        for (String ip : privateIPs) {
            String url = "http://" + ip;

            // Act & Assert
            BadRequest exception = assertThrows(BadRequest.class, () -> LinkLister.getLinksV2(url),
                    "Should throw BadRequest for private IP: " + ip);
            assertEquals("Use of Private IP", exception.getMessage(), "Exception message should match for: " + ip);
        }
    }

    @Test
    void getLinksV2_ShouldThrowBadRequest_WhenInvalidURL() {
        // Arrange
        String invalidUrl = "not a valid url";

        // Act & Assert
        BadRequest exception = assertThrows(BadRequest.class, () -> LinkLister.getLinksV2(invalidUrl),
                "Should throw BadRequest for invalid URL");
        assertTrue(exception.getMessage().contains("no protocol"),
                "Exception message should indicate invalid URL format");
    }

    // Additional test to verify logging
    @Test
    void getLinksV2_ShouldLogHostname() throws Exception {
        // Arrange
        String url = "https://example.com";
        URL mockUrl = mock(URL.class);
        when(mockUrl.getHost()).thenReturn("example.com");

        mockStatic(URL.class);
        when(URL.class.getConstructor(String.class).newInstance(url)).thenReturn(mockUrl);

        // Replace the logger in LinkLister with our mock
        java.lang.reflect.Field loggerField = LinkLister.class.getDeclaredField("LOGGER");
        loggerField.setAccessible(true);
        loggerField.set(null, mockLogger);

        // Act
        LinkLister.getLinksV2(url);

        // Assert
        verify(mockLogger).info("example.com");
    }
}
