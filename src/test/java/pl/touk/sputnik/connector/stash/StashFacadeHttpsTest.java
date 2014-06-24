package pl.touk.sputnik.connector.stash;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pl.touk.sputnik.configuration.ConfigurationSetup;
import pl.touk.sputnik.review.ReviewFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class StashFacadeHttpsTest {

    private static Integer PORT = 8089;
    private static Integer HTTPS_PORT = 8443;
    private static final ImmutableMap<String, String> STASH_CONFIG_MAP = ImmutableMap.of(
            "stash.host", "localhost",
            "stash.port", HTTPS_PORT.toString(),
            "stash.username", "user",
            "stash.password", "pass",
            "stash.useHttps", "true"
    );
    private static final ImmutableMap<String, String> STASH_PATCHSET_MAP = ImmutableMap.of(
            "cli.pullRequestId", "12",
            "stash.repositorySlug", "myproject",
            "stash.projectKey", "mykey"
    );

    private StashFacade stashFacade;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT, HTTPS_PORT);

    @Before
    public void setUp() {
        Map<String, String> joinedMap = new HashMap<>();
        joinedMap.putAll(STASH_CONFIG_MAP);
        joinedMap.putAll(STASH_PATCHSET_MAP);
        new ConfigurationSetup().setUp(joinedMap);
        stashFacade = new StashFacadeBuilder().build();
    }

    @Test
    public void shouldGetChangeInfo() throws Exception {
        configureFor("localhost", PORT);
        String changesUrl = "/rest/api/1.0/projects/mykey/repos/myproject/pull-requests/12/changes";
        stubFor(get(urlEqualTo(changesUrl))
                .withHeader("Authorization", equalTo("Basic dXNlcjpwYXNz"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IOUtils.toString(getClass().getResourceAsStream("/json/stash-changes.json")))));

        List<ReviewFile> files = stashFacade.listFiles();
        assertThat(files).hasSize(4);
    }
}