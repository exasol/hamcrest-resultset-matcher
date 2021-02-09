# hamcrest-resulset-matcher 1.4.0, released 2021-02-09

Code name: Fuzzy matching for Date and Timestamp

## Summary

In some situations you only need to compare the values, not the datatypes. In such situations you can use fuzzy matching(approximate matching). In the fuzzy mode, the result set matcher only compares the values. In this release we added support for fuzzy matching of dates and timestamps. So now in the fuzzy matching mode this matcher considers timestamps and dates as equal if they have the same value.

## Features / Enhancements

* # 26: Added fuzzy matching for Date and Timestamp
