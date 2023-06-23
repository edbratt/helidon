/*
 * Copyright (c) 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.integrations.oci.sdk.runtime;

import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Set;

import io.helidon.builder.api.Prototype;
import io.helidon.common.types.Annotation;
import io.helidon.config.Config;
import io.helidon.pico.api.InjectionPointInfo;
import io.helidon.pico.api.PicoServiceProviderException;
import io.helidon.pico.api.PicoServices;
import io.helidon.pico.api.Qualifier;
import io.helidon.pico.api.ServiceProvider;
import io.helidon.pico.api.Services;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import jakarta.inject.Named;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static io.helidon.pico.testing.PicoTestingSupport.resetAll;
import static io.helidon.pico.testing.PicoTestingSupport.testableServices;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OciAuthenticationDetailsProviderTest {

    PicoServices picoServices;
    Services services;

    @AfterAll
    static void tearDown() {
        resetAll();
    }

    void resetWith(Config config) {
        resetAll();
        this.picoServices = testableServices(config);
        this.services = picoServices.services();
    }

    @Test
    void testCanReadPath() {
        MatcherAssert.assertThat(OciAuthenticationDetailsProvider.canReadPath("./target"),
                                 is(true));
        MatcherAssert.assertThat(OciAuthenticationDetailsProvider.canReadPath("./~bogus~"),
                                 is(false));
    }

    @Test
    void testUserHomePrivateKeyPath() {
        OciConfig ociConfig = Objects.requireNonNull(OciExtension.ociConfig());
        MatcherAssert.assertThat(OciAuthenticationDetailsProvider.userHomePrivateKeyPath(ociConfig),
                                 endsWith("/.oci/oci_api_key.pem"));

        ociConfig = OciConfig.builder(ociConfig)
                .configPath("/decoy/path")
                .authKeyFile("key.pem")
                .build();
        MatcherAssert.assertThat(OciAuthenticationDetailsProvider.userHomePrivateKeyPath(ociConfig),
                                 endsWith("/.oci/key.pem"));
    }

    @Test
    void testToNamedProfile() {
        assertThat(OciAuthenticationDetailsProvider.toNamedProfile(null),
                   nullValue());

        InjectionPointInfo.Builder ipi = InjectionPointInfo.builder()
                .annotations(Set.of());
        assertThat(OciAuthenticationDetailsProvider.toNamedProfile(ipi),
                   nullValue());

        ipi.addAnnotation(Annotation.create(Prototype.Singular.class));
        assertThat(OciAuthenticationDetailsProvider.toNamedProfile(ipi),
                   nullValue());

        ipi.addAnnotation(Annotation.create(Named.class));
        assertThat(OciAuthenticationDetailsProvider.toNamedProfile(ipi),
                   nullValue());

        ipi.qualifiers(Set.of(Qualifier.create(Prototype.Singular.class),
                              Qualifier.create(Named.class, "")));
        assertThat(OciAuthenticationDetailsProvider.toNamedProfile(ipi),
                   nullValue());

        ipi.qualifiers(Set.of(Qualifier.create(Prototype.Singular.class),
                              Qualifier.create(Named.class, " profileName ")));
        assertThat(OciAuthenticationDetailsProvider.toNamedProfile(ipi),
                   equalTo("profileName"));
    }

    @Test
    void authStrategiesAvailability() {
        Config config = OciConfigTest.createTestConfig(
                        OciConfigTest.ociAuthConfigStrategies(OciAuthenticationDetailsProvider.TAG_AUTO),
                        OciConfigTest.ociAuthSimpleConfig("tenant", "user", "phrase", "fp", null, null, "region"))
                .get(OciConfig.CONFIG_KEY);
        OciConfig cfg = OciConfig.create(config);
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.AUTO.isAvailable(cfg),
                   is(true));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.CONFIG.isAvailable(cfg),
                   is(false));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.CONFIG_FILE.isAvailable(cfg),
                   is(false));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.INSTANCE_PRINCIPALS.isAvailable(cfg),
                   is(false));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.RESOURCE_PRINCIPAL.isAvailable(cfg),
                   is(false));

        config = OciConfigTest.createTestConfig(
                        OciConfigTest.ociAuthConfigStrategies(OciAuthenticationDetailsProvider.TAG_AUTO),
                        OciConfigTest.ociAuthConfigFile("./target", null),
                        OciConfigTest.ociAuthSimpleConfig("tenant", "user", "phrase", "fp", "pk", "pkp", null))
                .get(OciConfig.CONFIG_KEY);
        cfg = OciConfig.create(config);
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.AUTO.isAvailable(cfg),
                   is(true));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.CONFIG.isAvailable(cfg),
                   is(true));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.CONFIG_FILE.isAvailable(cfg),
                   is(true));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.INSTANCE_PRINCIPALS.isAvailable(cfg),
                   is(false));
        assertThat(OciAuthenticationDetailsProvider.AuthStrategy.RESOURCE_PRINCIPAL.isAvailable(cfg),
                   is(false));
    }

    @Test
    void selectionWhenNoConfigIsSet() {
        Config config = OciConfigTest.createTestConfig(
                OciConfigTest.basicTestingConfigSource());
        resetWith(config);

        ServiceProvider<AbstractAuthenticationDetailsProvider> authServiceProvider =
                services.lookupFirst(AbstractAuthenticationDetailsProvider.class, true).orElseThrow();

        PicoServiceProviderException e = assertThrows(PicoServiceProviderException.class, authServiceProvider::get);
        assertThat(e.getCause().getMessage(),
                   equalTo("No instances of com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider available for use. " +
                           "Verify your configuration named: oci"));
    }

    @Test
    void selectionWhenFileConfigIsSetWithAuto() {
        Config config = OciConfigTest.createTestConfig(
                OciConfigTest.basicTestingConfigSource(),
                OciConfigTest.ociAuthConfigStrategies(OciAuthenticationDetailsProvider.TAG_AUTO),
                OciConfigTest.ociAuthConfigFile("./target", "profile"));
        resetWith(config);

        ServiceProvider<AbstractAuthenticationDetailsProvider> authServiceProvider =
                services.lookupFirst(AbstractAuthenticationDetailsProvider.class, true).orElseThrow();

        PicoServiceProviderException e = assertThrows(PicoServiceProviderException.class, authServiceProvider::get);
        assertThat(e.getCause().getClass(),
                   equalTo(UncheckedIOException.class));
    }

    @Test
    void selectionWhenSimpleConfigIsSetWithAuto() {
        Config config = OciConfigTest.createTestConfig(
                OciConfigTest.basicTestingConfigSource(),
                OciConfigTest.ociAuthConfigStrategies(OciAuthenticationDetailsProvider.TAG_AUTO),
                OciConfigTest.ociAuthSimpleConfig("tenant", "user", "passphrase", "fp", "privKey", null, "us-phoenix-1"));
        resetWith(config);

        ServiceProvider<AbstractAuthenticationDetailsProvider> authServiceProvider =
                services.lookupFirst(AbstractAuthenticationDetailsProvider.class, true).orElseThrow();

        AbstractAuthenticationDetailsProvider authProvider = authServiceProvider.get();
        assertThat(authProvider.getClass(),
                   equalTo(SimpleAuthenticationDetailsProvider.class));
        SimpleAuthenticationDetailsProvider auth = (SimpleAuthenticationDetailsProvider) authProvider;
        assertThat(auth.getTenantId(),
                   equalTo("tenant"));
        assertThat(auth.getUserId(),
                   equalTo("user"));
        assertThat(auth.getRegion(),
                   equalTo(Region.US_PHOENIX_1));
        assertThat(new String(auth.getPassphraseCharacters()),
                   equalTo("passphrase"));
    }

}
