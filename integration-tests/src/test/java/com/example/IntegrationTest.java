package com.example;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Provider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public final class IntegrationTest {
  @Parameters(name = "{0}")
  public static Object[] parameters() {
    return Backend.values();
  }

  @Parameter public Backend backend;

  @Test public void componentProvider() {
    ComponentProvider component = backend.create(ComponentProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void componentProviderQualified() {
    ComponentProviderQualified component = backend.create(ComponentProviderQualified.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void staticProvider() {
    StaticProvider component = backend.create(StaticProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void bindsProvider() {
    BindsProvider component = backend.create(BindsProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void optionalBinding() {
    ignoreReflectionBackend();

    OptionalBinding component = backend.create(OptionalBinding.class);
    assertThat(component.string()).isEqualTo(Optional.of("foo"));
  }

  @Test public void optionalBindingAbsent() {
    ignoreReflectionBackend();

    OptionalBindingAbsent component = backend.create(OptionalBindingAbsent.class);
    assertThat(component.string()).isEqualTo(Optional.empty());
  }

  @Test public void optionalGuavaBinding() {
    ignoreReflectionBackend();

    OptionalGuavaBinding component = backend.create(OptionalGuavaBinding.class);
    assertThat(component.string()).isEqualTo(com.google.common.base.Optional.of("foo"));
  }

  @Test public void optionalGuavaBindingAbsent() {
    ignoreReflectionBackend();

    OptionalGuavaBindingAbsent component = backend.create(OptionalGuavaBindingAbsent.class);
    assertThat(component.string()).isEqualTo(com.google.common.base.Optional.absent());
  }

  @Test public void bindsInstance() {
    InstanceBinding component = backend.builder(InstanceBinding.Builder.class)
        .string("foo")
        .build();
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test public void bindsInstanceCalledTwice() {
    InstanceBinding component = backend.builder(InstanceBinding.Builder.class)
        .string("foo")
        .string("bar")
        .build();
    assertThat(component.string()).isEqualTo("bar");
  }

  @Test public void bindsInstanceNull() {
    InstanceBindingNull component = backend.builder(InstanceBindingNull.Builder.class)
        .string(null)
        .build();
    assertThat(component.string()).isNull();
  }

  @Test public void justInTimeConstructor() {
    JustInTimeConstructor component = backend.create(JustInTimeConstructor.class);
    assertThat(component.thing()).isNotNull();
  }

  @Test public void builderImplicitModules() {
    BuilderImplicitModules component = backend.builder(BuilderImplicitModules.Builder.class)
        .value(3L)
        .build();

    assertThat(component.string()).isEqualTo("3");
  }

  @Test public void builderExplicitModules() {
    BuilderExplicitModules component = backend.builder(BuilderExplicitModules.Builder.class)
        .module1(new BuilderExplicitModules.Module1("3"))
        .build();

    assertThat(component.string()).isEqualTo("3");
  }

  @Test public void builderExplicitModulesSetTwice() {
    BuilderExplicitModules component = backend.builder(BuilderExplicitModules.Builder.class)
        .module1(new BuilderExplicitModules.Module1("3"))
        .module1(new BuilderExplicitModules.Module1("4"))
        .build();

    assertThat(component.string()).isEqualTo("4");
  }

  @Test public void memberInjectionEmpty() {
    MemberInjectionEmpty component = backend.create(MemberInjectionEmpty.class);
    MemberInjectionEmpty.Target target = new MemberInjectionEmpty.Target();
    component.inject(target);
    // No state, nothing to verify, except it didn't throw.
  }

  @Test public void memberInjectionReturnInstance() {
    MemberInjectionReturnInstance component = backend.create(MemberInjectionReturnInstance.class);
    MemberInjectionReturnInstance.Target in = new MemberInjectionReturnInstance.Target();
    MemberInjectionReturnInstance.Target out = component.inject(in);
    assertThat(out.foo).isEqualTo("foo");
    assertThat(out).isSameAs(in);
  }

  @Test public void memberInjectionNoInjects() {
    MemberInjectionNoInjects component = backend.create(MemberInjectionNoInjects.class);
    MemberInjectionNoInjects.Target target = new MemberInjectionNoInjects.Target();
    component.inject(target);
    assertThat(target.one).isNull();
    assertThat(target.two).isNull();
    assertThat(target.three).isNull();
    assertThat(target.count).isEqualTo(0);
  }

  @Test public void memberInjectionFieldBeforeMethod() {
    MemberInjectionFieldBeforeMethod component =
        backend.create(MemberInjectionFieldBeforeMethod.class);
    MemberInjectionFieldBeforeMethod.Target target = new MemberInjectionFieldBeforeMethod.Target();
    component.inject(target);
    assertThat(target.fieldBeforeMethod).isTrue();
  }

  @Test public void memberInjectionFieldVisibility() {
    MemberInjectionFieldVisibility component = backend.create(MemberInjectionFieldVisibility.class);
    MemberInjectionFieldVisibility.Target target = new MemberInjectionFieldVisibility.Target();
    component.inject(target);
    assertThat(target.one).isEqualTo("one");
    assertThat(target.two).isEqualTo(2L);
    assertThat(target.three).isEqualTo(3);
  }

  @Test public void memberInjectionHierarchy() {
    MemberInjectionHierarchy component = backend.create(MemberInjectionHierarchy.class);
    MemberInjectionHierarchy.Subtype target = new MemberInjectionHierarchy.Subtype();
    component.inject(target);
    assertThat(target.baseOne).isEqualTo("foo");
    assertThat(target.baseCalled).isTrue();
    assertThat(target.subtypeOne).isEqualTo("foo");
    assertThat(target.subtypeCalled).isTrue();
  }

  @Test public void memberInjectionMethodVisibility() {
    MemberInjectionMethodVisibility component =
        backend.create(MemberInjectionMethodVisibility.class);
    MemberInjectionMethodVisibility.Target target = new MemberInjectionMethodVisibility.Target();
    component.inject(target);
    assertThat(target.count).isEqualTo(3);
    assertThat(target.one).isEqualTo("one");
    assertThat(target.two).isEqualTo(2L);
    assertThat(target.three).isEqualTo(3);
  }

  @Test public void memberInjectionMethodMultipleParams() {
    MemberInjectionMethodMultipleParams component =
        backend.create(MemberInjectionMethodMultipleParams.class);
    MemberInjectionMethodMultipleParams.Target target =
        new MemberInjectionMethodMultipleParams.Target();
    component.inject(target);
    assertThat(target.one).isEqualTo("one");
    assertThat(target.two).isEqualTo(2L);
    assertThat(target.two2).isEqualTo(2L);
    assertThat(target.three).isEqualTo(3);
  }

  @Test public void memberInjectionMethodReturnTypes() {
    MemberInjectionMethodReturnTypes component =
        backend.create(MemberInjectionMethodReturnTypes.class);
    MemberInjectionMethodReturnTypes.Target target = new MemberInjectionMethodReturnTypes.Target();
    component.inject(target);
    assertThat(target.count).isEqualTo(3);
  }

  @Test public void memberInjectionQualified() {
    MemberInjectionQualified component = backend.create(MemberInjectionQualified.class);
    MemberInjectionQualified.Target target = new MemberInjectionQualified.Target();
    component.inject(target);
    assertThat(target.fromField).isEqualTo("foo");
    assertThat(target.fromMethod).isEqualTo("foo");
  }

  @Test public void scoped() {
    ignoreReflectionBackend();

    Scoped component = backend.create(Scoped.class);
    assertThat(component.value()).isEqualTo(1);
    assertThat(component.value()).isEqualTo(1);
  }

  @Test public void multibindingSet() {
    ignoreReflectionBackend();

    MultibindingSet component = backend.create(MultibindingSet.class);
    assertThat(component.values()).containsExactly("one", "two");
  }

  @Test public void multibindingSetElements() {
    ignoreReflectionBackend();

    MultibindingSetElements component = backend.create(MultibindingSetElements.class);
    assertThat(component.values()).containsExactly("one", "two");
  }

  @Test public void multibindingProviderSet() {
    ignoreReflectionBackend();

    MultibindingProviderSet component = backend.create(MultibindingProviderSet.class);
    Provider<Set<String>> values = component.values();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    MultibindingProviderSet.Module1.oneCount.set(1);
    MultibindingProviderSet.Module1.twoCount.set(1);

    assertThat(values.get()).containsExactly("one1", "two1");
    assertThat(values.get()).containsExactly("one2", "two2");
  }

  @Test public void multibindingMap() {
    ignoreReflectionBackend();

    MultibindingMap component = backend.create(MultibindingMap.class);
    assertThat(component.values()).containsExactly("1", "one", "2", "two");
  }

  @Test public void multibindingProviderMap() {
    ignoreReflectionBackend();

    MultibindingProviderMap component = backend.create(MultibindingProviderMap.class);
    Provider<Map<String, String>> values = component.values();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    MultibindingProviderMap.Module1.oneCount.set(1);
    MultibindingProviderMap.Module1.twoCount.set(1);

    assertThat(values.get()).containsExactly("1", "one1", "2", "two1");
    assertThat(values.get()).containsExactly("1", "one2", "2", "two2");
  }

  @Test public void multibindingMapProvider() {
    ignoreReflectionBackend();

    MultibindingMapProvider component = backend.create(MultibindingMapProvider.class);
    Map<String, Provider<String>> values = component.values();
    assertThat(values.keySet()).containsExactly("1", "2");

    // Ensure each Provider is lazy in invoking its backing @Provides method.
    MultibindingMapProvider.Module1.twoValue.set("two");
    assertThat(values.get("2").get()).isEqualTo("two");

    MultibindingMapProvider.Module1.oneValue.set("one");
    assertThat(values.get("1").get()).isEqualTo("one");
  }

  private void ignoreReflectionBackend() {
    assumeTrue("Not yet implemented for reflection backend", backend != Backend.REFLECT);
  }
}
