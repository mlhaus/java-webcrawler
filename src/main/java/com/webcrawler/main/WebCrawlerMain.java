package com.webcrawler.main;

import com.google.inject.Guice;
import com.webcrawler.WebCrawler;
import com.webcrawler.WebCrawlerModule;
import com.webcrawler.json.ConfigurationLoader;
import com.webcrawler.json.CrawlResult;
import com.webcrawler.json.CrawlResultWriter;
import com.webcrawler.json.CrawlerConfiguration;
import com.webcrawler.profiler.Profiler;
import com.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    // TODO: Write the crawl results to a JSON file (or System.out if the file name is empty)
    String crawlResultsFileName = config.getResultPath();
    if(!crawlResultsFileName.isEmpty()) {
      Path path = Path.of(config.getResultPath());
      resultWriter.write(path);
    } else {
      Writer writer = new OutputStreamWriter(System.out);
      resultWriter.write(writer);
    }
    // TODO: Write the profile data to a text file (or System.out if the file name is empty)
    String profileDataFileName = config.getProfileOutputPath();
    if(!profileDataFileName.isEmpty()) {
      Path path = Path.of(config.getProfileOutputPath());
      profiler.writeData(path);
    } else {
      Writer writer = new OutputStreamWriter(System.out);
      profiler.writeData(writer);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
