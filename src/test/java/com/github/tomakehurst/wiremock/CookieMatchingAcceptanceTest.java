package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.net.HttpHeaders.COOKIE;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CookieMatchingAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void matchesOnWellFormedCookie() {
        stubFor(get(urlEqualTo("/good/cookie"))
            .withCookie("my_cookie", containing("mycookievalue"))
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response =
            testClient.get("/good/cookie", withHeader(COOKIE, "my_cookie=xxx-mycookievalue-xxx"));

        assertThat(response.statusCode(), is(200));
    }

    @Test
    public void matchesWhenMultipleCookiesAreSentAndRequired() {
        stubFor(get(urlEqualTo("/good/cookies"))
            .withCookie("my_cookie", containing("mycookievalue"))
            .withCookie("my_other_cookie", equalTo("exact-other-value"))
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response =
            testClient.get("/good/cookies", withHeader(COOKIE, "my_cookie=xxx-mycookievalue-xxx; my_other_cookie=exact-other-value; irrelevant_cookie=whatever"));

        assertThat(response.statusCode(), is(200));
    }

    @Test
    public void doesNotMatchWhenExpectedCookieIsAbsent() {
        stubFor(get(urlEqualTo("/missing/cookie"))
            .withCookie("my_cookie", containing("mycookievalue"))
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response =
            testClient.get("/missing/cookie", withHeader(COOKIE, "the_wrong_cookie=xxx-mycookievalue-xxx"));

        assertThat(response.statusCode(), is(404));
    }

    @Test
    public void doesNotMatchWhenExpectedCookieHasTheWrongValue() {
        stubFor(get(urlEqualTo("/bad/cookie"))
            .withCookie("my_cookie", containing("mycookievalue"))
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response =
            testClient.get("/bad/cookie", withHeader(COOKIE, "my_cookie=youwontfindthis"));

        assertThat(response.statusCode(), is(404));
    }

    @Test
    public void doesNotMatchWhenExpectedCookieIsMalformed() {
        stubFor(get(urlEqualTo("/very-bad/cookie"))
            .withCookie("my_cookie", containing("mycookievalue"))
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response =
            testClient.get("/very-bad/cookie", withHeader(COOKIE, "my_cookieyouwontfindthis;;sldfjskldjf%%"));

        assertThat(response.statusCode(), is(404));
    }

    @Test
    public void revealsCookiesInLoggedRequests() {
        testClient.get("/good/cookies", withHeader(COOKIE, "my_cookie=xxx-mycookievalue-xxx; my_other_cookie=exact-other-value; irrelevant_cookie=whatever"));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/good/cookies")));

        assertThat(requests.size(), is(1));
        assertThat(requests.get(0).getCookies().keySet(), hasItem("my_other_cookie"));
    }
}
