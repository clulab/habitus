See [this example](https://www.scrapingbee.com/blog/web-scraping-scala/) for hints.

Download the web pages from URLs and put them into files placed into directories based on the kind of scraper that needs to be used to extract the text.  For example, all pages scraped from SiteA that are of the same format go into SiteA directory.  Then the SiteAScraper works on them based on the expected elements in the pages.  An HTML file is turned into a TXT file of otherwise the same name.
