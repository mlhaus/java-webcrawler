package com.webcrawler;

import com.webcrawler.json.CrawlResult;
import com.webcrawler.parser.PageParser;
import com.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final PageParserFactory parserFactory;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      PageParserFactory parserFactory,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = Collections.synchronizedMap(new HashMap<>());
    Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    for (String url : startingUrls) {
      pool.invoke(new MyRecursiveTask(url, deadline, maxDepth, counts, visitedUrls));
//      crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
    }

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
              .setWordCounts(counts)
              .setUrlsVisited(visitedUrls.size())
              .build();
    }

    return new CrawlResult.Builder()
            .setWordCounts(WordCounts.sort(counts, popularWordCount))
            .setUrlsVisited(visitedUrls.size())
            .build();
//    return new CrawlResult.Builder().build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }

  public class MyRecursiveTask extends RecursiveTask<Boolean> {
    private String url;
    private Instant deadline;
    private int maxDepth;
    private Map<String, Integer> counts;
    private Set<String> visitedUrls;

    public MyRecursiveTask(String url, Instant deadline, int maxDepth, Map<String, Integer> counts, Set<String> visitedUrls) {
      this.url = url;
      this.deadline = deadline;
      this.maxDepth = maxDepth;
      this.counts = counts;
      this.visitedUrls = visitedUrls;
    }

    @Override
    protected Boolean compute() {
      if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
        return false;
      }
      for (Pattern pattern : ignoredUrls) {
        if (pattern.matcher(url).matches()) {
          return false;
        }
      }
//      if (visitedUrls.contains(url)) {
//        return false;
//      }
//      visitedUrls.add(url);
      synchronized (this) {
        if(!visitedUrls.add(url)) {
          return false;
        }
      }
      PageParser.Result result = parserFactory.get(url).parse();
      synchronized (this) {
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
          if (counts.containsKey(e.getKey())) {
            counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
          } else {
            counts.put(e.getKey(), e.getValue());
          }
        }
      }
      List<MyRecursiveTask> subtasks = new ArrayList<>();
      for (String link : result.getLinks()) {
        subtasks.add(new MyRecursiveTask(link, deadline, maxDepth - 1, counts, visitedUrls));
//        crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
      }
      invokeAll(subtasks);
      return true;
    }
  }
}
