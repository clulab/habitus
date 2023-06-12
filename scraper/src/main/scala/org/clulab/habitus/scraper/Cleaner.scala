package org.clulab.habitus.scraper

class Cleaner {

  def clean(name: String): String = {
    name.map { char =>
      if (char.isLetterOrDigit) char else '_'
    }
  }
}
