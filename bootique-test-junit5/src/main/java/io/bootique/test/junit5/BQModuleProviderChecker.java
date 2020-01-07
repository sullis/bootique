package io.bootique.test.junit5;

import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.counting;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BQModuleProviderChecker {
    private Class<? extends BQModuleProvider> provider;

    protected BQModuleProviderChecker(Class<? extends BQModuleProvider> provider) {
        this.provider = Objects.requireNonNull(provider);
    }

    /**
     * Verifies that the passed provider type is auto-loadable in a Bootique app.
     */
    public static void testAutoLoadable(Class<? extends BQModuleProvider> provider) {
        new BQModuleProviderChecker(provider).testAutoLoadable();
    }

    /**
     * Checks that config metadata for the Module created by the tested provider can be loaded without errors. Does not
     * verify the actual metadata contents.
     */
    public static void testMetadata(Class<? extends BQModuleProvider> provider) {
        new BQModuleProviderChecker(provider).testMetadata();
    }

    protected Stream<BQModuleProvider> matchingProviders() {
        return StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
                .filter(p -> p != null && provider.equals(p.getClass()));
    }

    protected BQModuleProvider matchingProvider() {
        return matchingProviders().findFirst().get();
    }

    protected void testAutoLoadable() {

        Long c = matchingProviders().collect(counting());

        switch (c.intValue()) {
            case 0:
                Assertions.fail("Expected provider '" + provider.getName() + "' is not found");
                break;
            case 1:
                break;
            default:
                Assertions.fail("Expected provider '" + provider.getName() + "' is found more then once: " + c);
                break;
        }
    }

    protected void testMetadata() {

        try {
            testWithFactory(testFactory -> {
                // must auto-load modules to ensure all tested module dependencies are present...
                BQRuntime runtime = testFactory.app().autoLoadModules().createRuntime();

                BQModuleProvider provider = matchingProvider();
                String providerName = provider.name();

                // loading metadata ensures that all annotations are properly applied...
                Optional<ModuleMetadata> moduleMetadata = runtime
                        .getInstance(ModulesMetadata.class)
                        .getModules()
                        .stream()
                        .filter(mmd -> providerName.equals(mmd.getProviderName()))
                        .findFirst();

                assertTrue(moduleMetadata.isPresent(), "No module metadata available for provider: '" + providerName + "'");
                moduleMetadata.get().getConfigs();
            });
        } catch (Exception e) {
            Assertions.fail("Fail to test metadata.");
        }
    }

    protected void testWithFactory(Consumer<BQTestExtension> test) throws Exception {
        BQTestExtension testExtension = new BQTestExtension();
        ExtensionContext extensionContext = new TestExtensionContext();
        try {
            testExtension.beforeEach(extensionContext);
            test.accept(testExtension);
        } finally {
            testExtension.afterEach(extensionContext);
        }
    }

    private static class TestExtensionContext implements ExtensionContext {

        @Override
        public Optional<ExtensionContext> getParent() {
            return Optional.empty();
        }

        @Override
        public ExtensionContext getRoot() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public Set<String> getTags() {
            return null;
        }

        @Override
        public Optional<AnnotatedElement> getElement() {
            return Optional.empty();
        }

        @Override
        public Optional<Class<?>> getTestClass() {
            return Optional.empty();
        }

        @Override
        public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
            return Optional.empty();
        }

        @Override
        public Optional<Object> getTestInstance() {
            return Optional.empty();
        }

        @Override
        public Optional<TestInstances> getTestInstances() {
            return Optional.empty();
        }

        @Override
        public Optional<Method> getTestMethod() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> getExecutionException() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getConfigurationParameter(String s) {
            return Optional.empty();
        }

        @Override
        public void publishReportEntry(Map<String, String> map) {}

        @Override
        public Store getStore(Namespace namespace) {
            return null;
        }
    }
}