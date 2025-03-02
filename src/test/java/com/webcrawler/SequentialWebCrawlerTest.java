package com.webcrawler;

import com.google.inject.Guice;
import com.webcrawler.json.CrawlerConfiguration;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

public final class SequentialWebCrawlerTest {
  @Inject
  private SequentialWebCrawler sequentialWebCrawler;

  @Test
  public void testMaxParallelism() {
    CrawlerConfiguration config = new CrawlerConfiguration.Builder().build();
    Guice.createInjector(new WebCrawlerModule(config), new NoOpProfilerModule())
        .injectMembers(this);
    assertThat(sequentialWebCrawler.getMaxParallelism()).isEqualTo(1);
  }
}
