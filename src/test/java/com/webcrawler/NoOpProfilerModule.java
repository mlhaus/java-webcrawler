package com.webcrawler;

import com.google.inject.AbstractModule;
import com.webcrawler.profiler.Profiler;

/**
 * Guice module that installs a {@link NoOpProfiler}.
 */
public final class NoOpProfilerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Profiler.class).toInstance(new NoOpProfiler());
  }
}
