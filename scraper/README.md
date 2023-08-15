See [this example](https://www.scrapingbee.com/blog/web-scraping-scala/) of scraping for hints.

There are several stages:

1. search - use the search term to get initial result counts
   1. download - run SearchScraperApp with appropriate searchcorpus.txt
   1. scrape 
1. index - from all search result pages, get links to the articles
   1. download
   1. scrape
1. article - access the articles
   1. download
   1. scrape

To download the web pages, run a `DownloaderApp`.

To convert the html files to txt, run a `ScraperApp`.

For GhanaWeb, which uses a POST for the queries, do each index page by hand.  Put them in the right index directory, www_ghanaweb_com, and add the pages to indexocrpus.txt.  From there the index pages can be scraped and the articles then downloaded.
