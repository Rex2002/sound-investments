# Sonifiable

## Attributes

-   name: String
-   symbol: String
-   market: String/Market???
-   earliest: Date
-   latest: Date

## Methods

-   getEoD(start: Date, end: Date): List<Price>
-   getIntraday(start: Date, end: Date, interval: TimePeriod): List<Price>

## Subclasses

-   Stock
    -   logo: Path/Image???
-   Index
-   ETF

# Price

-   day: Date
-   start: Time
-   end: Time
-   open: Int
-   close: Int
-   low: Int
-   high: Int

# TimePeriod: Enum

-   1min
-   5min
-   15min
-   30min
-   45min
-   1h
-   2h
-   4h
-   1day
-   1week
-   1month
